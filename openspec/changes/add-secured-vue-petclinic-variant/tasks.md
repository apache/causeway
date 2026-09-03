## 1. Shared SecMan Spring Bridge

- [x] 1.1 Add the optional presentation-neutral `security-secman` module to the web-components reactor with bounded dependencies and module metadata.
- [x] 1.2 Move the SecMan principal, controlled user-details lookup, Causeway authentication converter, and DAO provider wiring into the shared module.
- [x] 1.3 Add focused tests for valid, absent, locked, passwordless, failed-lookup, role, tenancy, locale, serialization, and credential-erasure behavior.
- [x] 1.4 Refactor `htmx-security-secman` to consume the shared bridge without changing HTMX-specific route or shell policy.
- [x] 1.5 Run existing secured HTMX integration and headless browser regressions against the extracted bridge.

## 2. Vue SecMan Server Integration

- [x] 2.1 Add the optional `vue-security-secman` module, bounded configuration properties, module import, and startup validation.
- [x] 2.2 Implement context-aware Vue, login, logout, authentication-context, GraphQL, asset, and unrelated-path matchers.
- [x] 2.3 Implement safe Vue GET request caching, authentication entry behavior, generic failure, successful deep-link restoration, session migration, and signed-out redirect.
- [x] 2.4 Implement the accessible server-rendered login page and same-origin public login stylesheet without Vue or GraphQL startup.
- [x] 2.5 Implement the authenticated no-store context endpoint with bounded username, CSRF names and token, and configured host paths.
- [x] 2.6 Configure current-CSRF login and logout POSTs, session invalidation, authentication clearing, and cookie cleanup.
- [x] 2.7 Add server integration tests for startup prerequisites, path scope, anonymous protection, failure equivalence, login, safe restoration, context confidentiality, CSRF rejection, logout, and session fixation.

## 3. Secured Vue Host Runtime

- [x] 3.1 Add `PetClinicVueSecuredApplication`, deterministic SecMan user data, secured properties, and dependencies without altering the ordinary application.
- [x] 3.2 Add an inert authentication-context metadata slot to the application-owned Vue document and bind it only in the secured route response.
- [x] 3.3 Add typed frontend authentication bootstrap and a CSRF-decorating GraphQL executor with full-document `401` login handling.
- [x] 3.4 Add stable current-user and native CSRF POST logout chrome to the Vue shell.
- [x] 3.5 Configure the Vue action policy to claim only exact framework Logout and submit the host logout form.
- [x] 3.6 Preserve ordinary Vue startup without an authentication-context request or secured shell chrome.
- [x] 3.7 Regenerate and verify committed Vue production assets.

## 4. Secured Acceptance and Launchers

- [x] 4.1 Add secured Vue Spring integration coverage using the shared deterministic credentials and route identities.
- [x] 4.2 Add a headless secured Vue journey covering generic invalid login, valid deep-link login, authorized reads and mutation, CSRF headers and rejection, host logout, history, and session expiry.
- [x] 4.3 Add `sample-htmx-petclinic/run-secured.sh` selecting the established secured HTMX profile.
- [x] 4.4 Add the secured Vue Maven run profile and `sample-vue-petclinic/run-secured.sh`.
- [x] 4.5 Verify both ordinary run scripts remain mapped to their existing applications and both secured scripts pass arguments and environment settings unchanged.

## 5. Documentation

- [x] 5.1 Document the shared bridge ownership boundary and opt-in dependency behavior.
- [x] 5.2 Update HTMX security and sample documentation to use the dedicated secured launcher and describe unchanged behavior.
- [x] 5.3 Update Vue viewer, Vue security, Vue sample, and top-level web-component documentation with setup, routes, credentials, CSRF, logout, session expiry, and run commands.
- [x] 5.4 Document that local-resource navigation, framework Logout identity, and host authentication remain separate contracts.

## 6. Verification

- [x] 6.1 Run shared bridge, HTMX security, Vue security, and sample Java integration suites.
- [x] 6.2 Run foundation and Vue frontend checks, declaration/build/pack verification, and stale generated-output checks.
- [x] 6.3 Run ordinary and secured HTMX and Vue headless Playwright suites.
- [x] 6.4 Run script syntax checks, relevant Maven package verification, Apache RAT, IDE inspections/build, strict OpenSpec validation, and whitespace checks.
