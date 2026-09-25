## Context

`InteractionServiceDefault` creates a `CausewayInteraction` with its UUID before starting the `causeway.root.interaction` observation.
That observation spans the lifetime of the top-level interaction and naturally parents Causeway semantic work, but it currently exposes only generic bean and module attributes.
Operators consequently cannot move directly from an interaction ID recorded in command, execution-log, or audit data to the corresponding trace.

The Java agent owns the telemetry SDK, HTTP entry spans, propagation, sampling, and export.
Causeway instrumentation must continue to use the existing Micrometer observation integration and must remain inert when the `observation` profile is inactive.

## Goals / Non-Goals

**Goals:**

- Correlate one root interaction span with the exact UUID of its `CausewayInteraction`.
- Export the UUID under the stable attribute name `causeway.interaction.id`.
- Classify the UUID as high-cardinality telemetry data.
- Verify the attribute both at observation creation and after Java-agent OTLP export.
- Explain how to locate a containing trace with Jaeger tag search.

**Non-Goals:**

- Changing the generated interaction ID or its lifecycle.
- Using an interaction ID as an OpenTelemetry trace ID or span ID.
- Changing span names, hierarchy, sampling, propagation, or export.
- Copying the interaction ID to the agent-owned HTTP span or every descendant span.
- Adding an application telemetry API, SDK, exporter, or dependency.

## Decisions

### Annotate the root interaction observation

`InteractionServiceDefault` will supply the newly created interaction's UUID while constructing the root observation and will add `causeway.interaction.id` before the observation starts.
This preserves the one-to-one relationship between a Causeway interaction and its root interaction span, including non-HTTP and background interactions.

Annotating the Java-agent-owned HTTP span was rejected because a request can theoretically contain multiple sequential interactions and because background interactions have no HTTP parent.
Annotating every descendant was rejected because trace ancestry already provides correlation and duplication would increase telemetry volume.

### Use a high-cardinality observation key value

The UUID will be represented as its canonical string and added with Micrometer's high-cardinality observation API.
The attribute is useful for exact lookup but unsuitable for metric dimensions or low-cardinality aggregation.

Using the UUID in the span display name was rejected because it would destroy useful operation grouping and create unbounded operation-name cardinality.

### Remain within the observation abstraction

Root observation creation will continue through `CausewayObservationIntegration.ObservationProvider` and `ObservationClosure`.
The runtime service will not manipulate an OpenTelemetry `Span` directly, preserving inactive-registry behavior and the existing Java-agent ownership boundary.

### Verify the exported contract

The root-observation unit test will use a deterministic interaction ID and inspect the observation context for the exact high-cardinality value.
The Java-agent compatibility fixture will also use a known UUID and assert that the OTLP-exported root interaction span contains `causeway.interaction.id` with that value.
The operations runbook will document exact Jaeger tag lookup and advise leaving the operation filter unset so the root interaction span can satisfy the attribute query.

## Risks / Trade-offs

- **High-cardinality indexing can increase backend storage and index costs** → Attach the UUID only to one span per interaction and document its intended exact-correlation use.
- **A backend may not index arbitrary span attributes** → Document the validated Jaeger behavior without claiming universal backend support.
- **Users may expect the attribute on the HTTP entry span** → Document that Jaeger returns the containing trace when the matching attribute is on the root interaction child span.
- **Traces held by Jaeger's in-memory store can expire before lookup** → Retain the existing ring-buffer warning and clarify that lookup only works while the trace remains stored.
