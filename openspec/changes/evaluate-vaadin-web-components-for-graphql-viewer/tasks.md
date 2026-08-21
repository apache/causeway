## 1. Baselines and candidate freeze

- [ ] 1.1 Inventory the existing rich GraphQL choice, interaction, collection-window, object-context, route, and custom-fragment contracts and map each required Vaadin data-provider operation to a public operation or an explicit gap.
- [ ] 1.2 Document the Wicket Select2 single-choice, multi-choice, remote lookup, debounce, identity, validation, event, and breadcrumb behaviors that form the reference-selection parity baseline.
- [ ] 1.3 Freeze exact Vaadin free-core package versions, entry points, integrity values, repositories, direct and transitive dependencies, licenses, maintenance evidence, browser policy, and explicit Pro-package exclusions.
- [ ] 1.4 Publish the common fixture, viewport, data volume, error, preference, lifecycle, accessibility, performance, bundle, and hard-gate evaluation plan before scoring the candidate.

## 2. Reproducible standalone harness and packaging

- [ ] 2.1 Create an analysis-only package manifest, lockfile, local server, shared fixture, and browser runner beneath the change directory without modifying production Maven or JavaScript modules.
- [ ] 2.2 Add direct browser ES-module loading and assertions that the fixture uses no Vaadin Flow bootstrap, router, protocol, server state, Binder, or Java DataProvider behavior.
- [ ] 2.3 Build deterministic selective Vaadin JavaScript and theme outputs for the evaluated controls and record source-package, dependency-closure, generated-file, request, raw, and compressed sizes.
- [ ] 2.4 Add an analysis Maven module that packages the selective assets, required licenses, and notices beneath `META-INF/resources` and verifies offline browser delivery.

## 3. GraphQL-backed widget prototypes

- [ ] 3.1 Implement the single-reference Combo Box adapter with debounced filtering, paging, stable identity, initial selection, clearing, disabled reasons, validation, loading, empty, partial-error, terminal-error, cancellation, and stale-response handling.
- [ ] 3.2 Implement the multi-reference selection adapter with deterministic identity and ordering, token or chip presentation, add and remove behavior, validation, cancellation, and Causeway-owned semantic change evidence.
- [ ] 3.3 Implement the lazy Grid adapter with count, page or visible-window requests, supported sorting and filtering, stable row identity, selection, object links, canonical navigation, loading, empty, partial-error, terminal-error, and narrow-layout behavior.
- [ ] 3.4 Prototype date, time, date-time, scalar, multiline, enum, boolean, required, read-only, disabled, localized, conversion-error, and validation-error field states through existing Causeway semantics.
- [ ] 3.5 Prototype an action prompt using candidate-backed controls while retaining Causeway validation, invocation, cancellation, concurrency, focus, and scalar, object, collection, void, and error outcomes.
- [ ] 3.6 Record every filter, paging, sorting, count, identity, conversion, validation, or interaction requirement that the current public GraphQL contract cannot express without adding candidate-only endpoints.

## 4. Composition and lifecycle integration

- [ ] 4.1 Build a router-selected custom object HTML fragment containing one route-level `<causeway-object-context>` and a representative mixture of semantic Causeway elements and evaluated Vaadin controls.
- [ ] 4.2 Demonstrate that an equivalent custom page can obtain standard behavior through semantic `<causeway-*>` wrappers without understanding Vaadin events or data-provider protocols.
- [ ] 4.3 Exercise an optional allowlisted raw `<vaadin-*>` page-authoring tier and document its loading, versioning, theming, accessibility, compatibility, and application-responsibility boundaries.
- [ ] 4.4 Run repeated HTMX generic-page and custom-page insertion, supersession, removal, back and forward navigation, stale-request cancellation, overlay cleanup, listener cleanup, focus restoration, console, and retained-DOM checks.
- [ ] 4.5 Run a bounded candidate injection or equivalent integration check against the Petclinic HTMX viewer without changing production source files and record fixture-only assumptions.

## 5. Quality, theme, and supply-chain evidence

- [ ] 5.1 Run automated accessibility checks and manual keyboard journeys for reference selectors, Grid, typed fields, validation, action prompts, object navigation, generic pages, and custom pages.
- [ ] 5.2 Capture identical desktop and narrow evidence in light, dark, reduced-motion, and forced-colors modes and record focus, contrast, long-label, dense-grid, overlay, and page-overflow defects.
- [ ] 5.3 Map `--causeway-*` customization variables to documented Vaadin theme properties or parts and record every unthemeable shadow-DOM state or global style leak.
- [ ] 5.4 Measure cold and warm candidate requests, compressed transfer, parse and initialization cost, route-ready time, relevant rendering timings, shared chunks, and eager versus route-lazy delivery against the current baseline.
- [ ] 5.5 Complete package-level license, notice, provenance, vulnerability, maintenance, update, cache, checksum, and Maven release assessments and fail any behavior that requires a commercial or ambiguous component.

## 6. Decision and reproducibility

- [ ] 6.1 Complete the hard-gate assessment and weighted matrix using 30% domain widget coverage, 25% GraphQL architecture and custom-page composability, 15% accessibility, 15% supply chain and packaging, 10% performance, and 5% theming.
- [ ] 6.2 Publish the GraphQL gap analysis and recommend internal-only Vaadin rendering, an optional allowlisted raw-widget tier, unrestricted application-owned widgets, or no Vaadin integration.
- [ ] 6.3 Publish an architectural decision recommending broad adoption, a constrained free-core subset, retention of current components, or a follow-up UI5 or enterprise-suite comparison, with rejected alternatives and unresolved limitations.
- [ ] 6.4 Produce staged migration, rollback, bundle and browser budgets, compatibility policy, free-core allowlist, and a separate implementation proposal outline when adoption is recommended.
- [ ] 6.5 Verify that a maintainer can reproduce the selective build, prototypes, browser evidence, Maven package, measurements, and decision from documented commands and that no production dependency or runtime behavior changed.
