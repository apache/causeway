## ADDED Requirements

### Requirement: Replay mapping SPI has a conditional default listener
The system SHALL autoconfigure a default `CommandReplayMappingListener` bean when no application-defined `CommandReplayMappingListener` bean is present.
The system MUST NOT instantiate the default listener bean when another `CommandReplayMappingListener` bean is already defined.
The conditional default listener SHALL participate in the same replay mapping listener collection used for replay input remapping and replay result mapping notifications.

#### Scenario: Default listener is autoconfigured when missing
- **WHEN** the application context contains no `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration provides the default `CommandReplayMappingListener` bean

#### Scenario: Default listener backs off for custom listener
- **WHEN** the application context already contains a custom `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration does not provide the default `CommandReplayMappingListener` bean

#### Scenario: Autoconfigured listener participates in replay
- **WHEN** the command log autoconfiguration provides the default `CommandReplayMappingListener` bean
- **THEN** command replay includes that listener when requesting input remaps and sending result mapping notifications
