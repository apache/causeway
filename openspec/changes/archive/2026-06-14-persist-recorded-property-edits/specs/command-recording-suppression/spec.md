## MODIFIED Requirements

### Requirement: Marked targets are ignored by command recording support
The system SHALL provide a marker interface that domain objects and view models can implement to opt out of command recording support.
When command-log recording support is `ENABLED` and an action target implements the marker interface, the system MUST NOT persist a command log entry for that action invocation solely through recording support.
When command-log recording support is `ENABLED` and a property edit target implements the marker interface, the system MUST NOT persist a command log entry for that property edit solely through recording support.
The command recording suppression marker MUST remain authoritative even when recording support enables property edit command publishing through the property command publishing facet model for the application metamodel.
The system SHALL continue to execute the member invocation normally when the target implements the marker interface.
The marker interface MUST NOT suppress command recording for unmarked targets.

#### Scenario: Marked action target is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an action target implements the command recording suppression marker interface
- **WHEN** a user invokes an action on that target
- **THEN** the system executes the action normally
- **AND** the system does not persist a command log entry for that action invocation solely through recording support

#### Scenario: Marked property target is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a property edit target implements the command recording suppression marker interface
- **WHEN** a user edits a property on that target
- **THEN** the system executes the property edit normally
- **AND** the system does not persist a command log entry for that property edit solely through recording support

#### Scenario: Marked property target remains suppressed after recording enables property publishing
- **GIVEN** an application context starts with command-log recording support configured as `ENABLED`
- **AND** recording support enables command publishing for ordinary property edits in the metamodel
- **AND** a property edit target implements the command recording suppression marker interface
- **WHEN** a user edits a property on that target
- **THEN** the system executes the property edit normally
- **AND** the system does not persist a command log entry for that property edit solely through recording support

#### Scenario: Unmarked target remains recordable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an action target does not implement the command recording suppression marker interface
- **WHEN** a user invokes an otherwise eligible action on that target
- **THEN** the system remains able to persist a command log entry for that invocation through recording support
