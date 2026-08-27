# htmx-viewer-local-secman-authentication Specification

## Purpose
Provide optional local SecMan-backed form authentication, browser-session protection, CSRF-safe viewer interaction, and host-owned logout for the HTMX Web Components viewer.

## Requirements

### Requirement: Optional local SecMan authentication integration
The project SHALL provide local SecMan-backed authentication as an explicitly installed HTMX viewer integration.
The generic HTMX viewer MUST retain its current dependency set and security behavior when the integration is absent.

#### Scenario: Integration is installed
- **WHEN** an application imports the local SecMan authentication integration with its documented prerequisites
- **THEN** the configured HTMX routes and same-origin GraphQL endpoint require an authenticated Spring browser session
- **AND** the viewer provides local login and logout behavior

#### Scenario: Integration is absent
- **WHEN** an application uses the generic HTMX viewer without the local authentication integration
- **THEN** no SecMan or Spring form-login behavior is added by the viewer
- **AND** existing bypass, external, or application-owned security remains unchanged

### Requirement: Existing security implementations remain unchanged
The initial integration MUST implement its SecMan-to-Spring bridge within the optional HTMX integration and MUST NOT modify existing Causeway core security, Spring security, SecMan, Wicket, or OAuth implementation code.
It SHALL expose the provisional boundary clearly enough for later extraction into shared security.

#### Scenario: Initial integration is built
- **WHEN** local HTMX authentication is implemented
- **THEN** shared security modules retain their existing defaults and implementation behavior
- **AND** HTMX-specific bridge classes are isolated in the optional integration

#### Scenario: Existing application does not opt in
- **WHEN** an application upgrades without importing the optional integration
- **THEN** no new security bean, filter chain, login route, logout route, or SecMan lookup is activated

### Requirement: CSRF protection is a startup prerequisite
The integration SHALL require `causeway.security.spring.allow-csrf-filters=true` and MUST fail application startup with a bounded configuration diagnostic when the setting is false.
It MUST NOT enable the setting implicitly or alter the framework default.

#### Scenario: CSRF filters are allowed
- **WHEN** the integration is installed and `causeway.security.spring.allow-csrf-filters=true`
- **THEN** startup may proceed to validate the remaining authentication configuration
- **AND** Spring CSRF protection remains active for the integration's browser-session endpoints

#### Scenario: CSRF filters are not allowed
- **WHEN** the integration is installed and the setting is false or absent
- **THEN** application startup fails before serving authenticated HTMX traffic
- **AND** the diagnostic names the required configuration property without exposing security state or secrets

### Requirement: SecMan credentials determine local authentication
The integration SHALL authenticate a submitted username and password against the matching unlocked SecMan `ApplicationUser` and the configured SecMan `PasswordEncoder`.
The lookup MUST execute in a controlled interaction and transaction boundary and MUST produce the same public failure outcome for absent users, wrong passwords, locked users, and passwordless users.

#### Scenario: Valid unlocked user signs in
- **WHEN** an unlocked `ApplicationUser` with an encrypted password submits the matching local credentials
- **THEN** Spring Security authenticates that username
- **AND** no plaintext password is persisted or published

#### Scenario: Password is incorrect
- **WHEN** an existing local user submits an incorrect password
- **THEN** authentication fails with the generic invalid-credentials outcome
- **AND** no authenticated session is established

#### Scenario: User is absent, locked, or passwordless
- **WHEN** the submitted username is absent or identifies a locked or passwordless `ApplicationUser`
- **THEN** authentication fails with the same generic invalid-credentials outcome
- **AND** the response does not disclose which account condition applied

#### Scenario: Lookup fails internally
- **WHEN** SecMan lookup or its controlled interaction fails unexpectedly
- **THEN** authentication fails closed
- **AND** the browser receives no persistence exception, password data, user record, role, tenancy, or stack trace

### Requirement: Authenticated principal preserves SecMan user context
A successful local login SHALL establish a Causeway `UserMemento` for the authenticated username with the effective SecMan roles, tenancy path, language, number locale, and time locale available at authentication time.
The provisional converter MUST operate without an unscoped repository access and MUST erase credential material according to Spring Security lifecycle behavior.

