## Why

A breadcrumb containing only the current object communicates no navigable hierarchy and consumes visual space without helping the user.
Root objects with no valid parents should therefore omit breadcrumb presentation while descendants retain the established ancestor trail.

## What Changes

- Hide `<cw-breadcrumbs>` and render no landmark when authoritative ready-state metadata contains no valid ancestor entries.
- Restore normal visible breadcrumb rendering when the same component later receives one or more valid ancestors.
- Preserve loading, unsupported, partial-error, and terminal-error diagnostics because those states communicate useful component status.
- Update Petclinic owner-page acceptance so root owners show no breadcrumb, while pet and visit pages continue showing their navigable hierarchy.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Change ready-state root breadcrumb presentation from a current-only landmark to no rendered breadcrumb.
- `generic-htmx-web-component-viewer`: Update the Petclinic breadcrumb demonstration and regression coverage for root-owner omission.

## Impact

The change affects the foundation breadcrumb renderer and unit tests, Petclinic Playwright assertions and helpers, and the corresponding Web Components and HTMX viewer specifications.
GraphQL breadcrumb discovery, ancestor navigation events, descendant presentation, and error handling remain unchanged.
