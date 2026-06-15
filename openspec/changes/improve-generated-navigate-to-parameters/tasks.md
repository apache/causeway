## 1. Regression Coverage

- [ ] 1.1 Add or extend parented collection navigation test fixtures with a mandatory boolean child property rendered as a collection column.
- [ ] 1.2 Add tests that assert generated mandatory boolean filter parameters are optional and can be represented as unselected.
- [ ] 1.3 Add validation/invocation tests showing unselected boolean filters are ignored while explicit `false` and explicit `true` filters are applied.
- [ ] 1.4 Add or extend ordering tests to cover collection column order with a boolean column and with an ineligible column between eligible filter columns.

## 2. Metamodel Generation

- [ ] 2.1 Update synthetic parented collection navigation parameter generation to preserve parented collection column order before eligibility filtering.
- [ ] 2.2 Ensure generated selector parameters for boolean child columns are installed as optional regardless of the underlying child property's mandatory facet.
- [ ] 2.3 Verify existing scalar and selectable reference parameter inclusion/exclusion rules remain unchanged.

## 3. Matching Semantics

- [ ] 3.1 Update parented collection navigation matching so null, unspecified, empty, or otherwise unselected boolean arguments do not add a predicate.
- [ ] 3.2 Preserve exact equality matching for explicitly supplied `true` and `false` boolean arguments.
- [ ] 3.3 Confirm validation and invocation use the same matching behavior for boolean and non-boolean filters.

## 4. Verification

- [ ] 4.1 Run the focused metamodel tests for `ParentedCollectionNavigationActionUtilTest`.
- [ ] 4.2 Run any affected core metamodel test suite target needed to validate no regressions.
- [ ] 4.3 Run `openspec status --change improve-generated-navigate-to-parameters` and confirm the change remains apply-ready.
