## MODIFIED Requirements

### Requirement: Executable Vue Petclinic acceptance application

The project SHALL provide a production-built Vue Petclinic application that reuses the same deterministic Petclinic domain, public logical types, and fixture identities as the HTMX sample without copying domain implementation.
The application SHALL demonstrate source-visible exact-type Vue pages, generic fallback, a stable application-owned shell, menus, interactions, results, canonical routing, and server refresh fallback.
For the shell and the `petclinic.HomePage`, `petclinic.PetOwner`, `petclinic.Pet`, and `petclinic.Visit` routes, the HTMX Petclinic application SHALL be the authoritative presentation reference.
Presentation equivalence SHALL preserve user-visible information architecture and responsive relationships without requiring framework wrapper markup or browser pixels to be identical.

#### Scenario: Reconciled Petclinic page loads

- **WHEN** HomePage, PetOwner, Pet, or Visit is opened through a direct canonical Vue bookmark
- **THEN** an exact-type Vue single-file component binds the declared object context and interaction controller
- **AND** it presents the same ordered headings, sections, selected semantic members, action placement, result outlets, descriptions, collection columns, paging, filtering, sorting, and row-preview affordances as the corresponding HTMX Petclinic page
- **AND** it does not expose technical or additional members omitted by that HTMX page

#### Scenario: Vue PetOwner page matches the reference composition

- **WHEN** the deterministic PetOwner fixture is rendered by Vue
- **THEN** Identity, Contact, and Details occupy the reference details column while Pets and Visits occupy the reference collections column
- **AND** the owner actions include the empty-result action and the declared standalone related-owner result
- **AND** the derived last-visit value, visit notes column, paging values, parameter presentation, and nested preview declarations match the HTMX page

#### Scenario: Vue shell is presented at a wide viewport

- **WHEN** a reconciled route is ready at the documented wide acceptance viewport
- **THEN** branding, primary menu order, utility-menu grouping, header geometry, content inset, typography, palette, footer content, and document-title suffix are equivalent to the HTMX shell
- **AND** the route uses the available width and reference column relationships without an accidental persistent container outline
- **AND** keyboard users retain a visible focus indication on an appropriate heading or route landmark

#### Scenario: Vue shell is presented at a narrow viewport

- **WHEN** a reconciled route is ready at the documented narrow acceptance viewport
- **THEN** shell navigation and page columns collapse in the same order and at an equivalent breakpoint to the HTMX presentation
- **AND** menus, actions, results, fields, collections, previews, and pagination remain visible and keyboard operable without horizontal page overflow

#### Scenario: Generic fallback fixture loads

- **WHEN** a deliberately unregistered acceptance logical type is opened through a direct canonical Vue bookmark
- **THEN** the generic Vue page renders `<cw-object>` for the same GraphQL identity
- **AND** direct refresh and browser history retain that route
- **AND** none of the four reconciled Petclinic route types is used to demonstrate fallback

#### Scenario: Existing HTMX Petclinic is rebuilt

- **WHEN** Vue presentation is reconciled with the HTMX reference
- **THEN** existing HTMX routes, page composition, shell, seed cardinalities, stable identities, and acceptance behavior remain unchanged
- **AND** the shared domain introduces no Vue dependency or presentation authority

#### Scenario: Presentation parity is regression tested

- **WHEN** the Vue browser profile executes
- **THEN** it verifies semantic parity for all four reconciled route types and high-value computed shell and layout relationships at representative wide and narrow viewports
- **AND** comparisons use bounded structural or computed-style assertions rather than pixel-perfect screenshot equality
- **AND** it covers both supported component-toolkit policies where their presentation differs

#### Scenario: Acceptance suite runs

- **WHEN** the Vue browser profile executes
- **THEN** it covers direct links, refresh, back and forward, exact custom-page precedence, deliberate generic fallback, menus, property and action interactions, object and collection results, partial errors, invalid routes, absent objects, responsive layout, keyboard focus, announcements, and both supported component toolkit policies
- **AND** browser console errors, page errors, failed resource requests, and accessibility violations fail the suite
