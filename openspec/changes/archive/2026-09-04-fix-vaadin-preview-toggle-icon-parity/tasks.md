## 1. Shared Icon Construction

- [x] 1.1 Add a dependency-free foundation utility that owns the preview SVG class, view box, path geometry, native markup, and namespace-aware DOM construction.
- [x] 1.2 Update native collection preview markup to consume the shared icon definition without changing its button contract.
- [x] 1.3 Replace the Vaadin Grid preview button's Unicode text glyph with the shared SVG DOM node while preserving cell recycling and interaction behavior.

## 2. Regression Coverage

- [x] 2.1 Extend shared preview tests to verify the utility's markup and DOM representations are equivalent and decorative.
- [x] 2.2 Extend Grid widget tests to prove the adapter emits no text glyph and uses the same SVG class, geometry, hidden semantics, and state-driven button attributes.
- [x] 2.3 Extend Petclinic browser coverage to compare Grid-qualified Pets and native Visits preview icon size and direction on the same page.

## 3. Documentation and Verification

- [x] 3.1 Update foundation and Grid adapter documentation to identify the shared disclosure icon boundary.
- [x] 3.2 Run complete foundation, HTMX, Vue, native/Vaadin, Maven packaging, OpenSpec, IDE, and whitespace validation.
