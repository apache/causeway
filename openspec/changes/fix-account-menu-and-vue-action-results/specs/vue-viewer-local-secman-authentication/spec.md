## MODIFIED Requirements

### Requirement: Secured Vue Petclinic acceptance runtime

The Vue Petclinic sample SHALL provide a deterministic secured application variant using the same shared domain, production frontend assets, local user credentials, and profile context as secured HTMX.
The deterministic secured user SHALL receive the established regular-user and configuration-access roles in addition to the Petclinic application role, so authorized framework utility actions remain available in one tertiary menu labelled with the authenticated username.
Exact Logout SHALL retain host-owned behavior while receiving a distinct accessible outlined presentation.
It SHALL provide integration and headless browser coverage without requiring HTMX, Wicket, bypass security, OAuth, or a live identity provider.

#### Scenario: Secured Vue runtime starts

- **WHEN** the secured Vue run profile starts
- **THEN** anonymous Vue and GraphQL access is protected and login remains available without GraphQL
- **AND** the ordinary Vue runtime remains available through its unchanged profile

#### Scenario: Authorized utility actions are preserved

- **WHEN** the deterministic secured user loads the Vue application menus
- **THEN** exact framework **Me**, **Configuration**, and Logout actions are present in one authoritative tertiary utility menu labelled `sven`
- **AND** Logout is presented as **Sign out** with a distinct accessible outlined or equivalently bounded treatment while **Me** and **Configuration** retain their authoritative labels, identities, and ordinary treatment
- **AND** no separate System or Account menu duplicates **Me** or remains empty
- **AND** the username label comes from authenticated host context and is not exposed before authentication
- **AND** the viewer does not synthesize an action that SecMan omits or identify Logout from presentation text

#### Scenario: Secured utility-menu object actions navigate cleanly

- **WHEN** the authenticated user activates **Me** and then **Configuration** from the username-labelled utility menu
- **THEN** Vue navigates each complete object result to its canonical object route
- **AND** `UserMemento` is presented through ordinary generic object rendering without a type-specific page
- **AND** neither transient source result chrome nor presentation belonging to the previous action remains visible

#### Scenario: Secured browser journey runs

- **WHEN** headless automation exercises invalid and valid login, deep-link restoration, authorized reads and mutations, username-menu framework actions, CSRF rejection, host logout, history, and session expiry
- **THEN** each transition satisfies the authentication, authorization, routing, result-lifecycle, CSRF, and confidentiality contracts
- **AND** unexpected browser, GraphQL, HTTP, persistence, or security failures fail the suite
