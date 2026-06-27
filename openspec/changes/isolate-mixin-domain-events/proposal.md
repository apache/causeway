## Why

When command-log recording support is enabled, the metamodel post-processing path synthesizes navigation actions for one-to-many collections and one-to-one reference properties.
The observed debugger state shows an `ActionDomainEventFacet` whose translation context belongs to an unrelated generic mixin action such as `Object_recentChanges#act()`, while its event type has been updated to an application-specific type such as `AgreementRole$ActionRestrictedEvent`.
That mismatch indicates a mutable facet from a shared mixed-in action holder is being reused across mixees, so the fix must isolate the post-processed facet state without redesigning command recording or ordinary member semantics.

## What Changes

- Constrain the production fix to metamodel post-processing for mixed-in domain-event synthesis and recording-support navigation synthesis.
- Stop post-processing from mutating the shared `ActionDomainEventFacet` that belongs to a mixin method when the effective event type depends on the current mixee type.
- Store any mixee-specific effective domain-event facet on the mixed-in member's object-type-specific layer, or use an equivalent local overlay, so one mixee cannot contaminate another.
- Ensure generated recording-support navigation actions for one-to-many collections and one-to-one references remain domain-event neutral.
- Ensure navigation synthesis does not install, update, or leak action domain-event facets onto unrelated pre-existing mixed-in actions.
- Add regression coverage for a generic mixed-in action contributed to multiple domain types where one type has an action domain-event default and another does not.
- Preserve existing synthetic navigate-to action identifiers, safe semantics, command publishing metadata, validation, invocation, and layout association behavior.
- Do not change command DTO generation, command logging, replay, export, persistence, or application-authored explicit domain-event annotations.

## Capabilities

### New Capabilities

### Modified Capabilities

- `parented-collection-selector-actions`: Recording-support navigation action synthesis and adjacent mixed-in domain-event post-processing must isolate mixee-specific event facets and remain domain-event neutral for generated navigation actions.

## Impact

- Affects `core/metamodel` post-processing, especially `SynthesizeDomainEventsForMixinPostProcessor`, `SynthesizeNavigationActionsPostProcessor`, and the synthetic navigation action helper tests.
- May require a narrow helper or facet variant under the existing action domain-event facet package to create local object-type-specific overlays for mixed-in members.
- Affects tests around generated navigation actions, mixed-in action domain-event defaults, safe command publishing metadata, and side effects on unrelated mixins.
- Applies equally when applications use renamed Causeway or legacy Isis configuration properties that enable the same command-log recording support path.
- No new dependencies are expected.
