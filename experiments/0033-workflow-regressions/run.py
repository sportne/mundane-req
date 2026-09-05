"""Bounded public-tool workflows from clean committed sources and replayable synthetic models."""
import argparse
import copy
import hashlib
import json
import random
import shutil
import subprocess
import sys
import tempfile
import time
from html.parser import HTMLParser
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
BIN=ROOT/'build/maintained';BUILD=ROOT/'build/experiment-0033'
RENDER=ROOT/'experiments/0029-verification-report/render.py'
DEADLINE=time.monotonic()+180

def execute(args,status=0):
    if time.monotonic()>DEADLINE:raise TimeoutError('180-second workflow budget exceeded')
    p=subprocess.run([str(x) for x in args],capture_output=True,timeout=30)
    assert p.returncode==status,(args,p.returncode,p.stderr[:300],p.stdout[-300:])
    return p

def write(path,value):path.write_text(json.dumps(value,ensure_ascii=False,sort_keys=True,separators=(',',':'))+'\n')
def digest(data):return hashlib.sha256(data).hexdigest()
def values(artifact):return [r['values'] for r in artifact['requirements']]

class Anchors(HTMLParser):
    def __init__(self):super().__init__();self.ids=[]
    def handle_starttag(self,tag,attrs):
        a=dict(attrs)
        if tag=='section':self.ids.append(a.get('id'))

class Workflow:
    def __init__(self,root,format):
        self.root=root;self.format=format;self.source=root/'current';self.out=root/'out';self.out.mkdir(exist_ok=True)
    def compile(self,status=0):
        p=execute([BIN/'mundanereq-compile','--source='+self.format,'--root',self.source,self.source],status)
        assert not p.stderr;a=json.loads(p.stdout)
        assert a['complete']==(status==0)
        if status:assert not a['requirements']
        (self.out/'current.json').write_bytes(p.stdout);return a
    def plan(self,ids):
        folder=self.root/'plan';folder.mkdir(exist_ok=True)
        (folder/'plan.tsv').write_text('format\tplan_id\tcontext\tbaseline_scope\tcurrent_scope\nmundane-plan-source-0.1\tPLAN\tprimary\tbaseline\tcurrent\n')
        (folder/'activities.tsv').write_text('activity_id\tmethod\tobjective\texpected_evidence\nREVIEW\treview\tReview the selected requirements.\tRecorded review observations.\n')
        (folder/'coverage.tsv').write_text('plan_id\tactivity_id\trequirement_id\n'+''.join('PLAN\tREVIEW\t'+id+'\n' for id in ids))
        self.compile_plan()
    def compile_plan(self):
        p=execute([BIN/'mundane-plan','--root',self.root,self.root/'plan']);assert not p.stderr
        (self.out/'plan.json').write_bytes(p.stdout)
    def baseline(self,ids):
        shutil.copyfile(self.out/'current.json',self.out/'baseline.json');self.plan(ids)
        self.manifest={'format':'mundane-imports-0.1','imports':[
            {'scope':'baseline','path':'out/baseline.json','kind':'requirements','sha256':digest((self.out/'baseline.json').read_bytes()),'dependsOn':[]},
            {'scope':'current','path':'out/current.json','kind':'requirements','sha256':None,'dependsOn':[]}]}
        self.save_manifest()
    def save_manifest(self):write(self.out/'imports.json',self.manifest)
    def analyze(self,status=0,context=None):
        args=[BIN/'mundane-verify','--root',self.root,'--plan',self.out/'plan.json']
        if context:args+=['--context',context]
        p=execute(args+[self.out/'imports.json'],status);assert not p.stderr;a=json.loads(p.stdout)
        assert a['complete']==(status!=2)
        (self.out/'analysis.json').write_bytes(p.stdout)
        report=execute(['python3',RENDER,self.out/'analysis.json'],0 if status!=2 else 2)
        if status==2:
            assert not a['coverage'] and not a['uncovered'] and not report.stdout
            return a,report.stdout
        assert not report.stderr
        anchors=Anchors();anchors.feed(report.stdout.decode());assert len(anchors.ids)==len(set(anchors.ids))
        for imported in a['linked']['imports']:
            assert digest((self.root/imported['path']).read_bytes())==imported['sha256']
        for row in a['coverage']:
            scope,path=row['location']['path'].split(':',1);assert scope=='plan'
            text=(self.root/path).read_text().splitlines()[row['location']['line']-1]
            assert text.split('\t')==[row['planId'],row['activityId'],row['requirementId']]
        current=next(i['artifact'] for i in a['linked']['imports'] if i['scope']=='current')
        for r in current['requirements']:
            loc=r['locations']['record'];assert (self.source/loc['path']).is_file()
            assert loc['start']['line']<=len((self.source/loc['path']).read_text().splitlines())
        assert len(anchors.ids)==len(current['requirements'])
        (self.out/'report.html').write_bytes(report.stdout);return a,report.stdout
    def format_twice(self):
        for iteration in range(2):
            execute([BIN/'mundanereq-format','--source='+self.format,'--write',self.source])
            snapshot={p.name:p.read_bytes() for p in self.source.iterdir()}
            if iteration:assert snapshot==first
            else:first=snapshot
    def blocked_imports(self):
        original=copy.deepcopy(self.manifest);raw=(self.out/'current.json').read_bytes();planraw=(self.out/'plan.json').read_bytes()
        for change in ['pin','scope','version','partial']:
            self.manifest=copy.deepcopy(original)
            if change=='pin':self.manifest['imports'][0]['sha256']='0'*64
            if change=='scope':
                plan=json.loads(planraw);plan['plans'][0]['currentScope']=None;write(self.out/'plan.json',plan)
            if change in ['version','partial']:
                a=json.loads(raw);a.update({'format':'unknown'} if change=='version' else {'complete':False});write(self.out/'current.json',a)
            self.save_manifest();self.analyze(2)
            (self.out/'current.json').write_bytes(raw);(self.out/'plan.json').write_bytes(planraw)
        self.manifest=original;self.save_manifest()
    def contexts(self):
        p=self.root/'plan/plan.tsv';original=p.read_bytes();p.write_text(p.read_text()+'mundane-plan-source-0.1\tOTHER\tother\tbaseline\tcurrent\n')
        c=self.root/'plan/coverage.tsv';coverage=c.read_bytes();c.write_text(c.read_text()+'OTHER\tREVIEW\tABSENT\n');self.compile_plan()
        self.analyze(context='primary');self.analyze(2);self.analyze(2,'other');self.analyze(2,'unknown')
        p.write_bytes(original);c.write_bytes(coverage);self.compile_plan()

