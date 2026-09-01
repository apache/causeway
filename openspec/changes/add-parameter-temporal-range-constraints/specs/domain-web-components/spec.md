## ADDED Requirements

### Requirement: Declarative local-temporal action-parameter ranges
An optional direct-child `<cw-parameter>` declaration SHALL accept `min` and `max` attributes and properties for authoritative `LocalDate`, `LocalTime`, and `LocalDateTime` action parameters.
Valid bounds MUST constrain the presented editor and Causeway-owned prompt validation without changing authoritative parameter existence, order, type, defaults, choices, required state, visibility, disabled state, canonical validation, confirmation, or invocation.

#### Scenario: Absolute local-date parameter interval is authored
- **WHEN** a declared authoritative `LocalDate` parameter has valid ascending ISO `min` and `max` bounds
- **THEN** its native or qualified editor exposes the closed date interval
- **AND** values equal to either boundary remain admissible

#### Scenario: Office-hour parameter interval is authored
- **WHEN** a declared authoritative `LocalTime` parameter has `min="08:00"` and `max="17:00"`
- **THEN** its editor exposes that closed time interval while retaining the supported quarter-hour picker choices
- **AND** accepted pending seconds, fractional seconds, and off-quarter-hour minutes remain unchanged until explicitly edited

#### Scenario: Local date-time parameter interval is authored
- **WHEN** a declared authoritative `LocalDateTime` parameter has compatible absolute ISO bounds
- **THEN** its editor receives matching local date-time bounds without UTC conversion
- **AND** comparison uses local calendar and clock parts

#### Scenario: Relative parameter bound begins a prompt
- **WHEN** `today` or `tomorrow` is declared for a `LocalDate`, or `now` is declared for a `LocalDateTime`, and the action prompt begins
- **THEN** the token resolves once from the browser's current local calendar or clock after the authoritative datatype is known
- **AND** the resolved boundary remains stable for that prompt generation and resolves again for a later opening

#### Scenario: Pending parameter lies outside its range
- **WHEN** a well-formed pending local-temporal parameter value compares outside its resolved closed interval
- **THEN** the prompt retains the pending value and presents a field-specific correction naming the violated boundary
- **AND** no GraphQL preparation, action validation, confirmation, or invocation request is issued for that attempt

#### Scenario: Pending parameter is corrected
- **WHEN** the user corrects the pending value into the resolved interval
- **THEN** the local range reason clears and canonical preparation and action validation resume
- **AND** canonical domain validation can still reject the value independently

#### Scenario: Prompt submission finds an out-of-range value
- **WHEN** submission occurs before a bounded parameter has committed a valid in-range value
- **THEN** submission fails locally and focus moves through the existing first-invalid-control lifecycle
- **AND** the prompt remains open with all pending values available for correction or cancellation

#### Scenario: Authored parameter interval is unusable
- **WHEN** either bound is blank, malformed, incompatible with the authoritative datatype, or resolves below the minimum
- **THEN** the entire authored interval is ignored and the canonical parameter editor remains usable
- **AND** the rendered parameter exposes a deterministic invalid-range diagnostic hook without fabricating a bound

#### Scenario: Parameter is undeclared or inapplicable
- **WHEN** no matching declaration exists, or `min` or `max` is declared for a non-local, offset, zoned, legacy, reference, or unsupported parameter
- **THEN** editor selection, parsing, validation, defaults, choices, and GraphQL behavior remain unchanged
- **AND** declaration data cannot create, remove, or reorder an authoritative parameter

#### Scenario: Prompt is cancelled or reopened
- **WHEN** a bounded prompt is cancelled and later reopened
- **THEN** cancellation uses the existing focus-restoration lifecycle without invoking the action
- **AND** the new prompt resolves a fresh immutable range from its own start time
