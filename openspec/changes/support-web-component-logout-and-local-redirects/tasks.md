## 1. Reproduce and Bound the Contracts

- [ ] 1.1 Add focused browser or integration coverage that reproduces the current HTMX and Vue framework Logout GraphQL failure before changing dispatch policy.
- [ ] 1.2 Record the exact `causeway.security.LogoutMenu#logout` identity, cancelable action-request contract, `LocalResourcePathValue` GraphQL shape, and existing authenticated HTMX exclusion as compatibility baselines.
- [ ] 1.3 Add negative fixtures for a similarly named application action and a non-logout local-resource path so exact matching remains testable.

## 2. Foundation Action and Result Semantics

- [ ] 2.1 Centralize the exact reserved framework Logout action identity and helper without importing core security into browser components.
- [ ] 2.2 Verify and, where necessary, strengthen action controls so a synchronously claimed or canceled `causeway-action-request` performs no validation, invocation, or synthetic result emission.
- [ ] 2.3 Centralize object and service action-result normalization and classify exact `LocalResourcePathValue` output as an immutable local-resource semantic result.
- [ ] 2.4 Fail closed for malformed local-resource payloads while preserving existing scalar, object, collection, void, Blob, Clob, and unsupported structured-result behavior.
- [ ] 2.5 Add foundation unit and integration tests for exact Logout matching, pre-invocation cancellation, result classification, and nonmatching actions.

## 3. Safe Local-Resource Navigation

- [ ] 3.1 Implement a reusable local-resource target resolver that accepts an explicit application-local base and rejects malformed, scheme-relative, cross-origin, credential-bearing, escaping, or unsupported targets.
- [ ] 3.2 Implement `SAME_WINDOW` full-document navigation and opener-isolated `NEW_WINDOW` behavior without passing resource targets to an object router.
- [ ] 3.3 Preserve host policy claims and bounded error reporting so applications can replace or further restrict default navigation without mutating canonical results.
- [ ] 3.4 Add unit coverage for root and nested deployment bases, context preservation exactly once, both opening strategies, popup-policy behavior, and unsafe-target rejection.

## 4. HTMX Host Policy

- [ ] 4.1 Remove the unclaimed framework Logout action from generic HTMX semantic menu controls while leaving similarly named application actions untouched.
- [ ] 4.2 Add a fail-closed HTMX action-request guard that blocks stale or custom framework Logout requests before GraphQL and composes with the existing authentication metadata handler.
- [ ] 4.3 Extend the HTMX document/runtime contract with an authoritative servlet-context local-resource base and handle local-resource semantic results through the shared resolver.
- [ ] 4.4 Verify that authenticated HTMX SecMan continues to use its accessible CSRF-protected POST logout control and that generic HTMX adds no security endpoint or session behavior.
- [ ] 4.5 Add HTMX unit, controller, integration, and headless browser coverage for default Logout suppression, stale-request blocking, claimed authenticated logout, local redirects, nested contexts, and unsafe targets.

## 5. Vue Host Policy

- [ ] 5.1 Add a typed pre-invocation Vue action policy with canonical detail, single-claim semantics, immutable policy context, disposal safety, and error-policy integration.
- [ ] 5.2 Remove the unclaimed framework Logout action from Vue semantic menu controls and block direct or stale requests before GraphQL while preserving nonmatching actions.
- [ ] 5.3 Add a documented Vue application-local resource base or resolver option independent of the Vue object-route base.
- [ ] 5.4 Handle local-resource semantic results through host result claims and the shared safe-navigation contract.
- [ ] 5.5 Add Vue unit, declaration, package-consumer, and headless browser coverage for action claims, default Logout suppression, no GraphQL invocation, local redirects, nested deployment, unsafe targets, and route lifecycle stability.

## 6. Samples, Documentation, and Verification

- [ ] 6.1 Add or reuse bounded typed `LocalResourcePath` acceptance actions without placing a misleading framework Logout action in an unauthenticated sample menu.
- [ ] 6.2 Update foundation, HTMX, Vue, and security documentation to distinguish host-owned secure logout from ordinary local-resource navigation and explain deployment-base configuration.
- [ ] 6.3 Regenerate and verify committed frontend assets and confirm stale-generated-output checks pass.
- [ ] 6.4 Run foundation and viewer frontend tests, Vue declaration/build/pack verification, relevant Maven integration suites, and HTMX and Vue headless Playwright suites.
- [ ] 6.5 Run secured HTMX logout regression coverage, script syntax checks, Apache RAT, IDE inspections/build, strict OpenSpec validation, and whitespace checks.