#### Scenario: User has roles and profile preferences
- **WHEN** a local user with SecMan roles, tenancy, and locale preferences signs in
- **THEN** authenticated HTMX and GraphQL requests execute with the corresponding refined `UserMemento`
- **AND** SecMan authorization sees the same effective username and roles

#### Scenario: Authentication completes
- **WHEN** Spring finishes validating the local credentials
- **THEN** raw credentials are absent from the session principal and authentication diagnostics
- **AND** retained browser-session state contains no plaintext password

### Requirement: Accessible server-rendered login
The integration SHALL provide a branded server-rendered login page outside the GraphQL-dependent shell.
The page MUST require no GraphQL request, MUST submit credentials through a CSRF-protected server POST, and MUST provide accessible local authentication outcomes.

#### Scenario: Anonymous user opens login
- **WHEN** an unauthenticated browser requests the configured login page
- **THEN** it receives labelled username and password controls, a submit control, the application brand, and valid CSRF evidence
- **AND** focus and instructions do not depend on custom-element upgrade or GraphQL availability

#### Scenario: Login fails
- **WHEN** submitted credentials are rejected
- **THEN** the login page presents one generic announced error
- **AND** it does not disclose whether the username, password, account status, role, or tenancy caused rejection

#### Scenario: Logout completed
- **WHEN** the login page is reached after successful logout
- **THEN** it presents a bounded signed-out confirmation
- **AND** no former route, user detail, CSRF token, or session identifier is exposed

### Requirement: Safe route restoration and session fixation protection
The integration SHALL preserve one valid same-origin HTMX GET route across authentication and SHALL migrate the browser session identifier when authentication succeeds.
It MUST fall back to the configured HTMX root for an absent, invalid, unsafe, or non-HTMX saved request.

#### Scenario: Deep link requires authentication
- **WHEN** an unauthenticated browser requests a canonical object route beneath the configured HTMX base path
- **THEN** successful login returns the browser to that exact safe route
- **AND** the authenticated session uses a migrated session identifier

#### Scenario: Saved destination is unsafe
- **WHEN** a saved request is cross-origin, malformed, non-GET, a login or logout endpoint, a GraphQL endpoint, or outside the configured HTMX base path
- **THEN** successful login routes to the configured HTMX root
- **AND** no untrusted redirect is followed

### Requirement: CSRF evidence covers browser-session POST requests
The authenticated shell SHALL expose the current Spring CSRF header name, parameter name, and token through bounded same-origin metadata compatible with the viewer content-security policy.
The viewer MUST submit current CSRF evidence with every same-origin GraphQL POST, unsafe HTMX request, login submission, and logout submission.

#### Scenario: GraphQL query or mutation is posted
- **WHEN** the authenticated browser sends any GraphQL POST through the viewer client
- **THEN** the request carries the current documented CSRF header and token
- **AND** no token appears in the GraphQL document, variables, result events, URL, or browser history

#### Scenario: Login or logout is submitted
- **WHEN** the browser submits the local login form or authenticated logout form
- **THEN** the request contains Spring's current CSRF parameter and token
- **AND** a missing or invalid token is rejected without changing authentication state

#### Scenario: Authenticated shell is replaced after login
- **WHEN** successful login creates or migrates the session
- **THEN** the resulting full shell contains CSRF evidence for that current session
- **AND** an earlier anonymous or expired token is not reused as authoritative state

### Requirement: Security chain is scoped to configured viewer paths
The integration SHALL protect the configured HTMX base path and same-origin GraphQL endpoint while permitting only its login endpoints and required public viewer assets.
It MUST NOT claim unrelated Wicket, actuator, console, application, or bearer-token API paths.

#### Scenario: HTMX or GraphQL path is requested anonymously
- **WHEN** an anonymous browser requests a protected HTMX route or configured GraphQL endpoint
- **THEN** the integration applies its authentication entry behavior
- **AND** protected domain data is not returned

#### Scenario: Required login asset is requested
- **WHEN** an anonymous browser requests a documented stylesheet, image, or other asset required by the login page
- **THEN** the asset can be served without establishing an authenticated session
- **AND** the public asset exposes no user, domain, token, or configuration secret

#### Scenario: Unrelated path is requested
- **WHEN** a request targets a path outside the integration's configured matchers
- **THEN** the integration does not authorize, reject, redirect, or create a session for that request
- **AND** application-owned security chains remain authoritative

