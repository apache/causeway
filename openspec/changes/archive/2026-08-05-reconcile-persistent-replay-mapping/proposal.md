## Why

Causeway 4 can retain replay-result mappings only in memory, so mappings are lost across application restarts and cannot be inspected or managed as durable commandlog data.
Now that the bookmark-only replay mapping SPI and `PERSISTENT` storage-strategy contract exist, M3 completes that contract using the Causeway 4 commandlog persistence architecture.

## What Changes

- Add an abstract commandlog applib model and repository contract for durable recorded-to-actual bookmark mappings, including the originating command interaction id.
- Add a persistent replay-mapping listener that looks up stored mappings, records identity and changed results, preserves the first observation, and applies the existing conflict policy.
- Conditionally provide the persistent listener when storage strategy is `PERSISTENT`, a mapping repository is available, and no application-defined replay-mapping listener exists.
- Add JPA entity and repository implementations, datastore uniqueness and lookup indexes, module registration, teardown support, and integration tests.
- Add commandlog menu actions and fallback layout metadata for listing, finding, inspecting, and deleting persisted mappings.
- Treat commandlog JDO persistence as not applicable on Causeway 4 because that adapter was deliberately removed; do not recreate it as part of M3.
- Keep replayable-command projections, unified manager behavior, export/import, reachability, workflow, and background-gate capabilities outside this change.

## Capabilities

### New Capabilities

- `persistent-command-replay-mapping`: Defines durable replay-result mapping storage, persistent listener behavior, JPA persistence, conditional wiring, query/menu access, interaction-id retention, and deletion.

### Modified Capabilities

None.

## Impact

- `extensions/core/commandlog/applib`: abstract mapping model and repository, persistent listener, conditional bean factory, menu actions, mixin, layout, and tests.
- `extensions/core/commandlog/persistence-jpa`: concrete Jakarta Persistence entity and repository, indexes and named queries, module registration, teardown, and integration tests.
- Existing `causeway.extensions.command-log.replay-result-mapping` configuration and `CommandReplayMappingListener` SPI are reused without changing their public shape.
- Applications opting into `PERSISTENT` gain durable mapping state and require the commandlog JPA persistence module and its schema update; `IN_MEMORY` remains the compatibility default.
- Programme context and the explicit Causeway 4 JPA-only adaptation are recorded under `openspec/reconciliation/maintenance-branch/`.
