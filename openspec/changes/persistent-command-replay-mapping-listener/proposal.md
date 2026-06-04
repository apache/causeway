## Why

The current default replay mapping listener can remember result mappings only for the lifetime of a single in-memory listener instance.
Replay workflows that span JVM restarts, multiple operators, or long-running imported command batches need an optional persistent mapping store while retaining the existing lightweight in-memory mode.

## What Changes

- Add a persistent `CommandReplayMappingListener` implementation backed by command log persistence entities.
- Store recorded bookmark logical type and identifier together with actual bookmark logical type and identifier, matching the structure used by the in-memory map but persisted in the datastore.
- Add abstract applib domain and repository types for replay result mappings, plus concrete JDO and JPA implementations following the `CommandLogEntry` pattern.
- Add configuration to select the built-in listener storage strategy, with `IN_MEMORY` preserving the current default and `PERSISTENT` enabling the new persistent implementation.
- Rework the current default in-memory listener so it is conditionally enabled only when the storage strategy is `IN_MEMORY` and no custom listener exists.
- Configure the persistent listener so it is enabled only when the storage strategy is `PERSISTENT` and no custom listener exists.
- Make the persistent listener honor the existing conflict policy for throwing an exception versus logging and continuing.
- Add a fallback layout XML for the persistent mapping entity.
- Add a command log menu action to list the persisted replay result mappings.

## Capabilities

### New Capabilities
- `persistent-command-replay-mapping-listener`: Provides persisted replay result mapping entities, repositories, menu access, and a persistent built-in `CommandReplayMappingListener` implementation.

### Modified Capabilities
- `default-command-replay-mapping-listener`: Add built-in listener storage selection so the existing in-memory listener and the new persistent listener are mutually exclusive conditional defaults.

## Impact

- Affects command log applib, persistence-jdo, and persistence-jpa modules.
- Adds new persistence entities, repositories, and layout metadata.
- Adds or changes command log configuration properties for storage strategy and listener conflict behavior.
- Updates autoconfiguration tests for in-memory and persistent listener activation.
- Adds persistence tests for JDO and JPA implementations.
