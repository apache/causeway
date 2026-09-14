## Why

Petclinic and the HTMX Reference Application currently inherit the shared theme's bounded desktop content and shell widths, leaving substantial unused horizontal space on wide displays.
These two demonstration and regression applications should exercise the available viewport width while preserving responsive gutters and narrow-screen behavior.

## What Changes

- Override the documented shell and content width variables in Petclinic's application stylesheet.
- Apply the same application-local overrides in the Reference Application HTMX stylesheet.
- Add browser and stylesheet regression checks for wide-screen use and retained viewport gutters.
- Leave shared foundation, HTMX viewer, and web-component styling unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Require Petclinic's application-owned presentation to use the available wide viewport.
- `reference-application-viewer-regression-suite`: Require the Reference Application's HTMX regression presentation to use the available wide viewport.

## Impact

Only the Petclinic and Reference Application HTMX application stylesheets and their tests are affected.
There are no shared web-component, viewer, route, GraphQL, dependency, or public API changes.
