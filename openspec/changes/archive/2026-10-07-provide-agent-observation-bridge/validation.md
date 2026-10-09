# Validation

2026-10-07, branch CAUSEWAY-4068-v4. Java 25.0.4, Maven 3.9.13, Boot 4.2.0-M1, Micrometer 1.18.0-M1 / Tracing 1.8.0-M1, OTel 1.64.0 and Java agent 2.31.1.

- Focused core/config reactor: 19 tests passed (8 agent wiring, 7 registry integration, 4 classifier).
- Exported-trace compatibility suite: 9 tests passed. Real attached-agent HTTP/JDBC and servlet/Quartz fixtures use production auto-configuration, with no fixture bridge or manually supplied agent exclusions. Verifies single trace ownership, framework ancestry, identities, classification, replay correlation and inactive observations. The agent fixture also exports Micrometer metrics to a local OTLP receiver with OTEL_METRICS_EXPORTER=none.
- Petclinic observation profile: clean reactor package passed after removing application-owned bridge source and agent properties. Configured frontend build completed.
- Both updated AsciiDoc pages rendered with Asciidoctor; strict change validation and git diff whitespace check passed.

The framework creates no SDK/exporter and does not attach the agent. Custom registry owners remain responsible for tracing handlers. Boot HTTP observation meters are absent in agent mode by design; other Micrometer metrics remain available. No interactive Docker/browser verification was performed in this change.

Post-checkpoint verification after `906a897df37`: the same 19 focused tests and 9 exported-trace/JPA tests passed, Petclinic packaged against the final installed bridge, both AsciiDoc pages rendered, and strict validation passed all 54 OpenSpec items.
