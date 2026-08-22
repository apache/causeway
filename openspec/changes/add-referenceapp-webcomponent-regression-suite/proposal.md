## Why

Petclinic provides cohesive end-to-end coverage but intentionally exercises only a small domain vocabulary.
The Apache Causeway Reference Application provides a much broader corpus of properties, actions, collections, choices, autocomplete, validation, layouts, value semantics, security, and extensions that can expose GraphQL and viewer gaps before Vaadin becomes the viewer-wide default.

## What Changes

- Copy a pinned, provenance-recorded subset of `apache/causeway-app-referenceapp` into isolated reactor modules rather than depending on another repository or unpublished snapshots.
- Preserve a viewer-neutral Reference Application domain and deterministic JPA fixture that can be reused by HTMX, Wicket, and future viewers.
- Add an HTMX Reference Application launcher combining the rich GraphQL viewer, generic HTMX viewer, Wicket comparison viewer, JPA persistence, and representative security configuration.
- Generate a machine-readable capability inventory that classifies discovered members and value shapes as supported, gracefully unsupported, GraphQL-limited, viewer-defective, or viewer-specific.
- Add staged integration and headless browser journeys covering representative menus, layouts, properties, actions, references, values, collections, lifecycle, security, accessibility, CSP, and responsive behavior.
- Record gaps without pruning difficult upstream domain cases or silently weakening the copied corpus.
- Keep heavy browser coverage opt-in while retaining ordinary reactor compilation, metamodel, GraphQL schema, and deterministic inventory checks.

## Capabilities

### New Capabilities

- `reference-application-viewer-regression-suite`: Defines the pinned reusable domain corpus, provenance, deterministic runtime, capability inventory, cross-viewer comparison, and staged regression gates.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Adds broad Reference Application qualification alongside the focused Petclinic acceptance application.

## Impact

The change adds approximately six megabytes and roughly nineteen hundred source and resource files beneath isolated non-release sample or regression modules.
It affects the Maven reactor, GraphQL and HTMX test launchers, JPA fixture startup, browser-test profiles, RAT exclusions for retained upstream resources, and viewer regression documentation.
No production runtime dependency, public GraphQL operation, default viewer configuration, or Vaadin selection policy changes in this change.
