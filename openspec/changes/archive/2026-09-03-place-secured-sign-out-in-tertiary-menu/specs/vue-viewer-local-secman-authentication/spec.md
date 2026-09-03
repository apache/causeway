## MODIFIED Requirements

### Requirement: Bounded authenticated Vue context

The integration SHALL expose an authenticated no-store context containing only the current username, CSRF header name, CSRF parameter name, CSRF token, login path, and logout path.
The ordinary Vue document SHALL identify no context endpoint, while the secured document SHALL bind the configured same-origin endpoint without embedding user or token values into committed assets.
The secured shell MAY retain the username for host policy but SHALL NOT render separate visible current-user chrome.

#### Scenario: Secured Vue bootstraps

- **WHEN** an authenticated secured Vue document loads
- **THEN** the host fetches current authentication context before its first GraphQL request
- **AND** the stable shell uses the context for CSRF-safe GraphQL and native logout submission
- **AND** visible logout presentation remains in the authoritative semantic menu rather than separate shell chrome

#### Scenario: Ordinary Vue bootstraps

- **WHEN** the ordinary bypass Vue application loads
- **THEN** it makes no authentication-context request, presents no secured-session chrome, and does not opt framework Logout into the semantic menu
- **AND** existing routes and GraphQL behavior remain unchanged

#### Scenario: Context is returned

- **WHEN** an authenticated browser requests the context endpoint
- **THEN** the response is private and no-store and contains only the bounded documented fields
- **AND** the token is absent from URLs, Vue Router state, local storage, GraphQL documents, variables, and semantic results

### Requirement: Host-owned Vue logout

The authenticated Vue host SHALL retain the exact authorized `causeway.security.LogoutMenu#logout` action in its authoritative tertiary semantic menu location, present it as **Sign out**, and route activation to a native form that posts to the configured logout endpoint with current CSRF evidence.
The shell SHALL NOT render separate visible current-user or logout chrome.
The Vue action policy SHALL claim exact framework Logout requests rather than invoking GraphQL.
Successful logout MUST clear authentication, invalidate the session, apply configured cookie cleanup, and redirect to the signed-out login page.

#### Scenario: Secured Vue menus load

- **WHEN** authenticated authorized menus contain exact framework Logout
- **THEN** the tertiary menu remains visible and presents **Sign out** in the action's authoritative menu and section position
- **AND** primary, secondary, tertiary, and unrelated action structure remains available
- **AND** no separate visible current-user or logout control duplicates it

#### Scenario: User activates host logout

- **WHEN** an authenticated user activates **Sign out** in the tertiary menu
- **THEN** the host submits its native current-CSRF POST form without GraphQL validation or invocation
- **AND** Spring validates CSRF, invalidates the session, and redirects to signed-out login
- **AND** subsequent Vue and GraphQL requests require authentication

#### Scenario: Framework Logout request is published

- **WHEN** stale or custom markup publishes the exact framework Logout action request
- **THEN** the Vue host claims it before GraphQL and submits the native logout contract
- **AND** ordinary action invocation emits no successful result

#### Scenario: Framework logout is not authorized

- **WHEN** authoritative menu and action state omits or hides exact framework Logout
- **THEN** the host does not manufacture a Sign out entry or tertiary menu structure
- **AND** no hidden action or authorization metadata is exposed

#### Scenario: Browser returns after logout

- **WHEN** the user navigates back after successful logout
- **THEN** protected Vue content is not restored as authenticated
- **AND** a new login is required
