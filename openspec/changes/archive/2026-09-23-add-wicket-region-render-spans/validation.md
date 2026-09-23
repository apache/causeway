# Validation

## Automated Tests

The focused Wicket and observation test suite passed with no failures.
`WicketRenderObservationTest` ran nine tests covering descriptor metadata, nested scope lifecycle, failure cleanup, Wicket rendering parentage, inactive mode, serialization, inclusion policy, exclusion policy, and Ajax partial rendering.
The existing `ObservationClosureTest`, `CausewayObservationConfigurationTest`, `InteractionServiceDefaultObservationTest`, and `MemberExecutorServiceDefaultObservationTest` suites also passed.

## Local Jaeger Trace

A representative `TestAppJdoWkt` application was run with OpenTelemetry Java agent 1.31.0, the `observation` Spring profile, always-on sampling, and Jaeger 2.21.0.
The `JdoInventoryJaxbVm` page contained multiple fieldsets, regular properties, collections with table rows and cells, entity actions, and a collection-associated action.

Full-page trace `c8f5306f661634960f4095a189715512` contained 35 spans, of which 24 were Causeway Wicket spans:

```text
causeway.wicket.page.render        1
causeway.wicket.fieldset.render    2
causeway.wicket.property.render    5
causeway.wicket.collection.render  4
causeway.wicket.action.render     12
```

The page span was a child of `causeway.root.interaction`.
Fieldsets and collections were children of the page span, regular properties were children of their fieldsets, and the collection-associated action was a child of its collection.
The exported attributes contained `causeway.module=viewer.wicket`, `causeway.object.type`, and only the applicable static fieldset or member identifier.
The unnamed fieldset was represented by the bounded identifier `<default>`.

The page displayed collection tables with multiple rows, but only the five regular entity properties produced property spans.
No per-cell property spans or per-row action spans were exported.

Editing the `name` property generated Ajax trace `b2f059051b188b62f524125cc7f7fb9e`.
That trace contained one `causeway.wicket.property.render` span beneath `causeway.root.interaction` and no `causeway.wicket.page.render` span, confirming partial-render behavior.
