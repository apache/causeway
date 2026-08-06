## ADDED Requirements

### Requirement: Unified manager supplies participant-reachability context
The unified manager SHALL supply each replayable command it creates with the manager instance's current baseline, limit, eligible non-excluded command order, recording-support state, domain-service classification, and reference-data classifiers. For a command at a given interaction id, only recorded results before that command in the same bounded order SHALL be available as prior knowledge. Replay state alone MUST NOT remove an otherwise eligible, non-excluded earlier result from this context. Manager mementos and repository entries MUST remain unchanged.

#### Scenario: Manager ordering controls prior knowledge
- **GIVEN** two eligible non-excluded commands occur at or after the manager baseline
- **WHEN** the first records a bookmark used by the second
- **THEN** the manager context makes that bookmark available to the second but not vice versa

#### Scenario: Replayed result remains prior knowledge
- **GIVEN** an eligible earlier command has replay state `OK` and a recorded result bookmark
- **WHEN** a later command's participants are evaluated
- **THEN** that recorded result is available as prior knowledge

#### Scenario: Context does not change manager identity
- **WHEN** the manager supplies reachability context
- **THEN** its baseline-and-limit memento remains unchanged

## MODIFIED Requirements

### Requirement: Fallback presentation exposes P2 review metadata only
The manager fallback layout SHALL expose baseline and limit with their state controls and SHALL present all four collections as sequence and replay review surfaces. Manager collection tables SHALL identify commands using interaction id, timestamp, member, replay state, result presence, and known-participants status, with known participants immediately after result presence. The layout MUST NOT expose redesigned export/import actions, workflow mutation, replay gates, or background-gate controls.

#### Scenario: Unified manager layout includes R2 feedback
- **WHEN** the manager is rendered from fallback layout metadata
- **THEN** baseline, limit, all four collections, the P1 identification columns, and R2 known-participants status are visible
- **AND** controls owned by later reconciliation slices are absent
