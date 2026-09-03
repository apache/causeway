## MODIFIED Requirements

### Requirement: Host-owned current-user and logout behavior

The authenticated HTMX host SHALL present **Sign out** as the exact `causeway.security.LogoutMenu#logout` action in its authoritative tertiary semantic menu location and SHALL route activation to a native CSRF-protected POST logout form outside domain action invocation.
It SHALL NOT add a separate visible username or logout block to the shell header.
Successful logout MUST clear the Spring Security context, invalidate the HTTP session, apply configured cookie cleanup, prevent authenticated history restoration, and redirect to the login page.

#### Scenario: Authenticated shell renders

- **WHEN** a local user opens an authenticated full HTMX shell and the authorized framework Logout action is present in the effective menus
- **THEN** the tertiary menu remains visible and presents **Sign out** in the action's authoritative location
- **AND** no separate visible current-user or logout chrome replaces or duplicates the menu
- **AND** the visible control requires no domain action invocation

#### Scenario: Logout succeeds

- **WHEN** the authenticated user activates **Sign out** in the tertiary menu with valid current CSRF evidence
- **THEN** the host submits its native POST logout contract without GraphQL validation or invocation
- **AND** the server invalidates the authenticated session and redirects to the signed-out login page
- **AND** subsequent HTMX and GraphQL requests require authentication

#### Scenario: Browser returns to cached history after logout

- **WHEN** the user navigates back after logout
- **THEN** protected shell and route content is not restored as an authenticated page
- **AND** access requires a new successful login

### Requirement: Legacy framework logout is intercepted by the HTMX host

While local HTMX authentication is active, the host SHALL retain the exact authorized `causeway.security.LogoutMenu#logout` action in its authoritative semantic menu location, present it as **Sign out**, and MUST prevent its request from reaching GraphQL invocation.
The host MUST use its native current-CSRF POST logout contract for activation and MUST NOT alter the action metamodel, schema, authorization, disabled state, hierarchy, or presentation in other viewers.

#### Scenario: Authenticated menus load

- **WHEN** the HTMX host presents authorized menus under local authentication
- **THEN** the framework Logout action remains in its effective tertiary menu position under the **Sign out** label
- **AND** primary, secondary, tertiary, menu, section, and unrelated action structure remains available

#### Scenario: Framework logout request is published

- **WHEN** the menu or stale custom HTMX markup publishes an action request for the exact framework logout service and member
- **THEN** the host prevents the ordinary GraphQL action flow
- **AND** submits or directs the browser to the native CSRF-protected logout contract

#### Scenario: Framework logout is not authorized

- **WHEN** authoritative menu and action state omits or hides exact framework Logout
- **THEN** the host does not manufacture a Sign out entry or tertiary menu structure
- **AND** no hidden action or authorization metadata is exposed

#### Scenario: Application defines another logout-like action

- **WHEN** an application action has a similar label but a different logical type or member identity
- **THEN** the host does not suppress, relabel, or reinterpret that domain action

## RENAMED Requirements

- FROM: `### Requirement: Legacy framework logout is excluded from HTMX invocation`
- TO: `### Requirement: Legacy framework logout is intercepted by the HTMX host`
