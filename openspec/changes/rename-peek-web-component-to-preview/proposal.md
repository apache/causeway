## Why

The public `<cw-peek>` element uses terminology that conflicts with the `previews/...` resources and the user-facing preview behavior it renders.
Renaming the element to `<cw-preview>` gives the component, resource convention, and feature one consistent name.

## What Changes

- **BREAKING** Rename the public custom element `<cw-peek>` to `<cw-preview>` without retaining `<cw-peek>` as an alias.
- Rename foundation source files, exports, registrations, CSS selectors, tests, and documentation from peek terminology to preview terminology where they identify the component.
- Update HTMX preview-resource validation and resolution to require a `<cw-preview>` root while preserving the existing `META-INF/causeway/webcomponents/previews/...` resource convention and HTTP behavior.
- Update HTMX and Vue Petclinic authored markup, acceptance coverage, and committed production assets to use `<cw-preview>`.
- Preserve collection-row preview identity, hydration, lifecycle, accessibility, security, and navigation semantics.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Rename the declarative collection-row preview element and its live subtree contract from `<cw-peek>` to `<cw-preview>`.
- `generic-htmx-web-component-viewer`: Require preview resources and inline declarations to use `<cw-preview>` while retaining the existing preview resource location and host-resolution semantics.

## Impact

The change affects the foundation component API, collection integration, styles, exports, tests, HTMX preview-resource client validation, sample markup, Vue production assets, acceptance tests, documentation, and the two corresponding OpenSpec capabilities.
Existing applications that author `<cw-peek>` must migrate their markup and imports to `<cw-preview>`.
