## 1. Deterministic PDF.js adapter assets

- [x] 1.1 Select and pin a `pdfjs-dist` release compatible with the repository Node toolchain, document the selected display, worker, CMap, font, and decoding assets, and retain required Apache-2.0 license and notice material.
- [x] 1.2 Add a foundation PDF.js asset subproject with locked installation, deterministic generation, audit, and verification scripts, then package its committed assets through the foundation Maven JAR.
- [x] 1.3 Add an explicit Maven regeneration profile and ordinary validation checks proving that packaged PDF.js assets match the locked generated output without requiring npm during normal builds.

## 2. PDF qualification and authored property options

- [x] 2.1 Extend normalized value-renderer state so `<cw-property>` can pass bounded PDF presentation options without exposing its DOM element or weakening the pure renderer contract.
- [x] 2.2 Add strict parsers for `pdf-render`, `pdf-initial-page`, and `pdf-zoom`, including defaults, bounds, diagnostics, connected attribute changes, and inert behavior for unqualified values.
- [x] 2.3 Register a standard PDF Blob renderer above the standard Blob renderer and below application renderers, qualifying only exact authoritative `application/pdf` values with acceptable same-origin bytes URLs.
- [x] 2.4 Preserve ordinary Blob metadata and resource-link rendering for link mode, unavailable bytes, non-PDF values, and every PDF initialization or rendering failure.

## 3. Continuous PDF document reader

- [x] 3.1 Add a property-owned PDF reader controller that lazily imports the packaged display module, configures the same-origin module worker, and owns loading-task, document, page, canvas, observer, and control state.
- [x] 3.2 Implement auto, manual, and link mode transitions so manual mode performs no PDF.js import or inline byte request before **Preview document** activation.
- [x] 3.3 Create ordered placeholders for every authoritative page and use bounded intersection-driven scheduling to render visible and nearby pages progressively.
- [x] 3.4 Implement current-page tracking, previous and next navigation, first and final page boundaries, initial-page resolution, and complete final-page reachability.
- [x] 3.5 Implement `page-width`, `page-fit`, `actual-size`, and bounded percentage zoom with zoom controls, capped device-pixel scaling, resize debounce, and generation-safe rerendering.
- [x] 3.6 Implement finite document, page, image, canvas, concurrency, and nearby-page limits and keep those limits out of authored per-property configuration.
- [x] 3.7 Disable excluded PDF.js layers and embedded behavior, reject password challenges without prompting, and retain bounded local failure plus the resource link.
- [x] 3.8 Cancel render tasks, disconnect observers, destroy PDF.js loading and document state, and reject stale completions on property rerender, source or option replacement, route replacement, and disconnection.
- [x] 3.9 Provide a labelled reader region, page and total status, page labels, keyboard-operable controls, visible focus, polite loading state, bounded error state, and explicit external-reader fallback without claiming canvas text accessibility.

## 4. Host integration and policy

- [x] 4.1 Extend the HTMX content-security policy with explicit `worker-src 'self'` while retaining existing same-origin script and connection policy and excluding remote, blob, data, eval, and inline allowances.
- [x] 4.2 Verify foundation PDF assets and authorized Blob URLs resolve correctly under root and non-root servlet or viewer paths for both HTMX and Vue hosts.
- [x] 4.3 Verify native and Vaadin presentation policies share the same PDF reader behavior, controls, diagnostics, resource links, and lifecycle ownership.

## 5. Deterministic sample and acceptance coverage

- [x] 5.1 Add a small deterministic, compatibly licensed multipage PDF Blob fixture to the shared Petclinic domain without introducing PDF.js, HTMX, or Vue dependencies there.
- [x] 5.2 Add authored HTMX and Vue sample property presentations covering auto, manual, and link modes while leaving effective-grid PDF attribute propagation absent.
- [x] 5.3 Add foundation tests for MIME qualification, application-renderer precedence, option parsing, loading modes, progressive page scheduling, navigation, zoom, safety limits, accessibility, failures, and generation-safe cleanup using a deterministic adapter seam.
- [x] 5.4 Add browser network and presentation assertions proving auto mode loads automatically, manual mode loads only after activation, link mode never loads PDF.js, and all pages including the final page are reachable.
- [x] 5.5 Run equivalent ordinary and secured HTMX and Vue journeys and verify resource authorization, session expiry, route replacement, console cleanliness, worker cleanup, native presentation, and Vaadin presentation.

## 6. Documentation and deferred metadata direction

- [x] 6.1 Document PDF qualification, `pdf-render`, `pdf-initial-page`, `pdf-zoom`, CSS sizing variables, controls, accessibility limits, fallback behavior, security boundaries, and deterministic PDF.js maintenance.
- [x] 6.2 Document that PDF options are authored HTML only and that effective grids, GraphQL metadata, and domain annotations do not supply them in this change.
- [x] 6.3 Record the deferred `@PdfJsViewer` facet direction and intended authored-attribute-over-facet-over-default precedence without adding annotation or metamodel code.

## 7. Verification

- [x] 7.1 Run focused foundation value-renderer, property, PDF adapter, lifecycle, accessibility, and style tests.
- [x] 7.2 Run HTMX controller and CSP tests plus ordinary and secured HTMX and Vue integration and headless browser suites.
- [x] 7.3 Run deterministic asset regeneration comparison, packaging, RAT and license checks, formatting, inspections, and strict OpenSpec validation for all affected modules.
