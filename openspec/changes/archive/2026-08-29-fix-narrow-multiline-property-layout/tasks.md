## 1. Effective multiline shell state

- [x] 1.1 Add a generated property-shell attribute derived from effective canonical, compatibility, or metadata multiline state in every render state.
- [x] 1.2 Replace host-spelling-dependent multiline selectors with shell-state selectors while preserving wide and narrow grid placement.

## 2. Regression coverage

- [x] 2.1 Add property component tests for canonical, legacy, and metadata-derived shell state across view, loading, error, and edit rendering.
- [x] 2.2 Update stylesheet regression tests to require effective shell selectors and reject the incomplete legacy host selector.
- [x] 2.3 Strengthen Petclinic geometry diagnostics and verify native narrow multiline alignment, bounded controls, and no overflow.

## 3. Validation and archive readiness

- [x] 3.1 Run foundation tests and package verification.
- [x] 3.2 Run focused and full Petclinic Playwright acceptance in default and native modes.
- [x] 3.3 Run Petclinic integration, strict OpenSpec validation, compilation, license, diff, and working-tree checks.
