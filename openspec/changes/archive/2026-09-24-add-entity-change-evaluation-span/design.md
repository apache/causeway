## Context

`EntityChangeTrackerDefault` enlists candidate property changes throughout a transaction and lazily memoizes their final state.
At transaction completion, `evaluateChangedProperties()` traverses the enlisted records, obtains each current post-value, compares pre-values and post-values, and retains the records that require publication.

For JDO entities, this traversal can evaluate derived properties as well as persisted state.
Derived accessors can execute application queries, so the OpenTelemetry Java agent exports their JDBC spans as otherwise unexplained children of the current action between command persistence and audit-trail persistence.
The work is not owned by the audit extension because it occurs before `EntityPropertyChangePublisher` invokes subscribers.

The tracker catches `ConcurrentModificationException` and repeats the evaluation against a defensive copy.
Property accessor failures that are recognized as non-deadlocks become an unknown post-value according to existing behavior, while deadlocks and other escaping failures terminate evaluation.
The lazy result can also be requested through tracker APIs, so the observation must surround the actual evaluation rather than assume `beforeCommit()` is always its first trigger.

## Goals / Non-Goals

**Goals:**

- Identify the aggregate cost of evaluating enlisted property changes and obtaining current post-values.
- Make automatic JDBC work from derived property accessors a descendant of a stable semantic span.
- Preserve one bounded observation around the complete evaluation, including any defensive retry.
- Avoid empty observations for transactions with no enlisted property-change records.
- Preserve natural context propagation and all existing entity-change publication semantics.
- Keep telemetry static and free of domain, identity, payload, and transaction data.

**Non-Goals:**

- Do not add one span per entity, property, accessor, or retry attempt.
- Do not change which persisted or derived properties are evaluated by JDO or JPA.
- Do not optimize, cache, suppress, or reorder derived property evaluation.
- Do not include audit persistence or command persistence in the evaluation span.
- Do not replace or configure Java-agent JDBC, HTTP, context propagation, sampling, or export behavior.
- Do not diagnose the application-specific implementation of an expensive derived accessor.

## Decisions

### Observe the actual changed-property evaluation

`EntityChangeTrackerDefault.evaluateChangedProperties()` will own the observation around its complete existing evaluation and defensive-copy retry.
This is the narrowest aggregate boundary that includes current post-value access and pre/post comparison while excluding publication callbacks.

Wrapping `beforeCommit()` was rejected because it would include entity-change publication and incorrectly make `write audit trail` a child of evaluation.
Wrapping only the `memoizeChangesIfRequired()` call in `beforeCommit()` was rejected because another tracker API can trigger lazy evaluation earlier, leaving the real work outside that span.
Wrapping `changedRecords()` was rejected because the concurrent-modification recovery path could create multiple spans for one logical evaluation.

### Use one static aggregate observation

The stable observation name will be `causeway.entitychange.evaluate` and the contextual display name will be `evaluate property changes`.
The observation provider will use `EntityChangeTrackerDefault` and `CausewayModulePersistenceCommons.NAMESPACE` for the existing static bean and module metadata.

No entity type, property identifier, target, bookmark, pre-value, post-value, user, tenant, transaction identifier, interaction identifier, sequence number, collection size, changed count, or retry count will be attached.
Per-property spans were rejected because they would amplify span volume, reveal application structure, and potentially encourage attaching sensitive values or identities.

### Emit only when there is candidate work

The tracker will bypass observation creation when the enlisted property-change record collection is empty.
When records are present, it will create one observation even if comparison ultimately finds that none changed, because post-value evaluation still occurred.

Always emitting a span from `beforeCommit()` was rejected because read-only and unrelated transactions would gain empty framework spans.

### Preserve natural hierarchy and Java-agent ownership

The observation will inherit the Micrometer/OpenTelemetry context current when lazy evaluation begins.
In the foreground action case, the intended hierarchy is:

[source,text]
----
act InvoiceSummary#invoiceAll
├── UPDATE isiscommand.CommandWithArchive
├── evaluate property changes
│   ├── SELECT codaproxy.CodaTax
│   └── ...
└── write audit trail
    ├── INSERT isisaudit.AuditEntryWithArchive
    └── ...
----

Evaluation and audit persistence remain siblings because evaluation completes before publication invokes the audit subscriber.
If evaluation occurs under an interaction, request, or background context without a current action, that natural context remains the parent.
The Java agent continues to create JDBC spans and automatically adopts the evaluation observation as their current parent.

### Preserve completion and failure semantics

The observation closes synchronously when evaluation returns or throws.
An escaping exception is recorded on the observation and rethrown unchanged.
An accessor failure already converted by `PropertyChangeRecord` to an unknown value remains handled and does not become a new escaping failure solely for telemetry.
The existing `ConcurrentModificationException` retry remains inside the same observation and retains its existing logging and result semantics.

When observation is inactive, the no-op registry executes the same evaluation closure without exporting a Causeway span.

## Risks / Trade-offs

- **[Risk] The span name may suggest that only changed records are accessed even though candidate records are evaluated before filtering.** → Use `evaluate property changes`, which describes the determination process rather than asserting that every candidate changed.
- **[Risk] Checking for candidate work could race with enlistment.** → Perform the check at the same transaction-scoped evaluation boundary without introducing additional mutable state or changing synchronization behavior.
- **[Risk] Derived accessors can mutate entities and trigger the existing retry.** → Keep both attempts inside one aggregate observation and preserve the defensive-copy fallback unchanged.
- **[Risk] A broad transaction callback span could accidentally absorb audit persistence.** → Instrument only actual changed-property evaluation and assert sibling hierarchy in tests.
- **[Risk] Framework instrumentation could duplicate JDBC spans.** → Add no JDBC instrumentation and verify that Java-agent spans remain unique descendants.

## Migration Plan

No application data or configuration migration is required.
Deploy the framework update with the existing `observation` profile and OpenTelemetry Java agent configuration.
Verify representative traces show `evaluate property changes` between command persistence and audit writing, with derived-property JDBC spans below evaluation.
Rollback consists of reverting the framework instrumentation; entity-change publication and persistence behavior remain compatible throughout.

## Open Questions

None.
