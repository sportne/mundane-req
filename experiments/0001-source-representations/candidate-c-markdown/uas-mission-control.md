# UAS Mission-Control Requirements

This document presents the illustrative mission-control requirements in an order intended for engineering review. Each fenced mundane-req block is an authoritative requirement record within this candidate.

## Purpose and operational context

These requirements define the mission boundary, identity constraints, flight-plan authority, and loss-of-link response.

```mundane-req
requirement OPS-001
title: Controlled mission lifecycle
allocation: Mission-control system
statement:
  The mission-control system shall enable an authorized operator to control one active vehicle
  from vehicle activation until mission completion or initiation of safe recovery.
rationale:
  The operator needs an unambiguous interval of control responsibility.
source: SRC-CONOPS-001
end requirement
```

```mundane-req
requirement OPS-002
title: Vehicle identity integrity
allocation: Mission-control system
statement:
  The mission-control system shall prevent a flight plan or control command intended for one
  registered vehicle from being applied to another vehicle.
rationale:
  Misrouting a plan or command can create unsafe vehicle behavior.
source: SRC-CONOPS-001
end requirement
```

```mundane-req
requirement OPS-003
title: Authorized flight-plan execution
allocation: Mission-control system
statement:
  The mission-control system shall cause the active vehicle to execute only the flight-plan
  revision authorized for that vehicle and mission.
rationale:
  Vehicle identity, mission identity, and plan revision must remain bound throughout dispatch.
source: SRC-CONOPS-001
end requirement
```

```mundane-req
requirement OPS-004
title: Loss-of-link safety response
allocation: Mission-control system
statement:
  When the command link to the active vehicle remains unavailable beyond the configured loss
  tolerance, the mission-control system shall initiate the configured safe-recovery response.
rationale:
  Continuing a mission without a usable command link can exceed the assumed operational risk.
source: SRC-SAFETY-001
end requirement
```

## Vehicle identity and activation

```mundane-req
requirement SYS-001
title: Unique registered vehicle identifier
allocation: Vehicle manager
statement:
  The mission-control system shall associate each registered vehicle with exactly one vehicle
  identifier that is unique within the mission repository.
rationale:
  Stable unique identity is necessary for activation, routing, and audit records.
source: SRC-CONOPS-001
decomposes: OPS-002
end requirement
```

```mundane-req
requirement VM-001
title: Duplicate registration rejection
allocation: Vehicle manager
statement:
  The vehicle manager shall reject a registration request when its vehicle identifier is already
  associated with a different registered vehicle.
rationale:
  The registration boundary must enforce the uniqueness promised by SYS-001.
decomposes: SYS-001
end requirement
```

```mundane-req
requirement SYS-002
title: Activation eligibility
allocation: Vehicle manager
statement:
  The mission-control system shall permit a vehicle to become active only when that vehicle is
  registered and its presented identity matches its registered vehicle identifier.
rationale:
  Activation is the boundary at which a registered identity becomes the target of mission
  operations.
source: SRC-CONOPS-001
decomposes: OPS-001
decomposes: OPS-002
end requirement
```

```mundane-req
requirement SYS-003
title: Single active vehicle
allocation: Vehicle manager
statement:
  The mission-control system shall maintain no more than one active vehicle for a mission.
rationale:
  The experiment assumes a single unambiguous command target.
source: SRC-CONOPS-001
decomposes: OPS-001
decomposes: OPS-002
end requirement
```

```mundane-req
requirement VM-002
title: Atomic activation guard
allocation: Vehicle manager
statement:
  The vehicle manager shall reject an activation transaction unless the requested vehicle
  identifier matches a registered vehicle and no different vehicle is active for the mission.
rationale:
  One atomic decision prevents registration state and active-vehicle state from being checked
  inconsistently.
decomposes: SYS-002
decomposes: SYS-003
end requirement
```

## Flight-plan authorization and dispatch

```mundane-req
requirement SYS-004
title: Flight-plan authorization binding
allocation: Flight-plan manager
statement:
  The mission-control system shall mark a flight-plan revision as authorized only when an
  authorized operator approves that revision for the active vehicle and current mission.
rationale:
  Authorization must bind the plan revision, vehicle, mission, and approving authority.
source: SRC-CONOPS-001
decomposes: OPS-001
decomposes: OPS-003
end requirement
```

```mundane-req
requirement SYS-005
title: Vehicle-bound dispatch
allocation: Ground-control adapter
statement:
  The mission-control system shall include the active vehicle identifier in every dispatched
  flight plan and control command and shall reject dispatch when the destination identifier
  differs from the active vehicle identifier.
rationale:
  Checking identity at dispatch protects the final system boundary before transport.
source: SRC-CONOPS-001
decomposes: OPS-001
decomposes: OPS-002
decomposes: OPS-003
end requirement
```

