## 1. Shell Resource and Contract

- [ ] 1.1 Split the current internal full-page template into a viewer-owned HTMX document scaffold and a separately named built-in default `<body>` shell without changing fallback output or behavior.
- [ ] 1.2 Add bounded zero-or-one discovery for `META-INF/causeway/webcomponents/shells/htmx.html`, deterministic duplicate handling, immutable registration, and private-resource delivery.
- [ ] 1.3 Implement cached and reload shell loading with bounded UTF-8, size, empty, NUL, document-boundary, structural-landmark, containment, and reserved-binding validation.
- [ ] 1.4 Separate required and optional shell bindings while preserving the closed vocabulary, context-appropriate escaping, trusted structural slots, and single-pass substitution.
- [ ] 1.5 Add focused tests for no resource, one resource, dependency resource, duplicate resources, cached content, reload edits, reload defects, oversized and malformed content, misplaced landmarks, and unknown tokens.

## 2. Full-Page Composition and Lifecycle

- [ ] 2.1 Refactor full-page rendering to bind the internal document scaffold and authoritative application or fallback shell as distinct stages around the selected route fragment.
- [ ] 2.2 Preserve language, canonical route attributes, widget policy, framework assets, configured application stylesheet, CSP-compatible markup, authentication metadata, authentication chrome, and comparison-link behavior across the split.
- [ ] 2.3 Make runtime shell checks and HTMX bootstrap selectors depend on protocol landmarks rather than the fallback shell's header, navbar, footer, or direct-child layout.
- [ ] 2.4 Preserve fragment-only responses, stable GraphQL and menu contexts, loading state, action results, announcements, history, focus, stale-response rejection, and route-context disposal.
- [ ] 2.5 Extend controller and renderer tests for fallback and application shells, secure and anonymous states, escaped bindings, flexible landmark layouts, malformed contracts, direct requests, and HTMX fragment requests.

## 3. Petclinic and Documentation

- [ ] 3.1 Add a Petclinic-owned `META-INF/causeway/webcomponents/shells/htmx.html` that preserves the current visual shell while making its body layout directly editable in application source.
- [ ] 3.2 Update HTMX usage documentation with document-versus-shell ownership, the private resource convention, required landmarks and containment, reserved tokens, optional presentation bindings, caching/reload behavior, diagnostics, CSS ownership, fallback, and migration guidance.
- [ ] 3.3 Add integration and browser assertions proving Petclinic selects its application shell and preserves direct navigation, HTMX navigation, menus, authentication, results, loading, history, focus, route teardown, and native/Vaadin presentation.

## 4. Qualification

- [ ] 4.1 Run HTMX Java and Node, foundation Node, Petclinic integration and supported Playwright, secured-viewer, JavaScript syntax, IntelliJ build, strict OpenSpec, and whitespace validation.
