## Context

The audit-trail extension receives enlisted `EntityPropertyChange` records through `EntityPropertyChangeSubscriberForAuditTrail` and persists an `AuditTrailEntry` for each change.
Its bulk callback delegates to `AuditTrailEntryRepository.createFor(Can<...>)`, which uses `RepositoryService.execInBulk(...)` while each entry is persisted and flushed.
The OpenTelemetry Java agent already creates JDBC spans for the resulting reads and inserts, but no semantic observation identifies the enclosing operation as audit-trail writing.
Consequently, a large audited action presents many database spans directly beneath the action and obscures their aggregate audit cost.

The existing `CausewayObservationIntegration` supplies profile-controlled Micrometer observations backed by the Java agent's current OpenTelemetry context.
It is a no-op without the `observation` profile and already supplies static `causeway.bean` and `causeway.module` metadata.
The Java agent must continue to own HTTP and JDBC instrumentation, propagation, sampling, and export.

## Goals / Non-Goals

**Goals:**

- Create one semantic parent around each audit-trail subscriber persistence callback.
- Preserve natural parentage beneath the currently active action, interaction, request, or background trace.
- Make all automatic JDBC work initiated by the callback descendants of the audit observation.
- Aggregate a bulk callback without adding one framework span per audit entry.
- Preserve audit configuration, persistence, transaction, exception, and ordering behavior.
- Keep names and metadata static, bounded, and free of audit payload or identity data.
- Preserve no-op behavior when observation is inactive.

**Non-Goals:**

- Trace generic entity-property-change publication or custom subscribers as audit work.
- Add a span for every audit entry within a bulk callback.
- Expose changed property identities, targets, bookmarks, old or new values, users, tenants, interaction IDs, sequences, or entry counts.
- Change the publisher's single-versus-bulk threshold or callback ordering.
- Change audit persistence batching, flush behavior, schema, or retention.
- Add an OpenTelemetry SDK, exporter, sampler, or dependency.

## Decisions

### Observe the audit-trail subscriber boundary

`EntityPropertyChangeSubscriberForAuditTrail` will open `causeway.audittrail.write` immediately before delegating to `AuditTrailEntryRepository` and close it after the repository callback completes.
The contextual name will be the static phrase `write audit trail`.
The enabled check will remain outside the observation so a disabled audit extension creates neither entries nor spans.

This boundary precisely identifies extension-owned audit work and includes detached-entry creation, initialization, repository persistence, flushing, and automatic JDBC calls.
Instrumenting `EntityPropertyChangePublisherDefault.publishChangedProperties()` was rejected because that boundary includes change extraction and every application-defined subscriber, not only audit persistence.
Instrumenting the JDO or JPA repository implementations was rejected because the common subscriber already covers both persistence technologies and any application-provided `AuditTrailEntryRepository` implementation.

### Aggregate according to the existing callback shape

The bulk `onChanging(Can<EntityPropertyChange>)` callback will create one observation around the complete repository bulk operation.
The individual `onChanging(EntityPropertyChange)` callback will create one observation around that individual repository operation.
The bulk implementation will not delegate through an observed individual callback, so one bulk publication does not create nested or per-entry framework spans.

This preserves the existing publisher threshold and callback contract.
For a small publication that the core publisher delivers as individual callbacks, repeated audit observations are accepted because there is no safe subscriber-local lifecycle boundary spanning all sibling callbacks.
Changing publisher loop ordering or retaining an observation across callbacks was rejected because it would alter subscriber semantics or risk overlapping scopes.

### Use the current trace context without explicit reparenting

The observation will use `CausewayObservationIntegration` and Micrometer's current context.
When audit persistence runs before an action observation closes, `write audit trail` will naturally be its child and Java-agent JDBC spans will naturally be children of the audit observation.
When publication occurs at another transaction or interaction boundary, the observation will remain under whatever request, interaction, or background context is current rather than fabricating an action parent.

Explicit span links, stored span contexts, and interaction-ID-based reparenting were rejected because they complicate lifecycle handling and can create invalid or cross-trace parentage.

### Attach static framework metadata only

The observation will rely on the existing static bean and module metadata and will add no audit payload attributes.
In particular, it will not attach the callback size because entry counts vary per interaction and are not required to identify the operation.
The stable and contextual names do not vary between single and bulk callbacks.

### Preserve failure and inactive behavior

The observation wrapper will record a thrown repository failure and rethrow the original exception unchanged.
The existing no-op registry will execute the same repository callback without exporting a span when observation is inactive.
No catch-and-suppress behavior or audit-specific sampling will be introduced.

## Risks / Trade-offs

- [Small publications can produce multiple sibling audit spans] → Preserve the existing callback contract and avoid unsafe cross-callback scopes; bulk publications still produce one aggregate span.
- [The current context is not always an action] → Retain truthful natural parentage under the active interaction, request, or background trace rather than forcing a stale action parent.
- [Audit SQL is already visible and the new span adds telemetry volume] → Add only one span per existing subscriber callback and no per-entry framework spans.
- [Observation code could alter persistence failures] → Use the standard observation wrapper and test that the original exception instance or type is rethrown.
- [Payload attributes could disclose sensitive audit data] → Emit only static framework metadata and test the absence of target, value, user, tenant, identifier, sequence, and count attributes.

## Migration Plan

Applications using the audit-trail extension receive the new observation only when the existing `observation` profile is active.
No configuration, schema, or data migration is required.
Rollback consists of removing the subscriber observation wrapper; audit entries and Java-agent JDBC spans remain unchanged.

## Open Questions

- Confirm during implementation whether the audit-trail applib's existing provided runtime dependency exposes `CausewayObservationIntegration` directly or whether an explicit existing-module dependency declaration is required.
- Confirm with the Java-agent compatibility fixture that repository JDBC spans retain the audit observation as their immediate semantic ancestor for both successful and failing persistence paths.
