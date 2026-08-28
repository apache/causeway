## 1. Authoritative Element Vocabulary

- [ ] 1.1 Rename all sixteen public `CausewayElementName` values from `causeway-*` to `cw-*`.
- [ ] 1.2 Rename the internal registered field-editor host to `cw-field-editor` and verify exactly seventeen `cw-*` definitions with no old aliases.
- [ ] 1.3 Add registration and contract tests for the complete new element-name set and absence of old registrations.

## 2. Foundation Source and Presentation

- [ ] 2.1 Migrate generated opening and closing tags, element-name selectors, `localName` checks, and declarative child discovery throughout foundation source.
- [ ] 2.2 Migrate custom-element type selectors in baseline and cohesive-theme styles while preserving every `causeway-*` class, data attribute, event, variable, and asset contract.
- [ ] 2.3 Migrate the foundation demo and all JavaScript tests to `cw-*` markup and selectors.

## 3. HTMX Viewer and Samples

- [ ] 3.1 Migrate server-rendered HTMX shell, route fragments, login/authentication policy selectors, and browser bridge selectors to `cw-*`.
- [ ] 3.2 Migrate the vanilla HTML sample, integration assertions, and documentation to `cw-*`.
- [ ] 3.3 Migrate Petclinic custom HTML pages, Java integration assertions, Playwright selectors, and secured journey to `cw-*`.

## 4. Documentation and Migration Safety

- [ ] 4.1 Migrate foundation, HTMX, sample, and top-level Web Components documentation examples and selector references to `cw-*`.
- [ ] 4.2 Add a source audit that rejects known former custom-element tags and literals while proving semantic events, CSS classes, data attributes, variables, and asset paths retain `causeway-*`.
- [ ] 4.3 Document the breaking one-to-one application markup and selector migration and the absence of compatibility aliases.

## 5. Validation

- [ ] 5.1 Run complete foundation and HTMX Node suites, Java module and sample integration tests, and old-tag audits.
- [ ] 5.2 Run ordinary and secured Petclinic Playwright acceptance plus syntax, formatting, and strict OpenSpec validation.
