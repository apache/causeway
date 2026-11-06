## Context

The maintenance implementation first loads TYPE_INTROSPECTED metadata, checks MixinFacet applicability, then requests FULLY_INTROSPECTED only for applicable candidates.
V4's SpecificationLoaderDefault instead introspects type hierarchies, bootstraps mixins in INTROSPECTING_MIXINS with non-mixin FULL requests downgraded, constructs MixinSpecStreamerEager, then introspects domain types after MIXINS_READY.
The eager streamer indexes facet mixeeType and selects candidates through the complete Java superclass/interface hierarchy.
MixedInMemberFactory consumes that selected sequence before streaming actions for either action or association discovery.
MixinSpecStreamerOnTheFly is deprecated and referenced only by a commented-out constructor call.

## Goals / Non-Goals

**Goals:**

- Protect exact, inherited and ordinary interface applicability and reject unrelated candidates before per-target member access.
- Protect existing contributed action/property/collection identities, element types, explicit ordering, navigation synthesis and domain-service Object-mixin exclusion.
- State precisely why no maintenance runtime patch is required.

**Non-Goals:**

- No production Java changes, on-demand/lazy mixin bootstrap, global suppression of unrelated mixin introspection, new prefilter, recursion fix or diagnostics change.
- No reactivation or redesign of the obsolete on-the-fly implementation.
- No promise about order among different hierarchy buckets beyond existing explicit member ordering.

## Decisions

### Test the active eager path, not the maintenance algorithm

Construct the actual eager streamer with controlled registry/loader/facet objects and test selection for class and interface hierarchies.
Observe member-access interactions through the real MixedInMemberFactory using empty member streams to isolate the candidate-selection boundary.
Global eager loads are expected during index construction; clear or separate bootstrap interactions before asserting absence of per-target member work.
A test demanding that unrelated mixins are never fully loaded would contradict v4's existing design and is deliberately rejected.

### Pair structural unit checks with real contributions

A real Spring bootstrap fixture supplies base/subtype/interface mixees and unrelated action/property/collection counterparts.
Assert contributed identities, element types, navigation synthesis, exclusion of unrelated contributions and opposing explicit action sequence.
Existing cyclic recording-navigation regressions continue to run as part of the full domain-model suite.
This avoids building brittle mock action/facet graphs merely to exercise contribution constructors.

### Keep the implementation tests-only

Do not port ObjectSpecificationAbstract.applicableMixinFacet or its maintenance-specific diagnostic caller filtering; neither helper exists in the v4 architecture.
No production API, state accessor, dependency or configuration is needed.
If a focused or real regression reveals an actual runtime defect, pause and revise scope with the user instead of silently expanding this verification into a fix.

## Risks / Trade-offs

- Mocked metadata can hide contribution defects → pair interaction tests with a real bootstrap fixture and full existing domain-model regressions.
- Global eager loading could be confused with avoidable per-target work → explicitly separate the stages in tests, specs and documentation.
- Interface hierarchy tests could accidentally rely on Domain.Include → use an ordinary unannotated interface.
- Fixture scanning could affect existing contexts → isolate fixtures in their own package and import their configuration explicitly.

## Migration Plan

No runtime migration or rollback is required.
Merge tests/docs after validation and archive; retain CAUSEWAY-4060 as a separate preceding PR.

## Open Questions

None; the user explicitly approved verification without a redundant runtime port.