def model(seed):
    randomizer=random.Random(seed);count=randomizer.randint(3,15);result=[]
    for n in range(count):
        id='R-'+str(n);blocks=[{'kind':'prose','text':'Component '+str(n)+' shall preserve μ and 😀.'}]
        if n%3==0:blocks.append({'kind':'math','language':'latex','payload':'x_'+str(n)+' < 4'})
        result.append({'id':id,'title':'Behavior '+str(n),'statement':blocks,'allocation':'Unit' if n%2 else None,
            'rationale':[{'kind':'prose','text':'Bounded fixture rationale.'}] if n%2 else None,'source':None,
            'decomposes':sorted(randomizer.sample(['R-'+str(i) for i in range(count)],randomizer.randrange(3)))})
    return sorted(result,key=lambda r:r['id'])

def author(folder,records,format,crlf=False):
    folder.mkdir(exist_ok=True)
    for p in folder.iterdir():p.unlink()
    groups=[records[i::3] for i in range(3)]
    for n,group in enumerate(groups):
        if format=='yaml-0.3':
            items=[]
            for r in group:
                item={k:v for k,v in r.items() if v is not None and k not in ['statement','rationale','decomposes']}
                item['statement']=[{'prose':b['text']} if b['kind']=='prose' else {'math':{'language':'latex','payload':b['payload']}} for b in r['statement']]
                if r['rationale'] is not None:item['rationale']=[b['text'] for b in r['rationale']]
                if r['decomposes']:item['decomposes']=r['decomposes']
                items.append(item)
            text='# Generated test source; seed is recorded by the runner.\n'+json.dumps({'format':'mundanereq-yaml-0.3','requirements':items},ensure_ascii=False,indent=2)+'\n';name=f'{n}.mreq.yaml'
        else:
            parts=[]
            for r in group:
                text='requirement '+r['id']+'\ntitle: '+r['title']+'\n'
                if r['allocation'] is not None:text+='allocation: '+r['allocation']+'\n'
                text+='statement:\n'
                for b in r['statement']:
                    text+=('  '+b['text']+'\n') if b['kind']=='prose' else '  math latex\n    '+b['payload']+'\n  end math\n'
                if r['rationale'] is not None:text+='rationale:\n  '+r['rationale'][0]['text']+'\n'
                for target in r['decomposes']:text+='decomposes: '+target+'\n'
                text+='end requirement\n';parts.append(text)
            text='# Generated test source; seed is recorded by the runner.\n'+'\n\n'.join(parts);name=f'{n}.mreq'
        (folder/name).write_bytes(text.replace('\n','\r\n').encode() if crlf else text.encode())

