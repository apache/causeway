## Why

Foreground traces are rooted in Java-agent-owned HTTP server spans, so Jaeger and similar backends list them under transport-facing names such as `GET /wicket/...` even when Causeway child spans already expose the domain operation.
Giving the foreground entry span a bounded semantic operation name makes trace lists searchable and understandable without inventing a trace-level name or replacing agent ownership.

## What Changes

- Treat the Java-agent-created foreground HTTP server span's operation name as the portable approximation of a trace display name.
- Capture that entry span and request-local naming state in `CausewayForegroundTraceFilter` while the request is active.
- Allow trusted Causeway action-invocation, action-prompt, and entity-page instrumentation to nominate bounded semantic names without exposing unrestricted trace naming as an application API.
- Select candidates deterministically with priority `act` over `prompt` over `view`, and retain the first deterministic candidate at equal priority.
- Rename the captured entry span before the foreground filter returns, while preserving standard HTTP attributes, execution-mode classification, trace parentage, sampling, and export.
- Leave the agent-provided HTTP operation name unchanged when no supported candidate exists, no Java agent is attached, or the current entry span is non-recording.
- Reuse the existing case-preserving 50-character naming and canonical-identifier policy without adding instance-specific data.
- Verify against OpenTelemetry Java agent 1.31.0 that the agent does not overwrite the selected name after the filter returns, with a bounded `causeway.trace.name` attribute and collector-side rename retained only as a documented fallback if that evidence gate fails.
- **BREAKING**: Foreground HTTP server span operation names, and therefore backend grouping based on those names, can change from transport routes to semantic `act`, `prompt`, or `view` names.

## Capabilities

### New Capabilities

- `semantic-trace-display-naming`: Defines request-local candidate nomination, deterministic selection, bounded entry-span renaming, safe fallback behavior, and preservation of transport telemetry.

### Modified Capabilities

- `semantic-span-display-naming`: Narrows the existing guarantee that automatic HTTP span names remain unchanged so that the selected foreground entry span may be renamed by the dedicated semantic trace-name mechanism while other automatic span naming remains unchanged.

## Impact

The change affects core webapp foreground filtering, internal observation coordination, action invocation instrumentation, Wicket page and prompt instrumentation, contextual naming utilities, tracing compatibility fixtures, and operational documentation.
Dashboards and alerts that group foreground server spans by operation name may need to use standard HTTP route attributes or the new semantic names instead.
The OpenTelemetry Java agent remains the owner of span creation, context propagation, HTTP and JDBC instrumentation, sampling, and export, and no application-owned SDK, processor, sampler, or exporter is introduced.
