## ADDED Requirements

### Requirement: Replayable command displays command participants
The system SHALL expose a participants collection on each replayable command.
The participants collection SHALL contain rows represented by `ReplayableCommandParticipant` view models.
Each participant SHALL expose the role, parameter name, recorded bookmark, target object, parameter object, result object, and actual bookmark in that order.
The system SHALL include target participants when the command DTO has recorded target bookmarks.
The system SHALL include reference parameter participants when the command DTO has recorded reference parameter bookmarks.
The system MUST NOT include non-reference parameters as bookmark participants.
The system SHALL include a result participant when the recorded command has a recorded result bookmark.
Each included participant SHALL expose its recorded bookmark.
Participants SHALL expose actual bookmarks only after the replayable command has successfully replayed.
When a successful replay has no explicit replacement bookmark for a target, parameter, or result participant, the participant SHALL use its recorded bookmark as the actual bookmark.

#### Scenario: Target participant appears in replayable command details
- **WHEN** a replayable command has recorded target bookmark `demoCustomer:1`
- **THEN** the replayable command participants collection contains a participant with role `TARGET`
- **AND** the participant has recorded bookmark `demoCustomer:1`

#### Scenario: Reference parameter participant appears in replayable command details
- **WHEN** a replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **THEN** the replayable command participants collection contains a participant with role `PARAMETER`
- **AND** the participant has parameter name `customer`
- **AND** the participant has recorded bookmark `demoCustomer:1`

#### Scenario: Non-reference parameter is omitted
- **WHEN** a replayable command has parameter `name` recorded as a string value
- **THEN** the replayable command participants collection does not contain a participant for parameter `name`

#### Scenario: Successful replay populates actual target bookmark
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the target participant has actual bookmark `demoCustomer:2`

#### Scenario: Successful replay keeps unchanged target actual bookmark populated
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the target participant has actual bookmark `demoCustomer:1`

### Requirement: Replayable command participant resolves target, parameter, and result objects best-effort
The `ReplayableCommandParticipant` view model SHALL expose a target object for target participants when the actual bookmark resolves locally.
The `ReplayableCommandParticipant` view model SHALL expose a parameter object for parameter participants when the actual bookmark resolves locally.
The `ReplayableCommandParticipant` view model SHALL expose a result object for result participants when the actual bookmark resolves locally.
The `ReplayableCommandParticipant` view model SHALL still expose recorded and actual bookmark values when the actual bookmark cannot be resolved locally.
The system MUST NOT require recorded bookmarks to resolve locally in order to display a participant.

#### Scenario: Target participant resolves actual object
- **WHEN** a target participant has actual bookmark `demoCustomer:2`
- **AND** bookmark lookup resolves `demoCustomer:2` to a local domain object
- **THEN** the participant target property exposes that local domain object
- **AND** the participant still displays recorded bookmark `demoCustomer:1`
- **AND** the participant still displays actual bookmark `demoCustomer:2`

#### Scenario: Parameter participant resolves actual object
- **WHEN** a parameter participant has actual bookmark `demoCustomer:2`
- **AND** bookmark lookup resolves `demoCustomer:2` to a local domain object
- **THEN** the participant parameter property exposes that local domain object
- **AND** the participant still displays recorded bookmark `demoCustomer:1`
- **AND** the participant still displays actual bookmark `demoCustomer:2`

#### Scenario: Parameter participant keeps unresolved bookmark values
- **WHEN** a reference parameter participant has actual bookmark `external.Customer:2`
- **AND** bookmark lookup cannot resolve `external.Customer:2` to a local domain object
- **THEN** the participant parameter property is empty
- **AND** the participant still displays recorded bookmark `external.Customer:1`
- **AND** the participant still displays actual bookmark `external.Customer:2`

### Requirement: Replayable command participant displays result after replay
The system SHALL include a result participant whenever the replayable command has a recorded result bookmark.
The result participant SHALL use the command's recorded result bookmark.
The result participant SHALL expose the actual result bookmark after the replayable command has successfully replayed.
The `ReplayableCommandParticipant` view model SHALL expose a result object for result participants when the actual result bookmark resolves locally.
The system MUST NOT resolve or display a result object for commands that have not successfully replayed.

#### Scenario: Pending command displays recorded result participant only
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **THEN** the replayable command participants collection contains a participant with role `RESULT`
- **AND** the participant has recorded bookmark `demoInvoice:1`
- **AND** the participant has no actual bookmark

#### Scenario: Successful replay displays result actual bookmark
- **WHEN** a replayable command has replay state `OK`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **AND** replay mapping data provides actual result bookmark `demoInvoice:2`
- **THEN** the replayable command participants collection contains a participant with role `RESULT`
- **AND** the participant has recorded bookmark `demoInvoice:1`
- **AND** the participant has actual bookmark `demoInvoice:2`

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants`.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`

#### Scenario: User views participant fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the layout displays the participant role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in order
