## Why

Applications that replay imported commands need a ready-to-use mapping listener for the common case where replay result mappings should feed later input remapping.
Without a default implementation, each application must repeat the same simple in-memory bookkeeping before command replay input remapping works out of the box.

## What Changes

- Add a default command replay mapping listener implementation backed by a simple in-memory `HashMap` from recorded bookmarks to actual replay result bookmarks.
- Record mappings when replay reports `onReplayResultMapped(...)` and consult the recorded-to-actual map from `remap(...)` before command execution.
- Register the default listener through Spring Boot autoconfiguration only when no other `CommandReplayMappingListener` bean is present.
- Leave existing custom listener implementations in control by ensuring the default bean is conditional on missing bean.

## Capabilities

### New Capabilities

- `default-command-replay-mapping-listener`: Provides an out-of-the-box replay mapping listener that remembers replay result mappings and remaps later replay inputs from those mappings.

### Modified Capabilities

- `command-replay-result-mapping`: The replay mapping SPI gains a default autoconfigured implementation when applications do not provide their own listener.

## Impact

- Affects the command log extension applib/autoconfiguration modules that expose and wire `CommandReplayMappingListener`.
- Adds a default Spring bean, guarded by `@ConditionalOnMissingBean`, without changing the SPI method signatures.
- Uses in-memory state, so mappings are scoped to the lifecycle of the default listener bean and are not persisted.
