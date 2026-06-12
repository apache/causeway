# command-export-command-exclusion Specification

## Purpose
TBD - created by archiving change exclude-recorded-commands. Update Purpose after archive.
## Requirements
### Requirement: Export manager excludes selected active commands
When command-log recording support is `ENABLED`, the export manager SHALL provide an action to exclude one or more selected commands from the active command sequence.
The action SHALL operate only on commands at or after the export manager baseline.
The action SHALL set each selected command log entry's replay state to `EXCLUDED`.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside the active `commands` collection.
The action MUST NOT delete command log entries.
The action MUST return the current export manager so the refreshed view can show updated collections.

#### Scenario: Selected commands become excluded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A` and `B` are in the active `commands` collection
- **WHEN** the user excludes commands `A` and `B`
- **THEN** command `A` has replay state `EXCLUDED`
- **AND** command `B` has replay state `EXCLUDED`
- **AND** command log entries for `A` and `B` still exist

#### Scenario: Cannot exclude without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the exclude action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command replay states are changed

#### Scenario: Cannot exclude command outside active collection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes the exclude action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Export manager defaults exclusion selection to non-exportable commands
When command-log recording support is `ENABLED`, the export manager exclusion action SHALL default its selected commands parameter to active commands whose exportability value is `false`.
The default selection MUST NOT include active commands whose exportability value is `true`.
The default selection MUST NOT include active commands whose exportability value is `null`.
The default selection MUST NOT include commands outside the active `commands` collection.
Users SHALL still be able to manually choose active commands to exclude.

#### Scenario: Non-exportable active command is selected by default
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **AND** command `A` has exportability value `false`
- **WHEN** the system provides defaults for the exclusion action selected commands parameter
- **THEN** command `A` is selected by default

#### Scenario: Exportable active command is not selected by default
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **AND** command `A` has exportability value `true`
- **WHEN** the system provides defaults for the exclusion action selected commands parameter
- **THEN** command `A` is not selected by default

#### Scenario: Unknown exportability command is not selected by default
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **AND** command `A` has exportability value `null`
- **WHEN** the system provides defaults for the exclusion action selected commands parameter
- **THEN** command `A` is not selected by default

### Requirement: Command exclusion requires recording support
The export manager exclusion action SHALL be enabled only when command-log recording support is `ENABLED`.
When command-log recording support is `DISABLED`, the export manager MUST disable command exclusion.
If invocation is attempted while command-log recording support is `DISABLED`, the system MUST NOT change any command replay state.

#### Scenario: Exclude action enabled when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** at least one command is in the active `commands` collection
- **WHEN** the framework evaluates the exclude action
- **THEN** the exclude action is enabled

#### Scenario: Exclude action disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the exclude action
- **THEN** the exclude action is disabled

#### Scenario: Direct invocation is guarded when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **WHEN** a caller bypasses the UI and invokes the exclude action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Export manager restores selected excluded commands
When command-log recording support is `ENABLED`, the export manager SHALL provide an action associated with `excludedCommands` to restore one or more selected excluded commands to the active command sequence.
The action SHALL operate only on commands at or after the export manager baseline whose replay state is `EXCLUDED`.
The action SHALL set each selected command log entry's replay state to `UNDEFINED`.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside the `excludedCommands` collection.
The action MUST NOT delete command log entries.
The action MUST return the current export manager so the refreshed view can show updated collections.

#### Scenario: Selected excluded commands become active
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A` and `B` are in the `excludedCommands` collection
- **WHEN** the user restores commands `A` and `B`
- **THEN** command `A` has replay state `UNDEFINED`
- **AND** command `B` has replay state `UNDEFINED`
- **AND** command log entries for `A` and `B` still exist

#### Scenario: Restored command returns to active commands collection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the `excludedCommands` collection
- **WHEN** the user restores command `A`
- **THEN** command `A` is eligible for the `commands` collection
- **AND** command `A` is no longer eligible for the `excludedCommands` collection

#### Scenario: Cannot restore without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the restore action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command replay states are changed

#### Scenario: Cannot restore command outside excluded collection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **WHEN** a caller bypasses the UI and invokes the restore action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Command restoration requires recording support
The export manager restore action SHALL be enabled only when command-log recording support is `ENABLED`.
When command-log recording support is `DISABLED`, the export manager MUST disable command restoration.
If invocation is attempted while command-log recording support is `DISABLED`, the system MUST NOT change any command replay state.

#### Scenario: Restore action enabled when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** at least one command is in the `excludedCommands` collection
- **WHEN** the framework evaluates the restore action
- **THEN** the restore action is enabled

#### Scenario: Restore action disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the restore action
- **THEN** the restore action is disabled

#### Scenario: Direct invocation is guarded when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in the `excludedCommands` collection
- **WHEN** a caller bypasses the UI and invokes the restore action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Export manager deletes selected excluded commands
The export manager SHALL provide a `deleteCommands` action associated with the `excludedCommands` collection.
The action SHALL allow one or more selected commands to be deleted in one invocation.
The action SHALL use `excludedCommands` as its selected-command choices source.
The action SHALL also provide explicit selected-command choices from the current `excludedCommands` collection.
The action SHALL delete the backing command log entries for selected excluded commands.
The action SHALL return the current export manager so the refreshed view can show updated collections.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside the current baseline-bounded `excludedCommands` collection.
The action MUST NOT delete active commands from the `commands` collection.

#### Scenario: Selected excluded commands are deleted
- **GIVEN** an export manager baseline is set
- **AND** commands `A` and `B` are in the `excludedCommands` collection
- **WHEN** the user invokes `deleteCommands` with commands `A` and `B` selected
- **THEN** the command log entries for `A` and `B` are deleted
- **AND** the export manager is returned

#### Scenario: Cannot delete without a selection
- **GIVEN** an export manager baseline is set
- **WHEN** the user invokes `deleteCommands` with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command log entries are deleted

#### Scenario: Cannot delete active command by direct invocation
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in the active `commands` collection
- **WHEN** a caller bypasses the UI and invokes `deleteCommands` with command `A` selected
- **THEN** the system rejects the invocation
- **AND** the command log entry for `A` is not deleted

#### Scenario: Selected command choices come from excluded commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in the `excludedCommands` collection
- **AND** command `B` is in the active `commands` collection
- **WHEN** the system provides choices for the `deleteCommands` selected commands parameter
- **THEN** command `A` is offered as a choice
- **AND** command `B` is not offered as a choice

### Requirement: Delete excluded commands action is disabled when empty
The export manager `deleteCommands` action SHALL be disabled when the `excludedCommands` collection is empty.
The action SHALL be enabled when the `excludedCommands` collection is not empty.

#### Scenario: Delete action disabled with no excluded commands
- **GIVEN** an export manager baseline is set
- **AND** the `excludedCommands` collection is empty
- **WHEN** the framework evaluates the `deleteCommands` action
- **THEN** the action is disabled

#### Scenario: Delete action enabled with excluded commands
- **GIVEN** an export manager baseline is set
- **AND** the `excludedCommands` collection contains command `A`
- **WHEN** the framework evaluates the `deleteCommands` action
- **THEN** the action is enabled

