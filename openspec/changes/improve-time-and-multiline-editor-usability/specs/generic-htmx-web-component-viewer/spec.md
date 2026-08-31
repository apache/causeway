## ADDED Requirements

### Requirement: Petclinic time and multiline editor usability qualification
The Petclinic browser profile SHALL qualify minute-resolution date-time parameter entry, keyboard and pointer clock-trigger operation, and single-ring multiline parameter focus through the public HTMX viewer.

#### Scenario: Visit date-time parameter is operated
- **WHEN** the browser opens the demonstrated visit-booking action and reaches its date-time parameter
- **THEN** the time field displays and selects at minute precision
- **AND** keyboard and pointer activation of its labelled clock trigger opens the time overlay without invoking the action

#### Scenario: Multiline parameter receives keyboard focus
- **WHEN** the browser focuses the demonstrated multiline reason parameter
- **THEN** exactly one visible focus ring identifies the editor
- **AND** the parameter remains editable, described, and submittable through its existing action prompt

#### Scenario: Browser qualification remains clean
- **WHEN** the time and multiline journeys complete or cancel
- **THEN** no unexpected mutation, action invocation, focus loss, console error, page error, CSP violation, external request, overlay leak, or horizontal overflow occurs
