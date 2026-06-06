## 1. Parameter Eligibility

- [ ] 1.1 Rename selector-action helper variables and constructor arguments from scalar-specific names to filter-property names where they now cover scalar and reference properties.
- [ ] 1.2 Extend child property eligibility so visible one-to-one value properties keep the current behavior.
- [ ] 1.3 Add selectable reference eligibility for child one-to-one non-value properties with a property choices facet, property autocomplete facet, or bounded referenced-type choices facet.
- [ ] 1.4 Preserve exclusions for child collections, hidden properties, blob and clob values, unconstrained references, and technical metadata property ids.

## 2. Matching and Facet Wiring

- [ ] 2.1 Pass the expanded filter-property list through synthetic action parameter type/name creation and action facet installation.
- [ ] 2.2 Pass the expanded filter-property list through validation and invocation facets without changing parent parameter behavior.
- [ ] 2.3 Keep string filter matching as contains matching and verify all non-string values, including references, use exact equality.

## 3. Tests

- [ ] 3.1 Add metamodel fixture properties for bounded, choices, autocomplete, and unconstrained reference cases on selector child rows.
- [ ] 3.2 Verify bounded reference properties become optional selector parameters when visible as collection columns.
- [ ] 3.3 Verify choices reference properties become optional selector parameters when visible as collection columns.
- [ ] 3.4 Verify autocomplete reference properties become optional selector parameters when visible as collection columns.
- [ ] 3.5 Verify unconstrained reference properties and non-column selectable references are excluded.
- [ ] 3.6 Verify selector validation and invocation can identify exactly one child by supplied reference parameter value.
- [ ] 3.7 Verify reference matching does not use title or partial string semantics.

## 4. Validation

- [ ] 4.1 Run the focused metamodel selector action test suite.
- [ ] 4.2 Run `openspec validate allow-selector-reference-parameters --strict`.
