## 1. Configuration and Conditional Beans

- [x] 1.1 Add a built-in replay mapping listener storage strategy enum with `IN_MEMORY` and `PERSISTENT` values.
- [x] 1.2 Add the storage strategy property to `CausewayConfiguration` with `IN_MEMORY` as the default.
- [x] 1.3 Update the existing in-memory listener bean factory so it is created only for `IN_MEMORY`, only when no custom listener exists, and still receives the conflict policy.
- [x] 1.4 Add tests for in-memory listener autoconfiguration, persistent-storage back-off, and custom listener back-off.

## 2. Applib Persistent Mapping Model

- [x] 2.1 Add an abstract applib replay result mapping entity with recorded and actual bookmark properties.
- [x] 2.2 Add bookmark conversion helpers on the applib entity for recorded and actual bookmarks.
- [x] 2.3 Add an abstract applib replay result mapping repository contract for lookup by recorded bookmark, listing all mappings, and creating mappings.
- [x] 2.4 Add a fallback layout XML for the replay result mapping entity.
- [x] 2.5 Add a command log menu action that lists persisted replay result mappings through the applib repository.

## 3. Persistent Listener Implementation

- [x] 3.1 Add a persistent `CommandReplayMappingListener` implementation that depends on the applib repository and conflict policy.
- [x] 3.2 Implement remapping from persisted recorded bookmark mappings without resolving domain objects.
- [x] 3.3 Implement result mapping persistence for first non-identity mappings.
- [x] 3.4 Implement idempotent repeated mapping handling.
- [x] 3.5 Implement conflict handling that throws or logs-and-continues according to the existing conflict policy.
- [x] 3.6 Add conditional bean creation for the persistent listener when storage strategy is `PERSISTENT`, no custom listener exists, and a repository is available.
- [x] 3.7 Add tests for persistent listener remapping, recording, idempotency, strict conflicts, lenient conflicts, and conditional bean creation.

## 4. JDO Persistence Implementation

- [x] 4.1 Add a concrete JDO replay result mapping entity following the command log persistence JDO conventions.
- [x] 4.2 Add a concrete JDO replay result mapping repository implementing the applib repository contract.
- [x] 4.3 Register the JDO entity and repository with the command log JDO persistence module.
- [x] 4.4 Add JDO tests for create, find by recorded bookmark, list all, idempotent repeated mapping, and conflict preservation.

## 5. JPA Persistence Implementation

- [x] 5.1 Add a concrete JPA replay result mapping entity following the command log persistence JPA conventions.
- [x] 5.2 Add a concrete JPA replay result mapping repository implementing the applib repository contract.
- [x] 5.3 Register the JPA entity and repository with the command log JPA persistence module.
- [x] 5.4 Add JPA tests for create, find by recorded bookmark, list all, idempotent repeated mapping, and conflict preservation.

## 6. Validation

- [x] 6.1 Run targeted command log applib tests covering both built-in listener implementations and menu action behavior.
- [x] 6.2 Run targeted command log JDO persistence tests for the new replay result mapping entity and repository.
- [x] 6.3 Run targeted command log JPA persistence tests for the new replay result mapping entity and repository.
- [x] 6.4 Run `openspec validate persistent-command-replay-mapping-listener --type change --strict`.
