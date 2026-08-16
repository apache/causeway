## 1. Confirm Metadata Scope

- [ ] 1.1 Map accepted analysis entries to property, action, parameter, collection, object, or service wrapper fields.
- [ ] 1.2 Classify each field as constraint, semantic behavior, optional presentation hint, structural layout, or excluded viewer-specific behavior.

## 2. Define and Implement Descriptors

- [ ] 2.1 Define reusable metadata descriptor types and stable enum-like values.
- [ ] 2.2 Add independent canonical friendly name and description fields.
- [ ] 2.3 Add confirmed property constraints and editing or navigation hints.
- [ ] 2.4 Add confirmed action semantics, prompt, association, ordering, icon, CSS, and redirect hints.
- [ ] 2.5 Add confirmed collection naming, presentation, page-size, ordering, sequence, icon, and CSS hints.
- [ ] 2.6 Add confirmed object and service metadata without duplicating grid or menu structure.

## 3. Discovery, Security, and Compatibility

- [ ] 3.1 Verify new fields through targeted one-type introspection and known-wrapper reads.
- [ ] 3.2 Verify no aggregate member-list API or metamodel object is exposed.
- [ ] 3.3 Verify hidden members and authorization-sensitive policies do not leak through metadata.
- [ ] 3.4 Preserve existing descriptions, generated names, and operation documents.

## 4. Verification and Documentation

- [ ] 4.1 Add reference-derived fixtures for names, descriptions, constraints, hints, defaults, missing facets, and translations where supported.
- [ ] 4.2 Add schema snapshots and component-consumer contract fixtures.
- [ ] 4.3 Document every field, source facet, default, authority, optional-client behavior, and relationship to grid and menu resources.
- [ ] 4.4 Run GraphQL tests, compatibility checks, documentation checks, formatting, and strict OpenSpec validation.
