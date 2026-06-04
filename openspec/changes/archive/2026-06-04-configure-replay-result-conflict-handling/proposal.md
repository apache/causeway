## Why

The default replay mapping listener currently always fails replay when the same recorded bookmark later maps to a different actual bookmark.
Some import/replay workflows need the strict behavior for safety, while others prefer to log conflicting mappings and continue processing subsequent commands.

## What Changes

- Add a configuration property that controls how the default command replay mapping listener handles conflicting replay result mappings.
- Keep the current strict behavior as the default, so a conflicting mapping throws an exception and fails replay.
- Add an alternative lenient behavior that logs the conflicting mapping and continues without replacing the existing recorded-to-actual mapping.
- Document the accepted property values and the default value.

## Capabilities

### New Capabilities

### Modified Capabilities
- `default-command-replay-mapping-listener`: Make conflict handling configurable while preserving strict conflict rejection as the default behavior.

## Impact

- Affects the command log applib default `CommandReplayMappingListener` implementation and its autoconfiguration.
- Adds a new command log configuration property, likely under the extensions command log replay namespace.
- Updates unit tests for strict default behavior and lenient log-and-continue behavior.
- No breaking changes are expected because the current exception-throwing behavior remains the default.
