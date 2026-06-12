## 1. Selector Parameter Discovery

- [x] 1.1 Update synthetic parented collection selector creation to derive child parameter candidates from the associated collection column model.
- [x] 1.2 Preserve the mandatory disabled parent parameter as the first selector action parameter.
- [x] 1.3 Apply the existing scalar, blob, clob, and technical metadata exclusions to the column-derived child parameter candidates.
- [x] 1.4 Preserve child parameter ordering according to the collection column order.

## 2. Tests

- [x] 2.1 Add or update metamodel tests proving that scalar child properties rendered as collection columns become optional selector filters.
- [x] 2.2 Add or update tests proving that eligible scalar child properties not rendered as collection columns are omitted.
- [x] 2.3 Add or update tests proving that child selector filter parameters follow collection column order.
- [x] 2.4 Keep coverage for parent parameter behavior and existing non-scalar, blob, clob, and technical metadata exclusions.

## 3. Validation

- [x] 3.1 Run the focused metamodel selector action test suite.
- [x] 3.2 Run `openspec validate limit-selector-parameters-to-collection-columns --strict`.
