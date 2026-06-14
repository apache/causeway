## ADDED Requirements

### Requirement: Recording support enables property edit command publishing
When command-log recording support is `ENABLED`, the system SHALL enable command publishing for ordinary property edits through the normal property command publishing facet model.
A property edit whose property has no explicit `@Property(commandPublishing)` setting SHALL be eligible for command-log persistence during recording support.
A property edit whose property command-publishing configuration would otherwise evaluate to not published SHALL be eligible for command-log persistence during recording support.
A property edit whose property is explicitly annotated with `@Property(commandPublishing = Publishing.DISABLED)` SHALL still be eligible for command-log persistence during recording support unless the target is suppressed from command recording.
When command-log recording support is `DISABLED`, property edits MUST retain existing command publishing behavior and MUST NOT be command logged solely because they are property edits.
The system SHALL apply this behavior through `CommandPublishingFacet#isPublishingEnabled` or an equivalent installed command publishing facet used by the normal command logging flow.
The system MUST NOT create a second property-edit command logging path outside the normal command publishing flow.

#### Scenario: Unannotated property edit is recorded when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a domain object property has no explicit `@Property(commandPublishing)` setting
- **WHEN** a user edits that property
- **THEN** the system creates a command log entry for the property edit through the normal command publishing flow
- **AND** the command log entry contains the property edit command DTO

#### Scenario: Property edit is not recorded by default when recording support is disabled
- **GIVEN** command-log recording support is `DISABLED`
- **AND** command-log persistence is enabled
- **AND** a domain object property has no explicit `@Property(commandPublishing)` setting
- **WHEN** a user edits that property
- **THEN** the system does not create a command log entry solely for that property edit

#### Scenario: Explicitly disabled property edit is recorded when recording support is enabled
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a domain object property is annotated with `@Property(commandPublishing = Publishing.DISABLED)`
- **WHEN** a user edits that property
- **THEN** the system creates a command log entry for the property edit through the normal command publishing flow
- **AND** the command log entry contains the property edit command DTO

#### Scenario: Recording support exposes enabled property command publishing metadata
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a domain object property is eligible for editing
- **WHEN** the framework introspects the property command publishing facet
- **THEN** the property command publishing facet reports publishing enabled

### Requirement: Recorded property edits remain replayable command log entries
A property edit recorded through command-log recording support SHALL be persisted as a normal command log entry.
The persisted command log entry SHALL include the command DTO needed for command replay.
The persisted command log entry SHALL be suitable for command export and replay eligibility processing according to existing replay rules.
Recording support MUST NOT create duplicate command log entries when the property edit is already command-published by annotation or global property command-publishing configuration.

#### Scenario: Recorded property edit stores replay command DTO
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a user edits an ordinary property on a bookmarkable domain object
- **WHEN** the command log entry for the property edit is persisted
- **THEN** the command log entry contains the command DTO for the property edit
- **AND** the entry can be considered by replayable command collection building

#### Scenario: Explicitly command-published property edit is not duplicated
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a domain object property is already configured for command publishing
- **WHEN** a user edits that property
- **THEN** the system stores at most one command log entry for that property edit
