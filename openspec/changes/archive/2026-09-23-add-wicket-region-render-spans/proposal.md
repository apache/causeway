## Why

Entity-page rendering time is currently hidden inside the broad Causeway root-interaction span, making it difficult to determine whether a slow page is dominated by a fieldset, property, collection, or action-button region.
Now that Causeway has an opt-in Micrometer Observation bridge to the OpenTelemetry Java agent, the Wicket viewer can add bounded semantic render spans without introducing another SDK or exporter.

## What Changes

- Add a semantic page-render observation around server-side Wicket entity-page rendering.
- Add nested semantic render observations for fieldsets, regular entity properties, entity collections, and relevant action buttons.
- Emit stable observation names and bounded layout or metamodel identifiers that let operators identify slow regions without recording object values or instance identifiers.
- Include both full-page and Ajax partial rendering when an instrumented region is rendered.
- Exclude collection table cells, per-row actions, action parameters, browser-side rendering, and component preparation from this initial capability.
- Preserve no-op behavior when the `observation` Spring profile is inactive and preserve the Java agent as the sole owner of the OpenTelemetry SDK, sampling, and export.
- Record rendering failures and defensively close active observation scopes at the end of the Wicket request cycle.
- Document the new viewer span taxonomy, privacy constraints, expected trace shape, and diagnostic limitations.

## Capabilities

### New Capabilities

- `wicket-region-render-observation`: Server-side semantic observations for Wicket page and logical-region rendering, including naming, metadata, nesting, lifecycle safety, exclusions, and inactive-mode behavior.

### Modified Capabilities

None.

## Impact

The change affects the Wicket UI components representing entity pages, fieldsets, properties, collections, and action links, together with the Wicket viewer request-cycle integration used for defensive cleanup.
It uses the existing `CausewayObservationIntegration` and Micrometer Observation dependencies and introduces no new telemetry SDK, exporter, or third-party dependency.
Operational tracing documentation and focused Wicket observation tests will be added or updated.
The feature remains opt-in through the existing `observation` profile, but an observed entity page will produce additional spans proportional to its logical regions.
