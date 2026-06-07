## 1. Applib Mapping Contract

- [ ] 1.1 Add an optional `commandInteractionId` property to `CommandReplayResultMapping` with domain event metadata and accessors.
- [ ] 1.2 Update `CommandReplayResultMapping#init(...)` to accept and set the command interaction id while preserving bookmark initialization.
- [ ] 1.3 Update `CommandReplayResultMappingRepository#createAndPersist(...)` and `CommandReplayResultMappingRepositoryAbstract` to create mappings with the command interaction id.
- [ ] 1.4 Preserve compatibility where useful by delegating any existing two-bookmark creation path to the new creation path with a null command interaction id.

## 2. Replay Mapping Listener Implementations

- [ ] 2.1 Update `CommandReplayMappingListenerPersistent` to pass `CommandLogEntry#getInteractionId()` when creating a new persistent mapping.
- [ ] 2.2 Ensure `CommandReplayMappingListenerPersistent` does not update `commandInteractionId` for idempotent existing mappings or logged conflicts.
- [ ] 2.3 Replace the in-memory listener value type with a mapping data structure that stores actual bookmark and command interaction id.
- [ ] 2.4 Ensure in-memory listener lookup and conflict handling preserve existing behavior while retaining the first command interaction id.

## 3. Persistence and Layout Metadata

- [ ] 3.1 Add nullable command interaction id storage to the JDO `CommandReplayResultMapping` entity.
- [ ] 3.2 Add nullable command interaction id storage to the JPA `CommandReplayResultMapping` entity.
- [ ] 3.3 Regenerate or update JDO query classes if required by the added persistent field.
- [ ] 3.4 Update `CommandReplayResultMapping.layout.fallback.xml` to display `commandInteractionId`.
- [ ] 3.5 Update `CommandReplayResultMapping.columnOrder.fallback.txt` to include `commandInteractionId`.

## 4. Tests and Validation

- [ ] 4.1 Add applib tests proving new persistent mappings capture the replayed command interaction id.
- [ ] 4.2 Add tests proving existing idempotent mappings keep their original command interaction id.
- [ ] 4.3 Add tests proving logged conflict handling keeps the original command interaction id.
- [ ] 4.4 Add in-memory listener tests for capturing and retaining the first command interaction id in the internal data structure.
- [ ] 4.5 Add or update persistence integration tests for JDO and JPA mapping entities.
- [ ] 4.6 Validate layout and column order metadata includes `commandInteractionId`.
- [ ] 4.7 Run `openspec validate capture-replay-mapping-command-interaction-id --strict`.
- [ ] 4.8 Run targeted Maven tests for commandlog applib, persistence-jdo, and persistence-jpa modules.
