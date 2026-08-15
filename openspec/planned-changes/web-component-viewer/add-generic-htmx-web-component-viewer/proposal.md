## Why

The semantic web-component library lets applications compose custom Causeway pages, but users also need a usable default object viewer that requires no page-specific frontend development.
An HTMX reference viewer will provide generic routing, composition, layout, and customization while proving that the framework-neutral components remain the sole implementation of domain-member semantics.

## What Changes

- Add an opt-in generic domain-object viewer whose shell, navigation, history, and page-fragment lifecycle use HTMX.
- Define canonical object routes based on logical type name and identifier and translate semantic component navigation events into those routes.
- Add a page-definition resolver that selects an application page registered for a logical type or falls back to generic schema-driven composition.
- Build generic object pages from the object context's introspected rich-schema description without requesting a duplicate member list.
- Compose the standard object header, property, action, collection, editor, and prompt components rather than duplicating their semantics.
- Interpret an available Causeway grid resource for ordering and grouping, with a deterministic conventional fallback when no usable grid is available.
- Preserve one object context across each page definition so generic and custom regions share schema descriptions, state, commands, and coordinated reads.
- Provide application extension points for custom page templates or factories, navigation policy, theme, and result presentation.
- Add deep-link, refresh, back and forward, loading, not-found, partial-error, and terminal-error behavior.

## Capabilities

### New Capabilities

- `generic-web-component-viewer`: Provides an HTMX-based generic domain-object page viewer and per-logical-type page customization over the semantic web-component library.

### Modified Capabilities

None.

## Impact

- Adds an optional viewer module, browser assets, route and fragment handling, default theme, and demonstration application.
- Depends on the archived `graphql-web-component-context` and `domain-web-components` capabilities.
- Uses the public rich GraphQL endpoint and its referenced resources; it does not access Causeway metamodel internals to construct pages.
- Does not require applications using the component library to adopt HTMX.
- Initially focuses on bookmark-addressable domain-object pages rather than replacing every application menu, authentication, home-page, or standalone-value feature of existing viewers.
