## ADDED Requirements

### Requirement: Replayable command inspection surfaces relevant replay mappings
The system SHALL make replay mapping data relevant to a replayable command visible from that replayable command's UI.
The visible data SHALL include target bookmark replacements discovered through replay mapping lookup.
The visible data SHALL include reference parameter bookmark replacements discovered through replay mapping lookup.
The visible data SHALL include recorded target, reference parameter, and result bookmarks before replay.
The visible data SHALL include actual target, reference parameter, and result bookmarks after the replayable command has successfully replayed.
The system SHALL preserve the existing replay mapping SPI contracts while surfacing this data.

#### Scenario: User inspects replay input participants
- **WHEN** a replayable command has a recorded target or reference parameter bookmark
- **THEN** the replayable command UI exposes the recorded bookmark for that participant

#### Scenario: User inspects replay input remappings after success
- **WHEN** a replayable command target or reference parameter is remapped by the replay mapping lookup flow
- **AND** the replayable command has successfully replayed
- **THEN** the replayable command UI exposes the recorded bookmark and actual bookmark for that participant

#### Scenario: User inspects replay result remapping after success
- **WHEN** a replayable command has replayed successfully
- **AND** the command has a recorded result bookmark mapped to an actual result bookmark
- **THEN** the replayable command UI exposes the recorded result bookmark and actual result bookmark

#### Scenario: Replay mapping SPI remains source-compatible
- **WHEN** an application implements `CommandReplayMappingListener`
- **THEN** the implementation remains source-compatible with the replay mapping SPI
