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
