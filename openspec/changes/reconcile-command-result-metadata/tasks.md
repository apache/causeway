## 1. Portable command result transfer contract

- [ ] 1.1 Add public `CommandExportDto`, `ImportedCommandDto`, and `BookmarkDto` value types to `CommandDtoUtils`, including null-safe factories and bookmark conversion without object resolution.
- [ ] 1.2 Add `CommandDtoUtils.copy(CommandDto)` using full-fidelity command-schema mapping and ensure copied nested values are independent.
- [ ] 1.3 Add multi-document YAML serialization for `CommandExportDto` values using Causeway 4 `CommandDtoJacksonSupport`, omitting null results and never emitting `returnedObject`.
- [ ] 1.4 Preserve the existing plain `CommandDto` list, multi-document, and read APIs without changing their accepted formats.
- [ ] 1.5 Add focused applib/mmtest coverage for transfer factories, unresolved bookmarks, unknown legacy fields, YAML output and ordering, null omission, deep copying, and legacy YAML regressions.

## 2. Runtime command result capture

- [ ] 2.1 Refactor `MemberExecutorServiceDefault` result candidate selection to accept direct results and exactly-one-element packed result containers.
- [ ] 2.2 Capture bookmarks for bookmarkable entities and view models while retaining transaction synchronization for persistable results that need identifiers.
- [ ] 2.3 Preserve an existing command result and leave empty, multi-element, null, unspecified, and non-bookmarkable results unset.
- [ ] 2.4 Add focused runtime-service tests for direct entities, view models, transient persistable results, singleton containers, empty and multiple containers, non-bookmarkable values, and existing-result preservation.

## 3. Integration, documentation, and verification

- [ ] 3.1 Verify commandlog consumers can construct result-bearing transfer values from recorded `CommandDto` and result bookmarks without introducing manager export/import or replay behavior.
- [ ] 3.2 Document the new public command result transfer and copy APIs, including the `result.type` and `result.id` YAML shape and the exclusion of `returnedObject`.
- [ ] 3.3 Run focused tests for applib/mmtest, runtime services, and commandlog, then run the affected aggregate Maven build with JDK 21.
- [ ] 3.4 Run IDE compilation/inspection and repository checks, and confirm the implementation does not include C2, C3/C4b, M1-M3, P1/P2, R1/R2, E1, W1, or B1/B2 behavior.
