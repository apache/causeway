## 1. Freeze the HTMX presentation baseline

- [ ] 1.1 Record a route-by-route parity matrix for the HTMX shell and HomePage, PetOwner, Pet, and Visit pages, including ordered landmarks, headings, selected members, actions, result outlets, collection declarations, paging values, and preview affordances.
- [ ] 1.2 Add or extend headless assertions that protect the authoritative HTMX semantic composition without changing its rendered presentation.
- [ ] 1.3 Record tolerant wide- and narrow-viewport invariants for header geometry, content inset, column relationships, breakpoint collapse, footer placement, palette, typography, focus treatment, and horizontal overflow.
- [ ] 1.4 Diagnose why the Vue application currently reverses primary menus and exposes Logout, User, and Configuration separately from the HTMX System grouping, identifying the supported projection or application-configuration correction before changing code.
- [ ] 1.5 Select an existing suitable unregistered logical type for generic-fallback acceptance, or specify a bounded menu-excluded acceptance fixture if none exists.

## 2. Reconcile the Vue application shell

- [ ] 2.1 Refactor `App.vue` to mirror the HTMX shell landmarks, branding, loading and announcement regions, shell result outlet, main-region semantics, and footer content while retaining Vue Router bindings and one stable GraphQL client.
- [ ] 2.2 Reconcile menu order and utility grouping through supported semantic menu projection and application configuration, without hardcoded replacement actions or payload rewriting.
- [ ] 2.3 Rework `petclinic.css` to use the HTMX Petclinic class vocabulary, foundation design tokens, wide shell geometry, responsive breakpoints, typography, palette, content spacing, and footer alignment.
- [ ] 2.4 Reconcile document titles with the HTMX `· Pet Clinic` convention and replace the persistent route-container border with an accessible keyboard focus treatment.
- [ ] 2.5 Add headless shell tests for wide and narrow layouts, menu keyboard operation, focus, reduced motion, overflow, title, footer, and stable provider and route boundaries.

## 3. Reconcile the PetOwner page

- [ ] 3.1 Restructure `PetOwnerPage.vue` into the HTMX object heading, details column, and collections column with separate Identity, Contact, Details, Pets, and Visits sections in reference order.
- [ ] 3.2 Restore the `noOwners` action, complete related-owner standalone result declaration, `daysSinceLastVisit`, member descriptions, label positions, and action parameter presentation from the HTMX page.
- [ ] 3.3 Reconcile Pets and Visits collection columns, page sizes, filtering, sorting, actions, prompt styles, parameters, row previews, and nested preview collections with the HTMX declarations.
- [ ] 3.4 Extend Vue acceptance tests to verify PetOwner semantic parity, column containment at wide width, single-column order at narrow width, action results, property edits, collection operations, previews, and both toolkit policies.

## 4. Add the remaining exact-type Vue pages

- [ ] 4.1 Implement and register `HomePage.vue` with the HTMX object heading, two collection sections, declared columns, filters, sorting, page sizes, and row previews.
- [ ] 4.2 Implement and register `PetPage.vue` with the HTMX breadcrumb, heading, Identity and Details grouping, selected properties, editability, and absence of technical or intentionally omitted members.
- [ ] 4.3 Implement and register `VisitPage.vue` with the HTMX breadcrumb, heading, Appointment and Details grouping, temporal presentation, selected properties, editability, and absence of technical members.
- [ ] 4.4 Add direct-link, refresh, history, semantic-structure, responsive-layout, and interaction coverage for each new exact-type page.

## 5. Preserve deliberate generic fallback

- [ ] 5.1 Add the bounded shared-domain acceptance fixture selected in task 1.5 only if no existing logical type can safely demonstrate fallback, preserving Petclinic menu contents and established seed cardinalities.
- [ ] 5.2 Move generic-page direct-link, refresh, and browser-history coverage from Pet or Visit to the deliberately unregistered acceptance type.
- [ ] 5.3 Verify exact-type precedence for all four reconciled Petclinic routes and generic `<cw-object>` rendering for the fallback fixture.

## 6. Regression, generated assets, and documentation

- [ ] 6.1 Add tolerant computed-style and bounding-box assertions for the agreed shell and route invariants rather than pixel-perfect screenshot comparisons.
- [ ] 6.2 Verify invalid routes, absent objects, partial errors, stale-generation rejection, announcements, semantic navigation and results, and provider lifecycle still behave as before.
- [ ] 6.3 Run the Vue package unit, declaration, build, pack, and clean-consumer checks and resolve any integration regression without changing public APIs unnecessarily.
- [ ] 6.4 Regenerate and verify committed Vue production assets and confirm the stale-generated-output check passes.
- [ ] 6.5 Run Vue and HTMX integration and headless Playwright suites, including native and Vaadin policies and representative wide and narrow viewports.
- [ ] 6.6 Update the Vue Petclinic and web-components documentation to identify HTMX as the presentation reference and explain where generic fallback is demonstrated.
- [ ] 6.7 Run relevant Maven verification, frontend tests, script syntax checks, Apache RAT, IDE inspections/build, and whitespace checks.
