## 1. Reference Coverage Baseline

- [ ] 1.1 Pin the audited reference-application revision and build a machine-readable matrix of representative object, service, property, action, parameter, collection, value, layout, and application-entry features.
- [ ] 1.2 Classify each feature as already supported, intentionally client or viewer specific, explicitly unsupported, or requiring a rich GraphQL capability.
- [ ] 1.3 Add reduced deterministic Causeway fixtures for every required capability rather than depending on a network checkout during the normal build.
- [ ] 1.4 Document the existing coverage for services, entities, view models, mixins, dynamic member state, supporting methods, bulk parameters, and action semantics to prevent duplicate work.

## 2. Reversible Value-Type Coverage

- [ ] 2.1 Replace silent generic-string fallback for input-capable values with explicit reversible marshaller selection or an actionable unsupported diagnostic.
- [ ] 2.2 Add round-trip marshallers and GraphQL scalar contracts for `LocalDateTime`, `URL`, `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp` using documented canonical formats.
- [ ] 2.3 Review and test existing local, offset, zoned, UUID, big-number, primitive, wrapper, enum, and void marshallers against the reference fixtures.
- [ ] 2.4 Define value-semantics descriptors and extension contracts for Causeway values and application custom values, including textual, temporal, numeric, resource, composite, and opaque categories.
- [ ] 2.5 Add schema-build, query, mutation, action-parameter, action-result, nullability, invalid-input, and round-trip tests for every supported value category.

## 3. Structured Member and Type Semantics

- [ ] 3.1 Define additive rich-wrapper metadata fields for canonical friendly name and description without conflating the two GraphQL introspection description uses.
- [ ] 3.2 Expose property constraints and hints needed by generic clients, including optionality, maximum length, regular-expression intent, file acceptance, multiline, typical length, label position, and navigability where semantically applicable.
- [ ] 3.3 Expose action semantics and hints needed by generic clients, including safe or mutating semantics, prompt style, redirect policy, position, field-set association, sequence, icon, and CSS hints where semantically applicable.
- [ ] 3.4 Expose collection semantics and hints needed by generic clients, including friendly name, description, default view, page size, ordering, table decoration, sequence, and CSS hints where semantically applicable.
- [ ] 3.5 Expose object and service presentation metadata needed outside Wicket while retaining `_meta.layout` and `_meta.grid` as the structural-layout resource contracts.
- [ ] 3.6 Verify metadata through targeted introspection and known-member wrapper reads without introducing a duplicate member-list endpoint.

## 4. Bounded Collection Access

- [ ] 4.1 Define deterministic collection-window arguments and result metadata for offset or cursor, requested size, returned size, total count when available, and continuation state.
- [ ] 4.2 Implement bounded rich collection retrieval without changing existing unargumented collection behavior during the compatibility period.
- [ ] 4.3 Apply supported collection ordering semantics deterministically and report unsupported client-supplied ordering rather than silently reordering.
- [ ] 4.4 Test empty, small, large, sorted, dynamically hidden, dynamically disabled, partial-error, concurrent-window, and out-of-range collection reads.
- [ ] 4.5 Document when domain-model collection access still materializes more data server-side than the GraphQL response window.

## 5. Resource and Custom Value Interaction

- [ ] 5.1 Define consistent Blob and Clob query, mutation, action-parameter, and action-result representations using inline bounded content or secured resource references as appropriate.
- [ ] 5.2 Expose accepted-file constraints and content metadata needed to construct a valid client input.
- [ ] 5.3 Define explicit behavior for image, markup, password, local-resource-path, tree, composite, embedded, and application custom values.
- [ ] 5.4 Ensure password, hidden, and otherwise sensitive values cannot be disclosed through metadata, diagnostics, errors, or fallback serialization.
- [ ] 5.5 Test supported resource round trips, size limits, media types, filenames, forbidden resources, malformed input, and explicit unsupported outcomes.

## 6. Application Entry Points

- [ ] 6.1 Define GraphQL metadata for domain-service grouping, ordering, friendly presentation, and visibility without exposing direct metamodel objects.
- [ ] 6.2 Define discovery of the configured home-page action and its invocation contract.
- [ ] 6.3 Verify service and home-page actions reuse the established parameter negotiation, validation, result, and safe or mutating operation contracts.
- [ ] 6.4 Document menu, home-page, authentication, and authorization boundaries that remain viewer or application policy.

## 7. Compatibility and Verification

- [ ] 7.1 Add schema snapshots and compatibility tests proving existing rich query and mutation fields remain valid.
- [ ] 7.2 Add reference-derived integration tests for representative datatype, member metadata, collection, service, home-page, resource, custom-value, bulk-action, mixin, entity, and view-model scenarios.
- [ ] 7.3 Add client contract tests showing targeted introspection can discover every new capability without broad repeated `__Type.fields` queries.
- [ ] 7.4 Document the complete reference-coverage matrix, canonical scalar formats, metadata fields, collection-window contract, resource policy, extension APIs, and intentional exclusions.
- [ ] 7.5 Run GraphQL model and viewer tests, reference-derived fixture tests, compatibility checks, formatting, documentation checks, and strict OpenSpec validation after promotion.
