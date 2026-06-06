## 1. Parameter Eligibility

- [x] 1.1 Rename selector-action helper variables and constructor arguments from scalar-specific names to filter-property names where they now cover scalar and reference properties.
- [x] 1.2 Extend child property eligibility so visible one-to-one value properties keep the current behavior.
- [x] 1.3 Add selectable reference eligibility for child one-to-one non-value properties with a property choices facet, property autocomplete facet, bounded referenced-type choices facet, or referenced-type autocomplete facet.
- [x] 1.4 Preserve exclusions for child collections, hidden properties, blob and clob values, unconstrained references, and technical metadata property ids.

## 2. Matching and Facet Wiring

- [x] 2.1 Pass the expanded filter-property list through synthetic action parameter type/name creation and action facet installation.
- [x] 2.2 Pass the expanded filter-property list through validation and invocation facets without changing parent parameter behavior.
- [x] 2.3 Keep string filter matching as contains matching and verify all non-string values, including references, use exact equality.

## 3. Tests

- [x] 3.1 Add metamodel fixture properties for bounded, property choices, property autocomplete, referenced-type autocomplete, and unconstrained reference cases on selector child rows.
- [x] 3.2 Verify bounded reference properties become optional selector parameters when visible as collection columns.
- [x] 3.3 Verify choices reference properties become optional selector parameters when visible as collection columns.
- [x] 3.4 Verify property autocomplete and referenced-type autocomplete reference properties become optional selector parameters when visible as collection columns.
- [x] 3.5 Verify unconstrained reference properties and non-column selectable references are excluded.
- [x] 3.6 Verify selector validation and invocation can identify exactly one child by supplied reference parameter value.
- [x] 3.7 Verify reference matching does not use title or partial string semantics.

## 4. Validation

- [x] 4.1 Run the focused metamodel selector action test suite.
- [x] 4.2 Run `openspec validate allow-selector-reference-parameters --strict`.
