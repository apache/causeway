## 1. Selector Parameter Eligibility

- [ ] 1.1 Add a selector-parameter eligibility predicate for child scalar properties in `ParentedCollectionSelectorActionUtil`.
- [ ] 1.2 Exclude blob and clob value types from the selector action scalar property list.
- [ ] 1.3 Exclude child property ids `logicalTypeName`, `id`, and `version` from the selector action scalar property list.
- [ ] 1.4 Ensure action parameter type generation, parameter naming, validation, and invocation all consume the filtered property list.

## 2. Test Coverage

- [ ] 2.1 Extend `ParentedCollectionSelectorActionUtilTest` with blob and clob child properties and assert no selector parameters are generated for them.
- [ ] 2.2 Extend `ParentedCollectionSelectorActionUtilTest` with `logicalTypeName`, `id`, and `version` child properties and assert no selector parameters are generated for them.
- [ ] 2.3 Assert ordinary scalar child properties remain available as optional selector filters.

## 3. Validation

- [ ] 3.1 Run the focused metamodel selector action tests.
- [ ] 3.2 Run `openspec validate omit-system-and-large-selector-parameters --strict`.
