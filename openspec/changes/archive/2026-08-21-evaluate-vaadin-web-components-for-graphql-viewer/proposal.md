## Why

The earlier theming analysis treated Bootstrap as a visual-system candidate but did not evaluate whether one maintained widget suite could replace the Wicket viewer's WicketStuff and Select2 ecosystem while preserving the composable GraphQL-driven web-component architecture.
Vaadin's standalone Web Components offer searchable and lazy data controls, grids, date and time editors, uploads, and a wider application widget vocabulary, and their relationship to a separately considered server-side Vaadin viewer provides useful product consistency without requiring this viewer to adopt Vaadin Flow state management.

## What Changes

- Evaluate the Apache-2.0 Vaadin Web Components free core as a browser-side widget layer for the existing GraphQL web-component and HTMX viewer.
- Keep the public rich GraphQL API as the exclusive source of domain descriptions, object state, choices, validation, interactions, and collection data.
- Exclude Vaadin Flow, server-side component state, server-side routing, and any implementation of a Vaadin server-side viewer from this change.
- Build bounded prototypes for GraphQL-backed lazy Grid, searchable single-reference Combo Box, multi-reference selection, date and time values, validation, disabled reasons, navigation, and action interaction.
- Exercise the prototypes both in the generic viewer and in a router-selected custom object HTML page to verify that application composition remains ordinary HTML rather than Java Vaadin extension code.
- Evaluate a tiered extension boundary in which stable `<causeway-*>` components and semantic events remain the primary contract while an allowlisted set of bundled `<vaadin-*>` widgets may be available as an optional lower-level page-authoring facility.
- Compare prototype behavior with the current Causeway components and the Wicket viewer's Select2-backed reference selection and collection behavior.
- Record accessibility, browser lifecycle, theming, performance, bundle, dependency, license, provenance, update, and deterministic offline Maven-packaging evidence.
- Reject Vaadin Pro or other commercially licensed components and identify any required capability that cannot be satisfied by the Apache-2.0 free core.
- Publish an architectural decision and a separate implementation proposal outline if adoption is recommended.
- Do not add a production Vaadin dependency or change runtime viewer behavior in this analysis change.

## Capabilities

### New Capabilities

- `vaadin-web-component-graphql-viewer-evaluation`: Defines the standalone Vaadin Web Component prototypes, GraphQL adapter behavior, composable custom-page checks, free-core licensing gates, comparative evidence, and adoption decision required before production use.

### Modified Capabilities

None.

## Impact

The change adds analysis artifacts, reproducible prototype assets, browser evidence, and decision records beneath its OpenSpec change directory.
It evaluates integration points in the web-component foundation, GraphQL object context, domain component renderers, HTMX route lifecycle, custom object fragment resolution, Petclinic reference application, Maven resource packaging, and Playwright acceptance suite without modifying their production behavior.
Any later adoption would require a separate OpenSpec change covering pinned Vaadin dependencies, generated browser assets, internal adapters, supported lower-level extension APIs, theme integration, compatibility policy, and migration or rollback.
A possible server-side Vaadin viewer remains an independent initiative that may share widget and theme decisions but not state management, rendering code, routing, or extension APIs with this viewer.
