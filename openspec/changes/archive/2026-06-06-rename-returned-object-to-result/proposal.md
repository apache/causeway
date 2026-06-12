## Why

Command export and replay YAML currently names returned object metadata as `returnedObject`.
The underlying `CommandLogEntry` property is named `result`, so renaming the YAML field to `result` makes exported and imported data align with the domain model.

## What Changes

- Rename the command export YAML returned object metadata field from `returnedObject` to `result`.
- Update replay import to read `result` for `CommandExportDto` result bookmark metadata.
- Remove support for the old `returnedObject` field; no backwards compatibility is required.
- Update tests, fixtures, and documentation/specs to use `result` consistently.
- **BREAKING**: Existing command export YAML using `returnedObject` must be regenerated or manually renamed to `result` before replay import.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-yaml-result`: exported and imported command result metadata uses the YAML field name `result` instead of `returnedObject`.

## Impact

- Affected code is expected around `CommandExportDto` serialization/deserialization and command replay import handling.
- Affected tests are expected around command export YAML generation and replay import of result bookmark metadata.
- Existing YAML fixtures or examples containing `returnedObject` need to be updated to `result`.
