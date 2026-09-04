## MODIFIED Requirements

### Requirement: Secured Petclinic acceptance runtime

The project SHALL provide a deterministic secured Petclinic application configuration and acceptance suite that exercises the local authentication capability without requiring Wicket, bypass security, OAuth, or a live identity provider.
The deterministic secured user SHALL receive the established regular-user and configuration-access roles in addition to the Petclinic application role, so authorized framework tertiary actions remain available.
The existing Petclinic default runtime and comparison journey MUST remain available, with changes limited to the intended consolidated utility-menu presentation.

#### Scenario: Secured runtime starts

- **WHEN** the secured Petclinic configuration starts with active CSRF filters and seeded SecMan users
- **THEN** anonymous HTMX and GraphQL access is protected
- **AND** the login page remains reachable without GraphQL

#### Scenario: Authorized tertiary actions are preserved

- **WHEN** the deterministic secured user loads the HTMX application menus
- **THEN** exact framework **Me**, **Configuration**, and Logout actions are present in one authoritative tertiary utility menu labelled `sven`
- **AND** Logout is presented as **Sign out** with a distinct accessible outlined or equivalently bounded treatment while **Me** and **Configuration** retain their authoritative labels, identities, and ordinary treatment
- **AND** no separate System or Account menu duplicates **Me** or remains empty
- **AND** the username label comes from authenticated host context and is not exposed before authentication
- **AND** the viewer does not synthesize an action that SecMan omits or identify Logout from presentation text

#### Scenario: Browser authentication journey runs

- **WHEN** the browser suite exercises invalid login, valid login, a saved deep link, authorized reads and mutations, username-menu framework actions, logout, history, and session expiry
- **THEN** each transition satisfies the local authentication, authorization, CSRF, routing, and confidentiality requirements
- **AND** no unexpected browser, GraphQL, HTTP, persistence, or security failure is recorded

#### Scenario: Ordinary Petclinic runtime starts

- **WHEN** the existing default Petclinic application is launched without the optional authentication configuration
- **THEN** its current HTMX, Wicket comparison, fixture, and bypass behavior remain available
- **AND** utility-menu differences are limited to consolidation, the authoritative username label when available, and exact-Logout presentation
