## MODIFIED Requirements

### Requirement: Duration filtering survives observation composition

Threshold observations SHALL retain their stop policy through fluent customization, start and lifecycle-helper use. Duration filtering SHALL be disabled by default. When explicitly enabled in configurations supporting duration filtering, successful observations below the threshold SHALL be suppressed and observations at or above it SHALL remain eligible for export. Failed observations SHALL NOT be suppressed solely for short duration. Any agent-mode limitation on discard export SHALL be documented and tested as retained-span behavior rather than silently assumed suppression.

#### Scenario: Fluent customization before start
- **WHEN** a threshold observation is customized through its fluent methods and run through the lifecycle helper
- **THEN** its threshold policy still runs when it stops

#### Scenario: Successful short observation
- **WHEN** a successful observation completes below the threshold with filtering explicitly enabled in a configuration supporting duration filtering
- **THEN** it is discarded from export

#### Scenario: Successful longer observation
- **WHEN** a successful observation reaches or exceeds the threshold
- **THEN** duration filtering does not discard it

#### Scenario: Short failed observation
- **WHEN** an observation fails below the threshold
- **THEN** duration filtering does not discard the failure

#### Scenario: Agent exporter cannot enforce discard
- **WHEN** the documented agent configuration cannot apply the existing discard policy
- **THEN** duration suppression is explicitly disabled for that configuration, collected spans confirm retention, and the configuration guide explains the limitation

#### Scenario: Filtering disabled by default
- **WHEN** a successful observation completes below the threshold without explicitly enabled duration filtering
- **THEN** duration filtering does not discard it

