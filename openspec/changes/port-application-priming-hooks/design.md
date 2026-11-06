## Context

Maintenance `19d2a36d2f9` supplies the public priming API, startup registry and action/view boundaries.
V4 uses record-based action executors, `correspondingClass()` metadata and a relocated `DomainObjectPage` with existing page preparation observations and entity refresh behavior.
Its current action invocation already separates physical and domain-facing identifiers.

## Goals / Non-Goals

**Goals:** Exact validated startup registration, immutable positional arguments, synchronous ordinary-action hooks after effective argument adjustment, authorized once-per-page/request view hooks and transparent callback failures.

**Non-Goals:** Primer observations (10b), background/async work, automatic batch-fetching algorithms, hierarchy matching, configurable per-request registration, persistence changes or new dependencies.

## Decisions

### Keep the public callback and registrar API

Add `ActionArguments`, `ActionPrimer<T>`, `ViewPrimer<T>`, `PrimingRegistrar` and `PrimingRegistry` in applib.
Arguments are a shallow defensive snapshot of the positional list, preserve nulls and array-valued parameters as one slot, and expose typed access and an unmodifiable list.
Callbacks must be synchronous, read-only, independently idempotent and safe for concurrent invocation.
Their purpose is to fill the caller's ORM persistence context; the framework does not enforce read-only behavior or manage a separate transaction.

### Collect registrations after complete metamodel loading

Register a metamodel service implementing the registration API, internal dispatch service and `MetamodelListener`.
Validate classes against exact already-loaded specifications using `correspondingClass()`, without triggering lazy superclass discovery.
Validate local names through `getAction(name, MixedIn.INCLUDED)` with exact member-name comparison, supporting contributed actions while rejecting malformed/unknown names.
Preserve service registry iteration and per-key registration order for deterministic invocation, but callbacks must not depend on that order.
Freeze defensive maps/lists for concurrent read-only use before serving requests; always close registration on startup failure and never publish partial registrations.
Fail startup with registrar/class/action diagnostics rather than silently dropping invalid registrations.

### Adapt the action boundary without changing invocation identity

Resolve the optional internal priming service through the action's facet holder, without adding state to the record.
Invoke it immediately before method/cache lookup after executing-event argument adjustments, including non-event ordinary actions.
Use `head.owner()` as the callback's mixed-in domain target and the current invocation's separate domain-facing member name.
Skip pass-through execution and all synthetic mixed-in property/collection facets, even if an association cannot be resolved.
Leave rule checking, transactions, event posting, query-result caching, return adaptation and physical invocation identity unchanged.

### Authorize and prime before domain-page traversal

Call a shared authorization/priming boundary in initial `DomainObjectPage.buildPage()` before layout/title/property/collection traversal and in later `onNewRequestCycle()` after its existing refresh behavior.
Check visibility before application callbacks.
Use request-cycle metadata containing an identity set of page instances, not serialized page fields or global/thread-local state.
Mark admission before calling the primer so lifecycle re-entry does not retry a failing callback in the same request.
Later full or Ajax request cycles have fresh metadata and may prime again.
Keep existing page observations and cleanup intact without adding dedicated primer observations.

## Risks / Trade-offs

- Callbacks can be expensive or mutate state → Document their intended read-only synchronous contract; keep failure propagation explicit.
- Startup callbacks may retain the registry → Reject mutation once the registration window closes, including after failed initialization.
- Action events can replace argument values → Snapshot effective arguments only after the executing event, before method invocation.
- Wicket may visit preparation more than once → Share identity-based request admission across initial/later hooks and test new/Ajax request cycles.
- Subtype or mixin implementation confusion → Use exact metamodel keys, mixee targets and existing domain-facing invocation identity.
- Stacked branch history → Rebase after PR #3911 merges, without mixing 10b into this change.

## Migration Plan

The feature is additive and opt-in through registrar beans; applications without registrars retain normal behavior.
No settings, schema, data or observation-profile migration is needed.
Rollback removes registrars before removing the additive API.

## Open Questions

None requiring a product decision.
