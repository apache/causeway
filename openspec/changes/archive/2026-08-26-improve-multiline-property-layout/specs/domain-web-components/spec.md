## MODIFIED Requirements

### Requirement: Read-only property semantics
The `<causeway-property>` component SHALL render property visibility, usability, description, null state, value, loading state, member-scoped errors, and semantic multiline presentation from its context state and public attributes.

#### Scenario: Visible property value
- **WHEN** a visible property read completes successfully
- **THEN** the component renders its semantic label and value using the selected value renderer

#### Scenario: Property description is available
- **WHEN** a property descriptor supplies a non-redundant description
- **THEN** the component exposes that description as the property's default explanatory tooltip and accessible description

#### Scenario: Described multiline property renders at wide width
- **WHEN** a read-only multiline property has a visible value, description, and built-in edit affordance at a wide layout width
- **THEN** the value remains in the normal value column beside the label
- **AND** the description appears beneath the label in the label column
- **AND** the edit affordance remains a compact content-sized control rather than stretching across the value column

#### Scenario: Described multiline property renders at narrow width
- **WHEN** the available inline size requires a multiline property to collapse
- **THEN** its label, description, value, and edit affordance remain readable in logical order
- **AND** the presentation introduces no horizontal page overflow, clipping, or overlap

#### Scenario: Hidden property
- **WHEN** the rich object state reports that a property is hidden
- **THEN** the component omits the property's label, value, and interactive content

#### Scenario: Disabled property
- **WHEN** the rich object state reports that a property is disabled
- **THEN** the component exposes its disabled state without presenting an edit affordance
- **AND** exposes the disabled reason through a separate focusable tooltip indicator and accessible description without replacing the property-description tooltip or requiring a key modifier

#### Scenario: Null property
- **WHEN** the property value is null
- **THEN** the component renders the library's explicit null presentation rather than an unsupported-value error
