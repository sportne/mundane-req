# Identity Continuity Decision

Status: Provisional decision for source contract 0.2

Date: 2026-08-29

[Experiment 0016](../experiments/0016-identity-continuity/README.md) compares an
atomic ID correction under the current model with an explicit continuity
assertion and a second durable identity. It exercises the complete bounded graph
through the existing ReqIF adapter and a reversible candidate durable-ID policy.
The alternatives can help external update matching, but only when additional
identity information is authoritative and shared with the consumer.

Retain the human-facing ID as sole snapshot identity. An ID correction is
removal and addition semantically, even when Git makes correction intent clear.
Update internal links atomically and allow stale external references to fail
precisely rather than silently aliasing them.

Do not add aliases, rename records, or machine IDs for 0.2 on this evidence.
Reopen the question when any independently baselined consumer demonstrates a
need for cross-baseline identity that must be exchanged before correction. The
experiment proves bounded mechanics, not independent interoperability or a
universal absence of need.
