## Why

Agent-managed tracing currently requires application-owned bridge code and Boot exclusions. This makes a supported telemetry setup unnecessarily difficult to adopt and maintain.

## What Changes

- Supply the agent observation bridge in Causeway when `observation,agent` profiles are active.
- Automatically exclude competing Boot tracing and HTTP observation configuration for the `agent` profile, preserving existing exclusions and Micrometer metrics.
- Keep the bridge dependency optional and respect application-provided registry/tracer beans.
- Remove sample/fixture bridge copies and update the M3 how-to and user guide to show configuration-only adoption.

## Capabilities

### New Capabilities
- `agent-observation-bridge`: Built-in agent-owned tracing setup with optional dependencies, profile activation and application overrides.

### Modified Capabilities

None. This implements the existing single-owner telemetry guarantees without changing instrumentation boundaries.

## Impact

Core configuration, Petclinic observation profile, exported-trace compatibility fixtures, M3 how-to and observability user guide. No duration filtering or exporter is introduced.
