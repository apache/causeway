## ADDED Requirements

### Requirement: Petclinic selective action-parameter presentation
Petclinic custom HTML pages SHALL demonstrate optional `<cw-parameter>` presentation declarations on selected parameterized actions while leaving other parameters and actions canonical.

#### Scenario: Maintainer inspects Petclinic HTML
- **WHEN** a maintainer opens the packaged Petclinic custom-page resources
- **THEN** selected `<cw-action>` elements contain natural nested `<cw-parameter>` declarations using representative `named`, `described-as`, `description-as`, and `multi-line` attributes
- **AND** at least one sibling parameter and at least one other parameterized action remain undeclared

#### Scenario: Selected Petclinic action prompt opens
- **WHEN** browser automation activates an action with matching parameter declarations
- **THEN** the prompt applies the authored parameter name, description presentation, and compatible multiline hint
- **AND** canonical defaults, choices, validation, invocation, result, focus, and route behavior remain unchanged

#### Scenario: Undeclared Petclinic parameter renders
- **WHEN** the same or another Petclinic prompt contains an authoritative parameter without a matching declaration
- **THEN** that parameter retains its established canonical name, description, and qualified editor
- **AND** no HTML declaration is required for it to participate in validation or invocation
