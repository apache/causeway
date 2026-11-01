## Why

The HTMX viewer now obtains its semantic provider and route boundaries from declarative HTML, but its only shell template is still packaged inside the viewer and cannot be shaped by an application developer.
Applications need to control stable layout decisions such as branding, menu placement, authentication chrome, result placement, side regions, and footer content without copying or maintaining the framework-sensitive document head.

## What Changes

- Split full-page composition into a viewer-owned HTMX document scaffold and an application-owned stable shell fragment.
- Discover at most one bounded application shell from a documented private classpath location, with deterministic diagnostics for duplicates, malformed content, unsafe size, missing required landmarks, or unresolved reserved bindings.
- Retain a validated built-in shell as the compatibility fallback when the application supplies none.
- Let the application shell own the stable `<cw-graphql-client>`, branding, `<cw-menubars>` placement, authentication-chrome slot, loading and announcement regions, action-result outlet, route region, route-content slot, and footer presentation.
- Validate required semantic and routing landmarks without prescribing their visual hierarchy, parent layout, CSS classes, or whether menus are placed in a header or side region.
- Keep the document doctype, `<html>` runtime attributes, framework-sensitive `<head>`, HTMX configuration and assets, common component assets, CSP-sensitive delivery, and authentication metadata under the HTMX adapter's bounded document scaffold.
- Bind only a documented closed set of escaped configuration values and validated structural slots; do not introduce a general template language.
- Make shell selection, caching, reload behavior, and failure handling explicit while preserving direct navigation, HTMX fragment navigation, history, authentication, focus, announcements, result presentation, and route-context disposal.
- Add a Petclinic-owned shell resource that demonstrates application-level shell customization while retaining the existing visual behavior.
- Keep vertical-menu rendering and any menu-orientation API out of scope; this change only establishes application ownership of menu placement.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Refine the stable semantic application-shell contract so applications can supply the shell body independently of the viewer-owned HTMX document scaffold, with bounded discovery, validation, fallback, and lifecycle behavior.

## Impact

- Affects HTMX shell loading, declarative-template validation and binding, full-page rendering, diagnostics, resource caching/reload policy, documentation, and tests.
- Adds a conventional private application-shell resource to the Petclinic sample.
- Preserves existing applications through the built-in fallback and does not alter route-page, collection-presentation, or row-preview resource conventions.
- Establishes a cleaner prerequisite for the planned vertical application-menu analysis without implementing vertical menus or changing `<cw-menubars>` behavior.
