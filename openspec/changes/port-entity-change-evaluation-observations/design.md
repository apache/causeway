## Context

The tracker lazily obtains candidate post-values and filters changed records before notifying publishers.
Derived accessors can execute SQL; concurrent modification already triggers a defensive-copy retry, while non-deadlock accessor failures already become unknown values.
Maintenance commit `02786054574` and its archived `add-entity-change-evaluation-span` design (at `1a9910c3e77^`) define the source behavior.
V4 uses `InteractionLayerTracker` rather than the maintenance interaction provider and already instruments transactions and publishing independently.

## Goals / Non-Goals

**Goals:** Identify aggregate evaluation cost, keep one boundary across retry, preserve natural context and publication behavior, and prove Boot/agent exports and audit sibling ancestry.

**Non-Goals:** Per-property spans, payload/count attributes, changed evaluation order, accessor optimizations, additional transaction/publishing spans, new configuration, SDKs or dependencies.

## Decisions

### Wrap actual evaluation, not transaction callbacks

Inject the existing `CausewayObservationIntegration` into `EntityChangeTrackerDefault`.
Keep `evaluateChangedProperties()` as the observed entry and extract its existing body unchanged into a private unobserved helper.
Bypass observation creation when the enlisted map is empty.
Otherwise use operation `causeway.entitychange.evaluate`, contextual name `evaluate property changes` and static tracker/module metadata.
Both the first attempt and the existing defensive-copy retry remain inside one `observe(Supplier)` call.
Wrapping `beforeCommit()` would incorrectly include subscribers and miss earlier lazy evaluation; wrapping `changedRecords()` would duplicate retry observations.

### Preserve lazy, failure and trace semantics

Do not alter memoization, record clearing, locks, changed-record filtering, transaction or publisher code.
Handled accessor failures remain handled by `PropertyChangeRecord`; only escaping failures reach the observation wrapper and are recorded and rethrown unchanged.
The observation closes before publication/audit callbacks and restores the current action, interaction, request or background parent without reconstructing context.
Automatic JDBC instrumentation remains agent-owned; Boot fixtures explicitly observe controlled SQL as in existing regression tests.

### Validate real boundaries with controlled collaborators

Focused tracker tests cover multiple candidates, unchanged/empty records, retry, handled/escaping failures, parent and child ancestry, exact metadata, no-op behavior and lazy memoization/publication order.
A fixture in the tracker test package exposes controlled records and invokes public tracker lifecycle methods for exported-trace tests.
Reuse the existing regression JVM and actual audit subscriber to verify that evaluation and audit spans are siblings under the current root interaction, with SQL descendants and failure-followed-by-success cleanup.
Records, property accessors and publication collaborators are controlled test doubles; this is not a full JDO/JPA domain deployment.
Update all explicit tracker constructor calls to supply the integration, without introducing a legacy constructor that could silently disable tracing.

## Risks / Trade-offs

- Derived accessors can mutate the candidate map → Preserve the existing retry inside one observation and test both attempts.
- Evaluation may happen before transaction completion → Test a public lazy accessor as well as `beforeCommit()` and ensure memoization avoids duplicate observations.
- Broad telemetry could leak property/value information → Assert exact static metadata and absence of payload sentinels in exports.
- Stacked PR history → Keep the change scoped to evaluation and rebase after audit PR #3897 merges.

## Migration Plan

No configuration, schema or data migration is required.
Manual tracker construction gains the existing integration parameter; Spring injects the registered integration.
Rollback removes the wrapper and restores the previous constructor without changing stored data.

## Open Questions

None requiring a product decision.
