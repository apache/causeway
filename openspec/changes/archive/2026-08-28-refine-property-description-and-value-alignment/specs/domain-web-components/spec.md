## MODIFIED Requirements

### Requirement: Read-only property semantics
The `<causeway-property>` component SHALL render property visibility, usability, description, null state, value, loading state, member-scoped errors, and semantic multiline presentation from its context state and public attributes.
A standard GraphQL `String` property value MUST be explicitly aligned to the logical start so its view and edit presentations remain consistent.

#### Scenario: Visible property value
- **WHEN** a visible property read completes successfully
- **THEN** the component renders its semantic label and value using the selected value renderer

#### Scenario: String property value
- **WHEN** a visible property has the standard GraphQL `String` output type
- **THEN** the component marks its output with the semantic string-value presentation class
- **AND** baseline and cohesive-theme presentation align the value to the logical start

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
- **WHEN** the rich object state reports that a property is disabled with a bounded reason
- **THEN** the component exposes its disabled state without presenting an edit affordance
- **AND** attaches the disabled-reason tooltip to the property label rather than rendering a separate information indicator
- **AND** makes the label tooltip available by pointer hover and ordinary keyboard focus
- **AND** retains the disabled reason as an accessible description without replacing a distinct property-description tooltip

#### Scenario: Null property
- **WHEN** the property value is null
- **THEN** the component renders the library's explicit null presentation rather than an unsupported-value error
