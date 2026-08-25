## 1. Baseline and Contract Fixtures

- [x] 1.1 Record the current HTMX registry, renderer, Petclinic factory, packaged resource, layout fallback, route, CSP, default-toolkit, native-toolkit, and browser acceptance baselines.
- [x] 1.2 Record checksums for the retained Petclinic grid, collection-column, menu, stylesheet, and provenance resources so the HTML migration cannot silently replace fallback evidence.
- [x] 1.3 Add deterministic logical-type page fixtures covering application directories, dependency jars, Java factories, absent pages, mixed registrations, invalid names, empty content, malformed UTF-8, NUL content, oversized content, and registry-count overflow.
- [x] 1.4 Define bounded safe source identifiers and expected error classifications without exposing full classpath URLs or page contents.

## 2. Private HTML Page Discovery

- [x] 2.1 Add the documented `META-INF/causeway/webcomponents/pages/*.html` classpath discovery contract to the HTMX viewer module.
- [x] 2.2 Derive each exact logical type from its filename without changing, interpreting, shortening, hashing, aliasing, or fabricating the logical name.
- [x] 2.3 Discover pages consistently from exploded application resources and dependency jars using the existing Spring classpath resource abstraction.
- [x] 2.4 Enforce non-empty strict UTF-8 decoding, NUL rejection, the 256-KiB per-page ceiling, a finite aggregate page-count ceiling, and one immutable loaded string per accepted page.
- [x] 2.5 Reject invalid names, unreadable resources, duplicate resources, and all other defective discovered registrations with bounded deterministic startup errors.
- [x] 2.6 Keep discovered pages private from ordinary static-resource mappings and avoid adding a route or controller that serves raw page resources.
- [x] 2.7 Add focused loader tests for every accepted and rejected resource shape, deterministic enumeration behavior, bounded messages, and immutable retained content.

## 3. Unified Page Registry and Route Resolution

- [x] 3.1 Introduce one internal page-definition abstraction that distinguishes resource, Java-factory, and generic sources while preserving the public factory SPI.
- [x] 3.2 Merge accepted resource definitions and injected `HtmxPageFragmentFactory` beans into one immutable exact-logical-type registry.
- [x] 3.3 Reject resource/resource, factory/factory, and resource/factory duplicates without selecting by bean, resource, or classpath order.
- [x] 3.4 Return literal resource content for an exact resource registration without passing route identity or application services into the page text.
- [x] 3.5 Preserve validated route identity delivery to existing Java factories and prove unchanged dynamic-factory behavior.
- [x] 3.6 Preserve `<causeway-object editable>` beneath the same route context when no exact custom definition exists.
- [x] 3.7 Preserve custom/generic page classification and add only the minimum safe source classification needed by tests and later prototype diagnostics.
- [x] 3.8 Add registry and renderer tests for exact matching, case sensitivity, resource selection, factory compatibility, every duplicate combination, absence fallback, escaped outer identity, one context, and one interaction controller.

## 4. Lifecycle, Security, and Packaging

- [x] 4.1 Verify full-page and HTMX fragment responses place resource HTML beneath the existing route context without duplicating the shell, GraphQL client, or interaction controller.
- [x] 4.2 Verify route replacement disconnects resource-page consumers and preserves cancellation, stale-response rejection, focus, announcements, history, and semantic result policy.
- [x] 4.3 Preserve the strict CSP, including `style-src-attr 'none'`, and verify resource pages require no inline handler, inline style, script, hash, nonce, or policy relaxation.
- [x] 4.4 Verify resource pages use the configured application stylesheet and documented Causeway selectors and variables without exposing raw internal Vaadin elements.
- [x] 4.5 Verify ordinary Maven resource processing packages private pages in exploded and jar forms without npm installation, JavaScript bundling, CDN retrieval, or executable Spring Boot repackaging.
- [x] 4.6 Run HTMX module unit, integration, route-policy, CSP, packaging, and RAT checks after registry integration.

