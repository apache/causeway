## ADDED Requirements

### Requirement: Petclinic corrected editor and pager presentation
The Petclinic browser profile SHALL verify user-visible keyboard time selection, one multiline editor boundary, and authoritative paged collection totals through the public HTMX viewer.

#### Scenario: Visit time overlay is keyboard opened
- **WHEN** the browser tabs from the visit time input to its clock trigger and presses Enter or Space
- **THEN** the real Vaadin time overlay is visibly open with quarter-hour choices formatted to minutes
- **AND** no booking mutation occurs until the user invokes the action

#### Scenario: Visit reason has one boundary
- **WHEN** the multiline visit reason receives keyboard focus
- **THEN** only the toolkit input-container boundary and focus ring are visible
- **AND** the slotted internal textarea contributes no nested native border or outline

#### Scenario: Visit pages show authoritative totals
- **WHEN** the browser navigates first, middle, or final pages of a multi-page Visits collection
- **THEN** each live range label includes the same authoritative total
- **AND** its start and end positions match the rows shown on that page

#### Scenario: Corrected journeys remain clean
- **WHEN** the editor and collection journeys complete or cancel
- **THEN** no unexpected mutation, invocation, focus loss, console error, page error, CSP violation, external request, overlay leak, or horizontal overflow occurs
