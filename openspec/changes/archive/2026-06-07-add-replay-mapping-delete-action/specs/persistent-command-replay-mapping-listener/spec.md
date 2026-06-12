## ADDED Requirements

### Requirement: Persistent replay mappings can be deleted from the entity UI
The system SHALL provide a delete action on persisted replay result mapping entities.
The delete action SHALL be implemented as a mixin contributed to `CommandReplayResultMapping`.
The delete action SHALL use idempotent are-you-sure semantics.
The delete action SHALL remove the selected replay result mapping from persistence.
The delete action SHALL be included in the persisted replay result mapping fallback layout metadata.

#### Scenario: User deletes a persisted replay mapping
- **GIVEN** a persisted replay result mapping exists for recorded bookmark `demoInvoice:1`
- **WHEN** a user confirms the replay mapping delete action
- **THEN** the system removes that replay result mapping from persistence

#### Scenario: Delete action requires confirmation
- **WHEN** a user views a persisted replay result mapping
- **THEN** the delete action is exposed with idempotent are-you-sure semantics

#### Scenario: Delete action appears in layout
- **WHEN** a user views a persisted replay result mapping using fallback layout metadata
- **THEN** the layout includes the delete action for that replay result mapping
