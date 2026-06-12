## ADDED Requirements

### Requirement: First command exportability follows export root rule
When command-log recording support is `ENABLED`, a replayable command that is first in the export-manager command ordering SHALL report exportable `true` only when its target and reference participants are export roots.
A first replayable action command targeting an ordinary domain object SHALL report exportable `false` when no prior command result establishes that object.
A first replayable property edit targeting an ordinary domain object SHALL report exportable `false` when no prior command result establishes that object.
A later replayable command targeting an ordinary domain object SHALL report exportable `true` when an earlier command result establishes that object.

#### Scenario: First replayable command on domain service is exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable action command targets a menu service annotated with `@DomainService`
- **WHEN** the system computes exportability for that replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`

#### Scenario: First replayable command on ordinary domain object is not exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable action command targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **WHEN** the system computes exportability for that replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `false`

#### Scenario: First replayable property edit on ordinary domain object is not exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable property edit targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **WHEN** the system computes exportability for that replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `false`

#### Scenario: Later replayable command on earlier result is exportable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier replayable command has result bookmark `demoCustomer:1`
- **AND** a later replayable command targets bookmark `demoCustomer:1`
- **WHEN** the system computes exportability for the later replayable command in the export manager commands collection
- **THEN** the replayable command exportability property is `true`
