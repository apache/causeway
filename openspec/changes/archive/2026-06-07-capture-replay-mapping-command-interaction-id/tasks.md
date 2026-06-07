## 1. Applib Mapping Contract

- [x] 1.1 Add an optional `commandInteractionId` property to `CommandReplayResultMapping` with domain event metadata and accessors.
- [x] 1.2 Update `CommandReplayResultMapping#init(...)` to accept and set the command interaction id while preserving bookmark initialization.
- [x] 1.3 Update `CommandReplayResultMappingRepository#createAndPersist(...)` and `CommandReplayResultMappingRepositoryAbstract` to create mappings with the command interaction id.
- [x] 1.4 Preserve compatibility where useful by delegating any existing two-bookmark creation path to the new creation path with a null command interaction id.

## 2. Replay Mapping Listener Implementations

- [x] 2.1 Update `CommandReplayMappingListenerPersistent` to pass `CommandLogEntry#getInteractionId()` when creating a new persistent mapping.
- [x] 2.2 Ensure `CommandReplayMappingListenerPersistent` does not update `commandInteractionId` for idempotent existing mappings or logged conflicts.
- [x] 2.3 Replace the in-memory listener value type with a mapping data structure that stores actual bookmark and command interaction id.
- [x] 2.4 Ensure in-memory listener lookup and conflict handling preserve existing behavior while retaining the first command interaction id.

## 3. Persistence and Layout Metadata

- [x] 3.1 Add nullable command interaction id storage to the JDO `CommandReplayResultMapping` entity.
- [x] 3.2 Add nullable command interaction id storage to the JPA `CommandReplayResultMapping` entity.
- [x] 3.3 Regenerate or update JDO query classes if required by the added persistent field.
- [x] 3.4 Update `CommandReplayResultMapping.layout.fallback.xml` to display `commandInteractionId`.
- [x] 3.5 Update `CommandReplayResultMapping.columnOrder.fallback.txt` to include `commandInteractionId`.

## 4. Tests and Validation

- [x] 4.1 Add applib tests proving new persistent mappings capture the replayed command interaction id.
- [x] 4.2 Add tests proving existing idempotent mappings keep their original command interaction id.
- [x] 4.3 Add tests proving logged conflict handling keeps the original command interaction id.
- [x] 4.4 Add in-memory listener tests for capturing and retaining the first command interaction id in the internal data structure.
- [x] 4.5 Add or update persistence integration tests for JDO and JPA mapping entities.
- [x] 4.6 Validate layout and column order metadata includes `commandInteractionId`.
- [x] 4.7 Run `openspec validate capture-replay-mapping-command-interaction-id --strict`.
- [x] 4.8 Run targeted Maven tests for commandlog applib, persistence-jdo, and persistence-jpa modules.
