# Trial Oracle

Status: Withheld from the TC-0702 participant

The reading task identifies `SYS-ALERT-NOTIFY-001`, its 60-second bound, and
direct parent `OPS-ALERT-001`. The added requirement has exactly one authored
parent, `SYS-ALERT-NOTIFY-001`. The retention change changes 30 to 90 days in
`SYS-RECORD-RETAIN-001`; the two child statements refer to the configured
retention period and need no content change. Moving `EDGE-STORE-001` changes no
inventory semantics. The temporary dangling target is rejected and absent from
the final source.

The component requirements transitively below `OPS-RECORD-001` are:

- `EXPORT-INTEGRITY-001`;
- `EXPORT-SELECT-001`;
- `STORE-ALERT-001`;
- `STORE-MONITOR-001`;
- `TIME-SERVER-001`; and
- `TIME-STATION-001`.

After the addition, the final inventory contains 61 requirements and 55
relationships. A semantically correct result may place or order records
differently because file location and record order are non-semantic.
