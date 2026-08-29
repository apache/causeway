## 1. Collection metadata and heading behavior

- [x] 1.1 Extend targeted collection requirements to select supported canonical friendly-name and description metadata.
- [x] 1.2 Implement `named` and `described-as` collection attributes with documented precedence, compatibility, duplicate suppression, and reactive rerendering.
- [x] 1.3 Remove visible collection-level disabled or unmodifiable reason markup while preserving read-only and member/action semantics.
- [x] 1.4 Refine collection description styling below the label with accessible subdued typography.

## 2. Foundation regression coverage

- [x] 2.1 Add object-context tests for targeted collection metadata selection, partial support, and request reuse.
- [x] 2.2 Add collection component tests for canonical and HTML heading precedence, reactive attributes, accessibility, duplicate suppression, and quiet unmodifiable state.
- [x] 2.3 Update stylesheet and component-contract coverage for the new public attributes and description presentation.

## 3. Petclinic demonstrations

- [x] 3.1 Add selective `@CollectionLayout(describedAs)` descriptions to Petclinic collections.
- [x] 3.2 Add selective `named` and `described-as` overrides to Petclinic HTML pages while leaving other collections metadata-driven or unchanged.
- [x] 3.3 Extend Petclinic integration and Playwright assertions for names, descriptions, omitted unmodifiable noise, and unaffected interactions.

## 4. Validation and archive readiness

- [x] 4.1 Run foundation tests and package verification.
- [x] 4.2 Run Petclinic integration and Playwright acceptance.
- [x] 4.3 Run strict OpenSpec validation, compilation, license, diff, and working-tree checks.
