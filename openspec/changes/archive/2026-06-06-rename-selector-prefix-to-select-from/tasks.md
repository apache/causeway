## 1. Implementation

- [x] 1.1 Update the synthetic parented collection selector action id prefix constant from `__causeway_select_` to `__causeway_select_from_`.
- [x] 1.2 Verify selector action id construction still appends the associated parented collection id without changing display name, layout association, parameters, or invocation behavior.

## 2. Tests and Documentation

- [x] 2.1 Update selector action unit tests and any fixtures that assert the old `__causeway_select_` prefix.
- [x] 2.2 Search source, tests, and OpenSpec artifacts for remaining references to the old prefix and update only the references that describe current behavior.

## 3. Validation

- [x] 3.1 Run the focused selector action test suite for `ParentedCollectionSelectorActionUtilTest`.
- [x] 3.2 Run `openspec validate rename-selector-prefix-to-select-from --strict`.
