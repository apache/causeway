## 1. Eligibility Rule

- [ ] 1.1 Update selector action eligibility so entity and view-model owners are both supported at runtime.
- [ ] 1.2 Keep selector action element eligibility constrained to entity, view model, or abstract domain types.
- [ ] 1.3 Remove the unit-test-only view-model eligibility special case.

## 2. Tests

- [ ] 2.1 Add metamodel test coverage for a view-model owner with entity collection elements.
- [ ] 2.2 Verify view-model-owned selector actions keep existing name, association, styling, parameter, validation, and invocation behavior through focused assertions where practical.
- [ ] 2.3 Preserve existing entity-owner selector action coverage.

## 3. Validation

- [ ] 3.1 Run the focused `ParentedCollectionSelectorActionUtilTest` Maven test set.
- [ ] 3.2 Run `openspec validate allow-view-model-selector-actions --strict`.
