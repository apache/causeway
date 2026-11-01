## 1. Public Layout Contracts

- [x] 1.1 Add `cw-fieldset`, `cw-row`, `cw-column`, `cw-tabgroup`, `cw-tab`, and `cw-metadata` to public element contracts, exports, registration, and prefix/registration tests.
- [x] 1.2 Implement shared direct-child validation, bounded diagnostics, mutation resynchronization, and safe unsupported-child presentation.
- [x] 1.3 Implement named fieldset grouping and twelve-track row/column span normalization for values 1 through 12.
- [x] 1.4 Add shared responsive, fieldset, column, invalid-child, and focus-visible styles to source and generated style modules.

## 2. Accessible Tab Components

- [x] 2.1 Implement tabgroup/tab naming, deterministic initial/current selection, control/panel ids, and dynamic child synchronization.
- [x] 2.2 Implement pointer, ArrowLeft/ArrowRight, Home, End, disabled-tab, focus, and RTL behavior.
- [x] 2.3 Add foundation tests for valid/invalid nesting, selected-state invariants, dynamic tabs, and keyboard accessibility.

## 3. Authoritative Metadata Component

- [x] 3.1 Extend the normalized object-layout API with a bounded extractor/renderer for exact fieldset id `metadata`, preserving ordered property and action identities.
- [x] 3.2 Implement `<cw-metadata>` as a generation-safe layout context consumer using schema description and authorized structural-resource loading.
- [x] 3.3 Render metadata properties as ordinary `<cw-property>` children and associated actions as ordinary `<cw-action>` children in one closed-by-default accessible Actions panel dropdown.
- [x] 3.4 Implement missing-context, missing-grid, absent-fieldset, malformed-resource, partial-error, supersession, and disconnect handling without invented members.
- [x] 3.5 Add foundation unit and integration tests for authoritative membership, action grouping, ordinary descendant requirements, errors, and lifecycle retirement.

## 4. HTMX and Vue Demonstration

- [x] 4.1 Update an HTMX Petclinic exact page to demonstrate fieldsets, rows, columns, tabs, and automatic metadata while preserving existing application ownership.
- [x] 4.2 Update the equivalent Vue Petclinic page with the same semantic custom-element structure and no Vue-owned layout behavior.
- [x] 4.3 Extend HTMX and Vue source/browser tests for wide/narrow spans, fieldsets, pointer/keyboard tabs, metadata properties, conditional Actions dropdown, stable routes, and parity.
- [x] 4.4 Regenerate Vue production assets through the established build lifecycle.

## 5. Documentation and Verification

- [x] 5.1 Document element grammar, attributes, responsive behavior, tab accessibility, metadata authority, action dropdown behavior, diagnostics, and examples.
- [x] 5.2 Run complete foundation JavaScript, HTMX, Vue, native/Vaadin, secured, Maven packaging, RAT/license, OpenSpec, IDE, and whitespace validation.
