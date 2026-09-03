# vue-viewer-local-secman-authentication Specification

## Purpose

Provide optional local SecMan-backed form authentication, CSRF-safe GraphQL traffic, safe route restoration, and host-owned logout for application-owned Vue web-component shells.

## Requirements

### Requirement: Optional local SecMan authentication for Vue

The project SHALL provide local SecMan-backed form authentication as an explicitly installed Vue web-component host integration.
Generic Vue, Vue Router, foundation components, and the ordinary Vue Petclinic runtime MUST retain their existing dependency and security behavior when the integration is absent.

#### Scenario: Vue integration is installed

- **WHEN** an application imports the Vue SecMan integration and its documented prerequisites
- **THEN** configured Vue routes, authentication context, and same-origin GraphQL require an authenticated Spring browser session
- **AND** the host provides local login and logout behavior

#### Scenario: Vue integration is absent

- **WHEN** an application uses the generic Vue viewer without the integration
- **THEN** no SecMan user lookup, security chain, login route, logout route, authentication bootstrap, or session policy is added
- **AND** application-owned security remains unchanged

### Requirement: CSRF protection is mandatory

The integration SHALL require `causeway.security.spring.allow-csrf-filters=true` and MUST fail startup with a bounded diagnostic when it is false or absent.
Every login POST, authenticated GraphQL POST, and logout POST SHALL carry current Spring CSRF evidence.

#### Scenario: Secured Vue starts with CSRF enabled

- **WHEN** the integration is installed with CSRF filters allowed
- **THEN** startup validates its paths and security-chain configuration
- **AND** browser-session POST requests remain CSRF protected

#### Scenario: CSRF evidence is missing

- **WHEN** login, GraphQL, or logout is submitted without valid current CSRF evidence
- **THEN** Spring rejects the request without changing authenticated state
- **AND** no protected domain result is returned

### Requirement: Accessible server-rendered login

The integration SHALL provide a branded server-rendered login page outside the Vue and GraphQL runtime.
The page MUST require no Vue, custom-element, or GraphQL initialization and MUST submit credentials through a CSRF-protected form.

#### Scenario: Anonymous user opens login

- **WHEN** an unauthenticated browser requests the configured Vue login page
- **THEN** it receives labelled username and password fields, a submit control, application branding, and current CSRF evidence
- **AND** initial focus and instructions work without frontend JavaScript

#### Scenario: Login fails

- **WHEN** credentials are rejected
- **THEN** the login page presents one generic announced failure
- **AND** no username, account state, role, tenancy, or internal failure is disclosed

#### Scenario: Logout completed

- **WHEN** the login page is reached after logout
- **THEN** it announces successful sign-out
- **AND** no former route, user detail, CSRF token, or session identifier is exposed

### Requirement: Safe Vue route restoration

The integration SHALL restore only a valid same-origin GET route beneath the configured Vue base path after login and SHALL migrate the session identifier on successful authentication.
Absent, malformed, unsafe, authentication, GraphQL, asset, or unrelated destinations MUST fall back to the configured Vue root.

#### Scenario: Protected deep link is requested

- **WHEN** an anonymous browser requests a canonical Vue object route
- **THEN** successful login returns it to that exact safe route
- **AND** the authenticated session identifier differs from the anonymous login session

#### Scenario: Saved destination is unsafe

- **WHEN** a candidate destination is cross-origin, malformed, non-GET, outside Vue routes, or an authentication, GraphQL, or asset path
- **THEN** login returns to the Vue root
- **AND** no untrusted redirect is followed

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

### Requirement: CSRF-safe GraphQL and authentication-loss handling

The secured Vue host SHALL decorate every same-origin GraphQL POST with the current context's CSRF header and token.
A GraphQL `401` SHALL cause full-document login navigation with the current safe Vue route, while an authenticated `403` SHALL remain an authorization or CSRF failure.

#### Scenario: GraphQL operation executes

- **WHEN** the secured semantic component client posts a query or mutation
- **THEN** the current CSRF header and token accompany the request
- **AND** authorized results retain their ordinary semantic handling

#### Scenario: Session expires during GraphQL

- **WHEN** a GraphQL request receives `401` after authentication is lost
- **THEN** the host performs full-document navigation to login with a safe continuation
- **AND** obsolete route or component errors do not replace the authentication outcome

#### Scenario: Authenticated request is forbidden

- **WHEN** an authenticated GraphQL request receives `403`
- **THEN** the viewer retains bounded authorization or CSRF error behavior
- **AND** does not claim the session expired

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

### Requirement: Scoped Vue security chain

The integration SHALL protect only its configured Vue routes, authentication context, GraphQL endpoint, and required assets while permitting login and immutable public presentation assets.
It MUST NOT claim unrelated Wicket, HTMX, actuator, console, application, or bearer-token paths.

#### Scenario: Protected Vue data is requested anonymously

- **WHEN** an anonymous request targets a Vue route, authentication context, or GraphQL
- **THEN** the integration applies its authentication entry behavior
- **AND** no protected domain or session context is returned

#### Scenario: Unrelated path is requested

- **WHEN** a request is outside configured matchers
- **THEN** the integration neither authorizes nor rejects it
- **AND** another application security chain may remain authoritative

### Requirement: Secured Vue Petclinic acceptance runtime

The Vue Petclinic sample SHALL provide a deterministic secured application variant using the same shared domain, production frontend assets, local user credentials, and profile context as secured HTMX.
It SHALL provide integration and headless browser coverage without requiring HTMX, Wicket, bypass security, OAuth, or a live identity provider.

#### Scenario: Secured Vue runtime starts

- **WHEN** the secured Vue run profile starts
- **THEN** anonymous Vue and GraphQL access is protected and login remains available without GraphQL
- **AND** the ordinary Vue runtime remains available through its unchanged profile

#### Scenario: Secured browser journey runs

- **WHEN** headless automation exercises invalid and valid login, deep-link restoration, authorized reads and mutations, CSRF rejection, host logout, history, and session expiry
- **THEN** each transition satisfies the authentication, routing, CSRF, and confidentiality contracts
- **AND** unexpected browser, GraphQL, HTTP, persistence, or security failures fail the suite
