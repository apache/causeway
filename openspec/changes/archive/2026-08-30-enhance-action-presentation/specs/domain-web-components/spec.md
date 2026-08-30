## ADDED Requirements

### Requirement: Authored and canonical action naming
`<cw-action>` SHALL accept an optional `named` attribute and reflected property for its visible action name.
The effective action name MUST use `named`, then compatible `label`, then canonical metadata, then the humanized action identifier.

#### Scenario: HTML name is supplied
- **WHEN** an action element declares `named="Place a new order"`
- **THEN** its ordinary control and any parameterized prompt use `Place a new order`
- **AND** the semantic action identifier remains unchanged

#### Scenario: Compatible label is supplied
- **WHEN** `named` is absent and an existing action element declares `label`
- **THEN** the compatible label remains the visible name
- **AND** canonical metadata and humanized fallback do not override it

#### Scenario: No authored name is supplied
- **WHEN** neither `named` nor `label` supplies a name
- **THEN** canonical action metadata supplies the visible name when available
- **AND** a humanized action identifier remains the bounded fallback for older schemas

### Requirement: Action tooltip composition
An ordinary action control SHALL present a bounded canonical description as an accessible tooltip.
When the action is disabled, its bounded disabled reason MUST appear in the tooltip as a separate section without replacing an available description.

#### Scenario: Enabled described action renders
- **WHEN** a visible enabled action has a canonical description
- **THEN** pointer and keyboard users can obtain that description from the action control tooltip
- **AND** the description remains escaped and associated through accessible descriptive semantics

#### Scenario: Disabled action has description and reason
- **WHEN** a visible action has both a canonical description and a disabled reason
- **THEN** its non-invoking control presents the description and disabled reason as separate tooltip sections
- **AND** activation remains unavailable

#### Scenario: Disabled action has only a reason
- **WHEN** a visible action has no description and has a disabled reason
- **THEN** the tooltip contains the disabled reason without an empty section
- **AND** the reason remains available to assistive technology

### Requirement: Parameterized action prompt presentation
A parameterized action prompt SHALL present the effective action name as its heading and a non-duplicate canonical action description as quiet explanatory text immediately below.

#### Scenario: Described parameterized action opens
- **WHEN** action preparation returns one or more parameters and current action presentation includes a description
- **THEN** the dialog heading uses the effective action name
- **AND** the escaped description appears below the heading and participates in the dialog description

#### Scenario: Parameterless action executes
- **WHEN** action preparation returns no parameters
- **THEN** established direct invocation remains in effect
- **AND** no presentation-only prompt is introduced

### Requirement: Font Awesome action icon presentation
Ordinary action controls SHALL render applicable static Font Awesome metadata as a decorative icon before or after the action name according to its canonical position.
Icon metadata MUST be bounded and tokenized and MUST NOT inject markup or alter action semantics.

#### Scenario: Left-positioned icon is supplied
- **WHEN** action metadata supplies accepted Font Awesome classes with `LEFT` position
- **THEN** a decorative icon precedes the action name
- **AND** the accessible name remains the textual action name

#### Scenario: Right-positioned icon is supplied
- **WHEN** action metadata supplies accepted Font Awesome classes with `RIGHT` position
- **THEN** a decorative icon follows the action name
- **AND** native and Vaadin action controls retain equivalent order

#### Scenario: Icon metadata is absent or invalid
- **WHEN** action metadata has no accepted static Font Awesome classes or position
- **THEN** no icon is rendered
- **AND** the action name, usability, invocation, and focus behavior remain unchanged
