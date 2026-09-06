## 1. Context Reference and Allocation Coordination

- [x] 1.1 Extend semantic requirement registration with backward-compatible value-free consumer provenance and member-reference revision subscriptions.
- [x] 1.2 Add tests for property, collection, and action reference registration, release, wrong-kind and stale requirements, hidden connected consumers, and disconnected cleanup.
- [x] 1.3 Implement one generation-scoped unreferenced member allocation coordinator per `<cw-object-context>` boundary using the authoritative object-description member map.
- [x] 1.4 Implement exact `(kind, id)` subtraction, authoritative ordering, catch-all descendant exclusion, nested-context isolation, and parser-late batched recalculation.
- [x] 1.5 Implement document-order destination selection, duplicate-destination diagnostics, deterministic reallocation, and retirement of obsolete generated ownership.
- [x] 1.6 Add claim-source reservations that block only declared member kinds until a producer resolves, settles empty/error, is superseded, or disconnects.
- [x] 1.7 Add coordinator tests for asynchronous inventory, explicit precedence independent of document order, multiple sinks, nested boundaries, inert declarations, generated descendants, mutations, supersession, and failures.

## 2. Unreferenced Member Components

- [x] 2.1 Add the shared lifecycle base and public contracts for loading, ready, empty, error, bounded names, no authored children, ownership markers, diagnostics, and cleanup.
- [x] 2.2 Implement `<cw-unreferenced-properties>` with an owned Other fieldset, authoritative property order, and explicit read-only-by-default `editable` propagation.
- [x] 2.3 Implement `<cw-unreferenced-collections>` with an owned accessible tabgroup, one tab per collection, required row and twelve-span column structure, and ordinary collection children.
- [x] 2.4 Implement `<cw-unreferenced-actions>` with an owned accessibly named wrapping horizontal group and ordinary action children.
- [x] 2.5 Register and export all three element names and constructors and add them to the component contract and element-prefix coverage.
- [x] 2.6 Extend strict `<cw-column>` direct-child validation for the three catch-all elements while retaining rejection and hiding of arbitrary children.
- [x] 2.7 Add synchronized source and installable styles for state visibility, fieldset, tabgroup, action grouping, responsive bounds, and visible keyboard focus.
- [x] 2.8 Add component tests for generated markup, labels, property editing opt-in, tab behavior, action behavior, authoritative ordering, empty/error states, invalid children, duplicate sinks, mutations, and lifecycle retirement.

## 3. Composite Claim Producers

- [x] 3.1 Integrate `<cw-metadata>` with pending property/action reservation and exact resolved claims before generated metadata members become presentable.
- [x] 3.2 Cover metadata missing-grid, malformed-resource, empty, partial-error, supersession, menu lifecycle, and disconnect paths so every reservation settles or retires.
- [x] 3.3 Integrate `<cw-object>` effective-grid and fallback composition with pending claim reservations for the member kinds it generates.
- [x] 3.4 Add regression tests proving metadata and object composition never stably duplicate members in catch-all output and cannot leave allocations blocked.
- [x] 3.5 Share deterministic allocation helpers with XML-plan unreferenced processing where practical and prove equivalent explicit precedence, kind validation, ordering, and allocate-once behavior.

## 4. HTMX and Vue Integration

- [x] 4.1 Permit the three element names and bounded public attributes through strict HTMX declarative template validation without adding host allocation behavior.
- [x] 4.2 Verify HTMX browser composition for remaining members, empty destinations, metadata claims, parser-late references, generated controls, route replacement, nested contexts, accessibility, and responsive bounds.
- [x] 4.3 Verify Vue treats all three names as native custom elements and presents equivalent allocation, generated controls, empty states, lifecycle, accessibility, and responsive behavior.
- [x] 4.4 Update foundation usage and HTMX and Vue documentation with direct declarations, precise reference semantics, one-destination rules, editable opt-in, states, and application-versus-foundation ownership.

## 5. Verification

- [x] 5.1 Run complete foundation JavaScript and host policy tests, HTMX and Vue default/secured/native/Vaadin browser suites, responsive and accessibility checks, and focused allocation lifecycle regressions.
- [x] 5.2 Run Maven packaging and RAT/license verification, Vue production build verification, IDE compilation and inspections, strict OpenSpec validation, and whitespace checks.

## 6. Conditional Property Tab Refinement

- [ ] 6.1 Permit `<cw-unreferenced-properties>` as a direct tabgroup child and synchronize a tab control and panel only while its allocation is ready and non-empty.
- [ ] 6.2 Preserve authored tab order, unaffected selection, keyboard navigation, relationships, and deterministic focus fallback when the conditional tab appears or disappears.
- [ ] 6.3 Move the HTMX and Vue PetOwner declarations between Identity and Metadata and verify the conditional Other tab without duplicate headings or members.
- [ ] 6.4 Update foundation and host documentation, focused component tests, browser parity assertions, generated Vue assets, and complete validation.
