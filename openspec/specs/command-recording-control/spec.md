## Purpose

Defines opt-in command recording controls, target-level suppression, nested commandlog persistence pauses, and startup fixture suppression.

## Requirements

### Requirement: Command recording support is opt-in
The system SHALL provide the `causeway.extensions.command-log.recording-support` configuration property.
The property SHALL accept `ENABLED` and `DISABLED` values.
The property SHALL default to `DISABLED`.
When recording support is `DISABLED`, the system MUST preserve existing command-publishing eligibility and MUST NOT broaden command logging solely because recording support exists.

#### Scenario: Recording support is disabled by default
- **WHEN** an application does not configure command-log recording support
- **THEN** recording support is `DISABLED`
- **AND** existing command-publishing eligibility is unchanged

#### Scenario: Application enables recording support
- **WHEN** an application configures `causeway.extensions.command-log.recording-support=ENABLED`
- **THEN** the configuration model reports recording support as enabled
- **AND** later recording-aware policies can use that state without a separate safe-action-specific configuration property

### Requirement: Marked targets are ignored by command recording support
The system SHALL provide a core applib marker interface that domain objects and view models can implement to opt out of command recording support.
When an action owner or target implements the marker interface, the system MUST NOT prepare that member interaction for command persistence through recording support.
When a property owner or target implements the marker interface, the system MUST NOT prepare that property edit for command persistence through recording support.
The system SHALL continue to execute the member invocation normally when its owner or target implements the marker interface.
The marker interface MUST NOT suppress command recording for unmarked owners and targets.

#### Scenario: Marked action target is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an action target implements the command recording suppression marker interface
- **WHEN** a user invokes an action on that target
- **THEN** the system executes the action normally
- **AND** the system does not persist a command log entry for that invocation through recording support

#### Scenario: Marked property target is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a property target implements the command recording suppression marker interface
- **WHEN** a user edits the property
- **THEN** the system executes the property edit normally
- **AND** the system does not persist a command log entry for that edit through recording support

#### Scenario: Marked member owner is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a contributed or mixin member owner implements the command recording suppression marker interface
- **WHEN** a user invokes the contributed or mixin member
- **THEN** the system executes the member normally
- **AND** the system does not persist a command log entry for that invocation through recording support

#### Scenario: Unmarked target remains eligible
- **GIVEN** command-log recording support is `ENABLED`
- **AND** an interaction owner and target do not implement the command recording suppression marker interface
- **WHEN** the interaction is otherwise eligible for command publishing
- **THEN** the marker policy does not prevent normal command publication

### Requirement: Commandlog helper objects suppress recording
The existing command export manager SHALL implement the command recording suppression marker interface.
The existing command replay manager SHALL implement the command recording suppression marker interface.
The replayable command view model SHALL implement the command recording suppression marker interface.
Command log entry view and entity types SHALL implement the command recording suppression marker interface where their shared type hierarchy permits it.
Interactions with marked commandlog helper objects MUST NOT be persisted as replayable user commands through recording support.

#### Scenario: Export manager interaction is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** a user invokes an action on the command export manager
- **THEN** the system does not persist that helper interaction through recording support

#### Scenario: Replay manager interaction is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** a user invokes an action on the command replay manager
- **THEN** the system does not persist that helper interaction through recording support

#### Scenario: Replayable command interaction is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** a user invokes an action on a replayable command view model
- **THEN** the system does not persist that helper interaction through recording support

#### Scenario: Command log entry interaction is not recorded
- **GIVEN** command-log recording support is `ENABLED`
- **WHEN** a user invokes an action on a marked command log entry object
- **THEN** the system does not persist that helper interaction through recording support

### Requirement: Command log persistence can be paused and resumed
The system SHALL provide `PauseCommandLoggingEvent` in core applib as an application-event contract for pausing command log persistence.
The system SHALL provide `ResumeCommandLoggingEvent` in core applib as an application-event contract for resuming command log persistence.
Command logging implementations SHALL consume these events without requiring event publishers to depend on a command logging implementation module.
While command log persistence is paused, the commandlog subscriber MUST NOT create command log entries for ready notifications.
While command log persistence is paused, the commandlog subscriber MUST NOT synchronize command log entries for started or completed notifications.
After command log persistence is resumed, subsequent eligible commands MUST remain persistable through the normal commandlog subscriber flow.
Nested pause scopes MUST NOT resume command log persistence until each active pause has received a matching resume.
An unmatched resume MUST NOT make pause depth negative or disable subsequent command logging.

#### Scenario: Paused logging skips command entry creation
- **GIVEN** commandlog persistence is enabled
- **AND** commandlog persistence has been paused by a `PauseCommandLoggingEvent`
- **WHEN** a command reaches the ready notification
- **THEN** the subscriber does not create a command log entry for that command

#### Scenario: Paused logging skips command entry synchronization
- **GIVEN** commandlog persistence is enabled
- **AND** commandlog persistence has been paused by a `PauseCommandLoggingEvent`
- **WHEN** a command reaches the started or completed notification
- **THEN** the subscriber does not synchronize a command log entry for that notification

#### Scenario: Resumed logging restores normal persistence
- **GIVEN** commandlog persistence is enabled
- **AND** commandlog persistence was paused and then resumed
- **WHEN** a subsequent eligible command reaches the ready notification
- **THEN** the subscriber can create a command log entry through the normal flow

#### Scenario: Nested pause requires matching resumes
- **GIVEN** commandlog persistence is enabled
- **AND** commandlog persistence has been paused twice
- **WHEN** commandlog persistence is resumed once
- **THEN** commandlog persistence remains paused
- **WHEN** commandlog persistence is resumed a second time
- **THEN** subsequent eligible commands can be persisted through the normal flow

#### Scenario: Unmatched resume is harmless
- **GIVEN** commandlog persistence is not paused
- **WHEN** a `ResumeCommandLoggingEvent` is published
- **THEN** commandlog persistence remains unpaused
- **AND** subsequent eligible commands can be persisted through the normal flow

### Requirement: Initial fixture script commands are not persisted
The system SHALL publish `PauseCommandLoggingEvent` immediately before installing a configured initial fixture script during application startup.
The system MUST publish the matching `ResumeCommandLoggingEvent` after initial fixture installation completes.
The system MUST publish the matching resume event if initial fixture installation fails.
The pause MUST apply only to commandlog persistence and MUST NOT prevent the fixture script from executing its domain logic normally.
The initial fixture installation path MUST NOT depend on the commandlog extension.

#### Scenario: Initial fixture commands are suppressed
- **GIVEN** commandlog persistence is enabled
- **AND** an application has a configured initial fixture script
- **WHEN** the framework installs the initial fixture script
- **THEN** commands produced by the fixture script are not persisted as command log entries
- **AND** the fixture script domain logic executes normally

#### Scenario: Logging resumes after successful fixture installation
- **GIVEN** commandlog persistence is enabled
- **AND** initial fixture installation completes successfully
- **WHEN** a subsequent eligible command is published
- **THEN** the command can be persisted through the normal commandlog subscriber flow

#### Scenario: Logging resumes after failed fixture installation
- **GIVEN** commandlog persistence is enabled
- **AND** initial fixture installation fails
- **WHEN** the fixture installation scope exits
- **THEN** commandlog persistence is no longer paused by that fixture installation scope
