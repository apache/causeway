## Context

The current HTMX full-page template is declarative but is loaded as one fixed viewer-internal `shell.html` containing the document scaffold, framework assets, semantic provider, menu placement, route landmarks, and application chrome.
Applications can author complete route pages but cannot author the stable shell that surrounds them.
This contradicts the intended cross-viewer ownership model in which the application declares semantic context boundaries and the host binds runtime values and manages routing lifecycle.

The document has two different rates and sources of change.
The doctype, `<html>` runtime attributes, `<head>`, HTMX configuration, script URLs, shared styles, CSP-sensitive markup, and authentication metadata are adapter concerns that evolve with the HTMX viewer.
The `<body>` layout, branding, menu location, result location, route geometry, auxiliary regions, and footer are application presentation concerns.

## Goals / Non-Goals

**Goals:**

- Give an application direct HTML ownership of the stable HTMX body shell.
- Keep framework-sensitive document and head boilerplate maintained by the viewer.
- Preserve one stable GraphQL client and all routing, authentication, result, focus, announcement, history, and teardown behavior.
- Permit arbitrary ordinary-HTML layout around required bounded landmarks without prescribing a horizontal header or particular CSS hierarchy.
- Provide deterministic classpath discovery, bounded validation, cached and reload behavior, and safe diagnostics.
- Preserve existing applications through a built-in default shell.
- Demonstrate the ownership boundary in Petclinic source.

**Non-Goals:**

- Implement vertical menus or add a horizontal/vertical `<cw-menubars>` API.
- Make the viewer document scaffold or framework asset list application-replaceable.
- Introduce a general template language, conditionals, loops, expressions, or domain-data interpolation.
- Define one literal shell file shared by HTMX, Vue, Svelte, and Angular.
- Change route-page, collection-presentation, preview, GraphQL, menu-resource, or authentication semantics.
- Permit arbitrary public URLs or filesystem paths as shell sources.

## Decisions

### Separate the document scaffold from an application body shell

The viewer SHALL own an internal complete-document scaffold through the end of `</head>` and SHALL insert one validated application shell whose single root is `<body>` before closing `</html>`.
The application shell SHALL therefore control body attributes and all stable body layout while remaining unable to replace document runtime attributes, security metadata, or framework assets.

The default result has this composition:

```text
HTMX document scaffold
├── <!doctype html>
├── <html data-causeway-*="...">
├── <head>viewer and configured application assets</head>
├── {{causeway.applicationShell}}
│   └── <body>application-owned stable shell</body>
└── </html>
```

A complete application-owned document was rejected because every application would inherit maintenance responsibility for HTMX versions, shared component assets, metadata, CSP-sensitive markup, and future viewer upgrades merely to rearrange body content.
A set of header, menu, main, and footer slots inside a viewer-owned body was rejected because it would continue to prescribe the application layout and would not support arbitrary side regions cleanly.
A new `<cw-application-shell>` component was rejected because ordinary light-DOM HTML and CSS already express the required composition without adding another semantic component or shadow-DOM styling boundary.

### Use one conventional private application resource with a built-in fallback

The application shell convention SHALL be `META-INF/causeway/webcomponents/shells/htmx.html`.
The loader SHALL enumerate the classpath at startup, normalize discovered resources, accept zero or one unique definition, and reject duplicate definitions deterministically.
Zero application definitions SHALL select the viewer's separately named internal default shell.
One application definition SHALL take authority, and a defective application definition SHALL fail closed rather than silently selecting the default.

The internal document scaffold and fallback shell SHALL live under a viewer-private internal location that is not also treated as the application convention.
This avoids relying on classloader shadowing order to decide whether application or library content wins.
An explicit arbitrary shell path property was rejected because a single convention is easier to package, test, document, and bound, while additional named shells can be considered later if a demonstrated use case requires them.

### Reuse the existing resource-page mode

Shell content SHALL follow the configured `CACHED` or `RELOAD` resource-page mode and its default of `CACHED`.
In cached mode, bounded UTF-8 content and its contract SHALL be validated and retained at application-context startup.
In reload mode, the unique shell registration SHALL remain fixed while its content is reopened and revalidated for each full-page render.
A defective reloaded shell SHALL fail that full-page render without serving stale content or falling back, while already-connected pages remain able to use ordinary HTMX fragment routes that do not re-render the shell.
Adding a second shell-specific mode was rejected because shell and page HTML are the same class of private developer-authored presentation resource and independent policies would add configuration without a current need.

### Validate protocol landmarks without fixing presentation hierarchy

