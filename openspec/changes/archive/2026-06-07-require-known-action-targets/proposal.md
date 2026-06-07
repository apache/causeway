## Why

Recorded command export must be replayable as a navigable sequence, which requires every action target to have a known dotted path from a root menu service or a previously recorded result.
During normal recording, however, it is difficult to know how far back to search in `CommandLogEntry` history to decide whether a target is known.
The export manager already has a baseline and selected command sequence, so export is the right point to validate whether the sequence is actually exportable.

## What Changes

- Validate the export manager's selected command sequence before creating YAML.
- Use the export manager baseline to define how far back target-knownness searches may go.
- Treat menu service actions on `@DomainService` objects as root actions that are always valid starting points for an exported sequence.
- Treat targets returned by earlier commands in the exportable range, including synthetic navigate-to actions, as known targets that can be used by later exported actions.
- Prevent export when any selected action uses an unknown target.
- Report a validation message that identifies which command failed and why the target is not exportable.
- Do not block additional commands while recording.

## Capabilities

### New Capabilities
- `command-export-known-targets`: Defines target knowledge and validation rules for exportable command sequences.

### Modified Capabilities
- `safe-action-command-publishing`: Clarifies that synthetic selector and navigate-to safe action results can establish known targets for later exported actions.
- `command-export-yaml-result`: Clarifies that command export validates selected sequences using prior result metadata before emitting YAML.

## Impact

- Command export manager validation and export action behavior.
- Command export and replay semantics for action target resolution.
- Synthetic navigate-to action handling and safe action command publishing.
- Applications using recording support may continue recording arbitrary commands, but invalid sequences will be prevented from exporting until the missing navigation or finder command is included.
