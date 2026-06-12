# replayable-command-exportability Specification

## Purpose

Indicates whether a replayable command is currently exportable in the command export manager context.
## Requirements
### Requirement: Replayable command exposes exportability state
A replayable command SHALL expose a nullable Boolean exportability property.
When export-manager context is available, the property SHALL be `true` if the command is exportable at its current point in the export-manager command ordering.
When export-manager context is available, the property SHALL be `false` if the command is not exportable at its current point in the export-manager command ordering.
When export-manager context is not available, the property SHALL be `null`.
The exportability property MUST NOT be persisted on the command log entry.
The exportability property MUST NOT change the command replay state.

#### Scenario: Exportable command reports true in export manager
- **GIVEN** an export manager baseline is set
- **AND** a replayable action command is constructed while rendering the export manager commands collection
- **AND** the command's target and reference parameters are known at that point in the export-manager command ordering
- **WHEN** the system reads the replayable command exportability property
- **THEN** the property is `true`

#### Scenario: Non-exportable command reports false in export manager
- **GIVEN** an export manager baseline is set
- **AND** a replayable action command is constructed while rendering the export manager commands collection
- **AND** the command has an unknown target or unknown reference parameter at that point in the export-manager command ordering
- **WHEN** the system reads the replayable command exportability property
- **THEN** the property is `false`

#### Scenario: Exportability is unknown outside export manager context
- **GIVEN** a replayable command is constructed outside the command export manager commands collection
- **WHEN** the system reads the replayable command exportability property
- **THEN** the property is `null`

#### Scenario: Exportability does not modify replay state
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** the system computes the replayable command exportability property
- **WHEN** the command log entry is inspected
- **THEN** the replay state remains `UNDEFINED`

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

### Requirement: Exportability appears after result presence in replayable command tables
When a replayable command table displays both result presence and exportability, the exportability property SHALL appear after `hasResult`.
This ordering requirement MUST NOT change the exportability property's value.
This ordering requirement MUST NOT make exportability visible in object forms where it is hidden.

#### Scenario: Exportability follows result presence in table layout
- **GIVEN** a replayable command has a `hasResult` property
- **AND** exportability is available in the current table context
- **WHEN** the table columns are ordered
- **THEN** the exportability column appears after the `hasResult` column

#### Scenario: Exportability value is unchanged by column ordering
- **GIVEN** a replayable command has exportability `true`
- **WHEN** the table columns are ordered with `hasResult` before exportability
- **THEN** the exportability property remains `true`

