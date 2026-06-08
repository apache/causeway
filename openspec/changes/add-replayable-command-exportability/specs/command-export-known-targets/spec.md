## ADDED Requirements

### Requirement: Exportability indicator uses export validation context
When command-log recording support is `ENABLED`, the exportability property for replayable commands in the export manager SHALL use the same known target and known reference-parameter rules as the export action.
When command-log recording support is `DISABLED`, the exportability property MUST NOT report a command as non-exportable merely because its target or reference parameters are unknown to the dotted-path export validation rule.
The exportability property SHALL evaluate target and reference-parameter knowledge using the export manager baseline and current command export ordering.
The exportability property SHALL treat only commands earlier than or equal to the evaluated command as available for validating that command.
The exportability property MUST NOT require the user to invoke export before receiving this validation feedback.

#### Scenario: Earlier result makes current command exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier command at or after the baseline has result bookmark `demoCustomer:1`
- **AND** a later replayable action command targets bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the later replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`

#### Scenario: Unknown target makes current command non-exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not a menu service root
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

#### Scenario: Disabled recording support does not mark unknown target non-exportable
- **GIVEN** command-log recording support is `DISABLED`
- **AND** an export manager baseline is set
- **AND** a replayable action command targets bookmark `demoCustomer:1`
- **AND** the target bookmark `demoCustomer:1` is not known in the baseline-bounded export sequence
- **WHEN** the system computes exportability for the replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is not `false` merely because of the unknown target validation rule
