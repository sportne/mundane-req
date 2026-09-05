"""Rebuild from immutable clean input archive; compare report bytes and reject invalid views."""
import copy
import json
import subprocess
import tempfile
from pathlib import Path
from html.parser import HTMLParser
from urllib.parse import unquote

ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
BUILD=ROOT/'build/experiment-0029';BUILD.mkdir(parents=True,exist_ok=True)
RENDER=HERE/'render.py';VERIFY=ROOT/'build/maintained/mundane-verify'
BASES=['--source-base','current=../../examples/yaml/vaccine-monitoring','--source-base','plan=../../experiments/0028-verification-contract']


class Links(HTMLParser):
    def __init__(self):super().__init__();self.ids=[];self.links=[]
    def handle_starttag(self,tag,attrs):
        attrs=dict(attrs)
        if 'id' in attrs:self.ids.append(attrs['id'])
        if tag=='a':self.links.append(attrs['href'])


def render(path):return subprocess.check_output(['python3',str(RENDER),str(path)]+BASES)

def analyze(root,status):
    result=subprocess.run([str(VERIFY),'--root',str(root),'--plan',str(root/'fixtures/plan.json'),str(root/'fixtures/imports.json')],capture_output=True)
    assert result.returncode==status,result.stderr
    return result.stdout


if __name__=='__main__':
    revision=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
    with tempfile.TemporaryDirectory(prefix='report-clean-inputs-') as directory:
        checkout=Path(directory)
        archive=subprocess.Popen(['git','archive',revision],cwd=ROOT,stdout=subprocess.PIPE)
        subprocess.run(['tar','-xf','-','-C',str(checkout)],stdin=archive.stdout,check=True);archive.stdout.close();assert archive.wait()==0
        root=checkout/'experiments/0028-verification-contract'
        # The renderer/analyzer are installed tools; source inputs come from this immutable clean archive.
        a=analyze(root,1);output=BUILD/'analysis.json';output.write_bytes(a)
        html=render(output);report=BUILD/'report.html';report.write_bytes(html)
        output.unlink();report.unlink()
        output.write_bytes(analyze(root,1));assert output.read_bytes()==a
        assert render(output)==html;report.write_bytes(html)
        assert b'Review-stale: 2.' in html and b'Planned assertions: 57.' in html
        assert b'current:SYS-009' in html and b'#L' in html and b'CR-001' in html
        expected=HERE/'expected/report.html'
        if not expected.exists():raise SystemExit('Review build/experiment-0029/report.html before freezing golden')
        assert expected.read_bytes()==html,'report golden mismatch'
        links=Links();links.feed(html.decode('utf-8'));assert len(links.ids)==len(set(links.ids))==57
        for href in links.links:
            if href.startswith('#'):assert unquote(href[1:]) in links.ids
            else:assert (report.parent/unquote(href.split('#')[0])).is_file(),href
        missing=subprocess.run(['python3',str(RENDER),str(BUILD/'absent.json')],capture_output=True)
        assert missing.returncode==2 and not missing.stdout and missing.stderr
        # Deliberate rebaseline keeps current source values, makes only review findings change.
        manifest=json.loads((root/'fixtures/imports.json').read_text());manifest['imports'][0].update(path='fixtures/current.json',sha256=None)
        (root/'fixtures/imports.json').write_text(json.dumps(manifest))
        output.write_bytes(analyze(root,0));current=render(output);assert b'Review-stale: 0.' in current and current!=html
        original=json.loads(a)
        for change in [lambda x:x.update(complete=False),lambda x:x.update(format='unknown'),lambda x:x['linked'].update(complete=False),lambda x:x['linked']['imports'][0]['artifact'].update(format='unknown'),lambda x:x['coverage'].pop()]:
            invalid=copy.deepcopy(original);change(invalid);output.write_text(json.dumps(invalid))
            result=subprocess.run(['python3',str(RENDER),str(output)],capture_output=True)
            assert result.returncode==2 and not result.stdout and result.stderr
        # Opaque LaTeX and HTML-looking prose remain literal escaped content.
        special=copy.deepcopy(original);record=special['linked']['imports'][1]['artifact']['requirements'][0]
        record['values']['statement']=[{'kind':'math','language':'latex','payload':'x < y\n<script>literal</script>'},{'kind':'prose','text':'<script>alert(1)</script>'}]
        output.write_text(json.dumps(special));safe=render(output)
        assert b'&lt;script&gt;' in safe and b'<script>' not in safe and b'Opaque LaTeX' in safe
        # Failure beyond an output prefix must not report a completed view.
        record['values']['statement'][1]['text']='x'*1024*1024;output.write_text(json.dumps(special))
        args=['python3',str(RENDER),str(output)]
        process=subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE);process.stdout.read(64);process.stdout.close();assert process.wait(timeout=30)==2
        for descriptor in [1,2]:
            result=subprocess.run(['bash','-c',f'exec "$@" {descriptor}>&-','closed']+args,capture_output=True);assert result.returncode==2
        output.write_bytes(a);report.write_bytes(html)
        (BUILD/'input-revision.txt').write_text(revision+'\n')
    print('PASS report: clean committed input archive, identical rebuild/golden, 57 assertions and 2 stale rows, rebaseline updates, invalid-input rejection, escaped prose/math, broken output')
