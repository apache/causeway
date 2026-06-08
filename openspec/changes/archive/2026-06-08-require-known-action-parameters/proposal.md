## Why

Command export already validates that each selected action target is reachable from a root or a previously recorded result, but action parameters can also reference domain objects that replay must resolve.
If a selected action includes an unknown reference parameter, the exported YAML can still be unreplayable even when the action target itself is known.

## What Changes

- Extend export validation so reference action parameters must be known in the same baseline-bounded export sequence as action targets.
- Treat a reference parameter as known when it is an export root or when an earlier command result in export order produced that bookmark.
- Reject selected exports that contain unknown reference parameters before YAML is emitted.
- Include the failing command, parameter name, and unknown parameter bookmark in the validation message.
- Preserve existing recording behavior so commands are not blocked while recording.
- Preserve existing command YAML shape and do not add path metadata.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `command-export-known-targets`: Extend known-target validation to action reference parameters as well as action targets.

## Impact

- Command export manager validation and export action behavior.
- Command export known-target validator in the command-log applib replay/export layer.
- Tests for selected exports that include reachable and unreachable reference parameters.
- Applications using recording support may continue recording arbitrary commands, but invalid export selections with missing parameter-producing navigation or finder steps will be prevented from exporting.
