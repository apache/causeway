## ADDED Requirements

### Requirement: Declarative collection row peek
The `<cw-collection>` component SHALL accept at most one direct `<cw-peek>` declaration that enables an accessible simplified object view for eligible collection rows without changing authoritative collection data or canonical navigation.
The declaration MUST remain hidden and inert until the collection creates a live preview for one selected row.

#### Scenario: Collection has no peek declaration
- **WHEN** a collection has no direct `<cw-peek>` child
- **THEN** its projection, rendering, interaction, loading, and navigation remain unchanged
- **AND** no preview requirement or disclosure control is introduced

#### Scenario: Collection has an inline peek declaration
- **WHEN** a collection has one non-empty direct `<cw-peek>` containing semantic layout and domain components
- **THEN** each eligible object row exposes a labelled disclosure control backed by a reusable clone of that declaration
- **AND** the declaration itself remains hidden, inert, and outside every live row context

#### Scenario: Collection has an empty peek declaration
- **WHEN** a collection has one direct `<cw-peek>` with no meaningful authored content
- **THEN** the collection asks its optional host resolver for a default using each row's authoritative runtime logical type
- **AND** a row exposes a disclosure control only when a safe default is available for its runtime type

#### Scenario: Collection has duplicate peek declarations
- **WHEN** more than one direct `<cw-peek>` belongs to the same collection
- **THEN** the collection fails closed with a bounded diagnostic
- **AND** it exposes no row preview controls or live preview subtree

#### Scenario: Inline and default previews both exist
- **WHEN** a non-empty inline declaration is present and a runtime-type default also exists
- **THEN** the inline declaration completely replaces the default for that collection
- **AND** the component does not merge, reorder, or reinterpret their contents

### Requirement: Single hydrated row preview lifecycle
An opted-in collection SHALL keep at most one row preview expanded and SHALL provide its live `<cw-peek>` subtree with a dedicated hydrated object context created from the selected authoritative row identity, data, and selection.

#### Scenario: User expands an eligible row
- **WHEN** the user activates a collapsed row's preview disclosure
- **THEN** the collection creates one live `<cw-peek>` after that row using a fresh clone of its effective template
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

### Requirement: Preview mutations refresh the parent collection
A collection SHALL perform one generation-safe forced reload after a successful action invocation or property update originating inside its live peek.
The reload SHALL retain current collection criteria and page policy while collapsing the preview.

#### Scenario: Action inside a preview succeeds
- **WHEN** a `<cw-action>` inside the live peek publishes a successful action result
- **THEN** established result routing, navigation, application claims, and announcements remain host-owned
- **AND** the parent collection retires the preview and reloads its authoritative rows
- **AND** stale projected cells cannot remain current after the refresh completes

#### Scenario: Property inside a preview is updated
- **WHEN** an editable `<cw-property>` inside the live peek publishes a successful property update
- **THEN** the parent collection applies the same collapse and authoritative reload boundary used for a successful action

#### Scenario: Mutation refresh is superseded
- **WHEN** navigation, route replacement, a newer collection load, or disconnection supersedes the mutation-triggered refresh
- **THEN** obsolete refresh work cannot render rows, restore a preview, or move focus in the newer lifecycle

#### Scenario: Mutation removes the invoking control
- **WHEN** a mutation refresh retires the focused preview subtree
- **THEN** the stable collection host is available as the programmatic fallback focus target
- **AND** an action-result host uses that connected collection fallback instead of retaining the soon-to-be-disconnected action control
