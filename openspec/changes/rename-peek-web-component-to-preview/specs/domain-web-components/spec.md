## MODIFIED Requirements

### Requirement: Declarative collection row preview
The `<cw-collection>` component SHALL accept at most one direct `<cw-preview>` declaration that enables an accessible simplified object view for eligible collection rows without changing authoritative collection data or canonical navigation.
The declaration MUST remain hidden and inert until the collection creates a live preview for one selected row.

#### Scenario: Collection has no preview declaration
- **WHEN** a collection has no direct `<cw-preview>` child
- **THEN** its projection, rendering, interaction, loading, and navigation remain unchanged
- **AND** no preview requirement or disclosure control is introduced

#### Scenario: Collection has an inline preview declaration
- **WHEN** a collection has one non-empty direct `<cw-preview>` containing semantic layout and domain components
- **THEN** each eligible object row exposes a labelled disclosure control backed by a reusable clone of that declaration
- **AND** the declaration itself remains hidden, inert, and outside every live row context

#### Scenario: Collection has an empty preview declaration
- **WHEN** a collection has one direct `<cw-preview>` with no meaningful authored content
- **THEN** the collection asks its optional host resolver for a default using each row's authoritative runtime logical type
- **AND** a row exposes a disclosure control only when a safe default is available for its runtime type

#### Scenario: Collection has duplicate preview declarations
- **WHEN** more than one direct `<cw-preview>` belongs to the same collection
- **THEN** the collection fails closed with a bounded diagnostic
- **AND** it exposes no row preview controls or live preview subtree

#### Scenario: Inline and default previews both exist
- **WHEN** a non-empty inline declaration is present and a runtime-type default also exists
- **THEN** the inline declaration completely replaces the default for that collection
- **AND** the component does not merge, reorder, or reinterpret their contents

### Requirement: Single hydrated row preview lifecycle
An opted-in collection SHALL keep at most one row preview expanded and SHALL provide its live `<cw-preview>` subtree with a dedicated hydrated object context created from the selected authoritative row identity, data, and selection.

#### Scenario: User expands an eligible row
- **WHEN** the user activates a collapsed row's preview disclosure
- **THEN** the collection creates one live `<cw-preview>` after that row using a fresh clone of its effective template
- **AND** it assigns the dedicated row context before descendant properties, actions, or collections connect
- **AND** focus remains on the disclosure while ordinary forward navigation reaches the preview content

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

#### Scenario: User presses Escape inside the preview
- **WHEN** focus is within a live preview and the user presses Escape
- **THEN** the collection collapses the preview and retires its context
- **AND** focus returns to the connected disclosure control for that row

#### Scenario: Collection presentation is superseded
- **WHEN** sorting, filtering, paging, reload, parent-context generation, responsive renderer replacement, range supersession, or disconnection changes the collection presentation
- **THEN** the current preview collapses without preserving expansion state
- **AND** late resource, context, or renderer work cannot reopen or overwrite a newer row generation

## RENAMED Requirements

- FROM: `### Requirement: Declarative collection row peek`
- TO: `### Requirement: Declarative collection row preview`
