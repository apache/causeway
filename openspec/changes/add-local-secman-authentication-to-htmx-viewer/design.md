## Context

The generic HTMX viewer currently renders an application shell and resolves domain state through a same-origin rich GraphQL endpoint.
The Petclinic sample imports bypass security and Wicket, so its Wicket comparison has a login page while the HTMX route does not establish or terminate an authenticated browser session.
Core `LogoutMenu#logout` returns Wicket-oriented control results and its Wicket logout handler deliberately ignores non-Wicket request cycles, which makes invoking it through rich GraphQL both non-portable and ineffective for HTMX.

Estatio demonstrates a local Spring form-login pattern over SecMan using a `UserDetailsService`, anonymous interaction around `ApplicationUserRepository` access, Spring session management, and custom Spring-to-Causeway principal conversion.
The current Causeway code includes generic Spring authentication converters and a SecMan `UserMementoRefiner`, but changing or reconciling those shared implementations is explicitly outside this first change.
The initial solution may adapt the Estatio pattern within an optional HTMX-specific integration and later promote the proven bridge through the separate `promote-htmx-secman-spring-security-bridge` draft.

Spring CSRF filters are disabled by default by `CausewayModuleSecuritySpring` unless `causeway.security.spring.allow-csrf-filters=true`.
Session-cookie authentication requires those filters to remain active because GraphQL uses POST for reads and mutations and logout changes session state.
The first secured acceptance runtime does not claim Wicket coexistence with active Spring CSRF filters; that remains covered by `enable-csrf-safe-wicket-htmx-coexistence`.

## Goals / Non-Goals

**Goals:**

- Provide opt-in local username/password login backed by SecMan `ApplicationUser` state and its configured password encoder.
- Establish one secure Spring browser session for HTMX routes and the viewer's same-origin GraphQL endpoint.
- Preserve safe deep links across login.
- Build a complete Causeway `UserMemento` containing the SecMan roles and user profile values needed by authorization and request execution.
- Keep login, current-user presentation, session-expiry navigation, and logout owned by the HTMX host.
- Require active CSRF protection without changing the framework default or mutating shared security configuration.
- Preserve the existing unsecured or externally secured HTMX viewer when the optional integration is absent.
- Prove the behavior through a deterministic secured Petclinic runtime and browser journey.

**Non-Goals:**

- OAuth 2.0, OIDC, bearer-token APIs, external identity providers, delegated-user auto-creation, or provider logout.
- Changes to existing core security, Spring security, SecMan, Wicket, GraphQL, or OAuth implementation modules.
- Wicket and HTMX coexistence under active Spring CSRF filters.
- Literal HTTP Basic authentication.
- User registration, password reset, multi-factor authentication, remember-me cookies, account administration, or brute-force infrastructure beyond bounded authentication failures.
- Replacing the existing framework-wide `LogoutMenu` contract.

## Decisions

### Add an optional HTMX SecMan security module

Add a dedicated `viewers/webcomponents/htmx-security-secman` module with an artifact such as `causeway-viewer-webcomponents-htmx-security-secman`.
The module depends on the generic HTMX viewer, Causeway Spring security, SecMan integration and applib, Spring Security web support, and the SecMan password-encoder contract.
Applications opt in by importing this module; the generic HTMX artifact retains no mandatory SecMan dependency and its current behavior remains unchanged.

The integration contributes typed authentication state and policy to the HTMX controller and shell through a narrow viewer-owned SPI rather than injecting arbitrary security HTML into the generic renderer.
The SPI exposes only the authenticated display identity, CSRF header and token values, login and logout paths, and exact host menu exclusions needed by the shell.

An alternative is to add SecMan and Spring Security directly to the generic HTMX module.
That is rejected because it would make one authentication technology part of the viewer's baseline and would blur the planned extraction boundary.

### Keep the provisional bridge HTMX-specific

Provide an HTMX-specific `UserDetailsService` that performs SecMan lookup inside a controlled anonymous interaction, modelled on Estatio's `InteractionService.callAnonymousAndCatch(...)` pattern under current Causeway APIs.
The lookup returns a credentials container carrying the username, encoded password, enabled or locked state, SecMan roles, tenancy path, language, number locale, and time locale needed to construct the request `UserMemento` without a second unscoped repository read.

Spring's username/password provider validates the supplied password through the configured SecMan `PasswordEncoder` bean.
The bridge MUST NOT introduce an accept-any encoder, compare plaintext itself, disclose whether a username exists, or allow locked and passwordless accounts to authenticate.
A high-priority HTMX-specific Spring authentication converter maps the successful principal to the complete Causeway `UserMemento` expected by SecMan authorization and interaction execution.

An alternative is to call Causeway's `AuthenticationManager` directly from an HTMX login controller and maintain a separate viewer session.
That is rejected because it would duplicate Spring session, CSRF, saved-request, and logout behavior and would make subsequent OAuth convergence harder.

### Scope one Spring Security chain to the viewer

The optional module contributes a documented-order Spring `SecurityFilterChain` scoped to the configured HTMX base path, configured same-origin GraphQL endpoint, login processing path, logout path, and required viewer assets.
The login page and static assets required to render it are public.
HTMX routes and GraphQL requests require authentication.
The chain does not claim unrelated application, Wicket, actuator, console, or bearer-token API paths.

The application uses Spring's request cache only for safe same-origin GET routes beneath the configured HTMX base path.
After successful login it restores a valid saved route, otherwise it uses the HTMX root.
Malformed, cross-origin, login, logout, GraphQL, and non-GET targets are never used as post-login redirects.
The session fixation strategy migrates the session identifier on authentication.

### Render login without GraphQL

