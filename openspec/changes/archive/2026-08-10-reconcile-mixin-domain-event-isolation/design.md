## Context

Mixed-in members are layered over the mixin's own `FacetedMethod`. `SynthesizeDomainEventsForMixinPostProcessor`
runs per member per mixee and, on `main`, calls `facet.initWithMixee(objectSpecification)` on the domain-event
facet obtained from that shared faceted method:

```
// core/metamodel/.../postprocessors/members/SynthesizeDomainEventsForMixinPostProcessor.java (HEAD 42ca10925fb)
:53  facet->facet.initWithMixee(objectSpecification),   // action
:64  facet->facet.initWithMixee(objectSpecification),   // property
:75  facet->facet.initWithMixee(objectSpecification),   // collection
```

Because the facet instance is shared across every mixee that the mixin is contributed to, `initWithMixee` mutates
shared state. `ActionDomainEventFacet.updateEventType(...)` is guarded by `if (!getEventTypeOrigin().isDefault())
return;`, but that guard only protects an explicit annotation — it does not prevent one mixee's *default* event
type from overwriting another's on the shared facet. Whichever mixee is post-processed last wins for all of them.
Execution then compounds this: `ObjectActionMixedIn.executeInternal` (`:179`) delegates unconditionally to the
shared `mixinAction`, so even a correctly-computed per-mixee event holder would not be consulted for actions.

Maintenance (`ecp` @ `1683383878939`, CAUSEWAY-4039 commit `d5cdc5da369`) replaced the shared mutation with
per-mixee installation. Its `SynthesizeDomainEventsForMixinPostProcessor`:

- guards on `!facet.getEventTypeOrigin().isDefault()` (skip if set explicitly on the member or mixin type);
- otherwise calls `<Facet>.createObjectTypeSpecificForMixin(objectSpecification, member.getFacetHolder())` and
  `member.addFacet(overlay)`;
- for actions additionally installs `ActionInvocationFacetForAction.createObjectTypeSpecific(overlayEventFacet,
  method, declaringType, returnType, member.getFacetHolder())`;
- and `ObjectActionMixedIn` routes via `hasLocalActionInvocationFacet() ? this.executeInternal(...) :
  mixinAction.executeInternal(...)` (per meta-analysis-1/ledger.md §MA-1, `ecp specimpl/ObjectActionMixedIn.java:197-207`).

`main` has none of these symbols today (`createObjectTypeSpecific*` = 0 hits in `core/metamodel`).

## Goals / Non-Goals

**Goals:**

- A mixed-in member resolves and stores its domain-event type **per mixee**, so a shared mixin over
  differently-annotated mixee types dispatches each mixee's own event type.
- For mixed-in actions, execution consults the mixee-specific event holder.
- Preserve today's behaviour for the single-mixee case and for explicitly-annotated event types.

**Non-Goals:**

- No change to post-processor ordering, mixin discovery/resolution, or `@DomainObject`/member event-type
  precedence rules.
- No change to any command-log, viewer, or persistence code.
- Not porting maintenance-branch source *shape* verbatim (Jakarta vs `javax`, lombok `val`, and — importantly —
  the pre-CAUSEWAY-4044 `ActionInvocationFacetForAction` construction signature). Behaviour is authoritative;
  the v4 implementation uses current HEAD APIs.

## Decisions

### Install a per-mixee overlay facet rather than mutating the shared facet

Adopt maintenance's approach: when the member's domain-event facet still has a **default** event-type origin,
build an object-type-specific overlay facet for the current mixee and add it to the *mixed-in member's* facet
holder (not the shared mixin faceted method). Facet lookup returns the most-specific facet, so the overlay wins
for that mixee while other mixees are unaffected.

Rejected — keep `initWithMixee` but clone the shared facet lazily: this still leaves the mutation entry point in
place and risks other post-processors observing a half-initialised shared facet. The per-mixee overlay is the
behaviour all three audits verified against maintenance.

### Guard on `getEventTypeOrigin().isDefault()`

Only install the overlay when the event type is still the framework default. If the action/property/collection or
the mixin type set the event type explicitly, that origin is not default and must be preserved unchanged — this
keeps the existing `@Action(domainEvent=…)` / `@DomainObject` precedence intact.

### Route mixed-in action execution through a local invocation facet

For actions only, install a mixee-specific `ActionInvocationFacetForAction` bound to the overlay event facet and
make `ObjectActionMixedIn` prefer it when present. Properties and collections need only the overlay event facet
(their modify/edit facets already read the event facet via lookup), so no analogous invocation-facet change is
required for them.

### Re-verify the CAUSEWAY-4044 immutability interaction before implementing

PR #3697 ("Work towards immutable ObjectSpecification — part 1") reworked member cataloguing and facet processing.
Two entry points this change depends on must be confirmed on HEAD before coding:

1. **Post-process-time facet installation is still permitted** on a mixed-in member's facet holder. Evidence it
   is: `FacetHolder.addFacet(...)` is declared (`facetapi/FacetHolder.java:146`) and `FacetUtil.addFacet(...)` is
   used by numerous postprocessors on HEAD (`ChoicesAndDefaultsPostProcessor`, `AuthorizationPostProcessor`,
   `ProjectionFacetsPostProcessor`, …). Use `FacetUtil.addFacet(...)` for consistency with the current codebase.
2. **The current `ActionInvocationFacetForAction` construction shape** (methods/declaring-type/return-type
   accessors and any immutability constraints introduced by CAUSEWAY-4044) — build the mixee-specific invocation
   facet from the current API, not the maintenance-branch `getMethods().getFirstElseFail()` signature if that has
   changed.

If part 2 of the immutability work has since frozen mixed-in members earlier than this post-processor, escalate:
the fix may need to compute the per-mixee event type during member synthesis rather than as a post-process
mutation. This is called out as the primary implementation risk.

### Naming

New capability `mixin-domain-event-isolation` (general metamodel), distinct from the command-log capabilities. It
has no dependency on any command-log spec.

## Acceptance evidence

- A focused `core/metamodel` (or `core/mmtest`) test that builds a mixin contributed to **two** mixee types — one
  annotated with an object-level domain-event default, one plain — over the same faceted method, and asserts:
  the annotated mixee's mixed-in member reports the object-level event type; the plain mixee's member and the
  shared faceted method report the framework default; neither leaks into the other regardless of processing order.
- An action-execution assertion that the mixee-specific event is the one dispatched for the annotated mixee.
- Existing `ActionAnnotationFacetFactoryTest_domainEvent` (nine single-mixee cases) continues to pass unchanged.
