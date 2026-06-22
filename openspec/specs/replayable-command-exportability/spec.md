# replayable-command-exportability Specification

## Purpose
Describe how a replayable command reports whether its target and reference parameters are known in the current command manager context.

## Requirements
### Requirement: Replayable command exposes known-participants state
A replayable command SHALL expose a Boolean known-participants property.
When command-manager context is available, the property SHALL be `true` if the command uses only known target and reference-parameter participants at its current point in the command ordering.
When command-manager context is available, the property SHALL be `false` if the command has an unknown target or unknown reference-parameter participant at its current point in the command ordering.
When command-manager context is not available, the property SHALL be `false`.
The known-participants property MUST NOT be persisted on the command log entry.
The known-participants property MUST NOT change the command replay state.

#### Scenario: Command with known participants reports true in command manager
- **GIVEN** an export manager baseline is set
- **AND** a replayable action command is constructed while rendering the command manager sequence
- **AND** the command's target and reference parameters are known at that point in the command ordering
- **WHEN** the system reads the replayable command known-participants property
- **THEN** the property is `true`

#### Scenario: Command with unknown participants reports false in command manager
- **GIVEN** an export manager baseline is set
- **AND** a replayable action command is constructed while rendering the command manager sequence
- **AND** the command has an unknown target or unknown reference parameter at that point in the command ordering
- **WHEN** the system reads the replayable command known-participants property
- **THEN** the property is `false`

#### Scenario: Known-participants is false outside command manager context
- **GIVEN** a replayable command is constructed outside a command manager collection that installs a participant tracker
- **WHEN** the system reads the replayable command known-participants property
- **THEN** the property is `false`

#### Scenario: Known-participants does not modify replay state
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** the system computes the replayable command known-participants property
- **WHEN** the command log entry is inspected
- **THEN** the replay state remains `UNDEFINED`

### Requirement: First command known-participants follows export root rule
When command-log recording support is `ENABLED`, a replayable command that is first in the command ordering SHALL report known participants `true` only when its target and reference participants are export roots or reference data.
A first replayable action command targeting an ordinary domain object SHALL report known participants `false` when no prior command result establishes that object.
A first replayable property edit targeting an ordinary domain object SHALL report known participants `false` when no prior command result establishes that object.
A later replayable command targeting an ordinary domain object SHALL report known participants `true` when an earlier command result establishes that object.

#### Scenario: First replayable command on domain service has known participants
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable action command targets a menu service annotated with `@DomainService`
- **WHEN** the system computes known participants for that replayable command in the command manager sequence
- **THEN** the replayable command known-participants property is `true`

#### Scenario: First replayable command on ordinary domain object has unknown participants
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable action command targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **WHEN** the system computes known participants for that replayable command in the command manager sequence
- **THEN** the replayable command known-participants property is `false`

#### Scenario: First replayable property edit on ordinary domain object has unknown participants
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** the first replayable property edit targets bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` is not a menu service root
- **WHEN** the system computes known participants for that replayable command in the command manager sequence
- **THEN** the replayable command known-participants property is `false`

#### Scenario: Later replayable command on earlier result has known participants
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an export manager baseline is set
- **AND** an earlier replayable command has result bookmark `demoCustomer:1`
- **AND** a later replayable command targets bookmark `demoCustomer:1`
- **WHEN** the system computes known participants for the later replayable command in the command manager sequence
- **THEN** the replayable command known-participants property is `true`

### Requirement: Known-participants appears after result presence in replayable command tables
When a replayable command table displays both result presence and known-participants state, the known-participants property SHALL appear after `hasResult`.
This ordering requirement MUST NOT change the known-participants property's value.
This ordering requirement MUST NOT make known-participants visible in object forms where it is hidden.

#### Scenario: Known-participants follows result presence in table layout
- **GIVEN** a replayable command has a `hasResult` property
- **AND** known-participants is available in the current table context
- **WHEN** the table columns are ordered
- **THEN** the known-participants column appears after the `hasResult` column

#### Scenario: Known-participants value is unchanged by column ordering
- **GIVEN** a replayable command has known-participants `true`
- **WHEN** the table columns are ordered with `hasResult` before known-participants
- **THEN** the known-participants property remains `true`
