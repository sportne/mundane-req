"""Public JVM/native recovery checks; original before captures are immutable evidence."""
import json
import subprocess
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'experiments/0031-parser-recovery'
CP=str(ROOT/'build/maintained/classes')+':'+str(ROOT/'build/dependencies/snakeyaml-engine-3.1.1.jar')
BIN=Path(sys.argv[1]).resolve()
for mode,source in [('custom','custom-0.2'),('yaml','yaml-0.3')]:
    folder=BASE/mode; paths=sorted(folder.iterdir()); snapshots={p:p.read_bytes() for p in paths}
    artifacts=[]
    for native in [False,True]:
        def command(tool):return [str(BIN/('mundanereq-'+tool.lower()))] if native else ['java','-cp',CP,'mundanereq.cli.'+tool+'Main']
        args=['--source='+source,'--root',str(folder),str(folder)]
        result=subprocess.run(command('Compile')+args,capture_output=True,timeout=30)
        assert result.returncode==1 and not result.stderr
        artifact=json.loads(result.stdout);assert not artifact['complete'] and not artifact['requirements']
        assert len(artifact['diagnostics'])==2 and all(d['phase']=='source' for d in artifact['diagnostics'])
        if mode=='custom':assert [(d['ruleId'],d['location']['start']['line']) for d in artifact['diagnostics']]==[('field-form',8),('field-form',21)]
        artifacts.append(result.stdout)
        for tool,options,status in [('Validator',[],1),('Formatter',['--write'],2)]:
            # Executable basenames differ from Java entry point names.
            native_name={'Validator':'validate','Formatter':'format'}[tool]
            executable=[str(BIN/('mundanereq-'+native_name))] if native else command(tool)
            check=subprocess.run(executable+['--source='+source]+options+[str(folder)],capture_output=True,timeout=30)
            assert check.returncode==status and check.stderr
        assert all(p.read_bytes()==data for p,data in snapshots.items())
    assert artifacts[0]==artifacts[1]
    expected=BASE/'results'/('after-'+mode+'.json')
    assert expected.read_bytes()==artifacts[0],('review changed recovery golden',expected)
print('PASS parser recovery: JVM/native primary diagnostics, incomplete compiled output, strict validation and preserved formatter inputs')
