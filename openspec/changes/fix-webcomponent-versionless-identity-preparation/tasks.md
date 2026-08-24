## 1. Confirm executable versionless shapes

- [ ] 1.1 Capture targeted introspection for representative versioned entity and versionless view-model metadata types.
- [ ] 1.2 Identify concrete Reference Application property, preparation, choice or autocomplete, action-result, and collection-row targets that omit `version`.
- [ ] 1.3 Record retained interface or union and opaque-route targets that remain outside this correction.

## 2. Centralize metadata selection

- [ ] 2.1 Add a shared helper that derives the advertised metadata subset for a concrete object type.
- [ ] 2.2 Preserve `id`, `logicalTypeName`, `title`, and `version` only when each field is advertised.
- [ ] 2.3 Return a bounded projection when an object type or metadata description is unavailable.
- [ ] 2.4 Keep semantic identity minimum validation separate from optional version handling.
- [ ] 2.5 Reuse the helper from direct interaction result selection without changing scalar, enum, resource, or union behavior.

## 3. Correct property and preparation projections

- [ ] 3.1 Replace unconditional object-valued property metadata selection with the shared advertised selection.
- [ ] 3.2 Verify versioned property references continue to retain returned versions.
- [ ] 3.3 Verify versionless property references remain renderable and convertible to pending identity values.
- [ ] 3.4 Verify action and property defaults, choices, autocomplete, validity, and datatype preparation omit absent metadata fields.
- [ ] 3.5 Preserve preparation cancellation, stale-response, validation, and protected-value behavior.

## 4. Correct collection-row projections

- [ ] 4.1 Derive the base row selection from the concrete collection element type.
- [ ] 4.2 Merge advertised row metadata with declared semantic columns without duplicate or speculative fields.
- [ ] 4.3 Preserve versioned row hydration, windows, ordering, offsets, lazy activation, and partial errors.
- [ ] 4.4 Support versionless concrete row hydration when `id` and `logicalTypeName` are advertised and returned.
- [ ] 4.5 Retain bounded non-navigable or partial behavior for abstract rows that require the later union-projection change.

## 5. Add focused foundation coverage

- [ ] 5.1 Add versioned and versionless metadata-selection fixtures and unit tests.
- [ ] 5.2 Add object-valued property tests proving absent `version` is not selected.
- [ ] 5.3 Add preparation, choices, and autocomplete tests for concrete versionless values.
- [ ] 5.4 Add versioned and versionless collection-row selection and hydration tests.
- [ ] 5.5 Add missing metadata-description and missing identity-minimum tests.
- [ ] 5.6 Add regression assertions that unions remain bounded and no version or bookmark is invented.

## 6. Qualify against the pinned Reference Application

- [ ] 6.1 Add or refine deterministic target-catalogue entries for versionless preparation and row journeys.
- [ ] 6.2 Convert missing-version browser assertions to successful typed interactions only where executable evidence supports the correction.
- [ ] 6.3 Preserve invalid, cancelled, stale, responsive, accessibility, CSP, console, external-request, and route-disposal assertions.
- [ ] 6.4 Preserve explicit union-projection and opaque-route known gaps.
- [ ] 6.5 Update capability inventory classifications only where executable evidence changes.
- [ ] 6.6 Verify clean and incremental inventory generation remain byte-identical.

## 7. Document compatibility and isolation

- [ ] 7.1 Document the identity-versus-version contract and concrete metadata selection algorithm.
- [ ] 7.2 Record representative introspection and operation evidence with reproducible commands.
- [ ] 7.3 Verify no public GraphQL, Causeway element, event, route, HTMX, Vaadin, dependency, CSP, or asset-policy contract changes.

## 8. Run final qualification gates

- [ ] 8.1 Run the complete foundation Node suite and web-component foundation and HTMX Maven tests.
- [ ] 8.2 Run relevant GraphQL model and integration tests.
- [ ] 8.3 Run the full Petclinic integration and Playwright suites.
- [ ] 8.4 Run the Reference Application clean package, integration, capability inventory, and Playwright suites.
- [ ] 8.5 Run strict CSP, accessibility, keyboard, responsive, theme, external-request, console-error, page-error, and overflow gates.
- [ ] 8.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
