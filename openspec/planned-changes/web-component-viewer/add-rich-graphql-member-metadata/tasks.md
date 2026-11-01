## 1. Confirm the Narrow Metadata Set

- [ ] 1.1 Translate matrix entries `REF-METADATA-01` and `REF-METADATA-02` into independent-name, independent-description, and standalone-editor fixtures.
- [ ] 1.2 Classify each candidate as local semantic constraint, grid structure, menu structure, viewer policy, Wicket-specific behavior, or explicit exclusion.
- [ ] 1.3 Reject action position, prompt, redirect, icon, CSS, collection paging, sorting, sequence, and other resource-owned fields unless new evidence changes ownership.

## 2. Define and Implement Local Metadata

- [ ] 2.1 Select the smallest reusable wrapper shape that does not materially inflate schema size.
- [ ] 2.2 Add independent canonical friendly name and description fields.
- [ ] 2.3 Add the accepted maximum-length, regular-expression, accepted-file, multiline, and typical-length fields.
- [ ] 2.4 Preserve GraphQL input nullability as structural requiredness and server validation as authoritative.

## 3. Discovery, Security, and Compatibility

- [ ] 3.1 Verify new fields through targeted one-type introspection and known-wrapper reads.
- [ ] 3.2 Verify no aggregate member list, metamodel object, grid structure, or menu structure is exposed.
- [ ] 3.3 Verify hidden members, sensitive values, disabled internals, and authorization policies do not leak through metadata.
- [ ] 3.4 Preserve existing descriptions, generated names, and operation documents.

## 4. Verification and Documentation

- [ ] 4.1 Add reference-derived fixtures for independent names, descriptions, accepted constraints, missing facets, and localization.
- [ ] 4.2 Measure generated schema-type and startup deltas.
- [ ] 4.3 Document every accepted field, source facet, default, authority, localization, and relationship to grid and menu resources.
- [ ] 4.4 Run GraphQL tests, compatibility checks, documentation checks, formatting, and strict OpenSpec validation.
