## 1. Baseline and ownership

- [x] 1.1 Record the existing rich property, collection, action, and action-parameter wrapper fields and generated schema baseline.
- [x] 1.2 Translate matrix entries `REF-METADATA-01` and `REF-METADATA-02` into independent-name, independent-description, constraint, absence, and localization fixtures.
- [x] 1.3 Classify every candidate as local semantic metadata, existing resource metadata, grid structure, menu structure, viewer policy, Wicket-specific behavior, or explicit exclusion.
- [x] 1.4 Confirm action position, prompt, redirect, icon, CSS, collection paging, sorting, sequence, and resource-owned fields remain excluded.

## 2. Shared scalar metadata support

- [x] 2.1 Add one reusable scalar metadata-field helper and exactly one shared GraphQL metadata object type.
- [x] 2.2 Add null-safe suppliers that execute during field resolution rather than schema construction.
- [x] 2.3 Normalize absent, unlimited, fallback-only, malformed, and non-positive constraint values to null.
- [x] 2.4 Unit test helper field names, output types, null behavior, and request-time supplier execution.

## 3. Independent names and descriptions

- [x] 3.1 Add non-null `metadata` with non-null `friendlyName` and nullable `description` to rich property wrappers.
- [x] 3.2 Add non-null `metadata` with non-null `friendlyName` and nullable `description` to rich collection wrappers.
- [x] 3.3 Add non-null `metadata` with non-null `friendlyName` and nullable `description` to rich action wrappers.
- [x] 3.4 Add non-null `metadata` with non-null `friendlyName` and nullable `description` to rich action-parameter wrappers.
- [x] 3.5 Resolve names and descriptions from canonical static facets without invoking imperative domain-object methods.
- [x] 3.6 Verify absent descriptions remain null rather than copying friendly names.

## 4. Property and parameter constraints

- [x] 4.1 Add nullable `maxLength` fields backed only by finite non-fallback facets.
- [x] 4.2 Add nullable `pattern` and `patternFlags` fields backed by non-fallback Java regular-expression facets.
- [x] 4.3 Add nullable `multiLine` fields backed only by positive non-fallback line counts.
- [x] 4.4 Add nullable `typicalLength` fields backed only by positive effective feature facets.
- [x] 4.5 Apply the same normalized field contract to rich properties and rich action parameters.
- [x] 4.6 Preserve GraphQL input nullability as structural requiredness and server validation as authoritative.
- [x] 4.7 Preserve established resource `fileAccept` fields without duplication or relocation.

## 5. Discovery, security, and compatibility

- [x] 5.1 Verify every added field through targeted one-type introspection.
- [x] 5.2 Verify known-wrapper reads return independent names, descriptions, constraints, and nulls.
- [x] 5.3 Verify no aggregate member list, metamodel object, grid structure, menu structure, or viewer policy is exposed.
- [x] 5.4 Verify hidden members, sensitive values, disabled internals, authorization policies, and imperative text methods do not leak through metadata.
- [x] 5.5 Verify equivalent requests under two supported locales do not share a cached translation.
- [x] 5.6 Preserve existing descriptions, generated names, resource fields, and established operation documents.

## 6. Reference-derived qualification

- [x] 6.1 Add deterministic GraphQL test-domain fixtures for distinct names and descriptions, every accepted constraint, and missing facets.
- [x] 6.2 Add property, collection, action, and parameter metadata query coverage.
- [x] 6.3 Add localization and authorization-negative coverage.
- [x] 6.4 Add Reference Application targets or executable probes for matrix entries `REF-METADATA-01` and `REF-METADATA-02`.
- [x] 6.5 Verify the reviewed Reference Application capability inventory remains byte-identical unless an evidence-backed reclassification is required.

## 7. Schema and performance evidence

- [x] 7.1 Measure generated schema type count and prove metadata adds exactly one shared GraphQL object type.
- [x] 7.2 Measure generated SDL byte growth and attribute it to documented scalar fields.
- [x] 7.3 Measure representative schema startup before and after the change.
- [x] 7.4 Record bounded acceptance criteria and measured results.

## 8. Documentation and release gates

- [x] 8.1 Document every added field, GraphQL type, source facet, null behavior, authority, and localization contract.
- [x] 8.2 Document the relationship to existing resource metadata and effective grid and menu resources.
- [x] 8.3 Document exclusions, migration compatibility, targeted introspection, and client adoption guidance.
- [x] 8.4 Run focused model tests and GraphQL integration tests.
- [x] 8.5 Run established GraphQL compatibility, resource, value-semantics, collection, and application-entry suites.
- [x] 8.6 Run Reference Application integration and inventory checks.
- [x] 8.7 Run applicable RAT, formatting, strict OpenSpec, and Git whitespace checks.
- [x] 8.8 Record final gate results, production isolation, and reproducible commands.
