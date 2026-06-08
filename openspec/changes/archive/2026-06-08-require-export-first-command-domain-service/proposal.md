## Why

Command export sequences need a reachable starting point.
The first exported action cannot rely on a prior result bookmark, so it should only be exportable when its target is an export root, meaning a domain service.

## What Changes

- Require the first selected/exportable command in a command export sequence to target a domain service export root.
- Ensure a first command targeting an ordinary domain object is rejected for export even if that object can be resolved locally.
- Ensure the `ReplayableCommand` exportability indicator reports `false` for a first command whose target is not a domain service.
- No breaking changes to exported YAML format or command recording.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-known-targets`: Clarifies that the first command is valid only when its target is an export root domain service because no prior command results are known.
- `replayable-command-exportability`: Ensures the per-command exportability indicator follows the same first-command domain-service rule.

## Impact

- Affects command export validation and replayable command exportability checks in `extensions/core/commandlog/applib`.
- Adds tests for first-command validation and exportability behavior.