## 5. Petclinic HTML Authoring

- [x] 5.1 Create `petclinic.HomePage.html` with an object header, application HTML regions, explicit owner and future-visit collections, and declarative collection columns.
- [x] 5.2 Create `petclinic.PetOwner.html` with semantic identity, contact, detail, editable, action, pet-collection, and visit-collection composition using accessible application-owned grouping.
- [x] 5.3 Create `petclinic.Pet.html` with semantic owner reference, name, species, notes, actions, and responsive application-owned grouping.
- [x] 5.4 Create `petclinic.Visit.html` with semantic pet reference, local temporal value, reason, notes, actions, and responsive application-owned grouping.
- [x] 5.5 Keep action placement accessible and adjacent where appropriate without implementing the separate nested associated-action proposal in this change.
- [x] 5.6 Remove `PetClinicHomeFragmentFactory` and verify no Petclinic Java class renders or registers application page markup.
- [x] 5.7 Retain the Petclinic Spring Boot application, module imports, common viewer properties, stylesheet, Wicket comparison, domain model, fixtures, and root redirect.
- [x] 5.8 Verify all retained object grids, collection-column files, menu resource, provenance, and stylesheet remain present and baseline-compatible.
- [x] 5.9 Add source and packaged-artifact tests proving exactly four logical-type HTML pages exist at the private location and contain public `<causeway-*>` markup.

## 6. Petclinic Integration and Browser Qualification

- [x] 6.1 Extend random-port Petclinic integration tests to request all four canonical logical-type routes and prove exact resource-page selection beneath one route context.
- [x] 6.2 Prove an absent registration uses generic `<causeway-object editable>` and retained layout resources without treating absence as an error.
- [x] 6.3 Prove a discovered defective or duplicate Petclinic-style registration fails startup rather than falling back.
- [x] 6.4 Exercise property reads and edits, reference values, choices, defaults, validation, object actions, collection loading, explicit columns, semantic results, and canonical navigation across the four pages.
- [x] 6.5 Run the default Vaadin policy matrix and prove reference, basic, numeric, and local-temporal assets remain independently route-lazy and absent from ineligible routes.
- [x] 6.6 Run the explicit native policy matrix and prove the same HTML pages work without Vaadin hashes or asset requests.
- [x] 6.7 Extend Playwright coverage for direct entry, HTMX navigation, back and forward history, replacement disposal, keyboard and focus behavior, responsive layouts, light and dark presentation, and Wicket-relative comparison.
- [x] 6.8 Fail browser qualification on CSP violations, unsuccessful GraphQL responses, console or page errors, unexpected external requests, accessibility violations, horizontal overflow, duplicate action requests, or protected-value disclosure.

## 7. Documentation and Release Evidence

- [x] 7.1 Document the private page location, exact filename registration, trusted literal-content boundary, route-context ownership, factory compatibility, absence fallback, defective-registration failure, size bounds, and packaging behavior in the HTMX viewer guide.
- [x] 7.2 Document Petclinic as the primary HTML-authored application example and explain why its layout and column-order resources intentionally remain as generic fallback evidence.
- [x] 7.3 Document that application page customization requires no HTMX-specific Java renderer, server template engine, frontend framework, or second domain-state channel.
- [x] 7.4 Document the separation from the associated-action composition and prototype page-authoring diagnostics proposals.
- [x] 7.5 Record implementation, production-isolation, source-visibility, packaging, fallback, CSP, default/native, accessibility, browser, and Wicket-comparison evidence with reproducible commands.
- [x] 7.6 Run applicable HTMX, foundation, Petclinic, GraphQL compatibility, Maven reactor, Node, browser, RAT, formatting, strict OpenSpec, and Git whitespace gates.
- [x] 7.7 Confirm no dependency, package lock, generated Vaadin asset, GraphQL schema, route format, inventory, retained layout, or Reference Application behavior changed unexpectedly.
