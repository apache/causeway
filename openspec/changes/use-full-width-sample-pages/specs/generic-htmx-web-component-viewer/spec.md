## ADDED Requirements

### Requirement: Full-width Petclinic presentation
The Petclinic HTMX application SHALL override the shared bounded shell and content widths through its application stylesheet so wide routes use the available viewport while retaining the viewer's responsive gutters.
The application MUST NOT require a shared foundation, HTMX viewer, or web-component source change to obtain this presentation.

#### Scenario: Petclinic is opened on a wide viewport
- **WHEN** a Petclinic HTMX route is rendered at desktop width
- **THEN** the stable shell and route content extend across the available viewport inside the established inline gutters
- **AND** they are not capped by the shared fixed desktop width defaults

#### Scenario: Petclinic is opened on a narrow viewport
- **WHEN** the same application is rendered below its responsive breakpoints
- **THEN** existing gutters, stacking, contained collection presentation, and no-overflow behavior remain effective
- **AND** the full-width override does not introduce horizontal document overflow
