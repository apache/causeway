## Why

Regression replay needs a complete dotted line through recorded workflows, including the safe finder actions that locate the domain objects later commands operate on.
Today command logging normally captures only state-changing commands, so replay result remapping cannot learn how a found object in the recording corresponds to the object found during replay.

## What Changes

- Add an opt-in configuration property that enables command publishing for safe actions through the normal command publishing facet model.
- Treat enabled safe action invocations as command-log entries with command DTO metadata, so they can be exported and replayed alongside state-changing commands.
- Store returned object bookmark metadata when the safe action result provides one.
- Keep existing default behavior unchanged when the property is disabled.
- Preserve existing command export and replay behavior so recorded finder results can feed the replay mapping SPI in the same way as other commands with returned object metadata.

## Capabilities

### New Capabilities
- `safe-action-command-publishing`: Opt-in command publishing for safe actions, enabling exported regression recordings to include finder steps and any returned object bookmarks that the framework can capture.

### Modified Capabilities
- `command-export-yaml-result`: Exported command YAML includes returned object metadata for safe action command log entries when a result bookmark is available.
- `command-replay-result-mapping`: Replay result mapping notifications include replayed safe action entries when both recorded and actual result bookmarks are available.

## Impact

- Affects command publishing facet creation for safe action invocations.
- Adds framework configuration under the command log settings namespace.
- Affects command log export and replay regression tests by allowing finder steps to appear in exported command streams.
- Requires unit and integration tests covering disabled-by-default behavior, enabled safe action logging, explicit command publishing, export metadata, and replay mapping notifications.
