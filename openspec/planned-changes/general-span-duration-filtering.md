# General span duration filtering

Status: backlog / exploration; no implementation ticket assigned.

Explore whether trivial spans of any kind can be removed by duration, consistently in Boot-managed and Java-agent-managed tracing. This includes Causeway framework spans and automatic HTTP/JDBC/library spans, rather than a JPA-only policy.

The removed JPA threshold decided at parent completion, after children might already have been exported. Dropping a parent alone leaves orphaned JDBC spans and an incomplete trace tree. A replacement must explicitly resolve that problem.

Questions to investigate:

- Where should filtering live: SDK/agent extension, collector, or backend presentation?
- Can spans and descendants be buffered until a decision is possible, within bounded memory and time limits, including concurrent/async and remote children?
- When a short parent has a useful child, should the parent be retained, descendants discarded as a group, or surviving children safely reparented? Preserve meaningful ancestry.
- Retain failures and their useful diagnostic context, even when a failed child belongs to a short successful parent.
- Distinguish actual export/storage savings from UI-only hiding and from whole-trace sampling. Assess runtime overhead and lost diagnostic value.
- Define handling for late/missing spans, shutdown, exporter failures and incomplete traces; verify consistent Boot and agent behavior with real exported traces.

Do not promise duration-based suppression at span creation: duration is known only when the span ends. Prefer complete trace trees until a coherent, tested policy is available.

Predecessor: `remove-jpa-duration-filtering`, following `classify-and-correlate-observations` on CAUSEWAY-4068-v4.
