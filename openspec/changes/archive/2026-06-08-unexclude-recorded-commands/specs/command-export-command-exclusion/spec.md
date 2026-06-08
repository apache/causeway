## ADDED Requirements

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
