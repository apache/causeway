## Why

The replay mapping SPI currently names its responsibilities around remapping and mapped results, even though replay must now surface every encountered result bookmark for downstream UI and persistence workflows.
Recording only non-identity result mappings hides replayed objects whose recorded and actual bookmarks are the same, which prevents the ReplayableCommand UI from showing the complete set of involved objects.

## What Changes

- **BREAKING**: Rename `CommandReplayMappingListener#remap(...)` to `lookup(...)` to reflect that listeners look up a previously observed bookmark rather than necessarily remapping it.
- **BREAKING**: Rename `CommandReplayMappingListener#onReplayResultMapped(...)` to `onReplayResult(...)` to reflect that listeners are notified for every available replay result bookmark pair, including identity results.
- Update the in-memory replay mapping listener to record every replay result notification keyed by recorded result bookmark, including equal recorded and actual bookmarks.
- Update the persistent replay mapping listener to persist every replay result notification keyed by recorded result bookmark, including equal recorded and actual bookmarks.
- Preserve existing conflict handling when a recorded result bookmark is later associated with a different actual result bookmark.
- Update replay execution and tests to call the renamed SPI methods.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-replay-result-mapping`: Rename the SPI methods and clarify that result notifications represent all replay results rather than only non-identity mappings.
- `default-command-replay-mapping-listener`: Change the built-in in-memory listener so identity replay results are stored and can be looked up later.
- `persistent-command-replay-mapping-listener`: Change the persistent listener so identity replay results are persisted and can be listed or looked up later.

## Impact

- Affects the command log applib SPI `CommandReplayMappingListener` and all implementations.
- Affects replay execution code in `ReplayableCommand` that consults listeners before execution and notifies them after successful replay.
- Affects in-memory and persistent replay mapping listener tests, replay mapping tests, and any downstream custom listener implementations because the SPI method names change.
- Does not introduce new dependencies or storage tables, but changes persistent listener semantics so identity mappings are saved in the existing replay result mapping repository.
