## Context

The HTMX Petclinic sample already has an opt-in secured application, a dedicated security integration module, seeded local SecMan users, server-rendered login, CSRF-protected GraphQL, saved-route restoration, host-owned POST logout, and headless acceptance coverage.
The Vue Petclinic sample currently starts only with bypass security, and its application-owned static document has no source of authenticated-user or CSRF state.
The existing SecMan `UserDetailsService`, principal, and Causeway authentication converter are intentionally located inside the HTMX integration, so copying them into Vue would duplicate security-sensitive behavior and couple future fixes to two implementations.

## Goals / Non-Goals

**Goals:**

- Provide an opt-in secured Vue runtime equivalent to the established secured HTMX journey.
- Share SecMan credential lookup and Causeway principal conversion without sharing viewer routing, login presentation, or shell policy.
- Keep generic Vue, generic HTMX, foundation components, and ordinary sample launch behavior authentication-neutral.
- Protect GraphQL POSTs and logout with current Spring CSRF evidence.
- Preserve safe Vue deep links through login and fail closed on session loss.
- Provide simple dedicated secured launch scripts for both reference viewers.

**Non-Goals:**

- OAuth, OIDC, external identity-provider logout, registration, password reset, remember-me, or multi-factor authentication.
- A universal authentication abstraction for non-Spring or non-SecMan applications.
- Moving route, shell, login-page, or redirect policy into the shared credential bridge.
- Server-side rendering or hydration of the Vue application.
- Changing the ordinary bypass-secured Petclinic launchers.

## Decisions

### Extract a presentation-neutral bridge inside the web-components reactor

A new optional `security-secman` module will own the SecMan-backed Spring `UserDetailsService`, serializable principal, Causeway authentication converter, and DAO authentication provider.
It will import the existing Spring security and SecMan integration modules but no HTMX or Vue viewer module.
The existing HTMX SecMan integration and the new Vue SecMan integration will both consume it.
This avoids a Vue-to-HTMX dependency and avoids duplicating credential, lock-state, role, tenancy, locale, and credential-erasure behavior.
A future broader promotion into shared Causeway security can move this bounded module without changing viewer contracts.

Alternatives rejected include making HTMX bridge classes public for Vue consumption, because that would invert viewer layering, and copying those classes into Vue, because security fixes could drift.

### Keep server security policy viewer-specific

The Vue integration will own Vue route matchers, login and logout paths, request caching, authentication entry behavior, CSRF context delivery, and login-page presentation.
The HTMX integration will retain its current HTMX-specific shell, fragment-expiry, and unsafe-request behavior.
Sharing only credential and principal conversion keeps host routing and lifecycle policy explicit.

### Keep login server-rendered and the authenticated shell client-rendered

Anonymous users will receive a server-rendered login page that performs no GraphQL request and posts credentials with Spring CSRF evidence.
After authentication, the existing production Vue application remains client-rendered and application-owned.
This avoids making login dependent on Vue assets, GraphQL, or custom-element initialization.

### Publish bounded authentication context to the secured Vue host

The Vue integration will expose an authenticated no-store JSON context endpoint containing only the current username, CSRF header name, CSRF parameter name, CSRF token, login path, and logout path.
The application-owned Vue document will contain an inert authentication-context metadata slot whose default value is empty.
The secured route controller will serve the same built document with that slot bound to the configured context endpoint, while the ordinary static runtime leaves it empty and performs no authentication bootstrap request.

The Vue bootstrap will fetch this context before mounting, install a CSRF-decorating GraphQL executor, and configure the exact framework Logout action policy to submit the application-owned logout form.
The stable Vue shell will render the current user and a native POST logout form from this context.
No token will enter GraphQL documents, variables, URLs, router state, local storage, or action results.

### Restore only safe Vue GET routes

A request cache will retain only canonical same-origin GET routes beneath the configured Vue base path.
Login, logout, authentication-context, GraphQL, static assets, unsafe methods, malformed paths, and external destinations will fall back to the Vue root.
Session fixation protection will migrate the session identifier after successful login.

### Treat authentication loss as full-document navigation

A protected full-page Vue route will redirect to the server login page.
A GraphQL `401` observed by the secured executor will trigger full-document login navigation using the current safe Vue route as a continuation hint.
An authenticated `403` will remain authorization or CSRF failure and will not be relabelled as session expiry.

### Add dedicated secured launch scripts

`sample-htmx-petclinic/run-secured.sh` and `sample-vue-petclinic/run-secured.sh` will select explicit Maven secured-run profiles.
Existing `run.sh` scripts and default profiles remain unchanged.
Both scripts will retain JDK discovery, `JAVA_HOME`, `MVN`, and pass-through argument behavior.

## Risks / Trade-offs

- **[Risk] The shared bridge changes existing HTMX authentication wiring.** → Keep the same principal data and provider policy, retain existing HTMX integration and browser suites, and compare all failure modes.
- **[Risk] A static Vue document cannot contain per-session CSRF values.** → Bind only the context-endpoint URL into the secured document and fetch current no-store context before GraphQL initialization.
- **[Risk] Authentication context may become stale after session rotation or expiry.** → Obtain it after login, use the current session token, and perform full-document login navigation on `401` rather than retrying with stale state.
- **[Risk] Public frontend assets expose implementation code.** → Permit only immutable presentation assets; protect routes, authentication context, and GraphQL data independently.
- **[Risk] Multiple security chains may claim unrelated paths.** → Use explicit path matchers and configurable ordering, and test that unrelated paths remain outside the integration.

## Migration Plan

1. Add and verify the shared bridge while retaining HTMX behavior.
2. Add the optional Vue integration and its bounded configuration.
3. Add the secured Vue sample application, authentication-aware frontend host code, and deterministic users.
4. Add secured launch profiles and scripts.
5. Run ordinary and secured HTMX and Vue integration and headless suites.
6. Roll back by removing the optional Vue integration import and secured profiles; ordinary viewers remain unchanged.

## Open Questions

None.
