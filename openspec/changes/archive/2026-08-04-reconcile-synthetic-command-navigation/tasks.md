## 1. Synthetic action infrastructure

- [x] 1.1 Add a narrow Causeway 4 object-specification mutation seam that appends synthetic actions, preserves deterministic ordering, and rebuilds all action indexes and caches.
- [x] 1.2 Add dedicated marker facets and shared metadata facets for parented-collection and scalar-reference navigation actions.
- [x] 1.3 Add `SynthesizeNavigationActionsPostProcessor` at `A0_BEFORE_BUILTIN`, gated by existing recording support, eligible owner type, and `CommandRecordingSuppressed`.
- [x] 1.4 Add metamodel tests for enabled and disabled recording support, entity and view-model owners, suppression, deterministic ids, authored-action collision handling, and ordinary action lookup compatibility.

## 2. Parented-collection selector model

- [x] 2.1 Construct one safe synthetic selector action for each eligible parented collection with marker, associated-member, `Navigate To`, secondary-button, icon, and publication metadata.
- [x] 2.2 Derive optional scalar and constrained-reference filter parameters from eligible collection columns while excluding child collections, unconstrained references, large objects, and technical metadata.
- [x] 2.3 Preserve explicit collection-column order and represent generated boolean filters as optional tri-valued parameters.
- [x] 2.4 Add focused metamodel tests for eligible entity, view-model, and abstract element types, metadata, parameter inclusion and exclusion, reference choices/autocomplete variants, boolean optionality, and column ordering.

## 3. Parented-collection selector behavior

- [x] 3.1 Implement shared collection matching with string containment and exact non-string scalar/reference equality.
- [x] 3.2 Add validation requiring exactly one matching child with clear no-match and ambiguous-match reasons.
- [x] 3.3 Add empty-collection usability and invocation facets that return the single matching child and repeat cardinality checks when validation is bypassed.
- [x] 3.4 Add behavior tests for empty and non-empty collections, omitted and supplied filters, partial strings, exact scalar/reference/boolean matching, zero/one/multiple matches, and direct invocation failures.

## 4. Scalar-reference navigation behavior

- [x] 4.1 Construct one parameterless safe synthetic action for each eligible scalar domain-object reference with deterministic marker and layout metadata.
- [x] 4.2 Add null-reference usability and invocation facets that return the referenced object or fail clearly when usability checking is bypassed.
- [x] 4.3 Add metamodel and invocation tests for entity and view-model owners, value-property exclusion, null and non-null references, suppression, metadata, and direct invocation failure.

## 5. Command lifecycle compatibility

- [x] 5.1 Verify synthetic safe actions use the single C2 publishing path, honor C1 target and owner suppression, and do not duplicate command publication.
- [x] 5.2 Adapt command DTO execution argument reconstruction only where required for generated selector parameters, without introducing replay mapping or advisor policy.
- [x] 5.3 Add runtime-service coverage proving selector/reference results use normal C4a result capture and existing D1 command DTO/result metadata.
- [x] 5.4 Update recording-support documentation to describe synthetic navigation opt-in, default compatibility, suppression, stable ids, and matching semantics.

## 6. Verification

- [x] 6.1 Run focused metamodel and runtime-service tests for synthetic navigation with JDK 21.
- [x] 6.2 Run the affected aggregate Maven build with JDK 21 and confirm C1, C2, C4a, and D1 regression tests remain green.
- [x] 6.3 Run IDE compilation and inspections plus strict OpenSpec and repository checks, confirming no D2/M1-M3, P1/P2, R1/R2, E1/W1, or B1/B2 behavior is introduced.
