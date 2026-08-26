## MODIFIED Requirements

### Requirement: Causeway-owned interaction semantics
Each field adapter SHALL map Causeway labels, descriptions, required and disabled state, validation, pending values, focus, keyboard clearing, cancellation, and semantic events into its internal control.
It MUST NOT establish a second interaction or validation state machine.

#### Scenario: User changes an editable property
- **WHEN** an internal control emits a value change
- **THEN** the owning Causeway property interaction parses, validates, and saves through its existing context
- **AND** success, failure, cancellation, focus restoration, and semantic events match native behavior

#### Scenario: User changes an action parameter
- **WHEN** an internal control changes a prepared object or service action parameter
- **THEN** the interaction controller updates that parameter through the existing argument negotiation path
- **AND** dependent parameter preparation and submission emit only advertised arguments

#### Scenario: Optional field exposes clearing
- **WHEN** a qualified optional non-protected field has a non-empty value and presents a visible clear affordance
- **THEN** keyboard users can reach that affordance through normal forward and reverse Tab navigation
- **AND** its accessible name identifies the semantic field being cleared
- **AND** keyboard or pointer activation clears through the existing Causeway pending-value path

#### Scenario: Keyboard user clears a field
- **WHEN** the keyboard user activates the Causeway-owned clear affordance
- **THEN** the now-empty clear affordance is removed from the tab sequence
- **AND** focus returns to the field without cancelling the interaction, submitting it, or exposing a second value state

#### Scenario: Validation rejects a value
- **WHEN** client codec or authoritative GraphQL validation rejects the pending value
- **THEN** the Causeway host owns the bounded error and invalid state
- **AND** the internal control cannot submit or display a successful-looking alternative value

#### Scenario: Protected text is edited
- **WHEN** a protected semantic editor is prepared, changed, validated, cancelled, fails, or submits
- **THEN** its prior or pending value is absent from markup, semantic events, errors, diagnostics, operation summaries, and route evidence
- **AND** the internal control initializes empty with appropriate password-autocomplete behavior
