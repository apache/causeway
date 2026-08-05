## 1. Applib persistent mapping model

- [x] 1.1 Add the abstract `CommandReplayResultMapping` domain type with recorded bookmark, actual bookmark, optional command interaction id, stable logical type, title behavior, and public API documentation.
- [x] 1.2 Add the persistence-neutral repository interface and reusable repository base for recorded/actual lookup, all/changed listing, create-and-flush, single removal support, and bulk removal.
- [x] 1.3 Add focused applib tests for mapping initialization, title and interaction-id behavior, repository query selection and parameters, creation, and removal.

## 2. Persistent listener and conditional wiring

- [x] 2.1 Implement `CommandReplayMappingListenerPersistent` against the applib repository using bookmark-only lookup and the existing replay-result-mapping conflict policy.
- [x] 2.2 Preserve first-observation semantics for identity and changed mappings, idempotent repeats, conflicting repeats, and originating interaction ids.
- [x] 2.3 Add Spring Boot 4 conditional wiring for `PERSISTENT` storage with repository presence and custom-listener back-off, while keeping the in-memory and persistent defaults mutually exclusive.
- [x] 2.4 Add listener and application-context tests for lookup, identity mapping, creation, interaction-id retention, both conflict policies, repository absence, custom-listener back-off, and `IN_MEMORY` exclusion.

## 3. Causeway 4 JPA persistence

- [x] 3.1 Add the Jakarta Persistence replay-result mapping entity with bookmark conversion, optional UUID interaction id, recorded-bookmark uniqueness, recorded/actual indexes, named queries, and Causeway entity integration.
- [x] 3.2 Add the concrete JPA repository and register the entity, repository, and persistent-listener configuration with the commandlog JPA module and entity scan.
- [x] 3.3 Extend commandlog JPA teardown to remove replay-result mappings without changing command-log entry behavior.
- [x] 3.4 Add JPA integration tests for create-and-flush, identity and changed rows, recorded and actual bookmark finders, changed/all listing, uniqueness, interaction-id persistence, and teardown.

## 4. Mapping inspection and deletion UI

- [x] 4.1 Add optional-repository commandlog menu actions for all mappings, changed mappings, recorded-bookmark lookup, and actual-bookmark lookup, hidden when persistence is unavailable.
- [x] 4.2 Add fallback layout and column-order metadata displaying recorded bookmark, actual bookmark, and optional originating command interaction id.
- [x] 4.3 Add an idempotent are-you-sure entity mixin for deleting one mapping and include it in module registration and fallback layout.
- [x] 4.4 Add an idempotent are-you-sure commandlog menu action for deleting all mappings, reporting the deleted count and leaving command log entries untouched.
- [x] 4.5 Order export/replay actions before mapping finders and the bulk-delete action after them, with focused menu, layout, visibility, deletion, and ordering tests.

## 5. Documentation and verification

- [x] 5.1 Document persistent storage activation, the commandlog JPA module/schema prerequisite, custom-listener back-off, first-mapping/conflict semantics, interaction-id retention, management actions, and the Causeway 4 JPA-only adaptation.
- [x] 5.2 Run focused commandlog applib and JPA tests with JDK 21, including existing in-memory mapping and replay lifecycle regressions.
- [x] 5.3 Run the affected aggregate Maven build, IDE compilation and inspections, strict OpenSpec validation, and repository checks.
- [x] 5.4 Confirm the implementation introduces no commandlog JDO module and no P1/P2, R1/R2, E1/W1, or B1/B2 behavior.
