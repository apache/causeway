## ADDED Requirements

### Requirement: Full-width Reference Application HTMX presentation
The Reference Application HTMX regression launcher SHALL override the shared bounded shell and content widths through its application stylesheet so wide corpus pages use the available viewport while retaining the viewer's responsive gutters.
The override SHALL remain regression-application-owned and MUST NOT alter shared web-component or HTMX viewer defaults.

#### Scenario: Reference Application is opened on a wide viewport
- **WHEN** an HTMX regression route is rendered at desktop width
- **THEN** the stable shell and route content extend across the available viewport inside the established inline gutters
- **AND** data-dense layouts and qualified collections can use space beyond the shared fixed desktop width default

#### Scenario: Reference Application is opened on a narrow viewport
- **WHEN** the regression launcher is rendered at mobile width
- **THEN** existing responsive menus, component stacking, contained scrolling, and no-overflow assertions remain valid
- **AND** the application-local width override does not broaden shared presentation behavior
