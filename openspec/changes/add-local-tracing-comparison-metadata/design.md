## Context

The local tracing helper is sourced from an application's working directory and configures the OpenTelemetry Java agent through environment variables.
It currently assigns `OTEL_SERVICE_NAME` but does not provide resource metadata that distinguishes two local instances of the same application.
A regression run commonly starts baseline and candidate processes from separate terminals or worktrees and sends both to the same Jaeger instance.
The Java agent remains the telemetry owner, and any metadata must be configured without adding application instrumentation or an SDK.

## Goals / Non-Goals

**Goals:**

- Keep baseline and candidate traces under one shared Jaeger service.
- Provide a low-cardinality role that supports direct `baseline` and `candidate` filtering.
- Identify the source commit automatically while permitting an explicit version override.
- Preserve resource attributes supplied by the caller or hosting platform.
- Make missing Git context and dirty-worktree provenance visible without preventing local tracing.

**Non-Goals:**

- Create spans or mutate Causeway semantic span names.
- Derive a Maven artifact version or inspect the application's dependency graph.
- Encode uncommitted file content into `service.version`.
- Add a regression-run identifier or coordinate process ports and application startup.
- Define production deployment metadata for Azure Container Apps or another platform.

## Decisions

### Use one shared service name

Both processes use the same `OTEL_SERVICE_NAME` because they represent one logical application and must appear under one Jaeger service.
Separate service names such as `<app>-baseline` and `<app>-candidate` were rejected because they fragment service-oriented searches and comparisons.

### Map helper inputs to resource attributes

`OTEL_SERVICE_ROLE` maps to the custom resource attribute `service.role`, and `OTEL_SERVICE_VERSION` maps to the standard resource attribute `service.version`.
These variables are helper-owned conveniences rather than OpenTelemetry Java agent configuration keys, so the script adds their resolved values to `OTEL_RESOURCE_ATTRIBUTES`.
`service.role` is intentionally a stable deployment role rather than an instance identifier, URL value, process identifier, or generated run identifier.
The role remains optional for ordinary one-process tracing, while the operations guide recommends `baseline` and `candidate` for comparisons.

### Prefer explicit version and otherwise use the application Git commit

A non-empty caller-supplied `OTEL_SERVICE_VERSION` is authoritative.
Otherwise the helper runs `git rev-parse --verify HEAD` against the current working directory and uses the full commit SHA.
The current working directory is intentional because the helper is sourced from the application directory even when the script itself resides in the Causeway checkout.
The full SHA is preferred over a Maven version or abbreviated SHA because it is reproducible and collision-resistant.
If no commit can be resolved, the helper omits `service.version`, emits a warning, and continues.

### Report but do not encode dirty-worktree state

When the version was Git-derived, the helper checks tracked and untracked worktree changes and warns if the checkout is dirty.
The helper does not append `-dirty` to `service.version` because that would stop the value from being the exact commit identifier without identifying the changed content.
An explicit `OTEL_SERVICE_VERSION` remains available when a locally installed framework build or another external input is more relevant than the application commit.

### Preserve resource metadata with deterministic helper precedence

The helper preserves unrelated entries already present in `OTEL_RESOURCE_ATTRIBUTES`.
When an existing `service.role` or `service.version` entry conflicts with a resolved helper value, the helper-owned value replaces that key so that each resource contains one deterministic value.
This avoids discarding Azure-provided resource metadata and avoids relying on duplicate-key behavior in a particular OpenTelemetry agent release.

### Show resolved comparison identity

The helper's completion output includes the service name and any resolved role and version.
This gives operators a pre-start verification point before the application JVM and Java agent consume the environment.

## Risks / Trade-offs

- [Risk] `service.role` is a custom semantic-convention extension and could conflict with a future OpenTelemetry definition. → Document that the helper owns its current meaning and keep values low-cardinality.
- [Risk] Parsing comma-separated `OTEL_RESOURCE_ATTRIBUTES` can mishandle unsupported delimiter characters in values. → Follow the OpenTelemetry environment-variable format, preserve valid unrelated entries, and cover replacement behavior with focused shell tests.
- [Risk] A clean commit SHA does not identify locally installed dependencies outside the application repository. → Allow `OTEL_SERVICE_VERSION` to override Git derivation.
- [Risk] A dirty checkout makes the commit SHA incomplete provenance. → Emit a clear warning while retaining the exact SHA.
- [Risk] Sourcing the helper outside the application checkout cannot derive a useful version. → Continue without `service.version` and explain the explicit override.
