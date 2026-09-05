#!/usr/bin/env python3
"""Author negative/pressure examples; expectations are independent of parser output."""
from pathlib import Path
import json
HERE=Path(__file__).resolve().parent
C='requirement 001\ntitle: A test\nstatement:\n  The display shall preserve its label.\nend requirement\n'
Y='requirements:\n  - id: "001"\n    title: "A test"\n    statement: >-\n      The display shall preserve its label.\n'
cases=[]
def case(name,c,y,cv,yv,layer=None,equal=None):
    folder=HERE/'invalid'/name;folder.mkdir(exist_ok=True)
    for filename,text in [('input.mreq.fixture',c),('input.yaml',y)]:
        (folder/filename).write_bytes(text if isinstance(text,bytes) else text.encode())
    cases.append(dict(name=name,custom_valid=cv,yaml_valid=yv,yaml_layer=layer,equal=equal))
case('missing-title',C.replace('title: A test\n',''),Y.replace('    title: "A test"\n',''),False,False,'schema')
case('unknown-field',C.replace('statement:','owner: team\nstatement:'),Y+'    owner: team\n',False,False,'schema')
case('duplicate-title',C.replace('title: A test','title: A test\ntitle: Again'),Y.replace('    title: "A test"','    title: "A test"\n    title: Again'),False,False,'syntax')
case('bad-id',C.replace('requirement 001','requirement -bad'),Y.replace('"001"','"-bad"'),False,False,'schema')
case('numeric-id',C,Y.replace('"001"','001'),True,False,'schema')
case('boolean-title',C.replace('A test','true'),Y.replace('"A test"','true'),True,False,'schema')
case('yaml12-yes',C.replace('A test','yes'),Y.replace('"A test"','yes'),True,True,equal=True)
case('unquoted-colon',C.replace('A test','status: ready # note'),Y.replace('"A test"','status: ready # note'),True,False,'syntax')
case('unquoted-hash-loss',C.replace('A test','status # ready'),Y.replace('"A test"','status # ready'),True,True,equal=False)
case('indentation',C.replace('  The display',' The display'),Y.replace('      The display','   The display'),False,False,'syntax')
case('empty-title',C.replace('title: A test','title: '),Y.replace('"A test"','""'),False,False,'schema')
case('null-allocation',C.replace('statement:','allocation: null\nstatement:'),Y+'    allocation: null\n',True,False,'schema')
case('empty-allocation',C.replace('statement:','allocation: \nstatement:'),Y+'    allocation: ""\n',False,False,'schema')
case('duplicate-target',C.replace('end requirement','decomposes: 001\ndecomposes: 001\nend requirement'),Y+'    decomposes: ["001", "001"]\n',False,False,'schema')
case('dangling-target',C.replace('end requirement','decomposes: MISSING\nend requirement'),Y+'    decomposes: [MISSING]\n',False,False,'domain')
case('duplicate-id',C+'\n'+C,Y+Y.split('requirements:\n',1)[1],False,False,'domain')
case('anchor',C,Y.replace('"A test"','&title "A test"'),True,False,'profile')
case('alias',C,Y.replace('"A test"','*missing'),True,False,'profile')
case('explicit-tag',C,Y.replace('"001"','!!str "001"'),True,False,'profile')
case('multiple-documents',C+'\n'+C.replace('requirement 001','requirement 002'),Y+'---\n'+Y.replace('"001"','"002"'),True,False,'syntax')
case('unterminated-source',C.rstrip(),Y.rstrip(),False,False,'physical')
case('invalid-utf8',C.encode().replace(b'A test',b'\xff'),Y.encode().replace(b'A test',b'\xff'),False,False,'physical')
case('two-errors',C.replace('title: A test\n','')+'\n'+C.replace('requirement 001','requirement 002').replace('title: A test\n',''),Y.replace('    title: "A test"\n','')+Y.split('requirements:\n',1)[1].replace('"001"','"002"').replace('    title: "A test"\n',''),False,False,'schema')
case('literal-prose-newline',C.replace('preserve its label.','preserve\n  its label.'),Y.replace('>-','|-').replace('preserve its label.','preserve\n      its label.'),True,False,'schema')
case('trailing-space',C.replace('A test','A test '),Y.replace('"A test"','"A test "'),False,False,'domain')
case('folded-terminal-newline',C,Y.replace('>-','>'),True,False,'schema')
case('escaped-id-newline',C,Y.replace('\"001\"','\"001\\n\"'),True,False,'schema')
case('escaped-control',C,Y.replace('\"A test\"','\"A\\u0000test\"'),True,False,'domain')
(HERE/'invalid/cases.json').write_text(json.dumps(cases,indent=2)+'\n')
