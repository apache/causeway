## Why

Main already implements CAUSEWAY-3975, but its observation lifecycle and telemetry wiring differ from the subsequently hardened maintenance implementation. Before forward-porting CAUSEWAY-4058 through CAUSEWAY-4068, main needs a tested foundation that preserves its existing instrumentation and connects framework spans to the intended telemetry pipeline.

## What Changes

- Harden main's observation closure for repeated cleanup, partial initialization, and `Throwable` recording while retaining its tagging and discard support.
- Preserve main's Boot-managed integration as the default architecture; establish explicit activation and registry selection behavior, including application-provided registries.
- Adapt the maintenance real-agent fixture to main to verify framework/HTTP/JDBC parentage, inactive behavior, safe operation without an agent, and absence of duplicate framework exports. Document the supported telemetry ownership configuration and any required exclusions.
- Characterize and correct threshold-wrapper behavior where fluent composition bypasses its lifecycle; preserve failures rather than discarding them solely for short duration.
- Document the existing observation inventory and a metadata-policy follow-up so subsequent ports do not erase main-only instrumentation.

## Capabilities

### New Capabilities

- `main-observation-foundation`: Lifecycle, opt-in activation, telemetry ownership, threshold behavior, and compatibility evidence required for forward ports. This formalizes existing implementation contracts as well as strengthening them; no corresponding main spec was found in the inspected snapshot.

### Modified Capabilities

None.

## Impact

Implementation targets **main**, not the maintenance working tree in which these planning artifacts were authored. Expected areas are `commons` observation utilities, `core/config` observation integration, their runtime/JPA consumers, and a main-compatible tracing regression fixture. Retain main's BOM-managed dependency versions; do not copy Boot 2.7 pins, Java 11 fixture assumptions, or maintenance's interaction implementation.

No public applib API change is intended. Error-span retention can increase exported short-failure spans. Registry wiring changes require application-registry compatibility tests. The roadmap and source references are in [the forward-port plan](../../../planned-changes/otel-forward-port-roadmap.md).

## Non-goals

No Wicket detail, semantic renaming, classification, correlation, priming, application span API, metamodel fix, or wholesale telemetry ownership migration. JPA naming and identity-data policy changes are a separate follow-up. This proposal does not authorize implementing on maintenance or deploying telemetry infrastructure.
