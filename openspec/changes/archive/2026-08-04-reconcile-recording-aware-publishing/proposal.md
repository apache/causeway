## Why

Causeway 4 now has the recording-support configuration and suppression foundation, but enabling it still does not make ordinary safe-action invocations or property edits available to the normal command-publishing pipeline.
Without those interactions, recorded command sequences can omit finder steps and state changes needed for faithful replay.

## What Changes

- Make `causeway.extensions.command-log.recording-support=ENABLED` enable normal command publishing for safe actions.
- Make enabled recording support install property command-publishing metadata for ordinary property edits, including properties that would otherwise resolve to disabled publishing.
- Keep safe-action explicit opt-outs, C1 recording suppression, and the existing single command-publishing/subscriber path authoritative.
- Preserve current action and property command-publishing behavior when recording support is disabled.
- Add focused metamodel and runtime or commandlog tests for enabled, disabled, suppressed, explicitly configured, and non-duplicated interactions.
- Defer synthetic navigation actions, replay mapping, export/import workflows, unified command management, and background-completion behavior to their later capability changes.

## Capabilities

### New Capabilities

- `recording-aware-command-publishing`: Defines how recording support broadens safe-action and property-edit command-publishing eligibility while preserving normal publishing flow, suppression, opt-outs, and disabled-mode compatibility.

### Modified Capabilities

None.

## Impact

- Affects action and property command-publishing facet selection in `core/metamodel`.
- Uses the existing immutable recording-support configuration in `core/config` without adding a new configuration property.
- Exercises the existing runtime command publication and commandlog subscriber path without changing command DTO or persistence schemas.
- Adds metamodel-level coverage and focused runtime or commandlog compatibility coverage.
