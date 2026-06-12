## ADDED Requirements

### Requirement: Replay result mappings can be deleted from the command log menu
The system SHALL provide a command log menu action that deletes all persisted command replay result mappings.
The delete action SHALL use an `ARE_YOU_SURE` action semantic.
The delete action SHALL be idempotent so invoking it when no mappings exist leaves the system with no mappings and no error.
The delete action SHALL report the number of deleted mappings to the user.
The delete action SHALL hide when no command replay result mapping repository is available.
The delete action MUST NOT delete command log entries or imported replay commands.

#### Scenario: User deletes all replay result mappings
- **GIVEN** command replay result mappings are persisted
- **WHEN** the user confirms the delete-all replay result mappings action
- **THEN** all command replay result mappings are removed
- **AND** the user is informed that two mappings were deleted
- **AND** command log entries are not removed

#### Scenario: Delete action is hidden without repository
- **GIVEN** no command replay result mapping repository is available
- **WHEN** the command log menu is rendered
- **THEN** the delete-all replay result mappings action is hidden

#### Scenario: Delete action is idempotent when no mappings exist
- **GIVEN** no command replay result mappings are persisted
- **WHEN** the user confirms the delete-all replay result mappings action
- **THEN** no command replay result mappings are persisted
- **AND** the action completes without error

### Requirement: Command replay menu actions are ordered for replay workflow
The command log menu SHALL order the command export and replay actions before replay result mapping finder actions.
The command log menu SHALL order `exportManager` before `replayManager`.
The command log menu SHALL order replay result mapping finder actions after `replayManager`.
The command log menu SHALL order the delete-all replay result mappings action after the replay result mapping finder actions.
The ordering SHALL be expressed with `@ActionLayout(sequence)` values on the menu actions.

#### Scenario: Export manager appears before replay manager
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** `exportManager` appears before `replayManager`

#### Scenario: Replay mapping finders appear after replay manager
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** each replay result mapping finder action appears after `replayManager`

#### Scenario: Delete action appears after replay mapping finders
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** the delete-all replay result mappings action appears after the replay result mapping finder actions
