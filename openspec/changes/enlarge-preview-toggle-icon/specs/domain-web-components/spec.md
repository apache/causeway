## MODIFIED Requirements

### Requirement: Single hydrated row preview lifecycle

An opted-in collection SHALL keep at most one row preview expanded and SHALL provide its live `<cw-preview>` subtree with a dedicated hydrated object context created from the selected authoritative row identity, data, and selection.
Each preview disclosure MUST present a clearly visible, appropriately sized directional indicator whose collapsed and expanded directions remain synchronized with its authoritative `aria-expanded` state.

#### Scenario: User expands an eligible row

- **WHEN** the user activates a collapsed row's preview disclosure
- **THEN** the collection creates one live `<cw-preview>` after that row using a fresh clone of its effective template
- **AND** it assigns the dedicated row context before descendant properties, actions, or collections connect
- **AND** focus remains on the disclosure while ordinary forward navigation reaches the preview content
- **AND** the visible indicator changes from the collapsed inline direction to the expanded downward direction without changing the button dimensions

#### Scenario: Preview component requests hydrated data

- **WHEN** a live preview descendant requests a member already selected into the collection row
- **THEN** the dedicated row context serves the hydrated data without repeating that object read

#### Scenario: Preview component requests additional data

- **WHEN** a live preview descendant requests an authorized member absent from row hydration
- **THEN** the dedicated row context loads the missing requirement through the established GraphQL client
- **AND** canonical metadata, hidden, disabled, validation, invocation, and error semantics remain authoritative

#### Scenario: Another row is expanded

- **WHEN** one preview is open and the user activates another eligible row disclosure
- **THEN** the current preview and its context are retired before the second preview becomes current
- **AND** no more than one live preview subtree or preview-owned context remains

#### Scenario: User collapses with the disclosure

- **WHEN** the user activates the disclosure for the currently expanded row
- **THEN** the preview and its context are retired
- **AND** the disclosure reports the collapsed state
- **AND** the visible indicator returns to the collapsed inline direction

#### Scenario: User presses Escape inside the preview

- **WHEN** focus is within a live preview and the user presses Escape
- **THEN** the collection collapses the preview and retires its context
- **AND** focus returns to the connected disclosure control for that row

#### Scenario: Collection presentation is superseded

- **WHEN** sorting, filtering, paging, reload, parent-context generation, responsive renderer replacement, range supersession, or disconnection changes the collection presentation
- **THEN** the current preview collapses without preserving expansion state
- **AND** late resource, context, or renderer work cannot reopen or overwrite a newer row generation
