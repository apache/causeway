## ADDED Requirements

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
