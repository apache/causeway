## MODIFIED Requirements

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view MUST NOT expose a separate open target action.
Target inspection and navigation SHALL be available through target rows in the participants collection.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`
- **AND** the participants table appears before the control field set

#### Scenario: User views participant fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the layout displays the participant role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in order

#### Scenario: User inspects target through participants
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout does not display separate `targetType` or `targetId` properties
- **AND** the layout does not display a separate `openTarget` action
- **AND** target information is available through the participants table
