## 1. Regression Contract

- [x] 1.1 Add foundation coverage proving prepared validity is hidden when a prompt first opens.
- [x] 1.2 Add foundation coverage proving input capture performs no parameter recomputation while focus remains in the editor.
- [x] 1.3 Add foundation coverage proving focus departure recomputes dependencies and reveals completed-field validity.
- [x] 1.4 Add foundation coverage proving Invoke validates the latest unblurred pending value and reveals invalid reasons.
- [x] 1.5 Add Petclinic browser coverage for quiet open, quiet typing, focus-leave feedback, correction, and successful invocation.
- [x] 1.6 Record the reproduced defect and unchanged authority/security boundaries.

## 2. Prompt Validation Lifecycle

- [x] 2.1 Track prompt-local parameter ids whose validation may be presented.
- [x] 2.2 Capture input and change values locally without debounced preparation.
- [x] 2.3 Commit a parameter through existing recomputation when its editor loses focus.
- [x] 2.4 Keep autocomplete search independent from focus-completion validation.
- [x] 2.5 Render validity and parameter errors only after focus completion while always retaining disabled reasons.
- [x] 2.6 Reveal all applicable parameter reasons and validate the complete latest pending set on Invoke.
- [x] 2.7 Preserve stale-response protection, focus, cancellation, toolkit fallback, and protected-value filtering.

## 3. Qualification

- [x] 3.1 Run the complete foundation and HTMX route-policy suites.
- [x] 3.2 Run focused Petclinic integration tests.
- [x] 3.3 Run Petclinic Playwright under Vaadin-default and explicit-native toolkit policies.
- [x] 3.4 Run RAT, packaging, strict OpenSpec validation, and whitespace checks.
