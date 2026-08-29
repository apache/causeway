## 1. Rich property metadata

- [x] 1.1 Extend rich GraphQL member metadata with the canonical property label-position facet and add model tests for name, description, multiline, and label position.
- [x] 1.2 Teach the web-component GraphQL client and object context to introspect and select supported nested property presentation metadata.
- [x] 1.3 Add foundation tests proving coordinated property reads remain compatible when metadata fields are present or absent.

## 2. Property override and rendering contract

- [x] 2.1 Add reflected `named`, `described-as`, `multi-line`, and `label-position` presentation behavior with deterministic compatibility-alias and metadata fallback precedence.
- [x] 2.2 Render descriptions below visible labels and implement accessible `LEFT`, `TOP`, and `NONE` view, loading, error, disabled, and edit presentations.
- [x] 2.3 Add responsive component styles and stable data/CSS hooks for consistent field-set ratios and narrow stacking.
- [x] 2.4 Add foundation unit tests for override precedence, bounded multiline values, all label positions, descriptions, and accessibility associations.

## 3. Generated object layouts

- [x] 3.1 Preserve effective-grid property name, description, multiline, and label-position hints in the layout plan and emit canonical `<cw-property>` attributes.
- [x] 3.2 Extend object-layout and object-component tests for direct/generated presentation equivalence and invalid-value fallback.

## 4. Petclinic examples and acceptance

- [x] 4.1 Add appropriate `@PropertyLayout(describedAs)` annotations to some but not all Petclinic properties and an annotation-derived `TOP` example.
- [x] 4.2 Add selective Petclinic HTML overrides for `named`, `described-as`, `multi-line`, and `label-position` while retaining non-overridden properties.
- [x] 4.3 Update Petclinic styles and browser/integration assertions to verify the visible and accessible presentation examples.

## 5. Verification

- [x] 5.1 Run focused foundation JavaScript tests and rich GraphQL metadata tests.
- [x] 5.2 Run the applicable Petclinic Maven integration and browser acceptance checks, or record any environment-specific limitation.
- [x] 5.3 Run OpenSpec validation and inspect the final diff for unrelated changes.
