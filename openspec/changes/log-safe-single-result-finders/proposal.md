## Why

Regression replay needs a complete dotted line through recorded workflows, including the safe finder actions that locate the domain objects later commands operate on.
Today command logging normally captures only state-changing commands, so replay result remapping cannot learn how a found object in the recording corresponds to the object found during replay.

## What Changes

- Add an opt-in configuration property that enables command logging for safe actions that return a single domain object.
- Treat enabled safe single-result finder invocations as command-log entries with command DTO and returned object bookmark metadata, so they can be exported and replayed alongside state-changing commands.
- Keep existing default behavior unchanged when the property is disabled.
- Exclude safe actions that return lists, collections, void, scalar values, or other non-single-domain-object results from this additional logging path.
- Preserve existing command export and replay behavior so recorded finder results can feed the replay mapping SPI in the same way as other commands with returned object metadata.

## Capabilities

### New Capabilities
- `safe-single-result-finder-command-logging`: Opt-in command logging for safe actions that return a single domain object, enabling exported regression recordings to include finder steps and their returned object bookmarks.

### Modified Capabilities
- `command-export-yaml-result`: Exported command YAML includes returned object metadata for safe single-result finder command log entries when the opt-in logging property is enabled.
- `command-replay-result-mapping`: Replay result mapping notifications include replayed safe single-result finder entries when both recorded and actual result bookmarks are available.

## Impact

- Affects command creation, command publishing, and command log subscriber behavior for safe action invocations.
- Adds framework configuration under the command logging or command publishing settings namespace.
- Affects command log export and replay regression tests by allowing finder steps to appear in exported command streams.
- Requires unit and integration tests covering disabled-by-default behavior, enabled finder logging, excluded safe result shapes, export metadata, and replay mapping notifications.
