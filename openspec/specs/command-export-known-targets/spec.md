# command-export-known-targets Specification

## Purpose

Defines command export known target validation rules.
## Requirements
### Requirement: Export validates known action targets
When command-log recording support is `ENABLED`, the export manager SHALL validate the target of every selected action command before creating export YAML.
When command-log recording support is `DISABLED`, the export manager MUST NOT require selected action command targets to be known by the dotted-path export validation rule.
When validation applies, the export manager SHALL treat an action target as known only when the target is an export root or when the target bookmark was produced as the result of an earlier command in the baseline-bounded exportable sequence.
When validation applies and a selected action target is unknown, the export manager MUST prevent the export from occurring.
The validation message SHALL identify the failing command and SHALL state that the target is unknown for command export.
The validation message SHALL indicate that a navigation or finder action returning the target must be included earlier in the exportable sequence.
The system MUST NOT block additional commands while recording merely because their targets are not yet known.

#### Scenario: Root service action is accepted for export
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager has a selected action command whose target is a menu service annotated with `@DomainService`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the command as a root action for the export sequence

#### Scenario: Action on previously returned target is accepted for export
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **AND** a selected later action command targets bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the later command for export

#### Scenario: Action on unknown target is rejected for export when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not a menu service root
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system prevents the export from occurring
- **AND** the validation message identifies the selected command that targets `demoCustomer:1`

#### Scenario: Action on unknown target is accepted for export when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not a menu service root
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system does not reject the selected command merely because the target is unknown to the dotted-path export validation rule

#### Scenario: Recording is not blocked by unknown target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** no earlier command has result bookmark `demoCustomer:1`
- **WHEN** a user invokes an action command that targets bookmark `demoCustomer:1`
- **THEN** the system does not reject the action merely because command export might later find the target unknown

### Requirement: Export roots are menu domain services
The export manager SHALL treat actions invoked on menu services annotated with `@DomainService` as root actions for command export.
Root actions SHALL be valid even when no earlier command result references the service target.
Root action results SHALL establish known target bookmarks for later selected commands when the command log entry stores a non-null result bookmark.
The export manager MUST NOT treat ordinary persisted domain objects as roots merely because their bookmarks can be resolved locally.

#### Scenario: Service finder establishes a known export target
- **GIVEN** an export manager baseline is set
- **AND** a menu service finder action at or after the baseline returns bookmark `demoCustomer:1`
- **WHEN** a selected later command targets bookmark `demoCustomer:1`
- **THEN** bookmark `demoCustomer:1` is known for command export

#### Scenario: Locally resolvable object is not an export root
- **GIVEN** an export manager baseline is set
- **AND** bookmark `demoCustomer:1` resolves to a local domain object
- **AND** no earlier command at or after the baseline produced `demoCustomer:1`
- **WHEN** a selected action command targets bookmark `demoCustomer:1`
- **THEN** the export manager rejects the selected sequence because local resolvability does not make the target an export root

### Requirement: Export target knowledge follows baseline-bounded export order
The export manager SHALL evaluate target knowledge using the same command ordering that command export uses for replayable sequences.
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

### Requirement: Export validates known action reference parameters
When command-log recording support is `ENABLED`, the export manager SHALL validate every selected action command reference parameter before creating export YAML.
When command-log recording support is `DISABLED`, the export manager MUST NOT require selected action command reference parameters to be known by the dotted-path export validation rule.
When validation applies, the export manager SHALL treat an action reference parameter as known only when the parameter bookmark is an export root or when the parameter bookmark was produced as the result of an earlier command in the baseline-bounded exportable sequence.
When validation applies and a selected action reference parameter is unknown, the export manager MUST prevent the export from occurring.
The validation message SHALL identify the failing command, the reference parameter name, and the unknown parameter bookmark.
The validation message SHALL indicate that a navigation or finder action returning the parameter object must be included earlier in the exportable sequence.
The system MUST NOT block additional commands while recording merely because their reference parameters are not yet known.

#### Scenario: Action with previously returned reference parameter is accepted for export
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **AND** a selected later action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the later command for export

#### Scenario: Action with root service reference parameter is accepted for export
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager has a selected action command with reference parameter `menu` whose bookmark identifies a menu service annotated with `@DomainService`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the reference parameter as a root participant for the export sequence

#### Scenario: Action with unknown reference parameter is rejected for export when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **AND** the parameter bookmark `demoCustomer:1` is not a menu service root
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system prevents the export from occurring
- **AND** the validation message identifies the selected command, parameter `customer`, and bookmark `demoCustomer:1`

#### Scenario: Action with unknown reference parameter is accepted for export when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **AND** the parameter bookmark `demoCustomer:1` is not a menu service root
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system does not reject the selected command merely because the reference parameter is unknown to the dotted-path export validation rule

#### Scenario: Later result does not validate earlier reference parameter
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **AND** a later selected command in export order has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system rejects the earlier selected command because the parameter was not known at that point in the export sequence

#### Scenario: Result before baseline does not validate selected reference parameter
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a command before the baseline has result bookmark `demoCustomer:1`
- **AND** a selected later action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the export manager rejects the selected command unless another command at or after the baseline establishes `demoCustomer:1`

#### Scenario: Recording is not blocked by unknown reference parameter
- **GIVEN** command-log recording support is `ENABLED`
- **AND** no earlier command has result bookmark `demoCustomer:1`
- **WHEN** a user invokes an action command with reference parameter `customer` set to bookmark `demoCustomer:1`
- **THEN** the system does not reject the action merely because command export might later find the parameter unknown

#### Scenario: Non-reference parameters are not export path participants
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has scalar parameter `name` with value `Alice`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the scalar parameter does not need to be known as an export target

