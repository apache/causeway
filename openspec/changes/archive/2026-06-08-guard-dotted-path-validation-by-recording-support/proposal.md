## Why

Command export known-target validation currently enforces dotted-path reachability for selected action targets and reference parameters without checking whether command-log recording support is enabled.
That can reject ordinary command-log exports in applications that have not opted into recording/replay support, even though the dotted-path contract only makes sense for recording-enabled exports.

## What Changes

- Guard command export known-target and known-reference-parameter validation behind `causeway.extensions.command-log.recording-support=ENABLED`.
- When recording support is disabled, preserve existing command export behavior and do not require selected command targets or reference parameters to have an earlier recorded navigation or finder path.
- Keep recording-enabled exports unchanged, including failure messages for unknown action targets and unknown reference parameters.
- Add regression coverage proving disabled recording support bypasses dotted-path validation while enabled recording support still enforces it.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-export-known-targets`: Command export known-target and reference-parameter validation only applies when command-log recording support is enabled.

## Impact

- Affects `CommandExportManager_exportSelected` and `CommandExportKnownTargetValidator` in the command-log applib replay export flow.
- Affects tests for command export known-target validation.
- No API, persistence, dependency, or YAML format changes are expected.
