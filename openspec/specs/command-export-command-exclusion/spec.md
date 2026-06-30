# command-export-command-exclusion Specification

## Purpose
Define how the command manager excludes, restores, and deletes command log entries from the replay/export workflow.

## Requirements
### Requirement: Export manager excludes selected commands in sequence
When command-log recording support is `ENABLED`, the export manager SHALL provide an `excludeCommands` action associated with `commandsInSequence`.
The action SHALL operate only on commands in the current baseline-bounded `commandsInSequence` collection.
The action SHALL set each selected command log entry's replay state to `EXCLUDED`.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside `commandsInSequence`.
The action MUST reject execution when command-log recording support is `DISABLED`.
The action MUST NOT delete command log entries.
The action MUST return the current export manager so the refreshed view can show updated collections.

#### Scenario: Selected commands become excluded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A` and `B` are in `commandsInSequence`
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

#### Scenario: Cannot exclude command outside command sequence
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes the exclude action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Export manager defaults exclusion selection to commands with unknown participants
When command-log recording support is `ENABLED`, the export manager exclusion action SHALL default its selected commands parameter to `commandsInSequence` entries whose known-participants property is `false`.
The default selection MUST NOT include commands whose known-participants property is `true`.
The default selection MUST NOT include commands outside `commandsInSequence`.
Users SHALL still be able to manually choose commands from `commandsInSequence` to exclude.

#### Scenario: Command with unknown participants is selected by default
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in `commandsInSequence`
- **AND** command `A` has known-participants value `false`
- **WHEN** the system provides defaults for the exclusion action selected commands parameter
- **THEN** command `A` is selected by default

#### Scenario: Command with known participants is not selected by default
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in `commandsInSequence`
- **AND** command `A` has known-participants value `true`
- **WHEN** the system provides defaults for the exclusion action selected commands parameter
- **THEN** command `A` is not selected by default

### Requirement: Command exclusion requires recording support
The export manager exclusion action SHALL be enabled only when command-log recording support is `ENABLED`.
When command-log recording support is `DISABLED`, the export manager MUST disable command exclusion.
If invocation is attempted while command-log recording support is `DISABLED`, the system MUST reject the invocation and MUST NOT change any command replay state.

#### Scenario: Exclude action enabled when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** at least one command is in `commandsInSequence`
- **WHEN** the framework evaluates the exclude action
- **THEN** the exclude action is enabled

#### Scenario: Exclude action disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the exclude action
- **THEN** the exclude action is disabled

#### Scenario: Direct exclusion invocation is guarded when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in `commandsInSequence`
- **WHEN** a caller bypasses the UI and invokes the exclude action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Export manager restores selected excluded commands to a chosen replay state
When command-log recording support is `ENABLED`, the export manager SHALL provide an `unexcludeCommands` action associated with `excluded`.
The action SHALL operate only on commands at or after the export manager baseline whose replay state is `EXCLUDED`.
The action SHALL require a non-excluded replay state parameter.
The allowed replay state choices SHALL be all replay states except `EXCLUDED`.
The action SHALL set each selected command log entry's replay state to the chosen non-excluded replay state.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside `excluded`.
The action MUST NOT delete command log entries.
The action MUST return the current export manager so the refreshed view can show updated collections.

#### Scenario: Selected excluded commands become recorded commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A` and `B` are in `excluded`
- **WHEN** the user restores commands `A` and `B` with replay state `UNDEFINED`
- **THEN** command `A` has replay state `UNDEFINED`
- **AND** command `B` has replay state `UNDEFINED`
- **AND** command log entries for `A` and `B` still exist

#### Scenario: Selected excluded command can be restored as pending replay work
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in `excluded`
- **WHEN** the user restores command `A` with replay state `PENDING`
- **THEN** command `A` has replay state `PENDING`
- **AND** command `A` is no longer eligible for `excluded`

#### Scenario: Cannot restore without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the restore action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command replay states are changed

#### Scenario: Cannot restore command outside excluded collection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is in `commandsInSequence`
- **WHEN** a caller bypasses the UI and invokes the restore action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** command `A` does not change replay state

### Requirement: Command restoration is disabled without recording support
The export manager restore action SHALL be enabled only when command-log recording support is `ENABLED`.
When command-log recording support is `DISABLED`, the export manager MUST disable command restoration.
Direct invocation of the restore action SHALL still validate only the selected commands and requested replay state.
Direct invocation while command-log recording support is `DISABLED` is outside the UI-supported workflow.

#### Scenario: Restore action enabled when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** at least one command is in `excluded`
- **WHEN** the framework evaluates the restore action
- **THEN** the restore action is enabled

#### Scenario: Restore action disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the restore action
- **THEN** the restore action is disabled

### Requirement: Export manager deletes selected excluded commands
The export manager SHALL provide a `deleteCommands` action associated with `excluded`.
The action SHALL allow one or more selected commands to be deleted in one invocation.
The action SHALL use `excluded` as its selected-command choices source.
The action SHALL also provide explicit selected-command choices from the current `excluded` collection.
The action SHALL delete the backing command log entries for selected excluded commands.
The action SHALL return the current export manager so the refreshed view can show updated collections.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when any selected command is outside the current baseline-bounded `excluded` collection.
The action MUST NOT delete active commands from `commandsInSequence`.

#### Scenario: Selected excluded commands are deleted
- **GIVEN** an export manager baseline is set
- **AND** commands `A` and `B` are in `excluded`
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
- **AND** command `A` is in `commandsInSequence`
- **WHEN** a caller bypasses the UI and invokes `deleteCommands` with command `A` selected
- **THEN** the system rejects the invocation
- **AND** the command log entry for `A` is not deleted

#### Scenario: Selected command choices come from excluded collection
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in `excluded`
- **AND** command `B` is in `commandsInSequence`
- **WHEN** the system provides choices for the `deleteCommands` selected commands parameter
- **THEN** command `A` is offered as a choice
- **AND** command `B` is not offered as a choice

### Requirement: Delete excluded commands action is disabled when empty
The export manager `deleteCommands` action SHALL be disabled when the `excluded` collection is empty.
The action SHALL be enabled when the `excluded` collection is not empty.

#### Scenario: Delete action disabled with no excluded commands
- **GIVEN** an export manager baseline is set
- **AND** the `excluded` collection is empty
- **WHEN** the framework evaluates the `deleteCommands` action
- **THEN** the action is disabled

#### Scenario: Delete action enabled with excluded commands
- **GIVEN** an export manager baseline is set
- **AND** the `excluded` collection contains command `A`
- **WHEN** the framework evaluates the `deleteCommands` action
- **THEN** the action is enabled