```mundane-req
requirement GCA-001
title: Dispatch destination guard
allocation: Ground-control adapter
statement:
  The ground-control adapter shall reject a flight plan or control command whose destination
  vehicle identifier does not equal the active vehicle identifier supplied by the vehicle manager.
rationale:
  The adapter is the last controlled boundary at which destination mismatch can be rejected.
decomposes: SYS-005
end requirement
```

## Command-link monitoring

```mundane-req
requirement SYS-008
title: Monotonic safety timing
allocation: Timing service
statement:
  The mission-control system shall use a monotonic time source with resolution no greater than
  10 ms for every elapsed-time comparison that determines command-link availability or a
  safe-recovery response deadline.
rationale:
  Wall-clock correction must not shorten or extend safety-related elapsed-time decisions.
source: SRC-DESIGN-001
end requirement
```

```mundane-req
requirement SYS-006
title: Command-link loss determination
allocation: Link monitor
statement:
  While a vehicle is active, the mission-control system shall declare its command link unavailable
  at the first link-evaluation instant defined by the following mathematical expression.

  math latex
    k^\ast = \min\left\{k \in \{0,1,2,\ldots\} \mid
    t_0 + k T_{\mathrm{eval}} - t_{\mathrm{last}} \ge T_{\mathrm{loss}}\right\},
    \qquad
    t_{\mathrm{detect}} = t_0 + k^\ast T_{\mathrm{eval}}

    T_{\mathrm{loss}} = 2.0\,\mathrm{s},
    \qquad
    0 < T_{\mathrm{eval}} \le 0.10\,\mathrm{s}
  end math

  t_0 is the first scheduled link-evaluation time after vehicle activation, in seconds.
  k is a nonnegative integer evaluation index.
  T_eval is the fixed interval between evaluations, in seconds.
  t_last is the monotonic receipt time of the most recent valid vehicle message, or the vehicle
  activation time if no valid vehicle message has yet been received, in seconds.
  T_loss is the configured link-loss tolerance, in seconds.
  t_detect is the time at which the link is declared unavailable, in seconds.
rationale:
  The expression makes the threshold boundary and evaluation quantization explicit rather than
  hiding them in prose.
source: SRC-SAFETY-001
decomposes: OPS-004
end requirement
```

```mundane-req
requirement GCA-002
title: Valid-message timestamp
allocation: Ground-control adapter
statement:
  The ground-control adapter shall publish the monotonic receipt time of each valid message from
  the active vehicle with resolution no greater than 10 ms.
rationale:
  SYS-006 requires an identity-checked last-message time on the same monotonic basis used for
  evaluation.
decomposes: SYS-006
decomposes: SYS-008
end requirement
```

## Loss-of-link response

```mundane-req
requirement SYS-007
title: Loss-of-link response
allocation: Mission-control coordinator
statement:
  Within 100 ms after the command link is declared unavailable, the mission-control system shall
  cause the ground-control adapter to begin the first transmission attempt of a safe-recovery
  command for the active vehicle.
rationale:
  Prompt initiation of safe recovery limits continued operation without a usable command link.
source: SRC-SAFETY-001
decomposes: OPS-001
decomposes: OPS-004
end requirement
```

```mundane-req
requirement GCA-003
title: Priority safe-recovery transmission
allocation: Ground-control adapter
statement:
  When the ground-control adapter receives a safe-recovery request, it shall place the resulting
  command ahead of queued non-safety commands and use the active vehicle identifier as the
  destination.
rationale:
  Queued routine traffic must not delay the safety response or change its destination.
decomposes: SYS-007
end requirement
```

```mundane-req
requirement GCA-004
title: Recovery transmission deadline allocation
allocation: Ground-control adapter
statement:
  The ground-control adapter shall begin the first transmission attempt of a safe-recovery command
  within 50 ms after receiving the safe-recovery request.
rationale:
  Allocating half of the system response budget to the adapter leaves time for detection
  notification, request construction, and dispatch.
decomposes: SYS-007
end requirement
```

```mundane-req
requirement SYS-009
title: Loss-of-link audit record
allocation: Event recorder
statement:
  Within 1 s after the command link is declared unavailable, the mission-control system shall
  record the link-loss declaration time, active vehicle identifier, and safe-recovery command
  identifier.
rationale:
  Audit recording supports event reconstruction but must not obscure the timing or identity of
  the primary safety action.
source: SRC-SAFETY-001, revision B
end requirement
```

```mundane-req
requirement MCS-001
title: Loss-of-link event persistence
allocation: Event recorder
statement:
  The event recorder shall persist the link-loss declaration time, active vehicle identifier, and
  safe-recovery command identifier within 100 ms after receiving the event-record request.
rationale:
  Prompt persistence supports reconstruction even if later mission-control processing fails.
decomposes: SYS-009
end requirement
```

## Traceability orientation

Outgoing decomposition relationships are authoritative in the fenced requirement records and can be inspected by searching for decomposes: lines. A disposable report may present the same relationships as a matrix or summary.
