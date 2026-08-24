# Production isolation verification

The implementation changes only the existing HTMX Java and browser route codecs, their tests and documentation, and pinned Reference Application qualification.
No Maven POM, npm manifest, lock file, dependency version, third-party source, GraphQL operation, generated GraphQL name, metamodel bookmark format, application fixture, or persisted data changed.

No public `<causeway-*>` element, semantic event name, event detail, context API, canonical route grammar, custom-page contract, HTMX response header, browser-history policy, base-path property, context-path behavior, or Wicket comparison route changed.
The authoritative identifier is preserved rather than transformed.

No JavaScript asset URL, route-lazy boundary, Vaadin bundle, checksum, exact CSP hash, CSP directive, widget qualification policy, native fallback, or default-selection behavior changed.
Rejected route content remains absent from markup, response headers, diagnostics, errors, logs, and evidence.

The accepted per-segment envelope remains bounded at 4,096 canonical encoded characters.
The change adds no server-side alias registry, session route state, query parameter, cookie, redirect, telemetry, CDN request, or application configuration.
