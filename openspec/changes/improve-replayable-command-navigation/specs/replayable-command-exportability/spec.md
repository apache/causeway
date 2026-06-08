## ADDED Requirements

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
