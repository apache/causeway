## Why

Synthetic parented collection selector actions are expected to participate in safe action command publishing, but they are not appearing in the command log for export.
This indicates that the synthetic action metamodel may not be exposing the command publishing metadata needed by the normal command logging and export pipeline.

## What Changes

- Ensure synthetic parented collection selector actions carry the same command publishing facet metadata expected by the safe action command publishing flow.
- Ensure invoking a synthetic selector action creates a command log entry when safe action command publishing and selector action creation are both enabled.
- Ensure logged synthetic selector action commands are available to command export with their command DTO and returned object metadata.
- Preserve disabled-by-default behavior when safe action command publishing is not enabled.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `safe-action-command-publishing`: clarify that synthetic selector actions must expose command publishing facet metadata and create command log entries through the normal flow when enabled.
- `command-export-yaml-result`: clarify that logged synthetic selector actions are exportable like other logged safe actions.

## Impact

- Affects synthetic selector action metamodel facet installation.
- Affects command logging behavior for selector action invocations when both relevant opt-in properties are enabled.
- Affects command export coverage for recorded parent-to-child navigation steps.
- Requires focused tests proving selector actions have command publishing metadata and can be exported once logged.
