## 1. Rich GraphQL breadcrumb metadata

- [ ] 1.1 Add one shared GraphQL breadcrumb-entry type and an optional `breadcrumbs` field to generated rich object metadata.
- [ ] 1.2 Implement root-to-parent traversal through `ObjectSpecification.getNavigableParent`, with bookmark identity, request-local titles, a 32-ancestor bound, cycle detection, and safe failures.
- [ ] 1.3 Add GraphQL model unit tests for empty, single, multi-level, unbookmarkable, cyclic, over-depth, and throwing parent chains.
- [ ] 1.4 Extend GraphQL schema and end-to-end contract fixtures to verify additive field shape, shared-type reuse, partial errors, and no traversal when unselected.

## 2. Browser context support

- [ ] 2.1 Extend targeted metadata introspection to discover the shared breadcrumb-entry type without broad schema introspection.
- [ ] 2.2 Add the semantic breadcrumbs requirement, nested selection translation, state mapping, projection coalescing, delta loading, refresh, and release behavior to `ObjectContextController`.
- [ ] 2.3 Add browser context and operation tests for supported, unsupported, ready, partial-error, late-registration, and released breadcrumb requirements.
- [ ] 2.4 Update rich-schema fixtures with deterministic empty, one-level, and multi-level breadcrumb metadata.

## 3. Breadcrumb web component

- [ ] 3.1 Implement and export `<cw-breadcrumbs>` as a context consumer rendering ancestor object links and one escaped current item in an accessible breadcrumb landmark.
- [ ] 3.2 Register the element and add its public element-name, host-class, stable selector, and semantic navigation contracts.
- [ ] 3.3 Add responsive breadcrumb styles for ordered separators, focus, wrapping, long titles, narrow layouts, dark mode, and forced colors.
- [ ] 3.4 Add component tests for loading, empty hierarchy, multiple ancestors, malformed entries, navigation events, escaping, errors, accessibility, and overflow-safe markup.
- [ ] 3.5 Document plain-HTML usage, GraphQL requirements, hierarchy semantics, error behavior, and framework-neutral navigation ownership.

## 4. Petclinic adoption

- [ ] 4.1 Mark `Pet.petOwner` and `Visit.pet` with the standard `Navigable.PARENT` property-layout semantic.
- [ ] 4.2 Compose `<cw-breadcrumbs>` into the Pet Owner, Pet, and Visit HTML-authored pages without adding application-specific breadcrumb code.
- [ ] 4.3 Extend Petclinic GraphQL integration coverage for zero-, one-, and two-ancestor fixture chains and private-page markup coverage for the component.
- [ ] 4.4 Extend Petclinic Playwright coverage for breadcrumb order, current state, keyboard activation, canonical HTMX navigation, responsive layout, accessibility, and clean browser diagnostics.
- [ ] 4.5 Update Petclinic documentation and screenshots or stable test hooks where the existing sample contract requires them.

## 5. Validation and archive readiness

- [ ] 5.1 Run focused GraphQL model tests and GraphQL end-to-end tests for breadcrumb metadata.
- [ ] 5.2 Run foundation unit, component, fixture, lint, and package verification suites.
- [ ] 5.3 Run HTMX viewer, Petclinic integration, and Petclinic Playwright acceptance suites.
- [ ] 5.4 Run applicable Maven verification, OpenSpec strict validation, formatting, license, diff, and working-tree checks.
