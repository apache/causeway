# Final gate results

## Passed

- Foundation Node suite: 129 tests passed with zero failures.
- Web-component foundation and HTMX Maven tests: `BUILD SUCCESS`.
- GraphQL model reactor and tests: `BUILD SUCCESS`.
- Pinned Reference Application direct service and parameterized object mutation integration: `BUILD SUCCESS`.
- Petclinic integration and Playwright reactor: `BUILD SUCCESS`.
- Petclinic Playwright: 4 tests passed with zero failures, errors, or skips.
- Reference Application ordinary clean package, integration tests, inventory tests, and deterministic fixtures: `BUILD SUCCESS`.
- Reference Application Playwright: 9 tests passed with zero failures, errors, or skips.
- Vaadin production-like pilot: 5 scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
- GraphQL model and Reference Application RAT checks: `BUILD SUCCESS`.
- New foundation source and test modules are approved as Apache License 2.0 by RAT.
- Strict OpenSpec validation and `git diff --check`: passed.

## Compatibility results

No dependency manifest changed.
No public GraphQL name, Causeway element, context method, semantic event name, canonical route, history policy, or browser asset URL changed.
No Vaadin bundle input, generated bundle, checksum, CSP hash, qualification policy, lazy-loading trigger, native fallback, or default-selection policy changed.

The standalone foundation RAT goal still reports the same pre-existing `vaadin-reference/README.adoc` and generated `vaadin-reference.js` files as unknown-license resources.
Neither file is introduced or modified by this change, and RAT explicitly approves both newly added action-dispatch files.
