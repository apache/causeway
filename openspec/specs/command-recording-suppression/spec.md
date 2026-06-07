# command-recording-suppression Specification

## Purpose
TBD - created by archiving change suppress-recording-for-replay-objects. Update Purpose after archive.
## Requirements
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

### Requirement: Command log persistence can be paused and resumed
The system SHALL provide an application-event based mechanism to pause command log persistence.
The system SHALL provide an application-event based mechanism to resume command log persistence after a previous pause.
While command log persistence is paused, the command-log subscriber MUST NOT create new command log entries for published commands.
While command log persistence is paused, the command-log subscriber MUST NOT sync existing command log entries for published command lifecycle updates.
After command log persistence is resumed, subsequent eligible commands MUST remain persistable by the normal command-log subscriber flow.
Nested pause scopes MUST NOT resume command log persistence until each active pause scope has been resumed.

#### Scenario: Paused logging skips command entry creation
- **GIVEN** command-log persistence is enabled
- **AND** command log persistence has been paused
- **WHEN** a command reaches the ready notification
- **THEN** the system does not create a command log entry for that command

#### Scenario: Paused logging skips command entry synchronization
- **GIVEN** command-log persistence is enabled
- **AND** command log persistence has been paused
- **WHEN** a command reaches the started or completed notification
- **THEN** the system does not sync a command log entry for that command notification

#### Scenario: Resumed logging restores normal persistence
- **GIVEN** command-log persistence is enabled
- **AND** command log persistence was paused and has then been resumed
- **WHEN** a subsequent eligible command reaches the ready notification
- **THEN** the system can create a command log entry for that command through the normal command-log subscriber flow

#### Scenario: Nested pause requires matching resumes
- **GIVEN** command-log persistence is enabled
- **AND** command log persistence has been paused twice
- **WHEN** command log persistence is resumed once
- **THEN** command log persistence remains paused
- **WHEN** command log persistence is resumed a second time
- **THEN** subsequent eligible commands can be persisted through the normal command-log subscriber flow

### Requirement: Initial fixture script commands are not logged
The system SHALL pause command log persistence while a configured initial fixture script is being installed during application startup.
The system MUST resume command log persistence after the initial fixture script installation completes.
The system MUST resume command log persistence if the initial fixture script installation fails.
The pause MUST apply only to command-log persistence and MUST NOT prevent the fixture script from executing its domain logic normally.

#### Scenario: Initial fixture script commands are suppressed
- **GIVEN** command-log persistence is enabled
- **AND** an application has a configured initial fixture script
- **WHEN** the system installs the initial fixture script during startup
- **THEN** commands produced by that initial fixture script are not persisted as command log entries
- **AND** the fixture script domain logic still executes normally

#### Scenario: Logging resumes after successful initial fixture installation
- **GIVEN** command-log persistence is enabled
- **AND** an application has installed its configured initial fixture script during startup
- **WHEN** a subsequent eligible command is published after initial fixture installation completes
- **THEN** the system can persist that command as a command log entry through the normal command-log subscriber flow

#### Scenario: Logging resumes after failed initial fixture installation
- **GIVEN** command-log persistence is enabled
- **AND** an application has a configured initial fixture script that fails during startup installation
- **WHEN** command processing continues after the failed initial fixture installation scope exits
- **THEN** command log persistence is no longer paused because of the initial fixture installation scope

