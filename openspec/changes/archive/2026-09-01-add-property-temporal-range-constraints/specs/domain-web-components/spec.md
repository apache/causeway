## ADDED Requirements

### Requirement: Declarative local-temporal property ranges
An authored editable `<cw-property>` SHALL accept optional `min` and `max` attributes and properties for authoritative `LocalDate`, `LocalTime`, and `LocalDateTime` values.
Valid bounds MUST constrain both the presented editor and Causeway-owned property validation without changing local-temporal lexical precision, timezone semantics, canonical GraphQL validation, or mutation authority.

#### Scenario: Absolute local-date interval is authored
- **WHEN** an editable `LocalDate` property declares valid ISO `min` and `max` bounds in ascending order
- **THEN** native and qualified editors expose that closed date interval
- **AND** values equal to either boundary remain admissible

#### Scenario: Office-hour interval is authored
- **WHEN** an editable `LocalTime` property declares `min="08:00"` and `max="18:00"`
- **THEN** the time control exposes that closed interval while retaining the supported quarter-hour picker choices
- **AND** accepted values within the interval retain their original seconds, fractional seconds, and off-quarter-hour minutes until explicitly changed

#### Scenario: Local date-time interval is authored
- **WHEN** an editable `LocalDateTime` property declares matching absolute ISO bounds
- **THEN** the date-time control receives both bounds without UTC conversion
- **AND** comparison uses local calendar and clock parts rather than a browser timezone instant

#### Scenario: Relative date bound begins editing
- **WHEN** a `LocalDate` property uses `today` or `tomorrow` and editing begins
- **THEN** the token resolves from the browser's current local calendar to an ISO date boundary
- **AND** that resolved boundary remains stable for the complete edit interaction

#### Scenario: Relative date-time bound begins editing
- **WHEN** a `LocalDateTime` property uses `now` and editing begins
- **THEN** the token resolves from the browser's current local date and time without an offset
- **AND** a later edit resolves the token again from its own start time

#### Scenario: Pending value is below the minimum
- **WHEN** a well-formed pending local temporal value compares below the resolved minimum
- **THEN** local validation retains the pending value and presents a field-specific correction naming the minimum
- **AND** no GraphQL property validation or mutation request is issued

#### Scenario: Pending value is above the maximum
- **WHEN** a well-formed pending local temporal value compares above the resolved maximum
- **THEN** local validation retains the pending value and presents a field-specific correction naming the maximum
- **AND** no GraphQL property validation or mutation request is issued

#### Scenario: Pending value enters the interval
- **WHEN** the user corrects the pending value to lie within the closed interval
- **THEN** canonical GraphQL validation and save resume through the existing property lifecycle
- **AND** the authored range does not suppress a canonical domain validation reason

#### Scenario: Authored interval is unusable
- **WHEN** either bound is malformed, blank, incompatible with the authoritative datatype, or the resolved minimum exceeds the maximum
- **THEN** the entire authored interval is ignored and the canonical editor remains usable
- **AND** the property exposes a deterministic invalid-range diagnostic hook without submitting or fabricating a bound

#### Scenario: Property datatype is not a supported local temporal
- **WHEN** `min` or `max` appears on a non-temporal, offset, zoned, legacy, read-only, or unsupported temporal property presentation
- **THEN** the attributes do not change value rendering, editor selection, parsing, validation, or GraphQL behavior
