## Context

Roadmap step 8a ports maintenance commit `7961910a089` and its archived `add-audit-trail-write-span` design (recoverable at `1a9910c3e77^`).
V4 already supplies a profile-controlled Micrometer registry shared with either Boot tracing or the Java agent.
The common audit subscriber delegates individual and bulk callbacks directly to `AuditTrailEntryRepository`, covering both persistence implementations and custom repositories.

## Goals / Non-Goals

**Goals:** Identify synchronous audit persistence cost, preserve natural ancestry and original failures, retain audit enablement and no-op behavior, and prove Boot/agent exports.

**Non-Goals:** Entity-change evaluation, publisher instrumentation, per-entry spans inside bulk callbacks, transaction changes, payload attributes, new configuration, SDKs or dependencies in production.

## Decisions

### Observe the existing subscriber callback boundary

Inject `CausewayObservationIntegration` and wrap each enabled repository delegation with its provider and `Observation.observe(Runnable)`.
Use operation name `causeway.audittrail.write`, contextual name `write audit trail`, and existing static bean/module metadata.
Keep the enabled guard outside observation creation.
Do not instrument the generic publisher, which includes non-audit subscribers, or repositories, which would duplicate persistence-specific implementations.
Bulk callbacks remain direct bulk delegations rather than loops through the individual callback.

### Preserve current context and pipeline ownership

Use the integration's current registry rather than importing maintenance's agent-only setup.
Natural context can be an action, interaction, request or background boundary; never store or fabricate an action parent.
The wrapper records and rethrows repository failures, closes its scope and restores its caller context.
Automatic JDBC instrumentation remains agent-owned; Boot fixtures explicitly observe controlled SQL as in the existing regression suite.

### Keep telemetry static and private

Add no payload, object/property identity, old/new value, username, tenant, interaction ID, sequence or entry-count attributes at the audit boundary.
Ambient tracing attributes owned by enclosing operations are not changed.

### Validate at two levels

Focused subscriber tests use a real observation registry with mocked repository collaborators to verify both overloads, disabled/no-op modes, scope restoration, original failures and exact metadata.
Extend the existing child-JVM fixture to invoke the actual audit subscriber with a controlled H2 repository backend under existing production interaction/action services.
Verify exported audit ancestry and JDBC descendants, bulk aggregation, failure followed by success, static attributes, inactive profiles and absent exporters under both supported ownership modes.
The audit applib already sees core configuration through its provided runtime dependency; the regression module alone needs a test dependency on the audit applib.

## Risks / Trade-offs

- Small publications can produce sibling audit spans because the publisher selects individual callbacks → Preserve that contract rather than retaining scopes across callbacks.
- Constructor injection changes manual construction → Update or add explicit construction sites and tests; Spring resolves the existing integration bean.
- Upstream Boot/agent versions differ from maintenance → Validate against this branch's managed versions and real OTLP exports.
- Trace fixtures use controlled persistence → State that limitation and test callback shape, failures and unchanged delegation separately.

## Migration Plan

No settings, database or data migration is required.
Applications with the audit extension and active observation gain one span per enabled subscriber callback.
Rollback removes the wrapper without altering stored audit data.

## Open Questions

None requiring a product decision.
