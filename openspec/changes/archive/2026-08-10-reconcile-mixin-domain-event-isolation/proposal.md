## Why

This is child change 1 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-1**;
second-opinion G1, third-opinion F2, fourth-opinion G1 — the single highest-value miss, ranked HIGH and
unanimous across all three audits and both meta-analyses).

When one mixin (an action, property, or collection contributed by a `@Mixin`/`@Action`/`@Property`/`@Collection`
type) is applied to **several mixee types that declare different `@DomainObject(...DomainEvent = …)` defaults**,
Causeway 4 mutates the **shared** domain-event facet held on the mixin's faceted method. Post-processing runs once
per mixee, so the last mixee processed wins and every mixee then dispatches the *same* event type — cross-mixee
event-type pollution. This is a general **metamodel** correctness defect, not a command-log concern, which is why
the command-log-scoped first analysis never saw it.

Maintenance fixed this (CAUSEWAY-4039, commit `d5cdc5da369`) by installing a **per-mixee, object-type-specific
overlay facet** on each mixed-in member, and — for actions — a mixee-specific `ActionInvocationFacetForAction` so
that execution reads the right event holder rather than delegating to the shared mixin action.

This change forward-ports that isolation to `main`. It is re-anchored to current HEAD (`42ca10925fb`): the merge
PR #3697 (CAUSEWAY-4044, "immutable ObjectSpecification") reworked neighbouring metamodel code but left this defect
intact — `SynthesizeDomainEventsForMixinPostProcessor` still calls the shared `initWithMixee(objectSpecification)`
at lines 53, 64, 75, `createObjectTypeSpecific*` has zero hits in `core/metamodel`, and
`ObjectActionMixedIn.executeInternal` at line 179 still delegates unconditionally to the shared mixin action.

## What Changes

- Replace the shared-mutation calls in `SynthesizeDomainEventsForMixinPostProcessor` (action/property/collection)
  with per-mixee installation: when the member's domain-event facet still has a **default** event-type origin,
  create an object-type-specific overlay facet for the current mixee and add it to the mixed-in member's facet
  holder. When the event type was set explicitly on the action/property/collection or on the mixin type, leave it
  untouched.
- Add `createObjectTypeSpecificForMixin(ObjectSpecification mixee, FacetHolder mixedInMember)` factory support to
  `ActionDomainEventFacet`, `PropertyDomainEventFacet`, and `CollectionDomainEventFacet`, returning the overlay
  facet only when a mixee-specific event type genuinely applies.
- For mixed-in **actions**, install a mixee-specific `ActionInvocationFacetForAction` (via a new
  `ActionInvocationFacetForAction.createObjectTypeSpecific(...)`) bound to the overlay event facet, and route
  `ObjectActionMixedIn` execution through the local invocation facet when present, otherwise through the shared
  mixin action (preserving today's behaviour for the single-mixee case).
- Do **not** change the metamodel post-processor ordering, the mixin resolution mechanism, the
  `@DomainObject(...DomainEvent=…)` precedence rules, or any command-log code. No configuration, schema, or
  persistence change.

## Capabilities

### New Capabilities

- `mixin-domain-event-isolation`: Defines that a mixed-in member's domain-event type is resolved and stored
  **per mixee**, so a mixin shared across differently-annotated mixee types dispatches each mixee's own event
  type, and action execution consults the mixee-specific event holder.

## Impact

- Affects `core/metamodel`: `SynthesizeDomainEventsForMixinPostProcessor`, the three domain-event facets
  (`ActionDomainEventFacet`, `PropertyDomainEventFacet`, `CollectionDomainEventFacet`),
  `ActionInvocationFacetForAction`, and `ObjectActionMixedIn` execution routing.
- Behavioural surface is metamodel-only; any application using a shared mixin over multiple object types with
  differing domain-event defaults is affected today (they silently get the wrong event type).
- **Merge interaction (CAUSEWAY-4044):** per-mixee facets are installed at post-process time. This remains
  supported on HEAD (`FacetHolder.addFacet` is declared; `FacetUtil.addFacet` is used by many postprocessors),
  but the implementation must confirm the immutable-`ObjectSpecification` work does not freeze mixed-in members
  before this post-processor runs, and must use the current `ActionInvocationFacetForAction` construction shape
  (post-CAUSEWAY-4044) rather than the maintenance-branch signature.
- Requires a focused metamodel regression that constructs an annotated mixee **and** a plain mixee over the same
  faceted method and asserts the annotated mixee receives its object-level event while the plain mixee (and the
  shared method) retain the default event.
