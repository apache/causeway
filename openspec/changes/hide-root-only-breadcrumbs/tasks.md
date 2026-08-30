## 1. Foundation Breadcrumb Rendering

- [ ] 1.1 Hide and clear ready-state `<cw-breadcrumbs>` when no valid ancestors remain.
- [ ] 1.2 Explicitly restore host visibility for descendant, loading, and error states.
- [ ] 1.3 Extend foundation tests for empty, malformed-only, descendant, and state-transition behavior.

## 2. Petclinic Acceptance

- [ ] 2.1 Update owner-page Playwright assertions to require no breadcrumb landmark while retaining pet and visit hierarchy checks.
- [ ] 2.2 Compile and run the focused Petclinic breadcrumb browser journey.

## 3. Verification

- [ ] 3.1 Run the foundation direct Node and Maven test suites.
- [ ] 3.2 Run strict OpenSpec validation and confirm the working diff is clean of formatting errors.
