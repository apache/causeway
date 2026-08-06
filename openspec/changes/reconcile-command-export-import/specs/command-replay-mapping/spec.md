## ADDED Requirements

### Requirement: Optional export remapping preserves recorded envelopes
When export remapping is requested, the system SHALL create a structurally independent `CommandExportDto` copy and apply the existing ordered replay-mapping lookup to every command target, populated reference-valued action parameter, and optional result bookmark. The first non-empty listener replacement SHALL be used for each identity; lookup failure SHALL be logged and later listeners SHALL still be consulted. Unmapped identities SHALL remain recorded values. Export remapping MUST NOT mutate the recorded command DTO, command-log result, replay state, or source export envelope. When export remapping is not requested, export SHALL emit recorded identities.

#### Scenario: Export remaps inputs and result together
- **GIVEN** mapping lookup replaces recorded bookmark `demo.Invoice:1` with actual bookmark `demo.Invoice:2`
- **WHEN** an export envelope uses `demo.Invoice:1` as a target, reference parameter, or result and remapping is requested
- **THEN** the exported copy uses `demo.Invoice:2` for each mapped occurrence
- **AND** the recorded envelope and command-log entry retain `demo.Invoice:1`

#### Scenario: Unmapped export identity is retained
- **WHEN** no listener supplies a replacement for a recorded export identity
- **THEN** the exported copy retains that recorded identity

#### Scenario: Listener failure permits later export mapping
- **WHEN** the first listener throws during export lookup
- **AND** a later listener supplies a replacement
- **THEN** the failure is logged
- **AND** the exported copy uses the later replacement

#### Scenario: Export remapping can be disabled
- **WHEN** sequence export is invoked without result remapping
- **THEN** targets, reference parameters, and result metadata use their recorded identities
