## 1. Theme boundary

- [x] 1.1 Scope every applicable application-defined native input, select, textarea, focus, padding, and resize selector away from toolkit-owned `slot="input"` controls.
- [x] 1.2 Preserve the established global theme rules for ordinary native and fallback controls.

## 2. Regression coverage

- [x] 2.1 Add foundation stylesheet tests proving toolkit slotted inputs are excluded and ordinary native controls remain styled.
- [x] 2.2 Add Petclinic browser coverage proving representative read-only text fields expose only one visible bordered rectangle.
- [x] 2.3 Confirm focus, accessibility, responsive, native-toolkit, and forced-colors behavior remains covered by existing acceptance checks.

## 3. Validation and archive readiness

- [x] 3.1 Run foundation verification and Petclinic integration and Playwright acceptance suites.
- [x] 3.2 Run strict OpenSpec validation, formatting, license, diff, and working-tree checks.
