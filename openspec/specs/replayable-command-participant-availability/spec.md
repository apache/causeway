# replayable-command-participant-availability Specification

## Purpose
Define when replayable command participant rows expose locally resolved target, argument, and result objects.

## Requirements
### Requirement: Target participants expose locally resolved target objects
A replayable command participant with role `TARGET` SHALL expose its target object when its actual bookmark resolves locally.
A replayable command participant with role `TARGET` MUST remain optional and SHALL expose no target object when the applicable bookmark cannot be resolved locally.
This target availability SHALL be independent of the owning command's replay state.
This target availability MUST NOT change the command replay state.

#### Scenario: Undefined command target is available
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its target participant has actual bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the target object for `demoCustomer:1` is returned

#### Scenario: Pending command target is available
- **GIVEN** a replayable command has replay state `PENDING`
- **AND** its target participant has actual bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the target object for `demoCustomer:1` is returned

#### Scenario: Unresolvable target remains absent
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its target participant has actual bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the participant target property
- **THEN** no target object is returned

### Requirement: Reference argument participants expose locally resolved argument objects
A replayable command participant with role `PARAMETER` SHALL expose its argument object when its actual bookmark resolves locally.
A replayable command participant with role `PARAMETER` MUST remain optional and SHALL expose no argument object when the applicable bookmark cannot be resolved locally.
This argument availability SHALL be independent of the owning command's replay state.
This argument availability MUST NOT change the command replay state.

#### Scenario: Undefined command reference argument is available
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its parameter participant is named `customer`
- **AND** the participant actual bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant argument property
- **THEN** the argument object for `demoCustomer:1` is returned

#### Scenario: Failed command reference argument is available
- **GIVEN** a replayable command has replay state `FAILED`
- **AND** its parameter participant is named `customer`
- **AND** the participant actual bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant argument property
- **THEN** the argument object for `demoCustomer:1` is returned

#### Scenario: Unresolvable recorded argument remains absent
- **GIVEN** a replayable command has replay state `OK`
- **AND** its parameter participant is named `customer`
- **AND** the participant actual bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the participant argument property
- **THEN** no argument object is returned

### Requirement: Result participants expose locally resolved result objects
A replayable command participant with role `RESULT` SHALL expose its result object when its actual bookmark resolves locally.
A replayable command participant with role `RESULT` MUST remain optional and SHALL expose no result object when the applicable bookmark cannot be resolved locally.
This result availability SHALL be independent of the owning command's replay state.
This result availability MUST NOT change the command replay state.

#### Scenario: Result participant is available
- **GIVEN** a replayable command has a result participant
- **AND** the participant actual bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant result property
- **THEN** the result object for `demoCustomer:1` is returned

#### Scenario: Unresolvable result remains absent
- **GIVEN** a replayable command has a result participant
- **AND** the participant actual bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the participant result property
- **THEN** no result object is returned
