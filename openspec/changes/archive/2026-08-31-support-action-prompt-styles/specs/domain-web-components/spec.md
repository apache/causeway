## ADDED Requirements

### Requirement: Effective action prompt-style presentation
The Web Components interaction layer SHALL normalize parameter prompt presentation to `INLINE`, `DIALOG_MODAL`, or `DIALOG_SIDEBAR`.
An accepted authored `<cw-action prompt-style>` value SHALL take precedence over canonical rich GraphQL `promptStyle` metadata for presentation only.
Prompt style MUST NOT add, remove, reorder, default, validate, confirm, or invoke parameters or actions.

#### Scenario: HTML supplies a supported prompt style
- **WHEN** an authored action declares `prompt-style="DIALOG_SIDEBAR"`, `prompt-style="DIALOG_MODAL"`, or `prompt-style="INLINE"` in any supported letter case
- **THEN** the reflected `promptStyle` property exposes the normalized effective override
- **AND** the standard interaction controller uses that style for parameter prompting

#### Scenario: HTML prompt style is absent or invalid
- **WHEN** `prompt-style` is absent, blank, or unsupported
- **THEN** compatible canonical metadata determines the effective style
- **AND** invalid HTML cannot suppress or fabricate canonical interaction semantics

#### Scenario: Canonical style requires normalization
- **WHEN** canonical metadata supplies `DIALOG`, `AS_CONFIGURED`, `NOT_SPECIFIED`, `INLINE_AS_IF_EDIT`, an unknown value, or no value
- **THEN** `DIALOG` and unsupported dialog values safely use `DIALOG_MODAL`
- **AND** `INLINE_AS_IF_EDIT` uses `INLINE` only when a property association is available
- **AND** an absent usable style safely uses `DIALOG_MODAL`

#### Scenario: Older schema lacks prompt-style metadata
- **WHEN** introspection reports no `promptStyle` field
- **THEN** object, service, and application-menu action queries omit it
- **AND** parameter prompts retain safe modal presentation unless HTML supplies a supported override

### Requirement: Inline property-associated action prompts
An effective `INLINE` parameter prompt SHALL temporarily replace the presentation of its associated property and associated action controls with the controller-owned prompt region.
The replacement SHALL support directly authored property children and effective-grid property associations without transferring interaction authority to the property or HTML.

#### Scenario: Directly nested property action opens inline
- **WHEN** a parameterized action directly beneath `<cw-property>` opens with effective style `INLINE`
- **THEN** the property's primary presentation and associated action controls are hidden without being destroyed
- **AND** a labelled non-modal prompt region occupies the member composition in their place

#### Scenario: Effective-grid property action opens inline
- **WHEN** a parameterized action structurally associated with a generated property opens with effective style `INLINE`
- **THEN** the generated property and associated-action group are temporarily replaced by the same standard prompt region
- **AND** the effective grid remains the source of association rather than action naming or application code

#### Scenario: Inline action lacks a property association
- **WHEN** an action with requested style `INLINE` is not associated with a property
- **THEN** the prompt safely uses `DIALOG_MODAL`
- **AND** no unrelated property or collection is hidden

#### Scenario: Inline prompt ends
- **WHEN** an inline interaction is cancelled, succeeds, disconnects, becomes obsolete, or transitions to canonical confirmation
- **THEN** the prompt portal is removed and every replaced node's prior hidden state is restored
- **AND** established value retention, result publication, refresh, and focus restoration behavior remains in effect

### Requirement: Modal and sidebar action prompt surfaces
An effective `DIALOG_MODAL` prompt SHALL render as a bounded movable modal dialog.
An effective `DIALOG_SIDEBAR` prompt SHALL render as a vertical modal sidebar at the viewport's inline end.
Both dialog styles SHALL preserve light-DOM accessibility, backdrop, Escape cancellation, focus containment, and originating-control focus restoration.

#### Scenario: Modal prompt opens
- **WHEN** a parameterized action opens with effective style `DIALOG_MODAL`
- **THEN** a labelled modal dialog is centered within the viewport with a bounded scrollable body
- **AND** its heading acts as a pointer drag handle whose movement remains clamped within the viewport

#### Scenario: Sidebar prompt opens
- **WHEN** a parameterized action opens with effective style `DIALOG_SIDEBAR`
- **THEN** a labelled modal dialog occupies a bounded vertical panel at the viewport's inline end
- **AND** its content scrolls without causing horizontal document overflow

#### Scenario: Keyboard user operates a dialog prompt
- **WHEN** a modal or sidebar prompt is active
- **THEN** Tab and Shift+Tab remain contained within the prompt, Escape cancels when invocation is not in progress, and initial focus enters the first applicable control
- **AND** cancellation restores focus to the originating action control

#### Scenario: Keyboard user operates an inline prompt
- **WHEN** an inline prompt is active
- **THEN** initial focus enters the first applicable control and Escape cancels when invocation is not in progress
- **AND** ordinary Tab navigation is not trapped within the non-modal region

#### Scenario: Reduced motion or narrow viewport applies
- **WHEN** modal or sidebar presentation is used under reduced-motion preference or a narrow viewport
- **THEN** the prompt remains fully reachable, readable, and operable
- **AND** movement or responsive sizing does not introduce horizontal overflow

### Requirement: Prompt-style interaction invariants
All prompt styles SHALL use one controller-owned preparation, parameter, validation, confirmation, invocation, cancellation, result, and stale-generation lifecycle.
Canonical are-you-sure confirmation SHALL remain a modal alert dialog regardless of parameter prompt style.

#### Scenario: Parameter validation fails in any style
- **WHEN** preparation, parameter validity, or whole-action validation rejects submitted values
- **THEN** the same values, protected error presentation, and first-invalid focus behavior remain in the active prompt style
- **AND** no invocation occurs

#### Scenario: Styled prompt requires confirmation
- **WHEN** a parameterized action in any style validates successfully and canonical metadata requires confirmation
- **THEN** parameter presentation closes and the standard modal confirmation alert dialog opens
- **AND** cancelling confirmation returns to the original style with values retained

#### Scenario: Invocation is submitted in any style
- **WHEN** a valid prompt is invoked
- **THEN** at most one mutation is issued for that activation
- **AND** stale responses cannot reopen, relocate, or overwrite a newer prompt

## MODIFIED Requirements

### Requirement: Parameterized action prompt presentation
A parameterized action prompt SHALL present the effective action name as its heading and a non-duplicate canonical action description as quiet explanatory text immediately below.
The heading and description SHALL participate in the accessible labelling and description of the effective inline, modal, or sidebar prompt surface.

#### Scenario: Described parameterized action opens
- **WHEN** action preparation returns one or more parameters and current action presentation includes a description
- **THEN** the prompt heading uses the effective action name
- **AND** the escaped description appears below the heading and participates in the prompt surface description

#### Scenario: Parameterless action executes
- **WHEN** action preparation returns no parameters
- **THEN** established direct invocation remains in effect
- **AND** no presentation-only parameter prompt is introduced
