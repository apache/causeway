# Evaluation plan

## Candidate and baselines

The candidate is the pinned Vaadin 25.2.8 free-core package allowlist loaded directly as browser Web Components.
The primary baseline is the current Causeway semantic web-component viewer.
Reference-selection parity is compared with the current WicketStuff Select2 integration.
No Flow runtime or commercial Vaadin package participates.

## Fixture data

The deterministic fixture contains at least 240 reference choices and 1,200 collection rows so filtering and lazy loading cannot be mistaken for a tiny static list.
Rows contain canonical logical type and opaque identifier, long and duplicate-like labels, status, date, amount, boolean, and nullable values.

The fixture exposes a GraphQL-shaped analysis executor that records operation name, variables, selection, start, completion, cancellation, errors, and generation.
It implements only behavior present in the public contract and separately marks unsupported paging, sorting, or filtering requests.

## Required states

- Single-reference fixed choices and autocomplete.
- Multi-reference add, remove, clear, ordering, and validation.
- Required, optional, disabled, read-only, loading, empty, ready, partial-error, terminal-error, and stale-response states.
- Grid initial, subsequent window, configured order, unsupported sort, unsupported filter, selection, object navigation, and error states.
- Date, time, date-time, text, multiline, enum, boolean, conversion, validation, and localization states.
- Action prompt open, invalid, valid, invoking, cancellation, scalar result, object result, collection result, void completion, and failure.
- Generic semantic page, mixed custom page, and raw-widget custom page.

## Viewports and preferences

- Desktop: 1440 by 1000 CSS pixels.
- Narrow: 390 by 844 CSS pixels.
- Light and dark color schemes.
- Reduced and normal motion.
- Forced colors through Chromium emulation or a documented manual fallback.

No retained screenshot may hide page-level overflow, open overlays, validation, focus, or long-label behavior needed for interpretation.

## Lifecycle journeys

- Connect a generic fragment and await custom-element definition.
- Open and close Combo Box, multi-select, Grid details or selection, typed fields, and action dialog.
- Supersede an in-flight autocomplete request with a newer filter.
- Replace generic with custom and raw-widget pages for at least twenty generations.
- Exercise back and forward navigation in the analysis router.
- Remove a fragment while an overlay and request are active.
- Verify overlay cleanup, listener cleanup, focus restoration, stale callback suppression, console errors, external requests, and retained DOM counts.

## Accessibility evidence

Automated axe-core checks run on generic, semantic custom, and raw-widget pages in desktop and narrow modes.
Manual keyboard journeys cover tab order, labels, required and invalid fields, Combo Box filtering and selection, multi-selection removal, Grid focus and navigation, dialog focus containment and restoration, object links, and error announcements.

Defects are classified as Vaadin component, Causeway adapter, fixture content, browser, or unsupported-contract issues.

## Performance and delivery evidence

The harness records:

- Installed source and dependency closure.
- Broad selective bundle and smaller per-widget entry bundles.
- Raw, gzip, and SHA-256 values.
- Cold and warm request count and encoded bytes.
- Module fetch, custom-element readiness, fixture ready, first Grid rows, route-ready, and interaction timings.
- Eager broad loading versus route-lazy reference and Grid chunks.
- External network requests, including any attempted usage-statistics submission.

Five successful headless runs provide median timings.

## Packaging and supply-chain evidence

A clean pinned npm acquisition builds deterministic local assets.
An analysis Maven JAR packages the generated assets and all required license material beneath `META-INF/resources` and `META-INF/licenses`.
The browser must run without a CDN.
The assessment records direct and transitive licenses, npm audit results, provenance, integrity, notices, update process, cache assumptions, and explicit Pro exclusions.

## Hard gates

- All required runtime packages pass project licensing and provenance policy.
- No required commercial component.
- No Flow runtime, route, state, Binder, Java DataProvider, or protocol.
- Domain data and interactions use only the public rich GraphQL contract.
- Searchable reference selection and lazy Grid behavior are credible without another widget library.
- Custom HTML page composition and stable Causeway semantics remain available.
- Selective offline Maven packaging is reproducible.
- Required keyboard, focus, names, validation, contrast, and responsive behavior is credible.

## Weighted scoring

- Domain widget coverage and Wicket parity: 30%.
- GraphQL architecture and custom-page composability: 25%.
- Accessibility and interaction correctness: 15%.
- Supply chain, maintenance, and Maven packaging: 15%.
- Performance and selective delivery: 10%.
- Theming and visual consistency: 5%.

Scores range from 0 to 5 and link to retained evidence.
Hard-gate failure cannot be compensated by a weighted score.
