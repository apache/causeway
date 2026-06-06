## 1. Matching Implementation

- [ ] 1.1 Update `ParentedCollectionSelectorMatchingUtil` so supplied string filter values match child string property values using containment rather than exact equality.
- [ ] 1.2 Keep non-string scalar filter values using exact equality and keep null or absent optional filter values ignored.
- [ ] 1.3 Verify validation and invocation both use the same updated matching behavior.

## 2. Test Coverage

- [ ] 2.1 Add or update selector action tests for a partial string filter that uniquely selects one child.
- [ ] 2.2 Add or update selector action tests for a partial string filter that matches multiple children and remains ambiguous.
- [ ] 2.3 Add or update selector action tests confirming non-string scalar filters still require exact equality.

## 3. Validation

- [ ] 3.1 Run the focused selector action test suite for `ParentedCollectionSelectorActionUtilTest`.
- [ ] 3.2 Run `openspec validate allow-selector-string-partial-match --strict`.