An application shell SHALL contain exactly one `<body>` root and exactly one stable `<cw-graphql-client>`.
The GraphQL client SHALL contain exactly one `<cw-menubars>`, one route region, one stable action-result outlet, one loading region, one announcement region, and one authentication-chrome slot.
The exact route-content slot SHALL occur inside the route region.
The shell SHALL contain the exact GraphQL-endpoint binding on the GraphQL client and the required structural bindings exactly once where uniqueness is necessary for lifecycle correctness.

Validation SHALL permit the application to choose ordinary wrapper elements, classes, order, CSS layout, branding markup, footer markup, and whether `<cw-menubars>` is placed in a header, aside, or another stable region.
Existing stable IDs used as HTMX targets and accessibility relationships SHALL remain required in this change unless the implementation can replace them with equally unambiguous marker attributes without changing observable behavior.
Nested route object contexts remain forbidden in the shell because route identity belongs only to replaceable route pages.
Scripts, document roots, and head elements inside the application shell SHALL be rejected so framework and CSP ownership cannot leak into the body resource.

### Divide the closed binding vocabulary by ownership layer

Document-scaffold binding SHALL retain language, base path, canonical path, widget-policy attributes, authentication metadata, brand title, context path, common assets, and configured application stylesheet.
Application-shell binding SHALL expose escaped `basePath`, escaped `brand`, escaped `graphQlEndpoint`, validated authentication chrome, validated route content, and the bounded comparison link needed by the default sample footer.
The application shell MAY omit optional presentation values such as brand or comparison link, but SHALL include required provider, authentication, and route structural bindings.
Unknown reserved `{{causeway.*}}` tokens SHALL fail closed, and inserted structural fragments SHALL not be reparsed for token substitution.
No metamodel, persistence, arbitrary configuration, request parameter, or GraphQL result becomes addressable through this vocabulary.

### Keep semantic ownership and lifecycle unchanged

The application owns the declared shell markup, while the HTMX adapter owns discovery, validation, escaped binding, document composition, request routing, and lifecycle policy.
Full-page responses SHALL compose the current shell around the selected route page.
HTMX fragment responses SHALL continue to return only the route page so the connected GraphQL client, menu state, result outlet, and shell DOM remain stable across navigation.
The browser bootstrap SHALL diagnose missing or duplicate runtime landmarks but SHALL not manufacture, move, or repair application shell elements.

### Make Petclinic demonstrate the convention without changing presentation

Petclinic SHALL package `META-INF/causeway/webcomponents/shells/htmx.html` with the current body shell structure and visual behavior.
This makes the editable shell visible beside its pages, collections, and previews while keeping the viewer's fallback useful to applications that have not migrated.
Documentation SHALL show both the minimum required shell contract and layout alternatives without presenting vertical menu rendering as already supported.

## Risks / Trade-offs

- [Risk] Application shell markup can break routing or accessibility by moving or omitting protocol landmarks. → Mitigation: validate exact cardinality, containment, required attributes, bindings, and bounded content at startup and on reload.
- [Risk] Classpath dependencies may accidentally contribute competing shells. → Mitigation: use one private convention, normalize resource identities, and fail startup on more than one unique application shell.
- [Risk] Splitting document and body templates can accidentally produce malformed document boundaries. → Mitigation: require one `<body>` root, reject document/head elements, and keep opening and closing `<html>` solely in the internal scaffold.
- [Risk] Existing CSS assumes the default body and navbar hierarchy. → Mitigation: retain the fallback unchanged, migrate Petclinic without visual changes, document that custom layouts own corresponding CSS, and keep protocol selectors independent of layout selectors.
- [Risk] Reload mode can make new full-page requests fail during an edit. → Mitigation: use stable bounded diagnostics and preserve the established no-stale-content reload policy.
- [Risk] Required authentication chrome can be accidentally hidden by application layout. → Mitigation: require the structural authentication slot while leaving its visual placement application-owned.
- [Trade-off] Applications cannot replace arbitrary head content in this change. → This intentionally keeps framework, security, and upgrade-sensitive concerns in the adapter; a bounded head-extension mechanism can be proposed separately if concrete needs emerge.

## Migration Plan

1. Introduce separate internal document and default-shell resources while preserving byte-for-byte equivalent default full-page structure where practical.
2. Add the application-shell loader, immutable zero-or-one registry, bounded validation, and cached/reload behavior.
3. Refactor full-page rendering into document binding followed by validated shell binding and route insertion.
4. Keep zero-resource applications on the built-in fallback with no configuration change.
5. Add the Petclinic application shell by copying the current default body structure and verify unchanged behavior.
6. Update documentation with the resource convention, required landmarks, tokens, diagnostics, CSS ownership, and migration steps.
7. Roll back by removing the application shell resource and selecting the built-in fallback; no route-page migration is required.

## Open Questions

None required before implementation.
Future work may evaluate bounded head extensions, named shell selection, and vertical menu rendering independently.
