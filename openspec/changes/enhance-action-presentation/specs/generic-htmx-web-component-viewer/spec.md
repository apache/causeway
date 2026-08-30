## ADDED Requirements

### Requirement: Same-origin Font Awesome action assets
The generic HTMX shell SHALL provide the pinned Font Awesome stylesheet and fonts from same-origin packaged WebJar resources for action and menu icon presentation.
Asset delivery MUST remain compatible with production CSP, offline operation, application styling, and browser external-request isolation.

#### Scenario: HTMX shell renders
- **WHEN** the generic shell is requested
- **THEN** it links the pinned same-origin Font Awesome stylesheet before application styles
- **AND** no CDN or external font request is introduced

#### Scenario: Font Awesome asset is requested
- **WHEN** a declared action icon causes the browser to resolve a font resource
- **THEN** the asset is served from the packaged application origin
- **AND** the response participates in established cache and security policy

### Requirement: Selective Petclinic action presentation demonstration
Petclinic HTML pages and domain actions SHALL demonstrate selected authored names, canonical descriptions, disabled tooltip sections, parameterized prompt descriptions, and left and right Font Awesome icon positions without changing unselected actions.

#### Scenario: Selected object action renders
- **WHEN** a representative Petclinic page composes a selected `<cw-action>`
- **THEN** its authored `named` value takes precedence for visible control and prompt text
- **AND** canonical tooltip and icon metadata remain domain-driven

#### Scenario: Selected service action renders
- **WHEN** a representative Petclinic service action appears in the application menu
- **THEN** its canonical description and positioned Font Awesome icon appear in native and Vaadin-backed menu presentations
- **AND** parameterized invocation uses the same effective action presentation

#### Scenario: Selected action is disabled
- **WHEN** Petclinic state disables a representative described action
- **THEN** its tooltip separates canonical description from the disabled reason
- **AND** browser acceptance verifies that the action cannot be invoked

#### Scenario: Unselected action renders
- **WHEN** a Petclinic action has no new authored name, description, or icon metadata
- **THEN** its established label, tooltip absence, and invocation behavior remain unchanged
- **AND** no default icon is fabricated
