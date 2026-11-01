## ADDED Requirements

### Requirement: Qualified action-parameter temporal range propagation
The qualified Vaadin local-temporal adapter SHALL consume Causeway-resolved `min` and `max` values from action-parameter editor contexts for editable date-picker, time-picker, and date-time-picker controls.
Causeway SHALL remain authoritative for declaration matching, interval normalization, local request gating, pending values, canonical validation, and invocation.

#### Scenario: Parameter date picker receives bounds
- **WHEN** an eligible `LocalDate` action parameter carries a valid resolved interval
- **THEN** the internal date picker receives matching ISO bounds before its pending or default value
- **AND** calendar choices outside the interval follow the toolkit's supported disabled-range behavior

#### Scenario: Parameter time picker receives office hours
- **WHEN** an eligible `LocalTime` action parameter carries `08:00` and `17:00` bounds
- **THEN** the internal time picker receives both bounds and retains the established 15-minute dropdown step
- **AND** its labelled clock trigger, synchronous keyboard activation, clear control, and focus behavior remain unchanged

#### Scenario: Parameter date-time picker receives bounds
- **WHEN** an eligible `LocalDateTime` action parameter carries a valid resolved interval
- **THEN** the internal date-time picker receives matching local ISO bounds without timezone conversion
- **AND** its composed date and time controls retain accessible names and triggers

#### Scenario: Parameter range is absent or invalid
- **WHEN** the action-parameter editor context carries no valid resolved interval
- **THEN** the internal Vaadin control receives no minimum or maximum
- **AND** qualification, native fallback, default value, required state, disabled state, and prompt lifecycle remain unchanged

#### Scenario: Bounded parameter prompt is cancelled
- **WHEN** a qualified bounded parameter editor is cancelled without invocation
- **THEN** its overlay, clear control, pending state, and focus are disposed through the existing prompt lifecycle
- **AND** reopening installs controls with newly resolved bounds and no stale adapter state
