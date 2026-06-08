## ADDED Requirements

### Requirement: First export command requires an export root target
When command-log recording support is `ENABLED`, the first selected command in an export sequence SHALL be valid only when its target is an export root.
A first selected command targeting an ordinary domain object MUST be rejected unless an earlier selected command in export order establishes that object as a known result.
The validation message SHALL identify the first selected command and SHALL state that the target is unknown for command export.
This rule MUST apply to action invocations and property edits.
This rule MUST follow the baseline-bounded export ordering used by command export validation.

#### Scenario: First selected command on domain service is accepted
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected command targets a menu service annotated with `@DomainService`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system accepts the first command target as an export root

#### Scenario: First selected action command on ordinary domain object is rejected
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected action command targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **AND** no earlier selected command in export order has result bookmark `demoCustomer:1`
- **WHEN** the export manager validates the selected command sequence
- **THEN** the system prevents the export from occurring
- **AND** the validation message identifies the first selected command target `demoCustomer:1`

#### Scenario: First selected property edit on ordinary domain object is rejected
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first selected property edit targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
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
