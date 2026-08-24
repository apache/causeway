## 1. Capture executable union evidence

- [x] 1.1 Capture targeted introspection for `rich__demo_ValueHolder__gqlv_union`, its advertised possible-type count, and representative concrete metadata.
- [x] 1.2 Execute typename-only and concrete-fragment operations for both `demo.CollectionTypeOfPage` collections.
- [x] 1.3 Record invalid direct union metadata selection and the retained opaque-route gap.
- [x] 1.4 Confirm repeated generated union registration currently retains incomplete first-registration membership.

## 2. Extend the internal selection model

- [x] 2.1 Define a reserved deterministic inline-fragment selection node.
- [x] 2.2 Render `__typename` and sorted `... on Type` fragments with GraphQL-name validation.
- [x] 2.3 Reject fragment names not advertised by a union or interface.
- [x] 2.4 Validate each fragment against its described concrete type and nested fields.
- [x] 2.5 Extend selection merge, difference, emptiness, and coverage helpers for fragments.
- [x] 2.6 Preserve scalar, enum, concrete object, resource, and argument rendering behavior.

## 3. Describe bounded concrete type closures

- [x] 3.1 Add helpers that expose advertised abstract possible-type names deterministically.
- [x] 3.2 Describe observed concrete object types and metadata through cached one-type introspection.
- [x] 3.3 Describe only requested concrete member wrappers and nested result types needed by row columns.
- [x] 3.4 Enforce fixed direct-fragment and observed-type limits.
- [x] 3.5 Reject returned typenames absent from the advertised possible-type set.
- [x] 3.6 Preserve GraphQL Java good-faith introspection behavior.
- [x] 3.7 Merge repeated rich GraphQL union registrations by concrete type name and add focused model coverage.

## 4. Plan abstract result selections

- [x] 4.1 Build direct inline fragments for bounded fully described unions and interfaces.
- [x] 4.2 Retain typename-only projection when direct expansion is incomplete or exceeds the bound.
- [x] 4.3 Select each concrete metadata subset through the existing version-aware helper.
- [x] 4.4 Preserve single-shot execution for mutating action results.
- [x] 4.5 Normalize concrete fragment results without inventing identity for typename-only values.

## 5. Add polymorphic collection probe and replay

- [x] 5.1 Replace the abstract-row rejection with a typename-only list or window probe.
- [x] 5.2 Collect distinct observed advertised concrete types under the configured bound.
- [x] 5.3 Build and execute one concrete-fragment replay with identical collection arguments.
- [x] 5.4 Include requested columns only where each concrete type advertises the member wrapper.
- [x] 5.5 Publish only the replay result while retaining probe and replay operation evidence.
- [x] 5.6 Preserve lazy activation, caching, window state, ordering, cancellation, stale generations, and route disposal.
- [x] 5.7 Report replay rows with unobserved typenames as bounded partial projections without retry loops.
- [x] 5.8 Preserve hydrated-row coverage using the matching concrete fragment selection.

## 6. Add focused foundation coverage

- [x] 6.1 Add inline-fragment rendering, ordering, validation, merge, and difference tests.
- [x] 6.2 Add small-union direct selection and broad-union typename fallback tests.
- [x] 6.3 Add abstract legacy-list and bounded-window probe and replay tests.
- [x] 6.4 Add mixed concrete row, missing column, unadvertised typename, and changed replay type tests.
- [x] 6.5 Add cancellation, stale response, cache, and inactive collection tests for two-stage reads.
- [x] 6.6 Add single-shot mutating abstract action-result coverage.

## 7. Qualify against the pinned Reference Application

- [x] 7.1 Add or refine deterministic target-catalogue entries for declared and runtime polymorphic collections.
- [x] 7.2 Add integration assertions for merged union membership, invalid direct selection, concrete type-of rows, and the raw-list schema/runtime mismatch.
- [x] 7.3 Convert declared type-of browser assertions to successful concrete rows and retain the incompatible raw-list union as a bounded local error.
- [x] 7.4 Preserve concrete versionless rows, configured collections, partial errors, lazy tabs, stale windows, and route replacement.
- [x] 7.5 Preserve the explicit opaque composite `invalid-route` assertion.
- [x] 7.6 Update capability inventory classifications only where executable evidence changes.
- [x] 7.7 Verify clean and incremental inventory generation remain byte-identical.

## 8. Document compatibility and isolation

- [x] 8.1 Document fragment selection grammar, bounds, probe/replay behavior, and non-repeatable outcome policy.
- [x] 8.2 Record representative introspection and operation evidence with reproducible commands.
- [x] 8.3 Verify no public GraphQL, Causeway element, event, route, HTMX, Vaadin, dependency, CSP, or asset-policy contract changes.

## 9. Run final qualification gates

- [x] 9.1 Run the complete foundation Node suite and web-component foundation and HTMX Maven tests.
- [x] 9.2 Run relevant GraphQL model and integration tests.
- [x] 9.3 Run the full Petclinic integration and Playwright suites.
- [x] 9.4 Run the Reference Application clean package, integration, capability inventory, and Playwright suites.
- [x] 9.5 Run strict CSP, accessibility, keyboard, responsive, theme, external-request, console-error, page-error, and overflow gates.
- [x] 9.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
