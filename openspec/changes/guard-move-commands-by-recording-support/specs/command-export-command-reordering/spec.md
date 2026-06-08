## MODIFIED Requirements

### Requirement: Export manager moves selected commands after a target command
When command-log recording support is `ENABLED`, the export manager SHALL provide an action to move one or more selected exportable commands after a target command.
When command-log recording support is `DISABLED`, the export manager MUST disable command movement.
The action SHALL operate only on commands at or after the export manager baseline.
The target command choices SHALL include commands at or after the export manager baseline.
The target command choices MUST exclude every command selected for movement.
The action MUST reject execution when no commands are selected.
The action MUST reject execution when the target command is missing.
The action MUST reject execution when the target command is one of the selected commands.
The action MUST reject execution when any selected command or target command is outside the baseline-bounded exportable command set.

#### Scenario: Target choices exclude selected commands
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** commands `A`, `B`, and `C` are exportable after the baseline
- **AND** commands `A` and `B` are selected for movement
- **WHEN** the system provides target choices for the move action
- **THEN** command `C` is offered as a target choice
- **AND** commands `A` and `B` are not offered as target choices

#### Scenario: Cannot move without a selection
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **WHEN** the user invokes the move action with no selected commands
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move after a selected command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is selected for movement
- **WHEN** the user invokes the move action using command `A` as the target command
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Cannot move commands outside the baseline-bounded set
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** command `A` is before the baseline
- **WHEN** a caller bypasses the UI and invokes the move action with command `A` selected
- **THEN** the system rejects the invocation
- **AND** no command timestamps are changed

#### Scenario: Move action is disabled when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **WHEN** the framework evaluates the move action
- **THEN** the move action is disabled
