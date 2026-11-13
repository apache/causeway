## Why

Classpath HTML pages are decoded into immutable strings during HTMX viewer startup, so editing an existing page during development requires restarting the Spring application context before the change can be observed.
An opt-in reload mode would shorten the authoring loop to build-and-refresh while preserving immutable, fail-fast packaged behavior by default.

## What Changes

- Add an explicit HTMX viewer resource-page mode with cached production behavior as the default and reload-on-render behavior as an opt-in development mode.
- Keep resource discovery, logical-type registration, page-count bounds, duplicate detection, and conflicts with Java fragment factories fixed and validated at startup in both modes.
- In reload mode, retain each discovered page resource and decode its current content whenever that already-registered page is rendered.
- Reapply the existing bounded, non-empty UTF-8 and NUL-content validation to every reload and report a bounded failure instead of silently serving stale content or generic fallback.
- Do not rescan the classpath during rendering, so adding, deleting, or renaming a page still requires application restart.
- Keep packaged dependency resources, private-resource visibility, CSP, route handling, response cache control, and Java fragment-factory behavior unchanged.
- Configure the Petclinic sample to demonstrate edit, IntelliJ build, and browser refresh without a manual application restart.
- Document the IntelliJ resource-copy requirement and the optional automatic-build setting that can remove the explicit build step.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Add opt-in current-content reloading for startup-registered private classpath HTML pages while retaining cached immutable behavior by default.

## Impact

The change affects HTMX viewer configuration properties, classpath page loading and page definitions, registry wiring, request-time error handling, module and integration tests, Petclinic development configuration, and viewer documentation.
No production default, route, public page URL, component vocabulary, third-party dependency, or packaged application contract changes.
