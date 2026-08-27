## 1. Optional Integration Module

- [ ] 1.1 Add the optional `htmx-security-secman` Maven module, dependencies, automatic module name, and viewer-reactor registration without changing the generic HTMX dependency set.
- [ ] 1.2 Add the opt-in Causeway module and typed configuration for login, logout, filter-chain order, and secured viewer paths.
- [ ] 1.3 Add startup validation that requires `causeway.security.spring.allow-csrf-filters=true` and reports bounded path or chain configuration conflicts.
- [ ] 1.4 Add a narrow typed HTMX authentication-shell SPI for user identity, CSRF metadata, endpoint paths, and exact host menu exclusions.

## 2. Provisional SecMan-to-Spring Bridge

- [ ] 2.1 Implement controlled anonymous SecMan user lookup that returns an immutable credentials-erasing principal with encrypted password, account status, roles, tenancy, and locale profile.
- [ ] 2.2 Configure Spring username/password authentication with the qualified SecMan `PasswordEncoder` and fail closed for absent, locked, passwordless, invalid, or internally failed users.
- [ ] 2.3 Implement the high-priority HTMX-specific Spring authentication converter that creates a complete refined Causeway `UserMemento` without an unscoped second repository lookup.
- [ ] 2.4 Add focused tests for successful validation, generic credential failures, account status, credential erasure, role and profile refinement, transaction cleanup, and absence of accept-any behavior.

## 3. Login, Session, and Request Restoration

- [ ] 3.1 Add the scoped Spring `SecurityFilterChain` for configured HTMX, GraphQL, login, logout, and public asset paths without claiming unrelated application paths.
- [ ] 3.2 Add the branded server-rendered login page and CSRF-protected form processing outside the GraphQL shell.
- [ ] 3.3 Configure session identifier migration and a safe request cache that restores only canonical same-origin HTMX GET routes.
- [ ] 3.4 Add generic invalid-credentials, signed-out, unsafe-destination fallback, and secure cache-control behavior with accessible focus and announcements.
- [ ] 3.5 Add HTTP tests for chain matching, public login resources, protected HTMX and GraphQL paths, login success and failure, session migration, and open-redirect rejection.

## 4. CSRF-Protected Viewer Requests

- [ ] 4.1 Publish the current Spring CSRF header, parameter, and token as bounded full-shell metadata compatible with the existing content-security policy.
- [ ] 4.2 Extend the GraphQL browser client to attach current CSRF evidence to every same-origin GraphQL POST without exposing it in operations, events, URLs, history, or diagnostics.
- [ ] 4.3 Configure unsafe HTMX requests plus login and logout forms to submit current CSRF evidence and reject missing, invalid, or stale tokens without changing authenticated state.
- [ ] 4.4 Add browser-side and server-side tests for accepted and rejected GraphQL query, mutation, login, logout, and session-renewal CSRF cases.

## 5. Authenticated Shell and Logout

- [ ] 5.1 Render the current username and accessible native POST logout control beside host navigation only when local authentication is active.
- [ ] 5.2 Implement logout session and security-context invalidation, configured cookie cleanup, no-store behavior, and redirect to the signed-out login page.
- [ ] 5.3 Exclude the exact framework `LogoutMenu#logout` action from HTMX menus and intercept any stale exact action request before GraphQL invocation without affecting similarly named application actions.
- [ ] 5.4 Add shell and policy tests for authenticated identity, logout success, post-logout protection, history restoration, exact legacy-action suppression, and generic domain-action preservation.

## 6. Authentication Loss and Authorization Denial

- [ ] 6.1 Implement full-page login redirection for anonymous route requests and full-browser redirect signaling for unauthenticated HTMX fragment requests.
- [ ] 6.2 Translate GraphQL `401` responses into safe full-page reauthentication while cancelling obsolete component work and preserving only the current canonical HTMX route.
- [ ] 6.3 Preserve bounded access-denied behavior for authenticated `403` and GraphQL authorization outcomes without misreporting session expiry.
- [ ] 6.4 Add tests for full-page, fragment, and GraphQL session expiry plus authenticated authorization denial.

## 7. Secured Petclinic Evidence

- [ ] 7.1 Add a dedicated secured Petclinic application configuration in the existing sample module using SecMan persistence, integration, password encoding, and the optional HTMX authentication module without Wicket or bypass security.
- [ ] 7.2 Seed deterministic unlocked, locked, passwordless, role-bearing, tenancy, and locale test users through supported SecMan APIs.
- [ ] 7.3 Add a headless browser journey covering anonymous protection, invalid and valid login, deep-link restoration, authorized reads and mutations, current-user chrome, CSRF rejection, logout, history, and session expiry.
- [ ] 7.4 Verify the existing default Petclinic HTMX and Wicket-comparison runtime remains unchanged when the optional integration is absent.

## 8. Documentation and Validation

- [ ] 8.1 Document installation, module import, required CSRF setting, SecMan and password-encoder prerequisites, endpoint scoping, filter-chain ordering, HTTPS proxy handling, secure cookie settings, and rollback.
- [ ] 8.2 Document the provisional bridge boundary, explicit exclusions, OAuth follow-on, shared-security promotion follow-on, and deferred Wicket CSRF coexistence.
- [ ] 8.3 Run focused module, SecMan integration, GraphQL client, HTMX browser, secured Petclinic, ordinary Petclinic, formatting, static analysis, and OpenSpec validation checks.
