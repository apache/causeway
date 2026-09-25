## Why

The execution-mode trace attribute key is hard-coded, so consuming applications cannot reliably reuse the framework's configured key in their own filters or instrumentation.
Exposing the key as configuration provides one shared source of truth while preserving the existing attribute name by default.

## What Changes

- Add the `causeway.execution.mode.key` configuration property with the default value `causeway.execution.mode`.
- Make Causeway's foreground and background trace classification use the configured execution-mode attribute key.
- Allow consuming applications to obtain the same key through `CausewayConfiguration` for custom filters and instrumentation.
- Cover the default and overridden key behavior with focused tests and update tracing configuration guidance.

## Capabilities

### New Capabilities

- `configurable-execution-mode-attribute`: Defines the shared configurable attribute key used to classify foreground and background execution spans.

### Modified Capabilities

None.

## Impact

The change affects `CausewayConfiguration`, execution-mode trace classification in core web and command-log paths, related tests, configuration metadata, and tracing documentation.
The default preserves the existing `causeway.execution.mode` telemetry contract, so no consuming application configuration change is required.
