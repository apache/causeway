## Context

The diagnostics introduced by `diagnose-metamodel-introspection-recursion` captured the complete chain from a production failure.
The chain had 395 entries and 395 distinct Java types, proving that the failure was not a cycle but an excessively deep depth-first traversal.
Of those entries, 201 were loaded by `createMixedInAssociation`, 55 by `createMixedInAction`, and 138 by action element-type resolution.

Almost every candidate was already `TYPE_INTROSPECTED` and was being advanced to `FULLY_INTROSPECTED`.
Both mixed-in discovery paths currently request full introspection before reading `MixinFacet` and calling `isMixinFor`, even though mixin identity, target type, and main method name are type-level metadata established by `introspectTypeHierarchy`.
Consequently, an inapplicable candidate can run member postprocessors, resolve action element types, synthesize navigation actions, and recursively scan further mixins before the original caller discards it.

This branch is being decommissioned, so the correction should be narrowly scoped and retain existing member behavior.

## Goals / Non-Goals

**Goals:**

- Decide mixin applicability before requesting full member introspection.
- Avoid full introspection of candidates that do not apply to the current target.
- Apply identical prefiltering semantics to mixed-in actions and mixed-in associations.
- Preserve contribution behavior, ordering, validation, and deterministic traversal for applicable mixins.
- Demonstrate a substantial reduction in nested full-introspection requests on a production-shaped fixture.

**Non-Goals:**

- Replace recursive metamodel construction with a general iterative work queue.
- Guarantee a fixed maximum depth when an application genuinely has a long chain of applicable mixins and element types.
- Change how `MixinFacet` determines applicability.
- Change navigation-action synthesis, action element-type derivation, or the public programming model.
- Remove the diagnostics added by the preceding change.

## Decisions

### Use a two-stage load for every candidate mixin

Each candidate will first be loaded with `IntrospectionState.TYPE_INTROSPECTED`.
The discovery path will read `MixinFacet`, reject missing or self specifications, and evaluate `isMixinFor` against the target specification's corresponding class.
Only an applicable candidate will then be loaded or advanced to `IntrospectionState.FULLY_INTROSPECTED` before its declared actions are streamed and converted into mixed-in members.

This directly removes the unnecessary transition identified by the production chain while using the existing specification cache and state machine.
Checking applicability from registry metadata alone was rejected because `MixinFacet` remains the authoritative implementation of mixin semantics.
Leaving one discovery path unchanged was rejected because both actions and associations appeared prominently in the captured chain.

### Share the applicability prefilter between action and association discovery

A small internal helper in `ObjectSpecificationAbstract` will perform the common type-stage loading and applicability checks and return the accepted mixin specification together with the `MixinFacet`, or an empty result.
Action-specific rules, including suppression of `Object_` mixins on contributing domain services, will remain in the action path.
Member-specific filtering and `_MixedInMemberFactory` construction will remain unchanged after full introspection.

Duplicating the two-stage logic in both paths was rejected because subtle differences could reintroduce eager loading later.
Moving the behavior into the public `SpecificationLoader` API was rejected because applicability is contextual to the target specification and no public API change is needed.

### Preserve full introspection before member streaming

After a candidate passes the type-level checks, the framework will explicitly advance that same cached specification to `FULLY_INTROSPECTED` before calling `streamActions`.
This preserves the existing assumption that declared action metadata and postprocessing are complete before a mixed-in action, property, or collection is constructed.

Streaming members from a merely type-introspected specification was rejected because it would alter initialization semantics and risk incomplete facets.

### Verify behavior and depth independently

Functional tests will verify applicable and inapplicable action and association mixins, catch-all mixins, explicit member ordering, and navigation-action synthesis.
Diagnostic-chain tests will register many inapplicable mixins and assert that evaluating them does not request `FULLY_INTROSPECTED`, while applicable candidates still do.
The production-shaped full-boot fixture will run with command recording enabled and a constrained JVM stack.

The tests will assert the absence of avoidable full-introspection transitions rather than a brittle exact total depth, because unrelated framework evolution can add legitimate specification loads.

## Risks / Trade-offs

- [Risk] Some `MixinFacet` information might currently be finalized during member introspection. → Add a characterization test proving applicability and main-method metadata are available at `TYPE_INTROSPECTED` before changing discovery.
- [Risk] Advancing an accepted specification in a second loader call could alter re-entrant state handling. → Reuse the cached specification, retain existing introspection guards, and cover cyclic and cross-type fixtures.
- [Risk] Invalid mixin declarations may now be fully validated later than before if they never apply to the current target. → Preserve eager metamodel traversal elsewhere and add validation coverage for malformed registered mixins.
- [Risk] The change reduces but cannot eliminate recursion when many candidates genuinely apply. → Retain diagnostics and treat an iterative metamodel work queue as a separate follow-up if production depth remains excessive.
- [Risk] A shared helper could accidentally apply action-only exclusions to associations. → Keep only common facet and target checks in the helper and test each member kind separately.

## Migration Plan

No data, configuration, or application-code migration is required.
Deploy with the existing deterministic mixin ordering and introspection diagnostics enabled by default.
Re-run the production application using the same command-recording configuration and compare the diagnostic depth if any failure remains.
Rollback consists of reverting this isolated discovery change; no persisted state is modified.

## Open Questions

- Does every supported mixin declaration style expose a complete `MixinFacet`, including target type and main method name, at `TYPE_INTROSPECTED`?
- Are malformed but globally registered mixins guaranteed to receive full validation independently of being applicable to a particular target?
- After prefiltering, what is the maximum observed production chain depth and which remaining transitions dominate it?
