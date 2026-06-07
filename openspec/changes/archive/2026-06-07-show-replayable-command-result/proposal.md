## Why

ReplayableCommand currently exposes the recorded `CommandDto` but hides the result metadata stored alongside imported/exported commands.
Operators need to inspect replay inputs and the recorded returned object in one place so they can verify replay mappings and exported command streams without opening the underlying command log entry.

## What Changes

- Add the recorded result metadata to the ReplayableCommand view model when a result bookmark is available.
- Render the result using the same logical type and identifier shape used by exported command YAML under the `result` element.
- Leave commands without result metadata unchanged.
- Preserve the existing command DTO display and replay behavior.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-yaml-result`: ReplayableCommand exposes the exported `result` metadata for inspection.

## Impact

- Affects the command-log applib replay view model, especially `ReplayableCommand` and related tests.
- May add a read-only property or DTO field rendering for the recorded result bookmark.
- No expected changes to replay execution, import/export YAML format, or public replay mapping SPI contracts.