def seeded(seed,root):
    expected=model(seed)
    for format in ['custom-0.2','yaml-0.3']:
        work=root/format;work.mkdir();w=Workflow(work,format);author(w.source,expected,format,True)
        assert values(w.compile())==expected;w.baseline([r['id'] for r in expected]);w.format_twice();assert values(w.compile())==expected;w.analyze()
        changed=copy.deepcopy(expected);changed[0]['title']='Revised '+changed[0]['title'];author(w.source,changed,format);w.compile()
        analysis,_=w.analyze(1);assert [(r['requirementId'],r['changedFields']) for r in analysis['coverage'] if r['state']=='review-stale']==[(changed[0]['id'],['title'])]
        # An ID correction has no implicit alias; plan references and baseline choice are explicit.
        renamed=copy.deepcopy(expected);old=renamed[0]['id'];new=old+'-NEW';renamed[0]['id']=new
        for r in renamed:r['decomposes']=sorted(new if target==old else target for target in r['decomposes'])
        author(w.source,renamed,format);w.compile();w.analyze(2)
        coverage=w.root/'plan/coverage.tsv';coverage.write_text(coverage.read_text().replace('\t'+old+'\n','\t'+new+'\n'));w.compile_plan();w.analyze(2)
        w.baseline([r['id'] for r in renamed]);w.analyze()
        invalid=copy.deepcopy(expected);invalid[0]['decomposes']=['ABSENT'];author(w.source,invalid,format);w.compile(1);w.analyze(2)
        snapshot={p.name:p.read_bytes() for p in w.source.iterdir()};execute([BIN/'mundanereq-format','--source='+format,'--write',w.source],2);assert snapshot=={p.name:p.read_bytes() for p in w.source.iterdir()}

if __name__=='__main__':
    args=argparse.ArgumentParser();args.add_argument('--seed',type=int);selected=args.parse_args();BUILD.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='workflow-regression-') as directory:
        root=Path(directory)
        seeds=[selected.seed] if selected.seed is not None else list(range(150100,150112))
        for seed in seeds:
            folder=root/str(seed);folder.mkdir()
            try:seeded(seed,folder)
            except Exception:
                target=BUILD/('failure-'+str(seed));shutil.copytree(folder,target,dirs_exist_ok=True)
                print('Replay: python3 experiments/0033-workflow-regressions/run.py --seed',seed,'; artifacts:',target,file=sys.stderr)
                raise
        if selected.seed is None:
            checkout=root/'clean-input';checkout.mkdir();revision=execute(['git','rev-parse','HEAD']).stdout.decode().strip()
            archive=execute(['git','archive',revision]).stdout
            p=subprocess.run(['tar','-xf','-','-C',str(checkout)],input=archive,capture_output=True,timeout=30);assert p.returncode==0
            summaries=[]
            for line in (HERE/'corpus.tsv').read_text().splitlines()[1:]:
                name,path,format,count=line.split('\t');folder=root/name;folder.mkdir();w=Workflow(folder,format);shutil.copytree(checkout/path,w.source)
                original={p.name:p.read_bytes() for p in w.source.iterdir()};a=w.compile();assert len(a['requirements'])==int(count);ids=[r['values']['id'] for r in a['requirements']];w.baseline(ids)
                analysis,report=w.analyze();(BUILD/(name+'.html')).write_bytes(report)
                summaries.append({'name':name,'requirements':int(count),'assertions':len(analysis['coverage']),'stale':[],'uncovered':[], 'reportSha256':digest(report)})
                # Delete all derived output and rebuild solely from the clean source copy.
                for p in w.out.iterdir():p.unlink()
                w.compile();w.baseline(ids);assert w.analyze()[1]==report
                w.blocked_imports();w.contexts()
                w.format_twice();assert values(w.compile())==values(a);w.analyze()
                suffix='.mreq.yaml' if format=='yaml-0.3' else '.mreq';first=sorted(w.source.glob('*'+suffix))[0];first.write_text('# Added author comment.\n'+first.read_text());first.rename(first.with_name('moved-'+first.name));w.compile();assert w.analyze()[1]!=report
            actual={'corpora':summaries,'seedStart':150100,'seedCount':12,'sourceContracts':['custom-0.2','yaml-0.3']}
            write(BUILD/'summary.json',actual);(BUILD/'input-revision.txt').write_text(revision+'\n')
            assert actual==json.loads((HERE/'golden/summary.json').read_text()),'review corpus/report golden'
    print('PASS workflow corpus: '+str(len(seeds))+' replayable seeds in both source modes; source/format/compile/plan/analyze/report and incomplete barriers'+('' if selected.seed is not None else '; five clean committed corpora, identical report rebuilds, pins/scopes/versions/contexts and moves'))
