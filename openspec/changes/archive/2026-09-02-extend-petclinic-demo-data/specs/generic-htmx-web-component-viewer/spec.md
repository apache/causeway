## ADDED Requirements

### Requirement: Representative deterministic Petclinic demo data
The Petclinic acceptance application SHALL seed a deterministic clinic graph rich enough to present representative owners, pets, historical visits, and upcoming visits across configured collection pages.
The richer graph MUST preserve established fixture identities and MUST remain idempotent across application startup.

#### Scenario: Petclinic sample starts with an empty database
- **WHEN** seed initialization runs for the first time
- **THEN** it creates varied owners and pets with stable IDs, species, contact details, notes, and optional values
- **AND** it creates both historical and upcoming visits using deterministic clock-relative dates and stable reasons

#### Scenario: Established acceptance fixtures are inspected
- **WHEN** integration or browser tests resolve Mary, Basil, Samantha, Helen, Max, or another pre-existing fixture by stable identity
- **THEN** the established identity and demonstrated values remain available
- **AND** additive demo data does not change canonical links, mutation targets, or row identity

#### Scenario: Seed initialization runs again
- **WHEN** the application starts after the established seed marker already exists
- **THEN** no duplicate owner, pet, or visit is created
- **AND** collection totals remain deterministic

#### Scenario: Demo collections are inspected
- **WHEN** owners, one owner's pets, one owner's visit history, and clinic-wide upcoming visits are loaded
- **THEN** each representative collection contains enough authoritative rows to cross its configured page boundary
- **AND** both sparse and populated owner pages remain available for compact and multi-page demonstrations

## MODIFIED Requirements

### Requirement: Selective Petclinic collection paging overrides
Petclinic HTML resource pages SHALL demonstrate declarative paging with sample-appropriate bounded sizes on owner, pet, and visit collections that can grow materially.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner collection
- **THEN** its HTML override declares `paged="5"`
- **AND** it does not rely on inert `offset` or `size` attributes

#### Scenario: Owner pet collection renders
- **WHEN** an owner page composes its companion-animal collection
- **THEN** that collection declares `paged="5"`
- **AND** sorting, filtering, associated actions, semantic columns, and row peeks remain unchanged

#### Scenario: Owner visit history renders
- **WHEN** an owner page composes visit history
- **THEN** that collection declares `paged="10"`
- **AND** its associated actions, semantic columns, and row peeks remain unchanged

#### Scenario: Nested pet visits render
- **WHEN** an expanded pet row peek composes the selected pet's visit collection
- **THEN** that nested collection declares `paged="10"`
- **AND** its dedicated row context and semantic columns remain unchanged

#### Scenario: Upcoming-visit summary renders
- **WHEN** the Petclinic home page composes the clinic-wide upcoming-visit collection
- **THEN** its HTML override declares `paged="10"`
- **AND** filtering, semantic columns, and row peeks remain unchanged

#### Scenario: Browser navigates a paged collection
- **WHEN** normalized metadata reports another page
- **THEN** the application exposes accessible Causeway previous and next controls and the configured range size
- **AND** navigation does not duplicate rows, associated actions, requests, row previews, or page-level headings

### Requirement: Petclinic compact collection and top-action qualification
The Petclinic browser profile SHALL verify compact authoritative small-Grid sizing and integrated responsive collection-heading action placement through the public HTMX viewer.

#### Scenario: Owner has few companion animals
- **WHEN** the wide owner page renders a paged Pets Grid whose authoritative total fits within its configured page size
- **THEN** the Grid height fits its header and current data rows without reserving a full five-row empty scrolling viewport
- **AND** sorting, filtering, paging, links, columns, and collection metadata remain operable

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
