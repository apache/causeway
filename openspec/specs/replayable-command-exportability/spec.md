# Replayable Command Exportability Specification

## Purpose

Define contextual participant-reachability feedback for replayable commands and its presentation in command review tables.

## Requirements

### Requirement: Replayable command exposes known-participants state
A replayable command SHALL expose a non-persisted Boolean `knownParticipants` property. In unified-manager context with command-log recording support enabled, it SHALL be `true` exactly when every target and reference-valued parameter is known at that command's current point in manager order. It SHALL be `false` when any participant is unknown, recording support is disabled, the command is absent from the bounded sequence, or manager context is unavailable. Reading the property MUST NOT resolve bookmarked objects, persist state, or change replay state.

#### Scenario: Manager command with known participants reports true
- **GIVEN** recording support is enabled and a manager-created replayable command has only root, reference-data, or earlier-result participants
- **WHEN** `knownParticipants` is read
- **THEN** it is `true`

#### Scenario: Unknown participant reports false
- **GIVEN** a manager-created replayable command has an unknown target or reference parameter
- **WHEN** `knownParticipants` is read
- **THEN** it is `false`

#### Scenario: Standalone command reports false
- **WHEN** a replayable command is reconstructed from its identity memento outside unified-manager context
- **THEN** `knownParticipants` is `false`

#### Scenario: Disabled recording reports false
- **GIVEN** command-log recording support is disabled
- **WHEN** `knownParticipants` is read in unified-manager context
- **THEN** it is `false`

#### Scenario: Computation leaves replay state unchanged
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **WHEN** `knownParticipants` is computed
- **THEN** its replay state remains `UNDEFINED`

### Requirement: Known-participants follows result presence in tables
When a replayable-command table displays result presence and participant reachability, `knownParticipants` SHALL appear immediately after `hasResult`. Column ordering MUST NOT affect the computed value or make the contextual property persistent.

#### Scenario: Manager table shows reachability after result presence
- **WHEN** a unified-manager command table is rendered
- **THEN** `hasResult` is followed by `knownParticipants`

#### Scenario: Ordering does not alter the value
- **GIVEN** a replayable command has known participants
- **WHEN** its table columns are ordered
- **THEN** `knownParticipants` remains `true`
