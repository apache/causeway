## ADDED Requirements

### Requirement: Unified manager exports a result-bearing command sequence
The unified manager SHALL provide a prototyping `exportSequence` action associated with `commandsInSequence`. The action SHALL serialize the implicit known-participant sequence in manager order as multi-document `CommandExportDto` YAML, embedding each recorded command and its optional result bookmark. It SHALL support a filename prefix defaulting to `commands`, an optional sanitized timestamp suffix derived from the first exported command, and optional export remapping. Export MUST NOT mutate command DTOs, results, timestamps, replay states, repository membership, baseline, or limit.

#### Scenario: Export emits ordered command envelopes
- **GIVEN** two known commands occur in manager order and the first has a recorded result
- **WHEN** `exportSequence` is invoked
- **THEN** the YAML contains two documents in manager order
- **AND** the first document contains its embedded command and result identity

#### Scenario: Export leaves legacy replay state unchanged
- **GIVEN** an exported command has replay state `UNDEFINED` or legacy `EXPORTED`
- **WHEN** `exportSequence` completes
- **THEN** that replay state is unchanged
- **AND** the command remains in the same manager review collections

#### Scenario: Timestamped filename is safe
- **WHEN** timestamp suffixing is enabled for sequence export
- **THEN** the output filename starts with the configured prefix
- **AND** its suffix is derived from the first exported timestamp with unsafe filename characters replaced

### Requirement: Unified manager imports replay command envelopes
The unified manager SHALL provide a prototyping, command-recording-suppressed `importCommands` action accepting `.yml` and `.yaml` content through the replay-import decoder. It SHALL persist every decoded command through `saveForReplay` and attach optional result bookmarks. The action SHALL default to moving the returned manager baseline to the oldest non-null imported command timestamp while preserving the current limit. When baseline movement is disabled, no usable timestamp exists, or no command is imported, it SHALL return the current manager state.

#### Scenario: Import stores commands and results for replay
- **WHEN** the manager imports supported YAML containing commands and result metadata
- **THEN** each command is saved with replay repository semantics
- **AND** each supplied result bookmark is attached to its corresponding entry

#### Scenario: Import moves baseline and retains limit
- **GIVEN** the manager limit is 50
- **AND** imported commands have timestamps 10:00 and 09:00
- **WHEN** import uses the default baseline movement
- **THEN** the returned manager baseline is 09:00 and its limit is 50

#### Scenario: Import can retain current baseline
- **WHEN** import disables baseline movement
- **THEN** the returned manager retains its original baseline and limit

#### Scenario: Import without usable timestamp is safe
- **WHEN** supported imported command data contains no non-null timestamp
- **THEN** the commands are still persisted
- **AND** the returned manager retains its original baseline and limit

## MODIFIED Requirements

### Requirement: Fallback presentation exposes P2 review metadata only
The manager fallback layout SHALL expose baseline and limit with their state controls and SHALL present all four collections as sequence and replay review surfaces. Manager collection tables SHALL identify commands using interaction id, timestamp, member, replay state, result presence, and known-participants status, with known participants immediately after result presence. The layout SHALL expose E1 sequence-export and replay-import actions. The layout MUST NOT expose workflow mutation, replay gates, or background-gate controls.

#### Scenario: Unified manager layout includes E1 actions
- **WHEN** the manager is rendered from fallback layout metadata
- **THEN** baseline, limit, all four collections, P1 identification columns, R2 known-participants status, and E1 export/import actions are visible
- **AND** controls owned by W1 and B1/B2 are absent
