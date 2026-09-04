## 1. Foundation component rename

- [x] 1.1 Rename the foundation preview element module, class, export, registration, and component contract from peek terminology to preview terminology.
- [x] 1.2 Update collection integration and native/Vaadin styling to discover and render only direct `<cw-preview>` declarations and live preview subtrees.
- [x] 1.3 Rename and update focused foundation tests to cover `<cw-preview>`, duplicate declarations, lifecycle, hydration, accessibility, and the absence of a `cw-peek` registration.

## 2. HTMX preview-resource integration

- [x] 2.1 Update the HTMX preview-resource client and viewer registration to require and resolve `<cw-preview>` roots without changing preview paths or endpoint behavior.
- [x] 2.2 Update JavaScript and Java HTMX tests for valid, missing, malformed, unsafe, duplicate, cached, and reload preview-resource behavior under the renamed element.

## 3. Samples and generated assets

- [x] 3.1 Migrate HTMX Petclinic page and preview fragments to `<cw-preview>` and update integration and browser acceptance assertions.
- [x] 3.2 Migrate Vue Petclinic source pages and browser acceptance assertions to `<cw-preview>`.
- [x] 3.3 Regenerate the Vue package and committed Petclinic production assets using the established deterministic build profiles.

## 4. Documentation and migration

- [x] 4.1 Update current foundation, adapter, HTMX, Vue, and sample documentation to present `<cw-preview>` and document the breaking one-token migration from `<cw-peek>`.
- [x] 4.2 Confirm supported current source, tests, and generated artifacts contain no remaining `cw-peek` references beyond explicit migration notices while leaving historical archived changes unchanged.

## 5. Verification

- [x] 5.1 Run focused foundation and HTMX preview tests.
- [x] 5.2 Run ordinary and secured HTMX and Vue sample integration and headless browser suites.
- [x] 5.3 Run packaging, license, deterministic-asset, formatting, inspection, and strict OpenSpec validation for affected modules.
