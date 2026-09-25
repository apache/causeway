## Context

Causeway currently writes foreground and background classifications under the constant OpenTelemetry span attribute key `causeway.execution.mode`.
The servlet filter and command-log Quartz job reach the static classifier through lightweight test seams, while `CausewayConfiguration` is already the public, typed configuration surface available to framework code and consuming applications.
The change crosses the config, webapp, command-log, tests, and documentation modules but must remain backward compatible on this decommissioning branch.

## Goals / Non-Goals

**Goals:**

- Expose `causeway.execution.mode.key` through the typed `CausewayConfiguration` hierarchy.
- Preserve `causeway.execution.mode` as the default key.
- Ensure framework foreground and background classification use the same configured key that applications can read.
- Retain no-agent and non-recording-span behavior.

**Non-Goals:**

- Change the bounded `foreground` and `background` attribute values.
- Change trace names, parentage, sampling, or exporter behavior.
- Introduce a new telemetry dependency or configuration subsystem.

## Decisions

### Model the key in `CausewayConfiguration`

Add an `execution.mode.key` hierarchy under the existing `causeway` root, initialized to `causeway.execution.mode`.
This gives application code a stable typed accessor and lets Spring Boot bind an override through its normal relaxed configuration rules.
A standalone `@Value` injection was considered, but it would not expose the setting through the framework's established configuration API.

### Pass the configured key to the classifier

Extend the trace-classification helper so callers provide the attribute key when classifying the current span, and update both the foreground web filter and background command job to obtain that key from `CausewayConfiguration`.
This keeps one classification implementation while ensuring every framework-owned execution-mode classification honors the same setting.
Leaving the classifier hard-coded while merely publishing a configuration value was rejected because an override would then make framework and application spans inconsistent.

### Preserve lightweight test seams

Keep constructor-level seams around span classification and add the configured key to those seams rather than requiring an OpenTelemetry SDK in unit tests.
Tests will verify default binding, override binding, and propagation of the selected key from each entry point.

## Risks / Trade-offs

- [Risk] Renaming the attribute through configuration can break existing backend searches and dashboards. → Preserve the old key as the default and document that overrides require corresponding observability-query updates.
- [Risk] One entry point could continue using the old constant. → Add focused tests for both foreground and background classification paths.
- [Trade-off] Adding a nested configuration hierarchy introduces API surface for one setting. → Use the existing `CausewayConfiguration` convention so consuming applications do not need a separate configuration bean.
