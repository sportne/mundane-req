"""Experimental YAML profile. Syntax parsing remains entirely in ruamel.yaml."""
from pathlib import Path
import io
import json
import re
from ruamel.yaml import YAML
from ruamel.yaml.events import AliasEvent
from ruamel.yaml.error import YAMLError
from jsonschema import Draft202012Validator

HERE = Path(__file__).resolve().parent
SCHEMA = json.loads((HERE / 'schema.json').read_text())
Draft202012Validator.check_schema(SCHEMA)
VALIDATOR = Draft202012Validator(SCHEMA)
BOUNDARY = '\u0009\u000a\u000b\u000c\u000d \u0085\u00a0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000'
CONTROL = re.compile('[\x00-\x09\x0b-\x1f\x7f-\x9f]')

def yaml():
    y = YAML(typ='rt', pure=True)
    y.version = (1, 2)
    y.preserve_quotes = True
    y.allow_duplicate_keys = False
    # ruamel's default resolver adds timestamps beyond the selected YAML Core schema.
    table = y.resolver.versioned_resolver
    for initial, entries in list(table.items()):
        table[initial] = [(tag, pattern) for tag, pattern in entries
                          if tag != 'tag:yaml.org,2002:timestamp']
    y.width = 88
    y.indent(mapping=2, sequence=4, offset=2)
    return y

def location(data, path):
    node = data
    pos = (0, 0)
    for part in path:
        try:
            pos = node.lc.item(part) if isinstance(node, list) else node.lc.key(part)
            node = node[part]
        except (AttributeError, KeyError, IndexError, TypeError):
            break
    return pos[0] + 1, pos[1] + 1

def diagnostic(file, layer, code, message, pos=(1, 1)):
    return dict(file=Path(file).name, layer=layer, code=code, message=message,
                line=pos[0], column=pos[1])

def parse(file):
    try:
        raw = Path(file).read_bytes()
        text = raw.decode('utf-8')
    except UnicodeDecodeError as e:
        before = raw[:e.start]
        return None, [diagnostic(file, 'physical', 'invalid-utf8', 'Invalid UTF-8',
                                (before.count(b'\n')+1, len(before.rsplit(b'\n',1)[-1])+1))]
    # Profile deliberately retains the current physical source restrictions.
    if text.startswith('\ufeff') or '\t' in text or re.search(r'\r(?!\n)',text) or not text.endswith('\n'):
        return None, [diagnostic(file,'physical','physical-profile','BOM/tab/bare CR/unterminated source')]
    try:
        for event in yaml().parse(text):
            if isinstance(event,AliasEvent) or getattr(event,'anchor',None) or getattr(event,'tag',None):
                return None,[diagnostic(file,'profile','indirection-or-tag','Anchors, aliases and explicit tags are outside this profile',
                                        (event.start_mark.line+1,event.start_mark.column+1))]
        data = yaml().load(text)
    except YAMLError as e:
        mark = getattr(e, "problem_mark", None) or getattr(e, "context_mark", None)
        return None,[diagnostic(file,'syntax',type(e).__name__,getattr(e, 'problem', None) or str(e),
                                (mark.line+1,mark.column+1) if mark else (1,1))]
    except Exception as e:
        raise RuntimeError(f'Unexpected parser failure for {file}') from e
    errors = sorted(VALIDATOR.iter_errors(data), key=lambda e: str(list(e.path)))
    if errors:
        return None,[diagnostic(file,'schema',str(e.validator),e.message,location(data,list(e.path))) for e in errors]
    return data, []

def normalize(data):
    output=[]
    for item in data['requirements']:
        statement=item['statement']
        if isinstance(statement,str): statement=[{'prose':statement}]
        blocks=[]
        for b in statement:
            blocks.append(['prose',str(b['prose'])] if 'prose' in b else ['math','latex',str(b['math']['payload'])])
        rationale=item.get('rationale')
        if isinstance(rationale,str):rationale=[rationale]
        output.append(dict(id=str(item['id']),title=str(item['title']),allocation=item.get('allocation'),
                           statement=blocks,rationale=None if rationale is None else [['prose',str(p)] for p in rationale],
                           source=item.get('source'),decomposes=sorted(map(str,item.get('decomposes',[])))))
    return sorted(output,key=lambda r:r['id'])

def inspect(paths):
    records=[]; diagnostics=[]; origins={}
    if not paths:
        return dict(valid=False, requirements=[], diagnostics=[diagnostic(".", "input", "no-source-files", "Empty source set")])
    for file in paths:
        data, ds = parse(file); diagnostics.extend(ds)
        if data is None:continue
        for i, item in enumerate(data['requirements']):
            r = normalize({'requirements': [item]})[0]
            key=(str(file),i)
            origins[key]=(data,i)
            records.append((file,key,r))
    by_id={}
    for file,key,r in records:
        data,i=origins[key]
        for field in ['title','allocation','source']:
            value=r[field]
            if value is not None and (value[0] in BOUNDARY or value[-1] in BOUNDARY):
                diagnostics.append(diagnostic(file,'domain','scalar-boundary','Scalar boundary whitespace',location(data,['requirements',i,field])))
        strings=[r['id'],r['title'],r['allocation'],r['source']]+[b[-1] for b in r['statement']]+([] if r['rationale'] is None else [b[1] for b in r['rationale']])
        if any(CONTROL.search(s) or any(0xd800 <= ord(c) <= 0xdfff for c in s) for s in strings if s is not None):
            diagnostics.append(diagnostic(file,'domain','control-character','Prohibited decoded character',location(data,['requirements',i])))
        if r['id'] in by_id:
            diagnostics.append(diagnostic(file,'domain','duplicate-id','Duplicate requirement ID',location(data,['requirements',i,'id'])))
        by_id[r['id']]=r
    for file,key,r in records:
        data,i=origins[key]
        for target in r['decomposes']:
            if target not in by_id:
                diagnostics.append(diagnostic(file,'domain','dangling-reference',f'Unknown requirement {target}',location(data,['requirements',i,'decomposes'])))
    return dict(valid=not diagnostics,requirements=sorted([r for _,_,r in records],key=lambda r:r['id']),diagnostics=diagnostics)

def roundtrip(text):
    y=yaml();data=y.load(text);stream=io.StringIO();y.dump(data,stream)
    return stream.getvalue()
