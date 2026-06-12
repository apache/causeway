## Purpose

Define the replayable command participants UI that surfaces recorded and actual target, reference parameter, and result bookmarks for command replay inspection.

## Requirements

### Requirement: Replayable command displays command participants
The system SHALL expose a participants collection on each replayable command.
The participants collection SHALL contain rows represented by `ReplayableCommandParticipant` view models.
Each participant SHALL expose the role, parameter name, recorded bookmark, target object, argument object, result object, and actual bookmark in that order.
The system SHALL include target participants when the command DTO has recorded target bookmarks.
The system SHALL include reference parameter participants when the command DTO has recorded reference parameter bookmarks.
The system MUST NOT include non-reference parameters as bookmark participants.
The system SHALL include a result participant when the recorded command has a recorded result bookmark.
Each included participant SHALL expose its recorded bookmark.
Target participants SHALL expose an actual bookmark whenever replay mapping lookup provides a mapped bookmark.
Reference parameter participants SHALL expose an actual bookmark whenever replay mapping lookup provides a mapped bookmark.
Target and reference parameter participants SHALL leave actual bookmarks empty before successful replay when replay mapping lookup provides no mapped bookmark.
Target and reference parameter participants SHALL use their recorded bookmark as the actual bookmark after successful replay when replay mapping lookup provides no mapped bookmark.
Result participants SHALL expose actual bookmarks only after the replayable command has successfully replayed.
When a successful replay has no explicit replacement bookmark for a result participant, the participant SHALL use its recorded bookmark as the actual bookmark.

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

#### Scenario: Pending command populates mapped target actual bookmark
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the target participant has actual bookmark `demoCustomer:2`

#### Scenario: Pending command populates mapped parameter actual bookmark
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the parameter participant has actual bookmark `demoCustomer:2`

#### Scenario: Pending command leaves unmapped target actual bookmark empty
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the target participant has no actual bookmark

#### Scenario: Pending command leaves unmapped parameter actual bookmark empty
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the parameter participant has no actual bookmark

#### Scenario: Successful replay populates mapped target actual bookmark
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the target participant has actual bookmark `demoCustomer:2`

#### Scenario: Successful replay keeps unchanged target actual bookmark populated
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the target participant has actual bookmark `demoCustomer:1`

#### Scenario: Successful replay keeps unchanged parameter actual bookmark populated
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the parameter participant has actual bookmark `demoCustomer:1`

### Requirement: Replayable command participant resolves target, parameter, and result objects best-effort
The `ReplayableCommandParticipant` view model SHALL expose a target object for target participants when the actual bookmark resolves locally.
The `ReplayableCommandParticipant` view model SHALL expose an argument object for parameter participants when the actual bookmark resolves locally.
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
- **THEN** the participant argument property exposes that local domain object
- **AND** the participant still displays recorded bookmark `demoCustomer:1`
- **AND** the participant still displays actual bookmark `demoCustomer:2`

#### Scenario: Parameter participant keeps unresolved bookmark values
- **WHEN** a reference parameter participant has actual bookmark `external.Customer:2`
- **AND** bookmark lookup cannot resolve `external.Customer:2` to a local domain object
- **THEN** the participant argument property is empty
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
The `ReplayableCommandParticipant` view model SHALL provide a human-readable title.
The `ReplayableCommandParticipant` view model SHALL expose a derived `replayableCommand` property that links to its owning replayable command.
The participant layout SHALL expose replayable command, role, parameter name, recorded bookmark, target, argument, result, and actual bookmark.
The participant object view MUST NOT expose `owningInteractionId`.
The participant object view SHALL expose `logicalTypeName` only in metadata.
The participant fallback layout SHALL use a first column with width `4`.
The first column SHALL contain a `general` fieldset with replayable command, role, and parameter name.
The first column SHALL contain a `metadata` tab or fieldset in the same tab group as `general` with logical type name.
The second column SHALL contain recorded bookmark, target, and argument.
The second column SHALL hide target unless the participant role is `TARGET`.
The second column SHALL hide argument unless the participant role is `PARAMETER`.
The third column SHALL contain actual bookmark and result.
The third column SHALL hide result unless the participant role is `RESULT`.
The participant table column order SHALL expose role, parameter name, recorded bookmark, target, argument, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view MUST NOT expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection.
Target, argument, and result inspection and navigation SHALL be available through object-valued links in the participants collection rows.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`
- **AND** the participants table appears before the control field set

#### Scenario: User views participant identity fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the first column has width `4`
- **AND** the first column general fieldset displays replayable command, role, and parameter name
- **AND** the first column metadata tab or fieldset displays logical type name
- **AND** the participant has a human-readable title
- **AND** the participant does not display owning interaction id

#### Scenario: User views participant recorded-side fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the second column displays recorded bookmark
- **AND** the second column displays target only for target participants
- **AND** the second column displays argument only for parameter participants

#### Scenario: User views participant actual-side fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the third column displays actual bookmark
- **AND** the third column displays result only for result participants

#### Scenario: User inspects target through participants
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout does not display separate `targetType` or `targetId` properties
- **AND** target information is available through the participants table

#### Scenario: User navigates using participant object links
- **WHEN** a user views a replayable command participant with an available target, argument, or result object
- **THEN** the user can navigate to that object through the corresponding object-valued participant row link

#### Scenario: User navigates to replayable command from participant
- **WHEN** a user views a replayable command participant
- **THEN** the participant exposes a replayable command property linked to the owning replayable command

#### Scenario: User does not see redundant open actions
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the replayable command does not expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection

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
