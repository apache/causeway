## Context

The Java agent creates HTTP and Quartz entry spans before framework or application code runs, and Causeway's semantic interaction, action, and JDBC spans inherit those contexts.
Jaeger can search traces by a span attribute in logfmt form, but the current spans do not carry a bounded attribute that distinguishes foreground HTTP work from scheduled background-command work.
Background command pollers can execute every ten seconds even when no command is pending, causing background traces to dominate searches while still being operationally useful when explicitly requested.
The maintenance publication process replaces Causeway naming with Isis throughout published source and resources, so the framework-owned attribute name and helper must follow the same transformation.
Some applications, including Estatio, use customized copies of the command-log job; framework changes cannot automatically classify those application-owned classes.

## Goals / Non-Goals

**Goals:**

- Mark agent-created HTTP entry spans as foreground from one framework webapp boundary shared by all viewers.
- Mark the command-log extension's agent-created Quartz entry spans as background before polling or execution.
- Provide one framework-internal helper that safely tags the current OpenTelemetry span and can also be called by application-defined jobs.
- Use one low-cardinality attribute with exactly two stable values that Jaeger and other backends can query.
- Preserve no-agent behavior and Java-agent ownership of the SDK and exporter.
- Verify the exported foreground attribute through the real-agent compatibility fixture and cover background classification with focused tests.

**Non-Goals:**

- Drop, sample, or prevent storage of background traces.
- Change Quartz schedules, command execution, or observation activation.
- Infer arbitrary application background work from thread names, usernames, or naming conventions.
- Attach user, command, tenant, target, or other instance-specific data.
- Add a public applib telemetry API or an application-owned OpenTelemetry SDK.

## Decisions

### Classify the current agent-owned entry span

A `CausewayTraceClassifier` helper in `core/config` will call the OpenTelemetry API's `Span.current().setAttribute(...)` using `causeway.execution.mode` and an enum-backed `foreground` or `background` value.
`core/config` already depends on the OpenTelemetry API while excluding SDK ownership, so the helper introduces no new telemetry runtime.
Calling the helper without the Java agent is safe because the current span is non-recording and attribute mutation is a no-op.

Creating new observations for classification was considered, but rejected because it would add spans rather than annotate the existing trace entry and would make Jaeger searches less direct.
Using Micrometer's current observation was considered, but rejected because the automatic HTTP or Quartz span exists before a Causeway observation and must be the trace-level search anchor.

### Use a framework-owned bounded attribute

The attribute will be `causeway.execution.mode` with exactly `foreground` and `background` values.
The maintenance source transformation is expected to publish this as `isis.execution.mode`, matching the transformed helper and other framework-owned telemetry identifiers.
An application-neutral key such as `execution.mode` was considered, but rejected because custom telemetry attributes should be namespaced to avoid collisions.
The existing action attribute `causeway.execution.initiatedBy` was considered, but rejected because it describes action invocation semantics and is absent from empty polling traces.

### Mark foreground at the common servlet boundary

A lightweight servlet filter in `core/webapp` will mark the current span before continuing the filter chain.
It will be registered for web requests through the existing `WebModuleAbstract` mechanism and imported by `CausewayModuleCoreWebapp`, allowing Wicket, RESTful Objects, GraphQL, health, and other HTTP paths to share one classification point.
The Java agent wraps the servlet filter chain, so its HTTP server span is current when the filter executes.

Adding classification independently to `WebRequestCycleForCauseway`, `CausewayRestfulObjectsInteractionFilter`, and GraphQL execution was considered, but rejected because it duplicates viewer-specific code and can miss future viewers.
Adding foreground classification to `InteractionServiceDefault` was considered, but rejected because a core interaction does not know whether its caller is HTTP, Quartz, a test, or another programmatic entry point.

### Mark framework background commands at their Quartz entry

`RunBackgroundCommandsJob.execute(...)` in the command-log extension will call the classifier before checking pause state or opening a Causeway interaction.
The Java agent's Quartz span is current at method entry, so one attribute classifies the complete trace containing polling, semantic, action, and JDBC descendants.
Paused polls remain background-classified because they are still Quartz executions and should not appear in foreground searches.

Inferring background mode from `scheduler_user` or a `QuartzJob:` thread prefix was considered, but rejected because user and thread naming are application-specific and fragile.
Classifying every framework interaction opened by the job was considered, but rejected because a single entry-span attribute is sufficient for trace search and avoids repeated attributes.

### Make custom jobs an explicit downstream responsibility

The classifier will be accessible from the exported core-config observation package so application-owned Quartz jobs can call it without importing or constructing an SDK.
The operations guide will show the transformed Isis call and attribute expected by maintenance-branch applications.
Causeway cannot transparently modify copied or independently implemented job classes, so downstream adoption will be documented rather than inferred.

### Test classification without adding an SDK

Focused tests will install a mock current `Span` in an OpenTelemetry `Context`, invoke the helper, servlet filter, or paused background-job path, and verify the bounded attribute call and unchanged control flow.
The Java 11 agent regression fixture will register the foreground filter and assert that the exported HTTP trace contains the foreground attribute.
Tests will also prove that classification remains safe when no valid agent span is current.

## Risks / Trade-offs

- [Risk] The web filter runs before an HTTP span is current under an unsupported servlet agent integration → Retain the real-agent exported-attribute regression test.
- [Risk] Application-defined jobs remain unclassified → Provide the reusable classifier and a transformed Isis example for custom jobs.
- [Risk] Operators assume classification reduces telemetry volume → State explicitly that Jaeger filtering changes discovery only; sampling or collector processing is still required to reduce ingestion and retention.
- [Risk] The source rename does not transform the literal as expected → Keep the attribute in framework source, verify transformed publication behavior, and document both source and published names.
- [Risk] A background trace also receives foreground classification through an internal HTTP call → Treat mode as an entry-span classification rather than a global trace invariant; search the intended entry operation and attribute together when mixed traces occur.
- [Trade-off] Health and static HTTP requests are classified as foreground → Prefer a consistent transport-origin definition over viewer-specific exclusions.

## Migration Plan

No application migration is required for framework HTTP requests or the framework-provided command-log job.
Maintenance-branch applications with custom background jobs will add one transformed classifier call at the job entry point.
Existing Jaeger searches continue to work, while new searches can use `causeway.execution.mode=foreground` or `causeway.execution.mode=background`, and renamed applications use the corresponding `isis.execution.mode` key.
Rollback removes the classifier calls and filter registration without affecting application behavior or trace parentage.

## Open Questions

None.
