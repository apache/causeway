## 1. Foundation Identifier Contract

- [ ] 1.1 Replace the custom `member` attribute and property with native `id` reflection on property, action, collection, and collection-column elements.
- [ ] 1.2 Update connected identifier-change handling, context requirements, collection-column capture, and generated object markup while retaining internal domain-member payload fields.
- [ ] 1.3 Add focused tests for native ID reflection, generated IDs, connected ID changes, and absence of the former compatibility API.

## 2. Markup, Selectors, and Documentation

- [ ] 2.1 Migrate foundation demos, fixtures, tests, styles or selectors, and usage documentation from the member attribute to `id`.
- [ ] 2.2 Migrate HTMX-rendered content, vanilla HTML, Petclinic pages, integration assertions, and browser selectors to `id`.
- [ ] 2.3 Migrate Reference Application selectors and applicable GraphQL or Web Components documentation to `id`.
- [ ] 2.4 Add a source audit that rejects DOM-facing uses of the former member attribute and custom property while preserving internal member terminology and `data-causeway-associated-member`.

## 3. Validation

- [ ] 3.1 Run complete foundation and HTMX Node suites plus Web Components Java and sample integration tests.
- [ ] 3.2 Run ordinary and secured Petclinic Playwright acceptance and Reference Application integration and Playwright coverage.
- [ ] 3.3 Run JavaScript syntax, formatting, source-audit, and strict OpenSpec validation checks.
