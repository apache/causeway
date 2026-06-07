## ADDED Requirements

### Requirement: Replayable command displays relevant remapping participants
The system SHALL expose a remappings collection on each replayable command.
The remappings collection SHALL contain rows represented by `ReplayableCommandParticipant` view models.
Each participant SHALL expose the owning command interaction id, role, target object, parameter name, result object, recorded bookmark, and actual bookmark.
The system SHALL include target participants when the recorded target bookmark has an actual replacement bookmark.
The system SHALL include reference parameter participants when the recorded reference parameter bookmark has an actual replacement bookmark.
The system MUST NOT include non-reference parameters as remapping participants.
The system SHALL omit participants for recorded bookmarks that have no actual replacement bookmark.

#### Scenario: Target remapping appears in replayable command details
- **WHEN** a replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the replayable command remappings collection contains a participant with role `TARGET`
- **AND** the participant has recorded bookmark `demoCustomer:1`
- **AND** the participant has actual bookmark `demoCustomer:2`

#### Scenario: Reference parameter remapping appears in replayable command details
- **WHEN** a replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the replayable command remappings collection contains a participant with role `PARAMETER`
- **AND** the participant has parameter name `customer`
- **AND** the participant has recorded bookmark `demoCustomer:1`
- **AND** the participant has actual bookmark `demoCustomer:2`

#### Scenario: Non-reference parameter is omitted
- **WHEN** a replayable command has parameter `name` recorded as a string value
- **THEN** the replayable command remappings collection does not contain a participant for parameter `name`

#### Scenario: Unmapped participant is omitted
- **WHEN** a replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the replayable command remappings collection does not contain a participant for that target bookmark

### Requirement: Replayable command participant resolves target and parameter objects best-effort
The `ReplayableCommandParticipant` view model SHALL expose a target object for target and parameter participants when the actual bookmark resolves locally.
The `ReplayableCommandParticipant` view model SHALL still expose recorded and actual bookmark values when the actual bookmark cannot be resolved locally.
The system MUST NOT require recorded bookmarks to resolve locally in order to display a participant.

#### Scenario: Target participant resolves actual object
- **WHEN** a target participant has actual bookmark `demoCustomer:2`
- **AND** bookmark lookup resolves `demoCustomer:2` to a local domain object
- **THEN** the participant target property exposes that local domain object
- **AND** the participant still displays recorded bookmark `demoCustomer:1`
- **AND** the participant still displays actual bookmark `demoCustomer:2`

#### Scenario: Parameter participant keeps unresolved bookmark values
- **WHEN** a reference parameter participant has actual bookmark `external.Customer:2`
- **AND** bookmark lookup cannot resolve `external.Customer:2` to a local domain object
- **THEN** the participant target property is empty
- **AND** the participant still displays recorded bookmark `external.Customer:1`
- **AND** the participant still displays actual bookmark `external.Customer:2`

### Requirement: Replayable command participant displays result only after successful replay
The system SHALL include a result participant only when the replayable command has successfully replayed.
The result participant SHALL use the command's recorded result bookmark and the actual result bookmark associated with that replayed command.
The `ReplayableCommandParticipant` view model SHALL expose a result object for result participants when the actual result bookmark resolves locally.
The system MUST NOT resolve or display a result object for commands that have not successfully replayed.

#### Scenario: Successful replay displays result remapping
- **WHEN** a replayable command has replay state `OK`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **AND** replay mapping data provides actual result bookmark `demoInvoice:2`
- **THEN** the replayable command remappings collection contains a participant with role `RESULT`
- **AND** the participant has recorded bookmark `demoInvoice:1`
- **AND** the participant has actual bookmark `demoInvoice:2`

#### Scenario: Pending command does not display result remapping
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **THEN** the replayable command remappings collection does not contain a result participant

### Requirement: Replayable remapping layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose owning interaction id, role, parameter name, target, result, recorded bookmark, and actual bookmark.
The `ReplayableCommand` fallback layout SHALL include the remappings collection as a table.

#### Scenario: User views remappings table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the remappings collection as a table

#### Scenario: User views participant fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the layout displays the participant role, parameter name, target, result, recorded bookmark, actual bookmark, and owning interaction id
