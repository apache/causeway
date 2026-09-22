## Why

Automatic HTTP, Quartz, JDBC, and Causeway semantic spans currently share a service without a stable way to distinguish user-facing request traces from scheduled background-command traces.
Frequent background polling therefore dominates Jaeger searches, so operators need bounded trace classification that survives the maintenance build's Causeway-to-Isis renaming.

## What Changes

- Add a framework-internal trace-classification helper that marks the current agent-owned span with a stable low-cardinality execution-mode attribute without creating an SDK or exporter.
- Add a core webapp observation filter that classifies Java-agent-created HTTP entry spans as `foreground` across Wicket, RESTful Objects, GraphQL, and other web entry points.
- Classify the command-log extension's `RunBackgroundCommandsJob` Quartz entry span as `background` before polling or executing commands.
- Use `causeway.execution.mode` with bounded values `foreground` and `background`, allowing the maintenance publication transform to produce the corresponding `isis.execution.mode` attribute.
- Make the helper reusable by application-defined background jobs, including customized copies of `RunBackgroundCommandsJob` that are outside the framework's control.
- Add focused no-agent, filter, background-job, rename-awareness, and real-agent exported-attribute coverage.
- Document Jaeger attribute searches and clarify that classification improves trace discovery but does not itself reduce ingestion, retention, or memory consumption.

## Capabilities

### New Capabilities
- `foreground-background-trace-classification`: Stable classification of HTTP foreground traces and scheduled background-command traces for filtering in Jaeger and other telemetry backends.

### Modified Capabilities

None.

## Impact

The change affects `core/config`, `core/webapp`, the command-log applib extension, tracing compatibility regression coverage, and the repository-local tracing operations guide.
It adds no OpenTelemetry SDK or exporter and does not change observation activation, sampling, command scheduling, or application behavior.
Applications with custom background job implementations must call the reusable classification helper themselves because framework instrumentation cannot identify arbitrary application schedulers.
The maintenance publication transform is expected to rename the framework-owned attribute namespace and helper from Causeway to Isis consistently.
