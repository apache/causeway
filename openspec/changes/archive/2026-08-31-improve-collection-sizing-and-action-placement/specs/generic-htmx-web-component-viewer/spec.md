## ADDED Requirements

### Requirement: Petclinic compact collection and top-action qualification
The Petclinic browser profile SHALL verify compact authoritative small-Grid sizing and responsive collection-associated action placement through the public HTMX viewer.

#### Scenario: Owner has one companion animal
- **WHEN** the wide owner page renders an unpaged Pets Grid whose authoritative total fits its projected rows
- **THEN** the Grid height fits its header and data rows without a large empty scrolling viewport
- **AND** sorting, filtering, links, columns, and collection metadata remain operable

#### Scenario: Owner pet actions render
- **WHEN** the Pets collection exposes Register a pet and Remove Pet as associated actions
- **THEN** the actions appear in declaration order in a right-aligned toolbar before the collection surface
- **AND** keyboard traversal reaches those actions before collection search, sort, row-link, and paging controls

#### Scenario: Collection action toolbar becomes narrow
- **WHEN** the owner page is resized below its responsive collection breakpoint
- **THEN** the action toolbar wraps without overlap, clipping, reordered keyboard focus, or horizontal document overflow
- **AND** native responsive collection presentation remains usable

#### Scenario: Petclinic collection journey remains clean
- **WHEN** browser acceptance operates the collection actions and collection controls
- **THEN** prompts, cancellation, invocation, refresh, navigation, focus restoration, and authoritative GraphQL results remain unchanged
- **AND** no console error, page error, CSP violation, external request, stale control, or overlay leak occurs
