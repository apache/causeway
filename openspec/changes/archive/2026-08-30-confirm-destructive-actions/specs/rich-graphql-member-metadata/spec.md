## ADDED Requirements

### Requirement: Static action confirmation metadata
The shared rich member metadata object SHALL expose nullable `areYouSure` metadata derived from canonical action semantics without invoking domain behavior or exposing the complete action-semantics model.

#### Scenario: Action requires confirmation
- **WHEN** an action's canonical semantics have are-you-sure behavior
- **THEN** `areYouSure` returns true

#### Scenario: Action does not require confirmation
- **WHEN** an action's canonical semantics do not have are-you-sure behavior
- **THEN** `areYouSure` returns false
- **AND** no confirmation requirement is inferred from the action's name, icon, description, or mutation placement

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** `areYouSure` returns null
- **AND** metadata resolution does not invoke domain-object methods

#### Scenario: Existing client omits confirmation metadata
- **WHEN** an existing GraphQL document does not select `areYouSure`
- **THEN** its operation and response shape remain unchanged
