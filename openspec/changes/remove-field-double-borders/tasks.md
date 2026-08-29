## 1. Theme boundary

- [ ] 1.1 Exclude toolkit-owned `slot="input"` controls from every applicable global native input, select, textarea, focus, padding, and resize selector.
- [ ] 1.2 Preserve the established global theme rules for ordinary native and fallback controls.

## 2. Regression coverage

- [ ] 2.1 Add foundation stylesheet tests proving toolkit slotted inputs are excluded and ordinary native controls remain styled.
- [ ] 2.2 Add Petclinic browser coverage proving representative read-only text fields expose only one visible bordered rectangle.
- [ ] 2.3 Confirm focus, accessibility, responsive, native-toolkit, and forced-colors behavior remains covered by existing acceptance checks.

## 3. Validation and archive readiness

- [ ] 3.1 Run foundation verification and Petclinic integration and Playwright acceptance suites.
- [ ] 3.2 Run strict OpenSpec validation, formatting, license, diff, and working-tree checks.
