## ADDED Requirements

### Requirement: Locale-aware local-date presentation
Qualified local-date editors SHALL format and parse visible calendar dates according to the active document language, falling back to the browser locale and then a safe default when no valid document language is available.
Locale presentation MUST NOT change the ISO lexical value retained by the temporal codec, pending interaction state, validation, or GraphQL submission.

#### Scenario: Document declares a non-US date convention
- **WHEN** a qualified `LocalDate` or `LocalDateTime` date portion is presented under a document language whose numeric date order differs from the default US order
- **THEN** the visible date uses that document language's numeric order and separators
- **AND** the underlying semantic value remains the same ISO local date

#### Scenario: User enters a localized date
- **WHEN** the user enters a valid date in the active locale's displayed numeric format
- **THEN** the picker resolves the corresponding calendar date
- **AND** the existing temporal codec receives its ISO lexical representation without timezone conversion

#### Scenario: Locale is absent or invalid
- **WHEN** the document has no valid language declaration
- **THEN** the adapter uses the browser locale when available and otherwise its safe default
- **AND** the field family does not fail solely because locale metadata is absent or malformed

#### Scenario: Localized calendar opens
- **WHEN** the platform exposes localized month names, weekday names, and first-day-of-week information
- **THEN** the date chooser uses those locale settings
- **AND** date selection still produces the same ISO local semantic value
