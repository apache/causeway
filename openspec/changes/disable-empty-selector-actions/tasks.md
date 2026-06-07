## 1. Action Disabled Facet

- [x] 1.1 Add an action-level disabled facet for synthetic parented collection selector actions.
- [x] 1.2 Implement the disabled facet so it reads the associated collection from the current action target and vetoes usability when the collection is empty.
- [x] 1.3 Install the disabled facet when `ParentedCollectionSelectorActionUtil` creates a synthetic selector action.

## 2. Tests

- [x] 2.1 Add metamodel test coverage that an empty associated collection disables the selector action with a clear reason.
- [x] 2.2 Add metamodel test coverage that a non-empty associated collection leaves the selector action enabled.
- [x] 2.3 Verify existing no-match validation behavior remains unchanged for non-empty collections.

## 3. Validation

- [x] 3.1 Run the focused `ParentedCollectionSelectorActionUtilTest` Maven test set.
- [x] 3.2 Run `openspec validate disable-empty-selector-actions --strict`.
