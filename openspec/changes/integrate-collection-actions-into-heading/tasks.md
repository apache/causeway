## 1. Stable integrated collection header

- [ ] 1.1 Add collection-specific primary rendering that removes an obsolete promoted heading before each shell update.
- [ ] 1.2 Promote the current effective heading before direct associated actions while preserving its ID, tooltip, and `aria-labelledby` relationship.
- [ ] 1.3 Omit heading promotion when the owner primary is hidden and preserve independently visible action behavior.
- [ ] 1.4 Extend collection and member-composition tests across loading, ready, filtering, hidden, and rerendered states without duplicate headings or action reconnection.

## 2. Unified generated association structure

- [ ] 2.1 Generate effective-grid collection actions as direct `<cw-collection>` children in declaration order.
- [ ] 2.2 Preserve the existing external wrapper and field alignment for property-associated actions.
- [ ] 2.3 Update object-layout and structural tests for equivalent authored and generated collection composition.

## 3. Compact responsive presentation

- [ ] 3.1 Make action-bearing collections own one bordered header and body panel without changing collections that have no associated actions.
- [ ] 3.2 Align the promoted heading and compact associated actions on one wide row with the body full-width below.
- [ ] 3.3 Stack the title and wrapping action toolbar within the same header at the narrow collection breakpoint.
- [ ] 3.4 Add scoped native and Vaadin compact button sizing while retaining labels, icons, focus indication, and unrelated action density.
- [ ] 3.5 Update structural style tests and Foundation usage documentation.

## 4. Petclinic acceptance and validation

- [ ] 4.1 Extend Petclinic Playwright coverage for shared heading/action geometry, compact controls, DOM and keyboard order, and narrow wrapping.
- [ ] 4.2 Preserve collection action prompts, invocation, refresh, focus, accessibility relationships, diagnostics, and no-overflow behavior.
- [ ] 4.3 Run complete Foundation Node and Maven suites plus relevant browser audits.
- [ ] 4.4 Run complete Petclinic Playwright acceptance, IDE compilation or inspections, strict OpenSpec validation, and final diff checks.
