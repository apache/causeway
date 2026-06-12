## ADDED Requirements

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
