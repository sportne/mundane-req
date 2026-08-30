# SVEMS System Context — Baseline A

Status: Pilot context definition

Date: 2026-08-30

## Product purpose

The Service-Point Vaccine Equipment Monitoring System (SVEMS) helps clinic
personnel and public-health programme staff observe the condition and operation
of one vaccine refrigerator, receive local and remote alarms, retain monitoring
records, and investigate excursions and equipment faults despite intermittent
site power and network connectivity.

## Product boundary

Inside SVEMS:

- one external equipment-monitoring device (EMD);
- local display, audible alarm, controls, internal energy storage, and remote
  communications in that device;
- a hosted remote data service;
- a browser-based user interface; and
- notification delivery through a configured external gateway.

Outside SVEMS:

- the vaccine refrigerator, its sensors, and its integrated logger;
- the logger's standardized M2M data and power interface;
- mains power, cellular or Internet networks, browsers, and message gateways;
- clinic and public-health operating procedures; and
- decisions about vaccine disposition or clinical use.

## Stakeholders

| Stakeholder | Concern in this pilot |
| --- | --- |
| Clinic vaccine coordinator | Understand current conditions and alarms locally, including during network loss. |
| Public-health programme monitor | Observe alarms, faults, and trends remotely across authorized equipment. |
| Service technician | Diagnose the EMD, replace serviceable energy storage, and restore operation. |
| Programme administrator | Configure users, equipment identity, alarm recipients, time zone, and service period. |
| Employer/data owner | Retain ownership and controlled access to raw and summarized monitoring data. |
| Product verification team | Obtain objective evidence that allocated requirements are satisfied. |

## Operational contexts

- `CTX-NORMAL`: refrigerator power and remote connectivity are available.
- `CTX-NETWORK-LOSS`: local power remains available but remote connectivity is
  interrupted and later restored.
- `CTX-POWER-LOSS`: refrigerator-supplied power is unavailable; remote
  connectivity may remain available intermittently.
- `CTX-EXCURSION`: the logger reports a configured temperature-alarm condition.
- `CTX-FAULT`: the logger or EMD reports a condition impairing normal operation.
- `CTX-SERVICE`: a trained technician installs, diagnoses, or services the EMD.

## Baseline A assumptions

- One SVEMS deployment monitors one compatible refrigerator logger.
- The logger supplies standardized records and relative timestamps through the
  WHO M2M interface; SVEMS does not duplicate the logger's temperature sensor.
- The programme defines alarm thresholds and recipient policy at deployment.
- A reachable external notification gateway accepts notification requests;
  end-to-end delivery by a telecommunications provider is not under SVEMS
  control.
- The pilot uses a three-year hosted-data service period.
- The deployment country, hosting country, communications technology, and
  programme-specific access policy are procurement/configuration decisions.

## Explicit exclusions

SVEMS does not control refrigerator temperature, certify that vaccines remain
usable, prescribe excursion response, replace daily staff checks, or establish
the legal adequacy of any retention or security policy.
