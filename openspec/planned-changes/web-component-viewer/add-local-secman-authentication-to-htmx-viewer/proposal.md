## Why

The generic HTMX Web Components viewer currently relies on bypass or externally established authentication and exposes the framework logout domain action even though that action cannot terminate an HTMX browser session.
Applications need an optional local sign-in experience that validates SecMan `ApplicationUser` credentials, establishes a secure browser session, protects GraphQL interactions, and provides host-owned logout without coupling the first implementation to changes in existing Causeway security modules.

## What Changes

- Add an optional HTMX-specific SecMan authentication integration, preferably as a dedicated viewer integration module so the generic HTMX viewer does not acquire a mandatory SecMan or Spring Security dependency.
- Preserve all existing security, GraphQL, Wicket, SecMan, and HTMX defaults when the optional integration is absent or disabled.
- Adapt the proven Estatio pattern locally within the HTMX integration: use Spring form login, a SecMan-backed `UserDetailsService`, controlled anonymous interaction for repository access, and HTMX-specific conversion from authenticated Spring principals to fully refined Causeway `UserMemento` state.
- Reuse SecMan password encoding and account-status rules and never include Estatio's application-specific accept-any-password behavior.
- Render a branded local login page outside the GraphQL-dependent shell and submit credentials only to a server-side Spring Security login endpoint.
- Preserve a safe same-origin requested HTMX route across authentication and return the user to that canonical route after successful login.
- Establish a server-side browser session with session-fixation protection and secure production cookie guidance for HTTPS deployments.
- Add authenticated-user chrome to the HTMX shell and perform logout through a CSRF-protected server endpoint that invalidates the Spring Security context and HTTP session before redirecting to the login page.
- Suppress the legacy `LogoutMenu` action from HTMX menu presentation without changing its metamodel or Wicket behavior.
- Require the existing `causeway.security.spring.allow-csrf-filters=true` setting when HTMX session authentication is enabled and fail startup with a bounded diagnostic when the prerequisite is absent.
- Publish the server CSRF token to the authenticated shell through a documented safe mechanism and attach it to GraphQL mutations, HTMX state-changing requests, and logout.
- Distinguish expired or absent authentication from denied authorization: full-page and fragment navigation must reach login safely on `401`, while authenticated `403` responses remain bounded access-denied outcomes.
- Keep OAuth/OIDC, external provider buttons, bearer-token API design, delegated-user auto-creation, and provider logout outside this first change.
- Use a dedicated secured acceptance runtime or profile that does not require Wicket to operate with enabled Spring CSRF filters; Wicket and HTMX CSRF-safe coexistence remains a separate planned change.
- Cover valid and invalid passwords, locked and absent users, route restoration, session fixation, CSRF acceptance and rejection, GraphQL mutation protection, logout, session expiry, authentication-versus-authorization outcomes, and legacy logout suppression.

## Capabilities

### New Capabilities

- `htmx-viewer-local-secman-authentication`: Provides optional local SecMan-backed form login, authenticated HTMX shell behavior, CSRF-safe browser interaction, and host-owned logout.

### Modified Capabilities

None.

## Impact

The change is initially confined to a new optional HTMX/SecMan security integration, HTMX authentication pages and shell chrome, GraphQL client request decoration, a secured acceptance application or profile, tests, configuration, and documentation.
It may copy and adapt selected Estatio integration patterns under current Causeway APIs, but it MUST NOT modify existing Causeway core security, Spring security, SecMan, Wicket, or OAuth implementation code.
The proposal requires design, delta specifications, tasks, and strict validation before promotion.
