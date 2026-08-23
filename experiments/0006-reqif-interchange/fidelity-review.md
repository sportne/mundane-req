# Experiment 0006: ReqIF Fidelity Review

Status: Completed assessment

Review date: 2026-08-22

## Result in brief

The complete mundane-req 0.1 semantic model can make a schema-valid ReqIF 1.2 round trip inside one explicit profile. Requirement fields, paragraph boundaries, opaque LaTeX payloads, and directed decomposition relationships survive with equal normalized inventories.

The result proves that ReqIF can carry the model. It does not prove that arbitrary ReqIF can be imported or that another requirements-management tool will preserve every profile convention.

## Preserved information

| Information | Mechanism | Result |
| --- | --- | --- |
| human-facing requirement ID | required profile string attribute | exact |
| title | profile string attribute and display `LONG-NAME` | exact profile value |
| optional allocation | optional profile string attribute | presence and text exact |
| statement paragraphs | profile XHTML `<p>` blocks | boundaries and text exact |
| `math latex` blocks | marked XHTML `<div>` containing `<pre>` | kind and payload exact |
| optional rationale | optional profile XHTML value | presence, boundaries, and text exact |
| optional external source | optional profile string attribute | presence and text exact |
| decomposition | directed profile `SPEC-RELATION` | child, parent, and count exact |
| semantic absence | omitted optional ReqIF attribute value | distinguished from empty text |

## Constructed transport information

The following ReqIF information has no corresponding mundane-req semantics and is constructed by the exporter:

- XML identifiers for objects, relationships, types, definitions, hierarchy nodes, and the document header;
- ReqIF datatype and attribute-definition objects;
- one flat, ID-sorted `SPECIFICATION` and its `SPEC-HIERARCHY` wrappers;
- one export timestamp copied to every required creation/last-change position;
- tool and source-tool profile identifiers;
- `LONG-NAME` display copies.

Import discards those values after checking the profile structure. They must not become requirement identity, authored order, file placement, per-object revision time, or evidence of approval.

## Information not exchanged

- source filenames, file grouping, line positions, and manual wrapping;
- Git commits, branches, tags, authors, commit messages, and baseline authority;
- third-party license files and repository-level provenance documents;
- verification-plan activities, executions, evidence, and results;
- project allocation-vocabulary policy and trace-completeness policy;
- information outside the selected `.mreq` source set;
- arbitrary XHTML, attachments, tables, lists, styling, and embedded objects;
- unknown ReqIF attributes, types, relation groups, tool extensions, and multiple specifications.

These omissions are not all defects. Most belong to source formatting, repository history, project policy, or unsupported ReqIF surface area rather than requirement-object semantics.

## Identity finding

ReqIF requires transport identifiers and also supports alternative identifiers. The profile deliberately derives a collision-free XML ID from the mundane-req human-facing ID and carries the human ID as an explicit attribute.

This avoids inventing a second durable identity during export, but it inherits the 0.1 rule: correcting the human ID changes the derived ReqIF object identifier and appears as replacement across independent exports. A cooperating tool may also assign or preserve its own identifiers.

Therefore the self-roundtrip does not resolve cross-baseline identity continuity. An independent-tool edit/roundtrip is the right next test for explicit continuity records versus a separate durable machine identity.

## Rich-content finding

ReqIF XHTML can preserve the current prose and math block model without changing `.mreq`. The adapter uses a small profile marker for LaTeX because ReqIF itself does not assign that domain meaning to `<pre>`.

An independent tool might preserve the XHTML exactly, normalize whitespace, remove CSS class values, convert rich text to a simplified representation, or expose it for user editing. Schema validity alone cannot predict that behavior. Cross-tool rich-content preservation remains unproven.

This is an interchange-profile concern, not evidence that `.mreq` needs XHTML or ReqIF-shaped fields.

## Attribute-model finding

ReqIF represents requirement content through typed, tool-defined attribute definitions and values. Mapping the six fixed mundane-req fields is straightforward. General arbitrary ReqIF import would require decisions about unknown attribute names, datatypes, multiplicity, defaults, enumerations, and whether values are intrinsic requirements information at all.

Do not answer that problem with a generalized metadata system before a real exchange partner and profile exist. A bounded partner-specific mapping is simpler, more inspectable, and less likely to import workflow accidents into authoritative source.

## Document-model finding

ReqIF separates flat `SpecObject` storage from ordered hierarchical `Specification` presentation. That is compatible with mundane-req's model/view separation.

The exporter adds one disposable flat specification because practical ReqIF tools commonly present objects through specifications. Import ignores it. No view grammar is needed merely to exchange the requirement set.

If a future exchange requires authored document order or section hierarchy, it should map from an independently justified mundane-req view model rather than from source file order.

## Decisions

1. Keep `.mreq` and the provisional 0.1 contract unchanged.
2. Treat ReqIF as derived interchange, not authoritative storage.
3. Retain the bounded profile as an experiment, not a general importer claim.
4. Preserve requirement content in named profile attributes rather than inferring meaning from arbitrary ReqIF labels.
5. Use XHTML only inside the adapter to preserve current semantic content blocks; do not adopt XHTML as requirement source.
6. Treat ReqIF specifications and hierarchy as presentation unless a future view model explicitly maps them.
7. Require explicit export time because ReqIF mandates timestamps absent from the source model; never infer per-requirement revision timestamps.
8. Reject unknown profile content rather than silently losing it.
9. Test an independent-tool roundtrip before designing configurable mappings.
10. Carry identity continuity into that cross-tool test.

## Remaining interoperability risks

- tool-specific handling of custom attribute definitions and `LONG-NAME`;
- preservation of XHTML class markers and `<pre>` whitespace;
- changes to ReqIF identifiers during import/export by another tool;
- relation direction conventions and user-visible labels;
- update-versus-create behavior in a receiving repository;
- unknown attribute preservation;
- schema-valid documents that use unsupported ReqIF features;
- active issues and implementation differences around the mature but historically layered ReqIF schema set.
