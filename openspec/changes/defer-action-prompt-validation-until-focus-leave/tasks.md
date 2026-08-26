## 1. Regression Contract

- [ ] 1.1 Add foundation coverage proving prepared validity is hidden when a prompt first opens.
- [ ] 1.2 Add foundation coverage proving input capture performs no parameter recomputation while focus remains in the editor.
- [ ] 1.3 Add foundation coverage proving focus departure recomputes dependencies and reveals completed-field validity.
- [ ] 1.4 Add foundation coverage proving Invoke validates the latest unblurred pending value and reveals invalid reasons.
- [ ] 1.5 Add Petclinic browser coverage for quiet open, quiet typing, focus-leave feedback, correction, and successful invocation.
- [ ] 1.6 Record the reproduced defect and unchanged authority/security boundaries.

## 2. Prompt Validation Lifecycle

- [ ] 2.1 Track prompt-local parameter ids whose validation may be presented.
- [ ] 2.2 Capture input and change values locally without debounced preparation.
- [ ] 2.3 Commit a parameter through existing recomputation when its editor loses focus.
- [ ] 2.4 Keep autocomplete search independent from focus-completion validation.
- [ ] 2.5 Render validity and parameter errors only after focus completion while always retaining disabled reasons.
- [ ] 2.6 Reveal all applicable parameter reasons and validate the complete latest pending set on Invoke.
- [ ] 2.7 Preserve stale-response protection, focus, cancellation, toolkit fallback, and protected-value filtering.

## 3. Qualification

- [ ] 3.1 Run the complete foundation and HTMX route-policy suites.
- [ ] 3.2 Run focused Petclinic integration tests.
- [ ] 3.3 Run Petclinic Playwright under Vaadin-default and explicit-native toolkit policies.
- [ ] 3.4 Run RAT, packaging, strict OpenSpec validation, and whitespace checks.
