## MODIFIED Requirements

### Requirement: Petclinic compact collection and top-action qualification
The Petclinic browser profile SHALL verify compact authoritative small-Grid sizing and integrated responsive collection-heading action placement through the public HTMX viewer.

#### Scenario: Owner has one companion animal
- **WHEN** the wide owner page renders an unpaged Pets Grid whose authoritative total fits its projected rows
- **THEN** the Grid height fits its header and data rows without a large empty scrolling viewport
- **AND** sorting, filtering, links, columns, and collection metadata remain operable

#### Scenario: Owner pet actions render at wide width
- **WHEN** the Pets collection exposes Register a pet and Remove Pet as associated actions
- **THEN** Companion animals and the compact actions share the collection's bordered header row
- **AND** keyboard traversal reaches the actions in declaration order before collection search, sort, row-link, and paging controls

#### Scenario: Collection action header becomes narrow
- **WHEN** the owner page is resized below its responsive collection breakpoint
- **THEN** the title remains first and the compact action toolbar wraps beneath it within the same header area
- **AND** no control overlaps, clips, reorders keyboard focus, or causes horizontal document overflow

#### Scenario: Collection heading remains authoritative
- **WHEN** collection state changes through loading, filtering, sorting, refresh, empty, disabled, or error presentation
- **THEN** the effective name, description, tooltip, and `aria-labelledby` relationship remain current
- **AND** associated action nodes retain identity, declaration order, pending interaction state, and focus semantics

#### Scenario: Collection action tooltip is revealed
- **WHEN** pointer or keyboard interaction reveals an associated action tooltip in the Pets collection header
- **THEN** the tooltip opens below its control and remains fully visible over the collection body
- **AND** it is not clipped above the bounded header or hidden behind collection content

#### Scenario: Petclinic collection journey remains clean
- **WHEN** browser acceptance operates the collection actions and collection controls
- **THEN** prompts, cancellation, invocation, refresh, navigation, focus restoration, and authoritative GraphQL results remain unchanged
- **AND** no console error, page error, CSP violation, external request, stale control, duplicate ID, or overlay leak occurs
