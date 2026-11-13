## 1. Capture the executable operation-shape baseline

- [x] 1.1 Record targeted introspection for the Reference Application parameterless `demo.ActionChoicesMenu.choices` service action, including its nested invocation and result shape.
- [x] 1.2 Record targeted introspection for representative safe, idempotent, and mutating object actions with and without parameters.
- [x] 1.3 Record the generated top-level mutation fields, declared target arguments, parameter arguments, and direct or enveloped result types for those actions.
- [x] 1.4 Add a focused foundation fixture for the direct versionless mutation result shape that previously produced `Action invocation failed`.
- [x] 1.5 Add focused failing fixtures proving that an advertised flat mutation must be preferred over a legacy nested `invokeNonIdempotent` field.
- [x] 1.6 Publish the reviewed object and service operation-shape matrix as reproducible change evidence.

## 2. Implement shared introspection-driven dispatch planning

- [x] 2.1 Add an immutable shared invocation-plan model for target kind, operation placement, mutability, selected field, arguments, result projection, extraction, and outcome type.
- [x] 2.2 Select nested `invoke` or `invokeIdempotent` only for advertised safe or idempotent query execution.
- [x] 2.3 Inspect the mutation root independently and prefer an advertised generated top-level mutation for mutating actions.
- [x] 2.4 Retain nested `invokeNonIdempotent` only as an explicit compatibility fallback when no corresponding mutation field exists.
- [x] 2.5 Map pending values only to arguments declared by the selected invocation field.
- [x] 2.6 Resolve an object mutation's target argument from its advertised generated input type or target name and reject an unsatisfied required target before execution.
- [x] 2.7 Ensure service mutations never receive a manufactured object target.
- [x] 2.8 Return a bounded unsupported plan without constructing an operation when no executable placement exists.

## 3. Derive safe result projection and extraction

- [x] 3.1 Detect and select an advertised `results` envelope without assuming that every nested invocation uses one.
- [x] 3.2 Support direct scalar, enum, object, collection, and void result fields without requesting child fields for scalar or void values.
- [x] 3.3 Select only fields advertised by a described direct object or established result envelope and never synthesize an absent `target` field.
- [x] 3.4 Preserve valid `__typename`-only bounded output when general polymorphic metadata projection remains unavailable.
- [x] 3.5 Normalize direct and enveloped values through the existing object, collection, scalar, and void semantic result kinds.
- [x] 3.6 Reject an unsafe or undescribed required result projection with a bounded planning error before execution.

## 4. Integrate object and service action contexts

- [x] 4.1 Route object action capability discovery and invocation through the shared dispatch planner.
- [x] 4.2 Route service action capability discovery and invocation through the same planner.
- [x] 4.3 Extend the GraphQL interaction operation builder only as needed for direct nested results and plan-defined extraction paths.
- [x] 4.4 Preserve current public context methods, GraphQL operation names, Causeway semantic events, and interaction result shapes.
- [x] 4.5 Preserve per-object and per-service mutation serialization and trigger authoritative refresh or change notification only after successful mutation.
- [x] 4.6 Preserve cancellation, disconnect, route replacement, and stale-generation checks so obsolete invocation results cannot update current state.
- [x] 4.7 Map expected capability, planning, GraphQL, invocation, and transport failures to bounded action-scoped interaction results before the controller's generic exception fallback.
- [x] 4.8 Verify protected action values remain absent from errors, diagnostics, semantic events, operation summaries, and rendered markup.

## 5. Close the automated operation-shape matrix

- [x] 5.1 Add foundation tests for parameterless and parameterized nested safe and idempotent object actions.
- [x] 5.2 Add foundation tests for parameterless and parameterized nested safe and idempotent service actions.
- [x] 5.3 Add foundation tests for object and service top-level mutations, object target mapping, and mutation preference over a legacy nested field.
- [x] 5.4 Add foundation tests for the legacy nested mutating fallback and the no-executable-capability unsupported result.
- [x] 5.5 Add foundation tests for direct and enveloped scalar, object, collection, and void outcome normalization.
- [x] 5.6 Add negative tests for undeclared arguments, missing required target mapping, absent result fields, bounded errors, cancellation, stale completion, and protected-value redaction.
- [x] 5.7 Keep existing Petclinic object and service action interaction tests passing without changing their public selectors or event assertions.

## 6. Qualify against GraphQL and the pinned Reference Application

- [x] 6.1 Add GraphQL integration assertions that the recorded nested safe and flat mutation operations execute successfully against authoritative fixtures.
- [x] 6.2 Replace the Reference Application `demo.ActionChoicesMenu.choices` visible-failure assertion with a successful parameterless service-action outcome and host navigation or presentation assertion.
- [x] 6.3 Add a successful parameterized object-action journey whose identity metadata is already complete, while retaining versionless-action failures for the next focused change.
- [x] 6.4 Cover valid, invalid, cancelled, stale, scalar, object, collection, and void action outcomes through representative Reference Application targets.
- [x] 6.5 Update the capability inventory and stable target catalogue only where focused executable evidence changes the reviewed classification.
- [x] 6.6 Prove clean and incremental inventory generation remain byte-identical after classification updates.

## 7. Document compatibility and production isolation

- [x] 7.1 Document nested safe-query, top-level mutation, and legacy nested-mutation fallback behavior for semantic action contexts.
- [x] 7.2 Publish reproduction steps, operation-shape evidence, before-and-after results, and any intentionally retained identity or union gaps.
- [x] 7.3 Verify no Maven or npm dependency, public GraphQL field or operation name, Causeway element, semantic event name, canonical route, or browser asset URL changes.
- [x] 7.4 Verify Vaadin bundle inputs, CSP hashes, route-lazy loading, native fallback, and default-selection policy remain unchanged.

## 8. Run final qualification gates

- [x] 8.1 Run the complete foundation Node suite and web-component foundation and HTMX Maven tests.
- [x] 8.2 Run GraphQL model and relevant integration tests for nested query and flat mutation action execution.
- [x] 8.3 Run the full Petclinic integration and Playwright suites.
- [x] 8.4 Run the Reference Application clean package, integration, capability inventory, and Playwright suites.
- [x] 8.5 Run strict CSP, accessibility, keyboard, light, dark, narrow, reduced-motion, forced-colors, external-request, console-error, page-error, and overflow gates.
- [x] 8.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
