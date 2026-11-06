## Why

Short spans can make traces noisy and expensive, but the removed JPA-only duration threshold could discard a parent after its JDBC children had already been exported.
Before introducing a general replacement, establish whether individual-span duration filtering can preserve meaningful ancestry and failures with bounded resources under both Boot-managed and Java-agent-managed tracing.

## What Changes

- Conduct an investigation and isolated proof-of-concept, not a production filtering rollout.
- Compare JVM processor/exporter, Collector and backend/UI approaches, explicitly distinguishing individual-span pruning, whole-trace sampling and display-only collapsing.
- Define a retention-first reference policy that preserves useful slow/error spans and their ancestors without silently reparenting or inventing roots.
- Use reproducible synthetic graphs and real Boot/agent exports covering framework, HTTP and JDBC spans, async/remote ancestry and late or incomplete traces.
- Evaluate bounded buffering, timeout/overflow/shutdown/exporter behavior, operational complexity and measured overhead/savings at each pipeline stage.
- Produce an evidence-backed go/no-go recommendation and identify the prerequisites, limitations and separate production change needed if an approach is viable.
- Retain current duration-independent observations, explicit discard behavior and single SDK/export ownership throughout the investigation.
- Retire disposable Python research tooling after recording the completed results, preserving its source/reproduction checkpoint in Git history rather than maintaining it as a regression suite.

## Capabilities

### New Capabilities

- `span-duration-filtering-evaluation`: Reproducible evaluation, safety criteria and decision evidence for ancestry-safe individual-span duration filtering across tracing ownership modes.

### Modified Capabilities

None.
The existing `observation-policy` and `main-observation-foundation` contracts remain unchanged; this change does not authorize production filtering or replacement configuration.

## Impact

Primarily test/experimental fixtures around `regressiontests/tracing-compatibility` and investigation documentation.
Any prototype dependencies or Collector configuration must be isolated, version-pinned and absent from the production BOM, application profiles and default export pipeline.
No new public API, production threshold, agent extension, second SDK/exporter or deployment requirement is introduced by this investigation.
The archived [JPA-filter removal](../archive/2026-10-07-remove-jpa-duration-filtering/proposal.md) and the retired backlog note in Git history supply provenance.
The completed investigation outcome is recorded in [evaluation.adoc](evaluation.adoc).