The security integration provides a server-rendered GET login page and Spring-owned POST processing endpoint beneath the configured HTMX base path.
The page uses the viewer brand and compatible stylesheet but sits outside `<causeway-graphql-client>` and performs no GraphQL request before authentication.
It contains labelled username and password fields, a CSRF token, a generic invalid-credentials outcome, an explicit signed-out outcome, and accessible focus and announcement behavior.

Credentials are submitted only as an HTTPS-protected server POST in production.
They are never placed in URLs, browser storage, semantic events, diagnostics, or client-side state beyond the native form controls needed for submission.

### Require rather than enable CSRF

Importing the optional authentication module requires `causeway.security.spring.allow-csrf-filters=true`.
Startup validation checks the bound Causeway configuration value and fails with a clear message when it is false.
The integration never flips the setting, edits the shared Spring filter chain after construction, or changes the default.

The authenticated full shell renders the current Spring `CsrfToken` as bounded metadata under the existing content-security policy.
The GraphQL client attaches the documented CSRF header and token to every same-origin GraphQL POST, including query operations.
The HTMX host attaches it to any unsafe HTMX request, while login and logout forms submit the corresponding hidden parameter.
Tokens, passwords, and cookies remain absent from result events, route URLs, browser history, errors, and diagnostics.

An alternative is to exempt `/graphql` or `/logout` from CSRF protection.
That is rejected because session cookies make those endpoints browser-ambient authorities.

### Make authentication chrome host-owned

The authenticated shell renders a current-user control beside the application menus and a native POST logout form supplied by the security integration.
Logout passes CSRF validation, clears the Spring Security context, invalidates the HTTP session, clears authentication cookies according to Spring policy, and redirects to the login page with a non-sensitive signed-out marker.
Back navigation cannot restore authenticated HTMX or GraphQL content from cache.

The host excludes the exact `causeway.security.LogoutMenu#logout` action from HTMX menu presentation when this integration is active.
The exclusion is local to the HTMX host and does not change metamodel visibility, authorization, GraphQL schema generation, or Wicket presentation.
If a stale or custom page still publishes that exact action request, the host prevents GraphQL invocation and initiates the same native logout flow.

### Treat authentication loss as full-page navigation

An unauthenticated full-page HTMX route request redirects through Spring Security to login and records a safe requested route.
An unauthenticated HTMX fragment request produces a full-browser redirect signal rather than swapping login markup into the route region.
An unauthenticated GraphQL request returns `401`; the shell treats that as session expiry and performs a full-page login navigation with the current safe HTMX route as the requested destination.
An authenticated but unauthorized request remains `403` or a bounded access-denied GraphQL outcome and MUST NOT be presented as session expiry.

### Use a dedicated secured Petclinic application configuration

Add a secured application configuration and acceptance suite inside the existing Petclinic sample module so it can reuse the domain, fixtures, custom pages, styles, and browser-test infrastructure without changing the sample's current default application class.
The secured configuration imports the optional HTMX SecMan security module and required SecMan persistence, integration, and password-encoder modules, but does not import Wicket or bypass security.
It seeds deterministic unlocked, locked, and passwordless users and roles using supported SecMan fixture or repository APIs.

This avoids claiming Wicket CSRF compatibility and avoids making the ordinary Petclinic comparison suite depend on authentication.
The secured suite runs headlessly without live identity providers and verifies both HTTP security behavior and the user-visible browser journey.

## Risks / Trade-offs

- [Risk] The provisional bridge duplicates functionality that belongs in shared security modules. → Keep it isolated behind the optional module, document its provisional status, and cover extraction through the committed promotion draft.
- [Risk] Multiple application security chains can overlap or change precedence. → Scope the chain to exact configured paths, document its order, test representative additional chains, and fail configuration when required paths are outside its matcher.
- [Risk] Loading SecMan users before an ordinary authenticated interaction may violate persistence assumptions. → Perform the lookup through a bounded anonymous interaction and test transaction cleanup on success and failure.
- [Risk] A serialized Spring principal could retain password material or stale authorization state. → Use a credentials-erasing principal, store only the encoded password until provider validation completes, and create a fresh refined principal at each login.
- [Risk] A CSRF token embedded in the shell may become stale after session changes. → Issue a new full shell after login, do not history-cache authenticated shells, and route CSRF rejection to a bounded reload or reauthentication outcome.
- [Risk] Protecting a GraphQL endpoint used by other clients could surprise an application. → Keep the integration opt-in, scope and document its browser-session chain, and leave bearer or mixed API support to later explicit configuration.
- [Risk] Hiding only the standard logout action may leave custom logout-like domain actions visible. → Match only the framework logical type and member and document that application actions remain ordinary domain behavior.
- [Risk] Returning to saved URLs can create open redirects. → Accept only canonical same-origin GET paths beneath the configured HTMX base path.

## Migration Plan

1. Applications continue using the generic HTMX module unchanged unless they add the optional SecMan security integration.
2. An opting-in application configures SecMan persistence and password encoding, imports the new integration, and sets `causeway.security.spring.allow-csrf-filters=true`.
3. The application verifies its HTMX base path, GraphQL endpoint, security-chain ordering, HTTPS proxy headers, and secure session-cookie settings.
4. Existing local users continue using their SecMan usernames, encrypted passwords, status, roles, tenancy, and locale preferences without a persistence migration.
5. Rollback removes the optional integration import and its application configuration; no SecMan schema or credential migration is required.

## Open Questions

- Should the final artifact and package use `htmx-security-secman` or the more explicit `htmx-authentication-secman` name?
- Should the provisional principal carry the complete refined SecMan profile from the initial lookup, or should a converter invoke the existing refiner inside a second controlled interaction?
- Should host menu exclusion become a reusable typed HTMX policy SPI in this change, or remain a narrowly coded exclusion until another host policy needs it?
