## Why

The first declarative-layout demonstration exposes visual and interaction mismatches: tab content sits on a grey surface unlike regular panels, metadata actions occupy a large disclosure row, and the artificial Layout help tab does not demonstrate a useful object-page composition.
The layout should fit the established viewer presentation while retaining foundation-owned semantics and equivalent HTMX and Vue behavior.

## What Changes

- Make tab controls and panels visually integrate with regular white viewer panels instead of introducing a grey tab-content background.
- Replace the metadata fieldset's full-width **Actions** disclosure with an accessible ellipsis menu attached to the fieldset heading.
- Keep metadata actions as ordinary authorized `<cw-action>` descendants, preserving their authoritative order, disabled state, prompts, invocation, results, and lifecycle.
- Recompose the PetOwner demonstration as an **Identity** tab followed by a **Metadata** tab.
- Remove the artificial **Layout help** tab and its non-domain placeholder content from both HTMX and Vue demonstrations.
- Preserve responsive layout, keyboard/pointer tab operation, direct-child grammar, object context, routing, and host neutrality.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine shared tab surfaces and present metadata actions through a compact accessible fieldset-heading ellipsis menu.
- `generic-htmx-web-component-viewer`: Recompose the HTMX PetOwner demonstration around useful Identity and Metadata tabs.
- `generic-vue-web-component-viewer`: Preserve equivalent Vue composition and behavior for the refined PetOwner tabs and metadata action menu.

## Impact

The change affects foundation layout-element rendering and shared styles, metadata action-menu presentation, foundation tests and documentation, HTMX and Vue PetOwner declarations, browser acceptance tests, and regenerated Vue production assets.
It introduces no new domain authority, GraphQL endpoint, host routing behavior, or third-party dependency.
