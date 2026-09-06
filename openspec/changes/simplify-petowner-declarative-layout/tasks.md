## 1. Foundation Column Composition

- [ ] 1.1 Extend `<cw-column>` direct-child validation to accept `<cw-tabgroup>` while continuing to fail closed for every unsupported child.
- [ ] 1.2 Give layout columns shared vertical stacking, start alignment, bounded gaps, and minimum-width behavior without changing span normalization.
- [ ] 1.3 Add foundation tests for direct tabgroup composition, multiple ordered children, unsupported-child diagnostics, twelve-track spans, and responsive stacking.
- [ ] 1.4 Update synchronized source and installable styles and declarative-layout documentation for the expanded column grammar.

## 2. HTMX PetOwner Simplification

- [ ] 2.1 Replace the HTMX PetOwner object-grid, details, and collections divs with one macro `<cw-row>` containing span-4 details and span-8 collections columns.
- [ ] 2.2 Remove redundant full-width wrappers around Contact and Details while retaining required rows and columns inside tabs.
- [ ] 2.3 Replace outer Pets and Visits card sections with direct labelled collections and represent Agreement as a direct named fieldset containing the existing property.
- [ ] 2.4 Remove obsolete Petclinic macro-grid CSS while preserving authored order, shared responsive stacking, card presentation, collection behavior, and PDF sizing.
- [ ] 2.5 Update HTMX source, integration, and browser assertions for direct-child grammar, wide 4/8 geometry, intermediate and narrow stacking, semantic collection headings, actions, previews, Agreement, routes, and absence of redundant wrappers.

## 3. Vue PetOwner Parity

- [ ] 3.1 Apply the equivalent simplified custom-element tree to the Vue PetOwner page without Vue-owned layout state.
- [ ] 3.2 Remove equivalent obsolete Vue sample CSS and update source and browser parity assertions.
- [ ] 3.3 Regenerate Vue production assets through the established build lifecycle.

## 4. Documentation and Verification

- [ ] 4.1 Update HTMX and Vue documentation to demonstrate foundation-owned macro layout and explain which application wrappers remain intentionally outside it.
- [ ] 4.2 Run complete foundation JavaScript, HTMX, Vue, native/Vaadin, secured, responsive, accessibility, Maven packaging, RAT/license, OpenSpec, IDE, and whitespace validation.
