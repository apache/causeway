## MODIFIED Requirements

### Requirement: Executable Petclinic reference application

The project SHALL include a deterministic current-Causeway Petclinic application ported from the documented pinned Apache source, exposing HTMX and Wicket viewers over the same domain model and using private HTML resources for all application-specific HTMX object-page composition.
The Petclinic application menu layout SHALL expose its selected utility actions through one tertiary utility menu and SHALL NOT retain a superfluous unreferenced-action System menu or a separate fixed Account menu.
The HTMX host SHALL label the consolidated utility menu with the authoritative current username when available, use Account only as a non-identifying fallback, and give exact Logout a distinct accessible outlined presentation.

#### Scenario: Petclinic sample starts

- **WHEN** the documented Maven profile launches the sample
- **THEN** Pet Owners, Pets, Visits, object home, service actions, object actions, choices, defaults, validation, effective menus, retained effective grids, and fixture data are available through GraphQL and the HTMX viewer
- **AND** the Wicket viewer is available at its documented comparison path over the same state

#### Scenario: Petclinic utility menu loads

- **WHEN** the ordinary or secured HTMX shell loads the effective application menus
- **THEN** the selected authorized **Me**, **Configuration**, and Logout actions occupy one tertiary utility menu according to host policy
- **AND** the menu title is the current username supplied by authoritative host context, or Account when no trustworthy display identity is available
- **AND** no separate System or Account menu duplicates **Me** or remains empty
- **AND** exact Logout is labelled **Sign out** and has a distinct accessible outlined or equivalently bounded treatment that adjacent actions do not inherit
- **AND** authorization can omit an action without manufacturing another menu or action
- **AND** title and appearance mapping do not change action identity, ordering, invocation, or CSRF-safe host ownership

#### Scenario: Copied source is reviewed

- **WHEN** a maintainer inspects the Petclinic sample
- **THEN** provenance identifies repository commit `16a10608129ca9ce8ae04d21df1462f4d69ac018`, copied concepts, license, omissions, and current-API porting changes
- **AND** obsolete starter, security, operational, and deployment infrastructure is not represented as current viewer behavior

#### Scenario: Petclinic HTML page is present

- **WHEN** a route addresses `petclinic.HomePage`, `petclinic.PetOwner`, `petclinic.Pet`, or `petclinic.Visit`
- **THEN** the exact convention-registered HTML resource composes ordinary HTML and semantic components beneath one route context
- **AND** no Petclinic Java page renderer or custom-page knowledge inside `<cw-object>` is required

#### Scenario: Petclinic HTML page is absent

- **WHEN** a Petclinic logical type is run without its corresponding packaged HTML resource
- **THEN** the HTMX router uses the generic `<cw-object editable>` page
- **AND** the retained effective grid and collection-column resources remain available for fallback composition

#### Scenario: Petclinic page resources are packaged

- **WHEN** the ordinary sample artifact is built and inspected
- **THEN** all four HTML pages and all retained layout fallback resources are present at their documented private locations
- **AND** no frontend package installation, JavaScript bundling, CDN retrieval, or executable Spring Boot repackaging is required for ordinary reactor packaging
