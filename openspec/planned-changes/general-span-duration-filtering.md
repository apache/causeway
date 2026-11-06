# General span duration filtering

Status: CAUSEWAY-4080 investigation complete; [evaluation and evidence](../changes/general-span-duration-filtering/evaluation.adoc) are ready for review.
The outcome is no-go for unrestricted live individual-span pruning under the existing arbitrary async/remote-span contracts, with conditional feasibility only for explicitly closed offline datasets.
No production filtering or supported threshold was introduced; any production implementation, UI collapsing or whole-trace sampling requires a separately approved scope.

Explicitly retained by user request on 2026-11-06 as idea #2 following the [maintenance-to-v4 reconciliation](forward-port-reconciliation-2026-11-06.md).
This remains a separate exploration, not an outstanding maintenance forward port or work included in the audit.

The investigation evaluated whether trivial spans can be removed by duration consistently across Boot-managed and Java-agent-managed tracing.
Its corpus includes Causeway framework and HTTP spans, agent SQL spans and Boot fixture JDBC observations rather than claiming identical automatic JDBC instrumentation in both modes.

The removed JPA threshold decided at parent completion, after children might already have been exported. Dropping a parent alone leaves orphaned JDBC spans and an incomplete trace tree. A replacement must explicitly resolve that problem.

Questions evaluated (see the linked evidence and limitations):

- Where should filtering live: SDK/agent extension, collector, or backend presentation?
- Can spans and descendants be buffered until a decision is possible, within bounded memory and time limits, including concurrent/async and remote children?
- When a short parent has a useful child, should the parent be retained, descendants discarded as a group, or surviving children safely reparented? Preserve meaningful ancestry.
- Retain failures and their useful diagnostic context, even when a failed child belongs to a short successful parent.
- Distinguish actual export/storage savings from UI-only hiding and from whole-trace sampling. Assess runtime overhead and lost diagnostic value.
- Define handling for late/missing spans, shutdown, exporter failures and incomplete traces; verify consistent Boot and agent behavior with real exported traces.

Do not promise duration-based suppression at span creation: duration is known only when the span ends. Prefer complete trace trees until a coherent, tested policy is available.

Predecessor: [remove JPA duration filtering](../changes/archive/2026-10-07-remove-jpa-duration-filtering/proposal.md), following [classification/correlation](../changes/archive/2026-10-07-classify-and-correlate-observations/proposal.md) on CAUSEWAY-4068-v4.
