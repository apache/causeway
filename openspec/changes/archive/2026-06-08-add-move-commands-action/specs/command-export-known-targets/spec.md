## MODIFIED Requirements

### Requirement: Export target knowledge follows baseline-bounded export order
The export manager SHALL evaluate target knowledge using the same command ordering that command export uses for replayable sequences.
The export manager SHALL use current command timestamps when evaluating baseline-bounded export order.
When command timestamps have been changed by an export-manager command move action, the changed timestamps SHALL determine whether a command result is earlier or later for target and reference-parameter validation.
A command result SHALL make a bookmark known only for selected commands that occur later in that ordering.
A command result MUST NOT make the same bookmark known for an earlier selected command or for another selected command whose ordering is not after the result-producing command.
A command before the export manager baseline MUST NOT make a target known for the selected export sequence.

#### Scenario: Later result does not validate earlier selected target
- **GIVEN** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** a later selected command in export order has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system rejects the earlier selected command because the target was not known at that point in the export sequence

#### Scenario: Earlier result validates later selected target
- **GIVEN** an export manager baseline is set
- **AND** a command with result bookmark `demoCustomer:1` appears earlier in export order and at or after the baseline
- **WHEN** a selected later action command targets bookmark `demoCustomer:1`
- **THEN** the export manager accepts the later command for export

#### Scenario: Result before baseline does not validate selected target
- **GIVEN** an export manager baseline is set
- **AND** a command before the baseline has result bookmark `demoCustomer:1`
- **WHEN** a selected later action command targets bookmark `demoCustomer:1`
- **THEN** the export manager rejects the selected command unless another command at or after the baseline establishes `demoCustomer:1`

#### Scenario: Moved finder result validates later selected target
- **GIVEN** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** a selected finder command has result bookmark `demoCustomer:1`
- **AND** the finder command originally occurs later than the action command
- **WHEN** the user moves the finder command before the action command using the export manager move action
- **AND** the export manager validates the selected command sequence
- **THEN** the export manager accepts the action command target as known

#### Scenario: Moved navigation result validates later selected reference parameter
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **AND** a selected navigation command has result bookmark `demoCustomer:1`
- **AND** the navigation command originally occurs later than the action command
- **WHEN** the user moves the navigation command before the action command using the export manager move action
- **AND** the export manager validates the selected command sequence
- **THEN** the export manager accepts the action command reference parameter as known
