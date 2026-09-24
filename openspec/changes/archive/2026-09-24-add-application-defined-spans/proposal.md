## Why

Applications with long-running Causeway actions need a supported way to divide an action invocation into meaningful child spans without depending on Causeway's internal observation classes or owning another telemetry SDK.
A public application-facing API can preserve the enclosing action's logical identity while allowing the application to identify each region clearly.

## What Changes

- Add an application-facing `ApplicationSpanService` with synchronous `call` and `run` operations for executing work within an application-defined observation span.
- Ensure action executions, including contributed mixin actions, expose their domain-facing logical member identity to the service.
- Derive each span's contextual display name from the current interaction's canonical `<logical-type-name>#<member-logical-name>` identity, one plain-space separator, and an application-provided suffix.
- Preserve the application-provided suffix when enforcing the 50-character contextual-name limit, rejecting suffixes longer than 46 characters and truncating or compacting the logical member identifier first.
- Retain the full logical member identifier and suffix as bounded, non-instance-specific observation attributes so truncated display names remain diagnosable.
- Parent application-defined spans beneath the currently active action or nested application span and propagate failures through the existing observation lifecycle.
- Make the service harmless when observation is inactive, without requiring applications to use Micrometer or OpenTelemetry APIs directly.

## Capabilities

### New Capabilities

- `application-defined-spans`: Public API, naming, parentage, metadata, error handling, and inactive-profile behavior for application-defined spans within Causeway interactions.

### Modified Capabilities

- None.

## Impact

The change adds a public service contract to `causeway-applib` and a runtime implementation backed by the existing `CausewayObservationIntegration` and dedicated observation registry.
It corrects the logical identifier stored by mixin `ActionInvocation` executions and affects observation naming utilities, runtime service composition, tests, and tracing documentation.
It introduces no new telemetry SDK, exporter, or third-party dependency and preserves the Java agent as the telemetry owner.
