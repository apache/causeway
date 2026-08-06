## Why

The unified command manager can now review, validate, export, and import replay sequences, but repairing a recorded sequence still depends on legacy manager actions or low-level row operations. W1 completes the forward workflow by bringing exclusion, restoration, deletion, and deterministic retimestamp-based movement onto the unified manager while preserving Causeway 4 persistence and compatibility boundaries.

## What Changes

- Add unified-manager actions to exclude selected active commands, defaulting the selection to commands with unknown participants, and to restore selected excluded commands to an explicitly chosen non-excluded replay state.
- Add guarded permanent deletion for selected commands from the manager's excluded collection.
- Add one bidirectional `moveCommands` action that repositions a selected ordered block after another active command by updating both command-log and embedded DTO timestamps.
- Preserve original positive timing gaps with a one-second minimum, with an optional mode that squashes all selected-command gaps to one second.
- Restrict selection and targets to the current baseline-bounded manager collections, enforce recording-support gates where required, and expose the actions in the unified-manager fallback layout.
- Retain legacy manager logical types, bookmarks, actions, replay states, and mementos as compatibility surfaces; add no schema, replay gate, background gate, or commandlog JDO adapter.

## Capabilities

### New Capabilities

- `command-export-command-exclusion`: Defines unified-manager exclusion, restoration, and deletion of baseline-bounded command-log entries.
- `command-export-command-reordering`: Defines selection validation and deterministic retimestamping for moving command blocks in the unified sequence.

### Modified Capabilities

- `unified-command-manager`: Exposes W1 workflow actions in the unified manager and updates its fallback presentation boundary while retaining existing state and collection contracts.

## Impact

- Affects commandlog applib manager mixins, workflow support, module registration, fallback layout, and focused tests.
- Uses existing mutable command-log entry contracts and the Causeway 4 Jakarta Persistence adapter; no repository schema or named-query addition is expected.
- Requires JPA integration coverage for replay-state, deletion, and timestamp/DTO persistence, plus compatibility guards for legacy managers and the deliberately absent commandlog JDO adapter.
