## ADDED Requirements

### Requirement: Marked targets are ignored by command recording support
The system SHALL provide a marker interface that domain objects and view models can implement to opt out of command recording support.
When command-log recording support is `ENABLED` and an action target implements the marker interface, the system MUST NOT persist a command log entry for that action invocation solely through recording support.
When command-log recording support is `ENABLED` and a property edit target implements the marker interface, the system MUST NOT persist a command log entry for that property edit solely through recording support.
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

#### Scenario: Unmarked target remains recordable
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an action target does not implement the command recording suppression marker interface
- **WHEN** a user invokes an otherwise eligible action on that target
- **THEN** the system remains able to persist a command log entry for that invocation through recording support

### Requirement: Command export and replay helper objects opt out of command recording support
The command export manager SHALL implement the command recording suppression marker interface.
The command replay manager SHALL implement the command recording suppression marker interface.
The replayable command view model SHALL implement the command recording suppression marker interface.
The replayable command participant view model SHALL implement the command recording suppression marker interface.
Command log entry entity and view types SHALL implement the command recording suppression marker interface.
When a user interacts with these marked helper objects during a recording session, the system MUST NOT persist those interactions as replayable user commands solely through recording support.

#### Scenario: Replayable command action is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a user is viewing a `ReplayableCommand`
- **WHEN** the user invokes an action on the `ReplayableCommand`
- **THEN** the system does not persist a command log entry for that helper action solely through recording support

#### Scenario: Replayable participant action is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a user is viewing a `ReplayableCommandParticipant`
- **WHEN** the user invokes an action on the `ReplayableCommandParticipant`
- **THEN** the system does not persist a command log entry for that helper action solely through recording support

#### Scenario: Command log entry action is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a user is viewing a `CommandLogEntry`
- **WHEN** the user invokes an action on the `CommandLogEntry`
- **THEN** the system does not persist a command log entry for that helper action solely through recording support
