## Context

Command-log recording support adds safe synthetic navigation actions during metamodel post-processing.
Those actions let a recorded command stream navigate from an object to one element of a parented collection or to a referenced object held by a scalar reference property.
The synthesis is currently initiated by `SynthesizeNavigationActionsPostProcessor` and implemented by the navigation helpers on `ObjectSpecificationAbstract`.

The screenshot points at a different but adjacent post-processing problem.
It shows a facet holder for an unrelated generic mixed-in action, `Object_recentChanges#act()`, whose `ActionDomainEventFacet` has been updated to `AgreementRole$ActionRestrictedEvent`.
The translation context belongs to the shared mixin method, while the event type belongs to one concrete mixee.
That combination is a strong signal that a facet attached to the shared mixin `FacetedMethod` has been mutated while processing a particular mixee.

`ObjectActionMixedIn` deliberately uses a layered facet holder.
The local layer is specific to the mixee member identifier, while the shared layer is the mixin method's `FacetedMethod` and is reused for every type that receives that mixin.
`SynthesizeDomainEventsForMixinPostProcessor` currently looks up the `ActionDomainEventFacet` on the mixed-in action and calls `initWithMixee(...)` on the facet returned by that lookup.
When the lookup resolves to the shared facet, `initWithMixee(...)` mutates shared state and the effective event type can bleed into other mixees.
Recording-support navigation synthesis makes this visible because it forces additional mixed-in members and association metadata to be materialized during the post-processing sweep.

## Goals / Non-Goals

**Goals:**

- Keep the fix in the metamodel post-processing layer.
- Prevent mixee-specific domain-event defaults from mutating shared mixed-in action facets.
- Keep generated recording-support navigation actions domain-event neutral.
- Prevent navigation synthesis from contaminating unrelated pre-existing mixed-in actions such as recent-changes actions.
- Preserve command publishing metadata on generated safe navigation actions when recording support is enabled.
- Cover both generated parented collection navigation actions and generated scalar reference navigation actions.
- Add regression tests that reproduce the recording-support-enabled post-processing path and the shared-mixin contamination pattern.

**Non-Goals:**

- Do not redesign layered facet holders or the general facet ranking model.
- Do not change command DTO structure, command-log persistence, replay, export, or result mapping.
- Do not change synthetic navigation action ids, names, layout association metadata, styling, validation, or invocation semantics.
- Do not remove existing application-authored explicit domain-event behavior.
- Do not annotate or special-case Estatio mixins such as `Object_recentChanges`; the framework should prevent contamination.
- Do not introduce new dependencies.

## Decisions

### Isolate mixee-specific domain-event state in post-processing

The implementation should change the mixed-in domain-event post-processing path so a mixee-specific event type is represented by a facet on the mixed-in member's local layer.
The shared facet attached to the mixin method should remain the mixin method default and must not be mutated with a concrete mixee's type-level domain-event default.

A small helper or subtype may be added to the existing action domain-event facet package to create an object-type-specific facet for the local layer.
That helper should be called only by the postprocessor when a mixed-in member's default event type must be resolved against the current mixee.

Alternative considered: update `DomainEventFacetAbstract.initWithMixee(...)` globally so all callers clone instead of mutate.
That is broader than necessary and risks changing non-mixin facet behavior.

Alternative considered: mark every `ActionDomainEventFacet` as object-type-specific.
That would alter normal facet installation and could change precedence behavior outside mixed-in members.

### Keep generated navigation actions domain-event neutral

Generated navigation actions are command-recording infrastructure, not application domain behavior.
They should not inherit an owner type's action domain-event default and should not fire or expose application action domain events.
The navigation action helpers should continue to install only the facets needed for naming, layout association, safe semantics, command publishing, usability, validation, and invocation.

If a later postprocessor would otherwise add a domain-event facet to generated navigation actions, the navigation path should mark or overlay those actions so that action domain-event processing ignores them.
The preferred implementation is to keep them as non-mixed-in synthetic `ObjectActionDefault` instances without `ActionDomainEventFacet` installation.

Alternative considered: install `ActionDomainEvent.Noop` explicitly on generated navigation actions.
That is acceptable only if needed to block later processing, but the smaller change is to avoid installing an event facet at all.

### Do not let navigation synthesis materialization leak into unrelated mixins

Navigation synthesis may read mixed-in association metadata to identify eligible collections and reference properties.
That traversal must not cause post-processing for existing application mixed-in actions to mutate shared facets.
The isolation fix should therefore be verified using a recording-support-enabled setup where navigation actions are synthesized and unrelated generic actions are also present.

Alternative considered: avoid all mixed-in member materialization during navigation synthesis.
That could reduce exposure but risks breaking existing support for mixed-in parented collections and is a larger behavioral change.

### Preserve safe command publishing behavior

Generated navigation actions must remain safe and command-published when recording support is enabled.
The fix must not remove `CommandPublishingFacet` installation from generated navigation actions and must not change generated action ids.

## Risks / Trade-offs

- A local overlay facet could interact with facet precedence on layered holders → Use the same facet type and event semantics as the existing facet, and make only the mixee-specific overlay object-type-specific.
- Regression tests could accidentally assert implementation details instead of behavior → Assert observable event-type isolation, absence of domain-event facets on generated navigation actions, and unchanged command publishing behavior.
- Narrow postprocessor changes could miss property or collection mixed-in members with the same sharing pattern → Review the property and collection branches of the same postprocessor and either apply the same local-overlay approach or document why actions are the only affected branch.
- Avoiding broad traversal changes means navigation synthesis still materializes metadata → This is acceptable if shared facet mutation is eliminated and existing mixed-in collection behavior is preserved.

## Migration Plan

No user migration is expected.
Applications should not need to annotate unrelated generic mixins to avoid stale domain-event facets caused by recording-support navigation synthesis.
Rollback is a code-only revert because no persisted data or public configuration changes are introduced.

## Open Questions

- Should the local overlay be implemented as a dedicated object-type-specific `ActionDomainEventFacet` subtype, or as a package-level factory method that creates the existing facet with an object-type-specific flag?
- Should the same local-overlay pattern be applied immediately to `PropertyDomainEventFacet` and `CollectionDomainEventFacet` in the same postprocessor, or should this change stay action-only until there is evidence of property and collection contamination?
