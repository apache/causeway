## ADDED Requirements

### Requirement: Qualified local-temporal range propagation
The qualified Vaadin local-temporal adapter SHALL consume Causeway-resolved `min` and `max` bounds for editable date-picker, time-picker, and date-time-picker controls without exposing Vaadin APIs in application markup.
Causeway SHALL remain authoritative for interval normalization, local validation, pending values, and property save lifecycle.

#### Scenario: Date picker receives bounds
- **WHEN** an eligible `LocalDate` property editor carries a valid resolved interval
- **THEN** the internal date picker receives matching ISO minimum and maximum values before its current value
- **AND** calendar choices outside the interval are unavailable according to the toolkit's supported range behavior

#### Scenario: Time picker receives office hours
- **WHEN** an eligible `LocalTime` property editor carries minimum `08:00` and maximum `18:00`
- **THEN** the internal time picker receives both bounds and retains the established 15-minute dropdown step
- **AND** its labelled clock trigger, keyboard activation, clear control, focus, and fallback behavior remain unchanged

#### Scenario: Date-time picker receives bounds
- **WHEN** an eligible `LocalDateTime` property editor carries a valid resolved interval
- **THEN** the internal date-time picker receives matching local ISO bounds without timezone conversion
- **AND** its composed date and time controls retain accessible names and triggers

#### Scenario: Range is absent or invalid
- **WHEN** the semantic editor host carries no valid resolved interval
- **THEN** the internal Vaadin control receives no minimum or maximum
- **AND** qualification, native fallback, current value, required state, and disabled state remain unchanged

#### Scenario: Read-only temporal value is presented
- **WHEN** a qualified temporal field is used only for read-only presentation
- **THEN** authored property bounds are not applied to that internal control
- **AND** accepted authoritative precision remains visible and unchanged
