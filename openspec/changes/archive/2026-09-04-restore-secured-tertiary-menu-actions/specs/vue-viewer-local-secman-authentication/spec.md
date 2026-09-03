## MODIFIED Requirements

### Requirement: Secured Vue Petclinic acceptance runtime

The Vue Petclinic sample SHALL provide a deterministic secured application variant using the same shared domain, production frontend assets, local user credentials, and profile context as secured HTMX.
The deterministic secured user SHALL receive the established regular-user and configuration-access roles in addition to the Petclinic application role, so authorized framework tertiary actions remain available.
It SHALL provide integration and headless browser coverage without requiring HTMX, Wicket, bypass security, OAuth, or a live identity provider.

#### Scenario: Secured Vue runtime starts

- **WHEN** the secured Vue run profile starts
- **THEN** anonymous Vue and GraphQL access is protected and login remains available without GraphQL
- **AND** the ordinary Vue runtime remains available through its unchanged profile

#### Scenario: Authorized tertiary actions are preserved

- **WHEN** the deterministic secured user loads the Vue application menus
- **THEN** exact framework **Me**, **Configuration**, and Logout actions are present in their authoritative tertiary menu locations
- **AND** Logout is presented as **Sign out** while **Me** and **Configuration** retain their authoritative labels and identities
- **AND** the viewer does not synthesize an action that SecMan omits

#### Scenario: Secured browser journey runs

- **WHEN** headless automation exercises invalid and valid login, deep-link restoration, authorized reads and mutations, tertiary framework actions, CSRF rejection, host logout, history, and session expiry
- **THEN** each transition satisfies the authentication, authorization, routing, CSRF, and confidentiality contracts
- **AND** unexpected browser, GraphQL, HTTP, persistence, or security failures fail the suite
