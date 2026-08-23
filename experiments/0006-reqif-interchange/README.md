# Experiment 0006: Bounded ReqIF Interchange

Status: Completed bounded profile experiment

Plan date: 2026-08-22

Result date: 2026-08-22

Reproducible result: annotated tag `experiment-0006-result`

## Question

Can the provisional mundane-req 0.1 semantic model be exported to and imported from a deliberately small ReqIF 1.2 profile without allowing ReqIF's document, attribute, or workflow model to reshape authoritative `.mreq` source?

## Standards baseline

- OMG ReqIF specification: <https://www.omg.org/spec/ReqIF/1.2/>
- Formal specification: OMG file `formal/16-07-01`
- Normative ReqIF schema: OMG file `dtc/11-04-05`
- Normative XHTML driver schema: OMG file `dtc/11-04-06`
- ReqIF namespace required by the normative schema: `http://www.omg.org/spec/ReqIF/20110401/reqif.xsd`
- Header `REQ-IF-VERSION` required by that schema: `1.0`

The experiment targets the published ReqIF 1.2 schema even though its namespace and fixed header value retain identifiers originating in earlier standard artifacts.

Eclipse Requirements Modeling Framework/ProR documentation is used as implementation prior art. It confirms the practical distinction between flat `SpecObject` storage and `Specification`/`SpecHierarchy` presentation and shows tool-defined attribute configurations as normal ReqIF usage.

## Bounded profile

The profile is identified in the ReqIF header tool IDs as `mundanereq-reqif-profile-0.1`. Import deliberately rejects arbitrary ReqIF documents; accepting data while silently discarding unknown types or attributes would give a false impression of interoperability.

### Requirement mapping

| mundane-req | ReqIF profile |
| --- | --- |
| requirement object | `SPEC-OBJECT` of one profile `SPEC-OBJECT-TYPE` |
| ID | required string attribute `mundanereq.id` |
| title | required string attribute `mundanereq.title`; also copied to `LONG-NAME` for ordinary tool display |
| allocation | optional string attribute `mundanereq.allocation` |
| statement blocks | required XHTML attribute `mundanereq.statement` |
| rationale blocks | optional XHTML attribute `mundanereq.rationale` |
| source | optional string attribute `mundanereq.source` |
| `decomposes` | directed `SPEC-RELATION`, source child and target parent, of type `mundanereq.decomposes` |

ReqIF XML IDs use a deterministic collision-free hexadecimal encoding of the UTF-8 mundane-req ID. They are transport identifiers, not a new requirement identity.

### Rich-content mapping

An XHTML `<div>` contains one child per semantic block:

- prose becomes `<xhtml:p>`;
- `math latex` becomes `<xhtml:div class="mundanereq-math-latex"><xhtml:pre>...</xhtml:pre></xhtml:div>`.

The profile importer recognizes only those forms. This preserves paragraph boundaries, the math content kind, and exact de-indented math payload text without claiming to interpret LaTeX.

### Specification mapping

The export contains one `SPECIFICATION` with a flat, ID-sorted list of `SPEC-HIERARCHY` references so that ordinary ReqIF tools can display every object. This order and hierarchy are derived interchange presentation. Import ignores them and does not create mundane-req order, modules, hierarchy, or file placement.

### Time metadata

ReqIF requires creation and last-change timestamps, while mundane-req 0.1 deliberately derives revisions from Git and has no per-object timestamp. Export therefore requires one explicit timestamp and applies it uniformly as transport metadata. Import ignores it. The adapter must not present that value as an original requirement revision time.

## Intended operations

The GraalVM-compatible Java probe will provide:

    reqifprobe export TIMESTAMP OUTPUT.reqif FILE_OR_DIRECTORY...
    reqifprobe import-inventory INPUT.reqif
    reqifprobe roundtrip FILE_OR_DIRECTORY...

`roundtrip` compares the normalized mundane-req inventory before export and after profile import. It tests semantic preservation, not byte-for-byte `.mreq` formatting or arbitrary third-party ReqIF acceptance.

## Success criteria

1. Exported XML validates against the official ReqIF 1.2 XSD set.
2. The 0.1 conformance fixture round-trips with equal semantic inventory.
3. The UAS corpus round-trips with all prose, rationale, LaTeX payloads, and 25 final relationships preserved.
4. The FRET transfer corpus round-trips with 19 requirements and zero invented relationships.
5. Import rejects out-of-profile content rather than silently flattening it.
6. No `.mreq` grammar or 0.1 contract change is required.

## Non-goals

- arbitrary ReqIF import;
- ReqIF tool extensions;
- embedded binary objects;
- tables, lists, styling, or arbitrary XHTML preservation;
- multiple specifications or relation groups;
- ReqIF package (`.reqifz`) handling;
- merge/update negotiation with another RM repository;
- preservation of unknown attributes and types;
- source formatting round-trip;
- production converter architecture.

## Implementation

The experiment adds a dependency-free Java 21 adapter beside the existing parser. It uses only JDK XML APIs and compiles to a native executable with GraalVM Native Image.

    make test
    make native

Generated classes, native binaries, and exported ReqIF documents remain ignored under `build/` or another disposable output location.

Import is intentionally profile-strict. It rejects foreign tool IDs, unknown attribute-value kinds, unknown attribute definitions, nested or incomplete profile specifications, unknown relation types, duplicate IDs/links, and references to missing objects. It does not silently flatten arbitrary ReqIF.

## Results

The JVM harness passes five grouped tests:

- the provisional 0.1 conformance fixture round-trips with 3 requirements and 3 relationships;
- the final UAS corpus round-trips with 21 requirements and 25 relationships;
- UAS prose, rationale, paragraph boundaries, and the exact opaque LaTeX payload survive;
- the FRET transfer corpus round-trips with 19 requirements and zero invented relationships;
- deterministic output, profile rejection, hexadecimal transport IDs, and all three CLI operations behave as specified.

The GraalVM CE Java 21.0.2 native executable builds without fallback and independently reports the same three successful semantic round trips.

Exports for all three corpora validate against the official ReqIF 1.2 schema set. The downloaded normative OMG artifacts used for the recorded check were:

| Artifact | SHA-256 |
| --- | --- |
| `reqif.xsd` (`dtc/11-04-05`) | `9243f345540f25db3b53403da9ad9cd4744277ef01492ac3589937f533ba94c0` |
| `driver.xsd` (`dtc/11-04-06`) | `4995bc97cf0a9b8462ca295006dd54d9a85fb820cf9fd6e134a51743fc44effd` |

The W3C XHTML modules were retrieved from <https://www.w3.org/MarkUp/SCHEMA/> because the historical `/TR/xhtml-modularization/SCHEMA/` locations referenced by the OMG driver rejected direct retrieval in this environment. A standard JAXP `SchemaFactory` with a local XML catalog resolved those equivalent W3C module filenames. All three documents returned a valid result.

The detailed mapping losses and implications are recorded in the [fidelity review](fidelity-review.md).

## Disposition

The bounded profile succeeds as a semantic self-roundtrip experiment. No `.mreq` grammar or provisional 0.1 contract change is required.

Do not describe this result as general ReqIF interoperability. The next credible interchange evidence would be a cross-tool roundtrip through at least one independent implementation such as Eclipse RMF/ProR, including edits to IDs, rich text, relationships, and unknown attributes. That work should test this profile before adding configurable mapping machinery.
