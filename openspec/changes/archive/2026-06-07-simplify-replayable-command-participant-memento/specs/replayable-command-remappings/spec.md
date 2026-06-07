## ADDED Requirements

### Requirement: Replayable command participant mementos are readable and derived
The `ReplayableCommandParticipant` view model SHALL use a readable memento based on the owning command interaction id and participant identity.
A target participant memento SHALL use the form `[commandInteractionId]--target`.
A parameter participant memento SHALL use the form `[commandInteractionId]--parameter--[parameterName]`.
A result participant memento SHALL use the form `[commandInteractionId]--result`.
The participant memento MUST NOT include recorded bookmark or actual bookmark values.
When a participant is reconstructed from its memento, the system SHALL derive recorded bookmark, actual bookmark, and object link values from the owning replayable command and command log entry.
The participant view model constructor used by the framework SHALL accept the string memento as its first parameter and injected services after it.

#### Scenario: Target participant has readable memento
- **WHEN** a replayable command participant has command interaction id `11111111-1111-1111-1111-111111111111` and role `TARGET`
- **THEN** the participant memento is `11111111-1111-1111-1111-111111111111--target`
- **AND** the memento does not contain recorded or actual bookmark values

#### Scenario: Parameter participant has readable memento
- **WHEN** a replayable command participant has command interaction id `11111111-1111-1111-1111-111111111111`, role `PARAMETER`, and parameter name `customer`
- **THEN** the participant memento is `11111111-1111-1111-1111-111111111111--parameter--customer`
- **AND** the memento does not contain recorded or actual bookmark values

#### Scenario: Result participant has readable memento
- **WHEN** a replayable command participant has command interaction id `11111111-1111-1111-1111-111111111111` and role `RESULT`
- **THEN** the participant memento is `11111111-1111-1111-1111-111111111111--result`
- **AND** the memento does not contain recorded or actual bookmark values

#### Scenario: Participant rehydrates derived bookmarks from memento
- **WHEN** a replayable command participant is reconstructed from a readable memento
- **AND** the owning command log entry can be found by command interaction id
- **THEN** the participant exposes the same recorded bookmark, actual bookmark, and object link values as the matching participant derived from the owning replayable command

#### Scenario: View model constructor uses framework-compatible parameter order
- **WHEN** the framework reconstructs a replayable command participant view model
- **THEN** the constructor accepts the string memento first
- **AND** injected services follow the string memento parameter
