## MODIFIED Requirements

### Requirement: Unreferenced action group composition

`<cw-unreferenced-actions>` SHALL render remaining actions as ordinary `<cw-action>` elements in one compact wrapping horizontal group whose accessible name defaults to **Other actions**.
When ready, the custom-element host MUST NOT introduce a block, panel, or spacing box around that group, and the group MUST use only the inline space required by its controls up to the available container width.
It MUST preserve authoritative action order and ordinary interaction ownership.

#### Scenario: Remaining actions resolve

- **WHEN** authorized unclaimed actions remain for the current context
- **THEN** the component renders one ordinary action control per remaining action in a labelled wrapping group
- **AND** the ready host adds no redundant layout box, panel padding, or margin around the group
- **AND** authorization, hidden and disabled reasons, prompts, parameters, confirmation, invocation, results, navigation, and focus remain ordinary action behavior

#### Scenario: Generated actions share a compact toolbar

- **WHEN** `<cw-unreferenced-actions>` is authored among explicit action controls in a wrapping toolbar
- **THEN** its generated group aligns with those controls and occupies no more inline or block space than its controls and established action gaps require
- **AND** the controls wrap within the available width without causing horizontal document overflow

#### Scenario: Generated actions are placed in a layout container

- **WHEN** `<cw-unreferenced-actions>` is authored in a block or grid layout region rather than a toolbar
- **THEN** its generated action group remains start-aligned and intrinsically compact
- **AND** no empty panel-like surface is introduced around the actions

#### Scenario: Application names the action group

- **WHEN** the application supplies a bounded `name`
- **THEN** the group uses that value as its accessible name without changing action labels or identities

#### Scenario: No generated action presentation exists

- **WHEN** the component is loading, empty, or in error
- **THEN** it contributes no visible group or layout spacing
- **AND** its inspectable allocation state remains available
