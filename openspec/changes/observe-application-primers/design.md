## Context

`ActionExecutor` dispatches matching action primers synchronously immediately before invoking the domain action method.
`EntityPage.onNewRequestCycle()` dispatches matching view primers synchronously before Wicket page preparation, guarded so that a page is primed once per request lifecycle.
`PrimingRegistryDefault` owns the immutable ordered registrations and invokes each matching callback directly.
Primer callbacks are intended to preload objects into the active ORM persistence context and can therefore account for significant Java-agent JDBC activity.
Today that activity inherits the enclosing action, interaction, or HTTP context without a boundary identifying the individual primer callback.

The observation profile already provides a framework-owned `CausewayObservationIntegration` backed by a no-op registry when observation is inactive.
The Java agent remains responsible for HTTP and JDBC instrumentation, propagation, sampling, and export.

## Goals / Non-Goals

**Goals:**

- Create one semantic observation around every matching action or view primer callback that is actually invoked.
- Isolate the duration and automatic JDBC descendants of each callback.
- Use domain-facing action and object identities with existing bounded naming rules.
- Preserve natural parentage, deterministic callback order, synchronous execution, and failure propagation.
- Preserve inactive-profile behavior and bounded static metadata privacy.

**Non-Goals:**

- Do not create an aggregate priming parent around all matching callbacks.
- Do not expose primer implementation classes, lambda names, registration order, targets, arguments, or loaded values.
- Do not change the public priming SPI, matching rules, registration lifecycle, Wicket visit guard, or callback order.
- Do not add observations when no primer matches.
- Do not count primer observations against the Causeway Wicket observation budget.
- Do not add an SDK, exporter, sampler, HTTP span, or JDBC span.

## Decisions

### Observe each callback at the registry dispatch boundary

`PrimingRegistryDefault` will wrap each `RegisteredActionPrimer.prime(...)` and `RegisteredViewPrimer.prime(...)` invocation separately.
This is the narrowest boundary that knows a callback actually matched and preserves one observation per registration.
The registry will obtain `CausewayObservationIntegration` through the existing service registry and will retain unchanged behavior when the integration is absent or no-op.

Instrumenting `ActionExecutor` and `EntityPage` was rejected because those call sites surround the entire lookup and dispatch opportunity, producing one aggregate observation rather than one observation per callback.
Instrumenting application implementations was rejected because it would require application authors to adopt tracing code and would not provide consistent framework semantics.
Using `ApplicationSpanService` was rejected because primers are framework-invoked lifecycle callbacks and need stable framework names rather than application-selected suffixes.

### Use separate stable names and bounded semantic contextual names

Action-primer observations will use stable name `causeway.priming.action` and contextual name `prime action <logical-member-identifier>`.
View-primer observations will use stable name `causeway.priming.view` and contextual name `prime view <logical-object-type>`.
Contextual names will use the existing case-preserving namespace fallback and deterministic 50-character truncation policy.

An action-primer observation will carry the complete domain-facing `causeway.object.type` and `causeway.action.id` identifiers.
A view-primer observation will carry the complete domain-facing `causeway.object.type` identifier.
The registry will retain canonical action identity from metamodel validation rather than derive identity from a primer implementation class.

Including an implementation class or lambda-generated name was rejected because such names can be unstable, proxy-dependent, operationally noisy, and unrelated to the domain operation being primed.
Adding an application-supplied primer label was rejected because it would expand the public SPI solely for telemetry and introduce another cardinality and privacy contract.

### Preserve natural parentage without synthetic scopes

Action-primer observations will naturally be children of the current action-invocation observation because action priming executes within that invocation before the action method.
View-primer observations will naturally be children of the current HTTP, interaction, or other request context and siblings of later page-preparation and page-render observations because view priming occurs before Wicket page preparation begins.
No action, page-preparation, or aggregate priming parent will be fabricated.

When several primers match one key, their observations will be sequential siblings with the same semantic name and canonical attributes.
Their separate durations and descendants identify callback boundaries without exposing registration order or implementation identity.

### Preserve failure and no-op semantics

Each observation will record a callback exception and close before the original exception propagates unchanged.
The existing fail-fast callback behavior remains unchanged, so a failing primer prevents later callbacks and the guarded action or page preparation from continuing as it does today.
When observation is inactive, the no-op path will invoke each callback exactly once without exporting metadata.

### Keep Java-agent work beneath the semantic callback

Automatic JDBC work initiated synchronously by a primer will inherit the active primer observation.
Causeway will not create a duplicate JDBC observation or alter Java-agent context propagation.
Non-primer work before or after the callback will remain beneath its existing natural parent.

## Risks / Trade-offs

- **[Risk] Multiple primers for one key create repeated sibling names.** → Preserve one span per callback as explicitly required and rely on separate timing and child structure rather than unstable implementation labels.
- **[Risk] A frequently invoked primer increases trace volume.** → Emit observations only for actual registered callback invocations and do not add an aggregate parent.
- **[Risk] A callback failure can leave observation scope active.** → Use the established synchronous observation closure pattern and test success, runtime failure, error, and no-op cleanup.
- **[Risk] Contributed actions could expose implementation identity.** → Resolve and retain the same domain-facing metamodel action identity used for registration and action invocation.
- **[Risk] Operators could mistake view priming for Wicket preparation.** → Document that view priming precedes page preparation and appears as its sibling under the natural request context.

## Migration Plan

No application migration is required.
Deployments with the `observation` profile will begin exporting one additional semantic observation per invoked primer callback.
Operators should account for the new stable names in span-volume estimates and may filter them by canonical action or object type.
Rollback removes the semantic observations without affecting priming execution or Java-agent JDBC instrumentation.

## Open Questions

None.
