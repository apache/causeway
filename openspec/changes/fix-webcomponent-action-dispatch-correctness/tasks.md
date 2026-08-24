## 1. Capture the executable operation-shape baseline

- [ ] 1.1 Record targeted introspection for the Reference Application parameterless `demo.ActionChoicesMenu.choices` service action, including its nested invocation and result shape.
- [ ] 1.2 Record targeted introspection for representative safe, idempotent, and mutating object actions with and without parameters.
- [ ] 1.3 Record the generated top-level mutation fields, declared target arguments, parameter arguments, and direct or enveloped result types for those actions.
- [ ] 1.4 Add a focused failing foundation fixture for the nested executable shape that currently produces `Action invocation failed`.
- [ ] 1.5 Add focused failing fixtures proving that an advertised flat mutation must be preferred over a legacy nested `invokeNonIdempotent` field.
- [ ] 1.6 Publish the reviewed object and service operation-shape matrix as reproducible change evidence.

## 2. Implement shared introspection-driven dispatch planning

- [ ] 2.1 Add an immutable shared invocation-plan model for target kind, operation placement, mutability, selected field, arguments, result projection, extraction, and outcome type.
- [ ] 2.2 Select nested `invoke` or `invokeIdempotent` only for advertised safe or idempotent query execution.
- [ ] 2.3 Inspect the mutation root independently and prefer an advertised generated top-level mutation for mutating actions.
- [ ] 2.4 Retain nested `invokeNonIdempotent` only as an explicit compatibility fallback when no corresponding mutation field exists.
- [ ] 2.5 Map pending values only to arguments declared by the selected invocation field.
- [ ] 2.6 Resolve an object mutation's target argument from its advertised generated input type or target name and reject an unsatisfied required target before execution.
- [ ] 2.7 Ensure service mutations never receive a manufactured object target.
- [ ] 2.8 Return a bounded unsupported plan without constructing an operation when no executable placement exists.

## 3. Derive safe result projection and extraction

- [ ] 3.1 Detect and select an advertised `results` envelope without assuming that every nested invocation uses one.
- [ ] 3.2 Support direct scalar, enum, object, collection, and void result fields without requesting child fields for scalar or void values.
- [ ] 3.3 Select only fields advertised by a described direct object or established result envelope and never synthesize an absent `target` field.
- [ ] 3.4 Preserve valid `__typename`-only bounded output when general polymorphic metadata projection remains unavailable.
- [ ] 3.5 Normalize direct and enveloped values through the existing object, collection, scalar, and void semantic result kinds.
- [ ] 3.6 Reject an unsafe or undescribed required result projection with a bounded planning error before execution.

## 4. Integrate object and service action contexts

- [ ] 4.1 Route object action capability discovery and invocation through the shared dispatch planner.
- [ ] 4.2 Route service action capability discovery and invocation through the same planner.
- [ ] 4.3 Extend the GraphQL interaction operation builder only as needed for direct nested results and plan-defined extraction paths.
- [ ] 4.4 Preserve current public context methods, GraphQL operation names, Causeway semantic events, and interaction result shapes.
- [ ] 4.5 Preserve per-object and per-service mutation serialization and trigger authoritative refresh or change notification only after successful mutation.
- [ ] 4.6 Preserve cancellation, disconnect, route replacement, and stale-generation checks so obsolete invocation results cannot update current state.
- [ ] 4.7 Map expected capability, planning, GraphQL, invocation, and transport failures to bounded action-scoped interaction results before the controller's generic exception fallback.
- [ ] 4.8 Verify protected action values remain absent from errors, diagnostics, semantic events, operation summaries, and rendered markup.

## 5. Close the automated operation-shape matrix

- [ ] 5.1 Add foundation tests for parameterless and parameterized nested safe and idempotent object actions.
- [ ] 5.2 Add foundation tests for parameterless and parameterized nested safe and idempotent service actions.
- [ ] 5.3 Add foundation tests for object and service top-level mutations, object target mapping, and mutation preference over a legacy nested field.
- [ ] 5.4 Add foundation tests for the legacy nested mutating fallback and the no-executable-capability unsupported result.
- [ ] 5.5 Add foundation tests for direct and enveloped scalar, object, collection, and void outcome normalization.
- [ ] 5.6 Add negative tests for undeclared arguments, missing required target mapping, absent result fields, bounded errors, cancellation, stale completion, and protected-value redaction.
- [ ] 5.7 Keep existing Petclinic object and service action interaction tests passing without changing their public selectors or event assertions.

## 6. Qualify against GraphQL and the pinned Reference Application

- [ ] 6.1 Add GraphQL integration assertions that the recorded nested safe and flat mutation operations execute successfully against authoritative fixtures.
- [ ] 6.2 Replace the Reference Application `demo.ActionChoicesMenu.choices` visible-failure assertion with a successful parameterless service-action outcome and host navigation or presentation assertion.
- [ ] 6.3 Add a successful parameterized object-action journey whose identity metadata is already complete, while retaining versionless-action failures for the next focused change.
- [ ] 6.4 Cover valid, invalid, cancelled, stale, scalar, object, collection, and void action outcomes through representative Reference Application targets.
- [ ] 6.5 Update the capability inventory and stable target catalogue only where focused executable evidence changes the reviewed classification.
- [ ] 6.6 Prove clean and incremental inventory generation remain byte-identical after classification updates.

## 7. Document compatibility and production isolation

- [ ] 7.1 Document nested safe-query, top-level mutation, and legacy nested-mutation fallback behavior for semantic action contexts.
- [ ] 7.2 Publish reproduction steps, operation-shape evidence, before-and-after results, and any intentionally retained identity or union gaps.
- [ ] 7.3 Verify no Maven or npm dependency, public GraphQL field or operation name, Causeway element, semantic event name, canonical route, or browser asset URL changes.
- [ ] 7.4 Verify Vaadin bundle inputs, CSP hashes, route-lazy loading, native fallback, and default-selection policy remain unchanged.

## 8. Run final qualification gates

- [ ] 8.1 Run the complete foundation Node suite and web-component foundation and HTMX Maven tests.
- [ ] 8.2 Run GraphQL model and relevant integration tests for nested query and flat mutation action execution.
- [ ] 8.3 Run the full Petclinic integration and Playwright suites.
- [ ] 8.4 Run the Reference Application clean package, integration, capability inventory, and Playwright suites.
- [ ] 8.5 Run strict CSP, accessibility, keyboard, light, dark, narrow, reduced-motion, forced-colors, external-request, console-error, page-error, and overflow gates.
- [ ] 8.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
