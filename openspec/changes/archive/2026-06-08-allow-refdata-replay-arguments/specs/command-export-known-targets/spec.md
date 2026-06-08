## MODIFIED Requirements

### Requirement: Export validates known action targets
When command-log recording support is `ENABLED`, the export manager SHALL validate the target of every selected action command before creating export YAML.
When command-log recording support is `DISABLED`, the export manager MUST NOT require selected action command targets to be known by the dotted-path export validation rule.
When validation applies, the export manager SHALL treat an action target as known when the target is an export root, when the target is application-declared replay reference data, or when the target bookmark was produced as the result of an earlier command in the baseline-bounded exportable sequence.
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

#### Scenario: Action on reference-data target is accepted without prior result
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCategory:STD`
- **AND** a registered replay reference-data SPI implementation classifies `demoCategory:STD` as reference data
- **AND** no earlier command at or after the baseline has result bookmark `demoCategory:STD`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the selected command for export

#### Scenario: Action on unknown target is rejected when no reference-data classifier accepts it
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not a menu service root
- **AND** no registered replay reference-data SPI implementation classifies `demoCustomer:1` as reference data
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

### Requirement: Export validates known action reference parameters
When command-log recording support is `ENABLED`, the export manager SHALL validate every selected action command reference parameter before creating export YAML.
When command-log recording support is `DISABLED`, the export manager MUST NOT require selected action command reference parameters to be known by the dotted-path export validation rule.
When validation applies, the export manager SHALL treat an action reference parameter as known when the parameter bookmark is an export root, when the parameter bookmark is application-declared replay reference data, or when the parameter bookmark was produced as the result of an earlier command in the baseline-bounded exportable sequence.
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

#### Scenario: Action with reference-data reference parameter is accepted without prior result
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `category` with bookmark `demoCategory:STD`
- **AND** a registered replay reference-data SPI implementation classifies `demoCategory:STD` as reference data
- **AND** no earlier command at or after the baseline has result bookmark `demoCategory:STD`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the selected command for export

#### Scenario: Action with unknown reference parameter is rejected when no reference-data classifier accepts it
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a selected action command has reference parameter `customer` with bookmark `demoCustomer:1`
- **AND** the parameter bookmark `demoCustomer:1` is not a menu service root
- **AND** no registered replay reference-data SPI implementation classifies `demoCustomer:1` as reference data
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

### Requirement: Exportability indicator uses export validation context
When command-log recording support is `ENABLED`, the exportability property for replayable commands in the export manager SHALL use the same known target and known reference-parameter rules as the export action.
When command-log recording support is `DISABLED`, the exportability property SHALL be `null` because exportability is undefined without command-log recording support.
The exportability property SHALL evaluate target and reference-parameter knowledge using the export manager baseline and current command export ordering.
The exportability property SHALL treat only commands earlier than or equal to the evaluated command as available for validating that command.
The exportability property SHALL treat application-declared replay reference data as known for the evaluated command even when no earlier command produced that bookmark.
The exportability property MUST NOT require the user to invoke export before receiving this validation feedback.

#### Scenario: Earlier result makes current command exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **AND** a later replayable action command targets bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the later replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`

#### Scenario: Reference-data target makes current command exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCategory:STD`
- **AND** a registered replay reference-data SPI implementation classifies `demoCategory:STD` as reference data
- **AND** no earlier command at or after the baseline has result bookmark `demoCategory:STD`
- **WHEN** the system computes exportability for the replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`

#### Scenario: Reference-data parameter makes current command exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command has reference parameter `category` with bookmark `demoCategory:STD`
- **AND** a registered replay reference-data SPI implementation classifies `demoCategory:STD` as reference data
- **AND** no earlier command at or after the baseline has result bookmark `demoCategory:STD`
- **WHEN** the system computes exportability for the replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`

#### Scenario: Unknown target makes current command non-exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not a menu service root
- **AND** no registered replay reference-data SPI implementation classifies `demoCustomer:1` as reference data
- **AND** no earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `false`

#### Scenario: Later result does not make current command exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCustomer:1`
- **AND** a later command in export order has result bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the replayable action command in the export manager commands collection
- **THEN** the replayable command exportability property is `false`

#### Scenario: Earlier invalid command does not poison later command exportability
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier replayable action command is not exportable because it targets unknown bookmark `demoCustomer:1`
- **AND** a following finder command returns bookmark `demoCustomer:1`
- **AND** a later replayable action command targets bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the later replayable action command in the export manager commands collection
- **THEN** the later replayable command exportability property is `true`

#### Scenario: Disabled recording support makes exportability undefined
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the replayable command
- **THEN** the replayable command exportability property is `null`

### Requirement: First export command requires an export root target
When command-log recording support is `ENABLED`, the first selected command in an export sequence SHALL be valid when its target is an export root or application-declared replay reference data.
A first selected command targeting an ordinary domain object MUST be rejected unless the target is application-declared replay reference data or an earlier selected command in export order establishes that object as a known result.
The validation message SHALL identify the first selected command and SHALL state that the target is unknown for command export.
This rule MUST apply to action invocations and property edits.
This rule MUST follow the baseline-bounded export ordering used by command export validation.

#### Scenario: First selected command on domain service is accepted
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected command targets a menu service annotated with `@DomainService`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the first command target as an export root

#### Scenario: First selected command on reference data is accepted
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected action command targets bookmark `demoCategory:STD`
- **AND** a registered replay reference-data SPI implementation classifies `demoCategory:STD` as reference data
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the first command target as known reference data

#### Scenario: First selected action command on ordinary domain object is rejected
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected action command targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **AND** no registered replay reference-data SPI implementation classifies `demoCustomer:1` as reference data
- **AND** no earlier selected command in export order has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system prevents the export from occurring
- **AND** the validation message identifies the first selected command target `demoCustomer:1`

#### Scenario: First selected property edit on ordinary domain object is rejected
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected property edit targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **AND** no registered replay reference-data SPI implementation classifies `demoCustomer:1` as reference data
- **AND** no earlier selected command in export order has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system prevents the export from occurring
- **AND** the validation message identifies the first selected command target `demoCustomer:1`

#### Scenario: Earlier finder allows later domain object command
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected action command targets a menu service annotated with `@DomainService`
- **AND** the first selected action command has result bookmark `demoCustomer:1`
- **AND** a later selected action command targets bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the later command target as known
