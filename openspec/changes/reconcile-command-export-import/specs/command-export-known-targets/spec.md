## ADDED Requirements

### Requirement: Known participants form the implicit export sequence
The system SHALL derive the unified manager's implicit export sequence from `commandsInSequence` entries whose contextual `knownParticipants` value is `true`. Entries whose value is `false` MUST NOT be emitted by sequence export. The implicit sequence SHALL retain manager order, and deriving or exporting it MUST NOT change any command replay state or manager state. Sequence export SHALL be unavailable when the implicit sequence is empty.

#### Scenario: Known command is implicitly exported
- **GIVEN** command `A` appears in manager order with known participants
- **WHEN** the manager exports its sequence
- **THEN** command `A` is present in the YAML in that order

#### Scenario: Unknown command is omitted
- **GIVEN** command `B` appears in `commandsInSequence` with an unknown target or reference parameter
- **WHEN** the manager exports its sequence
- **THEN** command `B` is not present in the YAML

#### Scenario: Empty implicit sequence disables export
- **GIVEN** no command in `commandsInSequence` has known participants
- **WHEN** sequence export availability is evaluated
- **THEN** export is disabled with a message explaining that no command has known participants

#### Scenario: Export selection is observational
- **GIVEN** a known command has replay state `UNDEFINED`
- **WHEN** the implicit sequence is derived and exported
- **THEN** the command retains replay state `UNDEFINED`
- **AND** the manager retains its baseline and limit