### Requirement: Authentication loss triggers full-page login navigation
The viewer SHALL distinguish absent or expired authentication from authenticated authorization denial.
It MUST route authentication loss to the login page as a full-browser navigation and MUST NOT swap login markup into an HTMX route fragment.

#### Scenario: Full-page route session is absent
- **WHEN** an unauthenticated browser requests a full HTMX route
- **THEN** Spring Security redirects to login
- **AND** records only a safe restorable HTMX GET route

#### Scenario: Fragment request session expires
- **WHEN** an HTMX fragment request reaches the server after its session has expired
- **THEN** the response causes a full-browser login navigation
- **AND** login HTML is not inserted into the route region

#### Scenario: GraphQL session expires
- **WHEN** a viewer GraphQL request receives `401`
- **THEN** the shell initiates full-page login navigation using the current safe HTMX route as the requested destination
- **AND** obsolete component errors do not replace the authentication outcome

#### Scenario: Authenticated user lacks permission
- **WHEN** an authenticated request receives `403` or a bounded GraphQL access-denied result
- **THEN** the viewer presents its access-denied behavior
- **AND** does not claim that the session expired

### Requirement: Host-owned current-user and logout behavior
The authenticated HTMX shell SHALL present the current local username and a CSRF-protected POST logout control outside domain action invocation.
Successful logout MUST clear the Spring Security context, invalidate the HTTP session, apply configured cookie cleanup, prevent authenticated history restoration, and redirect to the login page.

#### Scenario: Authenticated shell renders
- **WHEN** a local user opens an authenticated full HTMX shell
- **THEN** the shell presents the current username and an accessible logout control beside the host navigation
- **AND** neither control requires domain metadata or GraphQL action invocation

#### Scenario: Logout succeeds
- **WHEN** the authenticated user submits logout with valid CSRF evidence
- **THEN** the server invalidates the authenticated session and redirects to the signed-out login page
- **AND** subsequent HTMX and GraphQL requests require authentication

#### Scenario: Browser returns to cached history after logout
- **WHEN** the user navigates back after logout
- **THEN** protected shell and route content is not restored as an authenticated page
- **AND** access requires a new successful login

### Requirement: Legacy framework logout is excluded from HTMX invocation
While local HTMX authentication is active, the host SHALL exclude the exact framework `causeway.security.LogoutMenu#logout` action from HTMX menu presentation and MUST prevent a stale request for that action from reaching GraphQL invocation.
The exclusion MUST NOT alter the action metamodel, schema, authorization, or presentation in other viewers.

#### Scenario: Authenticated menus load
- **WHEN** the HTMX host presents menus under local authentication
- **THEN** the framework logout action is absent from semantic menu controls
- **AND** the host-owned logout control remains available

#### Scenario: Stale framework logout request is published
- **WHEN** stale or custom HTMX markup publishes an action request for the exact framework logout service and member
- **THEN** the host prevents the ordinary GraphQL action flow
- **AND** uses or directs the browser to the native CSRF-protected logout contract

#### Scenario: Application defines another logout-like action
- **WHEN** an application action has a similar label but a different logical type or member identity
- **THEN** the host does not suppress or reinterpret that domain action

### Requirement: Secured Petclinic acceptance runtime
The project SHALL provide a deterministic secured Petclinic application configuration and acceptance suite that exercises the local authentication capability without requiring Wicket, bypass security, OAuth, or a live identity provider.
The existing Petclinic default runtime and comparison journey MUST remain available unchanged by default.

#### Scenario: Secured runtime starts
- **WHEN** the secured Petclinic configuration starts with active CSRF filters and seeded SecMan users
- **THEN** anonymous HTMX and GraphQL access is protected
- **AND** the login page remains reachable without GraphQL

#### Scenario: Browser authentication journey runs
- **WHEN** the browser suite exercises invalid login, valid login, a saved deep link, authorized reads and mutations, logout, history, and session expiry
- **THEN** each transition satisfies the local authentication, CSRF, routing, and confidentiality requirements
- **AND** no unexpected browser, GraphQL, HTTP, persistence, or security failure is recorded

#### Scenario: Ordinary Petclinic runtime starts
- **WHEN** the existing default Petclinic application is launched without the optional authentication configuration
- **THEN** its current HTMX, Wicket comparison, fixture, and bypass behavior remains unchanged
