## 1. Pin and review the copied corpus

- [ ] 1.1 Record upstream repository `apache/causeway-app-referenceapp`, revision `29b43bfe4f77d525fb345394e5a52bd7d85a91ba`, license, source paths, and review date in `PROVENANCE.adoc`.
- [ ] 1.2 Inventory domain, common manifest, JPA fixture, resource, documentation, Wicket-specific, operational, code-generation, test, and deployment paths before selecting the retained boundary.
- [ ] 1.3 Define explicit retained-path and omitted-path manifests with a reason for every omitted source family and no blanket omission of unsupported domain semantics.
- [ ] 1.4 Copy the approved source and resources without package renames, stylistic rewrites, generated build output, obsolete launchers, or repository metadata.
- [ ] 1.5 Generate deterministic per-file checksums and add offline verification that reports missing, additional, and changed copied paths.
- [ ] 1.6 Review Apache headers, NOTICE implications, binary and generated resources, and narrow RAT exclusions for the retained corpus.

## 2. Add reusable regression modules

- [ ] 2.1 Add a non-release `regressiontests/referenceapp` aggregator and wire it into the regression-test reactor without changing production module dependencies.
- [ ] 2.2 Add a reusable copied-domain module with Causeway reactor parentage, adapted dependency management, retained resources, and deterministic checksum verification.
- [ ] 2.3 Add viewer-neutral JPA application support containing the approved common and JPA manifests, fixture services, stable identities, and semantic target catalogue.
- [ ] 2.4 Keep Wicket panels, Wicket UI extensions, browser drivers, and viewer routing out of the neutral domain and JPA support modules.
- [ ] 2.5 Add targeted compilation and package tests proving that the neutral modules produce reactor artifacts while remaining install- and deploy-ineligible outside the test workflow.
- [ ] 2.6 Measure clean and incremental compile time, artifact size, and ordinary reactor impact against the pre-copy baseline.

## 3. Establish the deterministic rich runtime

- [ ] 3.1 Add an HTMX Reference Application launcher importing the copied JPA manifest, rich GraphQL viewer, generic HTMX viewer, Wicket comparison viewer, persistence, and required value-type modules.
- [ ] 3.2 Configure non-conflicting `/graphql`, `/htmx`, and `/wicket` routes, strict CSP, application entries, branding, deterministic database state, and no external runtime assets.
- [ ] 3.3 Configure the Reference Application security model with deterministic demonstration credentials and a separate diagnostic-only bypass mode if startup diagnosis requires it.
- [ ] 3.4 Add startup tests for the effective metamodel, GraphQL schema, application home, menu resource, grid resources, representative object identities, and both viewer routes.
- [ ] 3.5 Add deterministic fixture reset or per-test fixture isolation for mutable, destructive, and ordering-sensitive journeys.
- [ ] 3.6 Verify that unsupported retained features fail visibly and are not removed, hidden through configuration, or converted into successful-looking fallback values merely to pass startup.

## 4. Build the capability inventory

- [ ] 4.1 Define a versioned machine-readable inventory schema for logical types, members, operations, value shapes, structural resources, fixture targets, classifications, and bounded reasons.
- [ ] 4.2 Generate inventory inputs from the effective metamodel, standard GraphQL introspection, rich operation discovery, menu and grid resources, and stable fixture identities.
- [ ] 4.3 Implement the `SUPPORTED`, `GRACEFUL_UNSUPPORTED`, `GRAPHQL_GAP`, `VIEWER_DEFECT`, `VIEWER_SPECIFIC`, and reasoned `NOT_EXERCISED` classification rules.
- [ ] 4.4 Require every in-scope discovered item to have exactly one classification and reject unknown, duplicate, silently omitted, or successful-looking lossy behavior.
- [ ] 4.5 Redact credentials, hidden values, resource bodies, submitted arguments, authorization details, response bodies, stack traces, and remote exception text from inventory and diffs.
- [ ] 4.6 Check in and review the initial inventory baseline with stable counts, identifiers, representative targets, and bounded semantic diffs for future drift.
- [ ] 4.7 Add tests for additions, removals, classification changes, unsupported behavior, redaction, and deterministic regeneration.

## 5. Add staged integration and browser coverage

- [ ] 5.1 Add an opt-in Java Playwright profile and headless lifecycle that starts the application on a random port without affecting ordinary browser-independent builds.
- [ ] 5.2 Add stable authenticated journeys for application home, service menus, direct generic objects, canonical navigation, grids, tabs, and route history.
- [ ] 5.3 Add representative property journeys for text, multiline, boolean, nullable, numeric, enum, temporal, reference, custom, resource, editable, hidden, disabled, valid, invalid, cancelled, and stale states.
- [ ] 5.4 Add representative action journeys for parameterless and parameterized actions, choices, autocomplete, defaults, dependent parameters, validation, disabled reasons, cancellation, concurrency, and object, scalar, collection, and void outcomes.
- [ ] 5.5 Add representative collection journeys for empty and populated collections, configured columns, polymorphic rows, bounded windows, associated actions, stale windows, and partial row errors.
- [ ] 5.6 Add lifecycle assertions for HTMX replacement, disconnection, request cancellation, route supersession, overlay disposal, focus restoration, menu dismissal, and repeated navigation.
- [ ] 5.7 Add accessibility, keyboard, narrow, light, dark, reduced-motion, forced-colors, CSP, external-request, console-error, page-error, and overflow gates for retained representative pages.
- [ ] 5.8 Compare representative Wicket and HTMX outcomes over the same authenticated fixture while allowing viewer-specific DOM, theme, route, and extension behavior.
- [ ] 5.9 Make mutating journeys order-independent through disposable records or deterministic restoration and verify repeated suite execution.

## 6. Publish and integrate the baseline

- [ ] 6.1 Document targeted compilation, ordinary regression, inventory regeneration, secured runtime, diagnostic runtime, Playwright, and cleanup commands.
- [ ] 6.2 Publish the initial support and gap report with classification totals, representative examples, reactor cost, browser outcomes, and reproduction evidence.
- [ ] 6.3 Separate discovered correctness, GraphQL contract, scalability, Vaadin adapter, viewer-specific, and content exceptions into prioritized follow-on recommendations without fixing them opportunistically.
- [ ] 6.4 Run the complete affected Maven reactor, strict OpenSpec validation, RAT, package verification, GraphQL startup, inventory, HTMX, Wicket comparison, and headless browser suites.
- [ ] 6.5 Verify that production viewer JARs, dependencies, default configuration, routes, and browser assets are unchanged by the regression-only modules.
