## Why

Command replay remapping changes target and reference parameter bookmarks before execution, but the imported `CommandLogEntry` command DTO is the audit record of what was exported and imported.
If replay remapping mutates that recorded DTO, later inspection, retries, and mapping diagnostics can no longer distinguish the recorded bookmark from the actual replay bookmark.

## What Changes

- Ensure replay target remapping operates on a replay-time copy of the command DTO.
- Ensure replay reference parameter remapping operates on a replay-time copy of the command DTO.
- Preserve the original command DTO stored on the `CommandLogEntry` after replay, retry, or replay failure.
- Add tests proving remapped targets and reference parameters are supplied to replay execution without mutating recorded command data.
- Preserve existing replay mapping SPI contracts and replay result mapping behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `command-replay-result-mapping`: Strengthen replay input remapping requirements so remapped targets and reference parameters never mutate recorded command DTO data.

## Impact

- Replayable command replay-or-retry flow in the command-log applib replay layer.
- Command DTO copy/remapping utilities used immediately before command execution.
- Tests for target and reference parameter remapping, replay retry, and recorded DTO inspection.
- No schema, YAML, or SPI signature changes are expected.
