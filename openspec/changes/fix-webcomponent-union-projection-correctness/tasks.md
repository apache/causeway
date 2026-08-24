## 1. Capture executable union evidence

- [ ] 1.1 Capture targeted introspection for `rich__demo_ValueHolder__gqlv_union`, its advertised possible-type count, and representative concrete metadata.
- [ ] 1.2 Execute typename-only and concrete-fragment operations for both `demo.CollectionTypeOfPage` collections.
- [ ] 1.3 Record invalid direct union metadata selection and the retained opaque-route gap.

## 2. Extend the internal selection model

- [ ] 2.1 Define a reserved deterministic inline-fragment selection node.
- [ ] 2.2 Render `__typename` and sorted `... on Type` fragments with GraphQL-name validation.
- [ ] 2.3 Reject fragment names not advertised by a union or interface.
- [ ] 2.4 Validate each fragment against its described concrete type and nested fields.
- [ ] 2.5 Extend selection merge, difference, emptiness, and coverage helpers for fragments.
- [ ] 2.6 Preserve scalar, enum, concrete object, resource, and argument rendering behavior.

## 3. Describe bounded concrete type closures

- [ ] 3.1 Add helpers that expose advertised abstract possible-type names deterministically.
- [ ] 3.2 Describe observed concrete object types and metadata through cached one-type introspection.
- [ ] 3.3 Describe only requested concrete member wrappers and nested result types needed by row columns.
- [ ] 3.4 Enforce fixed direct-fragment and observed-type limits.
- [ ] 3.5 Reject returned typenames absent from the advertised possible-type set.
- [ ] 3.6 Preserve GraphQL Java good-faith introspection behavior.

## 4. Plan abstract result selections

- [ ] 4.1 Build direct inline fragments for bounded fully described unions and interfaces.
- [ ] 4.2 Retain typename-only projection when direct expansion is incomplete or exceeds the bound.
- [ ] 4.3 Select each concrete metadata subset through the existing version-aware helper.
- [ ] 4.4 Preserve single-shot execution for mutating action results.
- [ ] 4.5 Normalize concrete fragment results without inventing identity for typename-only values.

## 5. Add polymorphic collection probe and replay

- [ ] 5.1 Replace the abstract-row rejection with a typename-only list or window probe.
- [ ] 5.2 Collect distinct observed advertised concrete types under the configured bound.
- [ ] 5.3 Build and execute one concrete-fragment replay with identical collection arguments.
- [ ] 5.4 Include requested columns only where each concrete type advertises the member wrapper.
- [ ] 5.5 Publish only the replay result while retaining probe and replay operation evidence.
- [ ] 5.6 Preserve lazy activation, caching, window state, ordering, cancellation, stale generations, and route disposal.
- [ ] 5.7 Report replay rows with unobserved typenames as bounded partial projections without retry loops.
- [ ] 5.8 Preserve hydrated-row coverage using the matching concrete fragment selection.

## 6. Add focused foundation coverage

- [ ] 6.1 Add inline-fragment rendering, ordering, validation, merge, and difference tests.
- [ ] 6.2 Add small-union direct selection and broad-union typename fallback tests.
- [ ] 6.3 Add abstract legacy-list and bounded-window probe and replay tests.
- [ ] 6.4 Add mixed concrete row, missing column, unadvertised typename, and changed replay type tests.
- [ ] 6.5 Add cancellation, stale response, cache, and inactive collection tests for two-stage reads.
- [ ] 6.6 Add single-shot mutating abstract action-result coverage.

## 7. Qualify against the pinned Reference Application

- [ ] 7.1 Add or refine deterministic target-catalogue entries for declared and runtime polymorphic collections.
- [ ] 7.2 Add integration assertions for union possible types, invalid direct selection, probe typenames, and valid observed fragments.
- [ ] 7.3 Convert polymorphic browser assertions to successful concrete row identity and value behavior where supported.
- [ ] 7.4 Preserve concrete versionless rows, configured collections, partial errors, lazy tabs, stale windows, and route replacement.
- [ ] 7.5 Preserve the explicit opaque composite `invalid-route` assertion.
- [ ] 7.6 Update capability inventory classifications only where executable evidence changes.
- [ ] 7.7 Verify clean and incremental inventory generation remain byte-identical.

## 8. Document compatibility and isolation

- [ ] 8.1 Document fragment selection grammar, bounds, probe/replay behavior, and non-repeatable outcome policy.
- [ ] 8.2 Record representative introspection and operation evidence with reproducible commands.
- [ ] 8.3 Verify no public GraphQL, Causeway element, event, route, HTMX, Vaadin, dependency, CSP, or asset-policy contract changes.

## 9. Run final qualification gates

- [ ] 9.1 Run the complete foundation Node suite and web-component foundation and HTMX Maven tests.
- [ ] 9.2 Run relevant GraphQL model and integration tests.
- [ ] 9.3 Run the full Petclinic integration and Playwright suites.
- [ ] 9.4 Run the Reference Application clean package, integration, capability inventory, and Playwright suites.
- [ ] 9.5 Run strict CSP, accessibility, keyboard, responsive, theme, external-request, console-error, page-error, and overflow gates.
- [ ] 9.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
