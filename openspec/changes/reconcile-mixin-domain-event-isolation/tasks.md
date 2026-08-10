## 1. Confirm the metamodel entry points on current HEAD

- [ ] 1.1 Verify that a mixed-in member's facet holder still accepts a facet at post-process time on HEAD
      (`FacetHolder.addFacet` / `FacetUtil.addFacet`), and that the CAUSEWAY-4044 immutable-`ObjectSpecification`
      work does not freeze mixed-in members before `SynthesizeDomainEventsForMixinPostProcessor` runs.
- [ ] 1.2 Confirm the current `ActionInvocationFacetForAction` construction shape (method(s), declaring type,
      return type accessors) so the mixee-specific invocation facet is built from HEAD APIs, not the
      maintenance-branch signature.

## 2. Per-mixee overlay facets

- [ ] 2.1 Add `createObjectTypeSpecificForMixin(ObjectSpecification mixee, FacetHolder mixedInMember)` to
      `ActionDomainEventFacet`, `PropertyDomainEventFacet`, and `CollectionDomainEventFacet`, returning an
      optional overlay facet carrying the mixee-resolved event type; return empty when no mixee-specific type
      applies.
- [ ] 2.2 In `SynthesizeDomainEventsForMixinPostProcessor`, replace the shared `initWithMixee(objectSpecification)`
      calls for action, property, and collection with: skip when `getEventTypeOrigin()` is not default; otherwise
      create the object-type-specific overlay and add it to the mixed-in member's facet holder.

## 3. Mixee-specific action invocation

- [ ] 3.1 Add `ActionInvocationFacetForAction.createObjectTypeSpecific(...)` bound to the overlay action
      domain-event facet, constructed from the current HEAD accessors.
- [ ] 3.2 Install the mixee-specific invocation facet on the mixed-in action's facet holder when an action overlay
      was created.
- [ ] 3.3 Route `ObjectActionMixedIn` execution through the local invocation facet when present, otherwise
      through the shared `mixinAction` (unchanged single-mixee path).

## 4. Preserve precedence and single-mixee behaviour

- [ ] 4.1 Ensure an explicit `@Action(domainEvent=…)` / member-level / mixin-type event type (non-default origin)
      is left untouched for every mixee.
- [ ] 4.2 Ensure a mixin contributed to a single mixee behaves exactly as before (no observable change).

## 5. Tests

- [ ] 5.1 Add a focused two-mixee isolation test: annotated mixee + plain mixee over the same faceted method;
      assert per-mixee event types for action, property, and collection, order-independently, with no cross-mixee
      or shared-method leakage.
- [ ] 5.2 Add an action-execution assertion that the mixee-specific event is dispatched for the annotated mixee.
- [ ] 5.3 Confirm the existing `ActionAnnotationFacetFactoryTest_domainEvent` single-mixee cases still pass.

## 6. Verification

- [ ] 6.1 Run the focused metamodel domain-event and mixin tests plus the affected `core/metamodel` / `core/mmtest`
      reactor under JDK 21, and strict OpenSpec validation.
- [ ] 6.2 Confirm no configuration, schema, persistence, post-processor-ordering, or command-log change was
      introduced.
