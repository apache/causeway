## Why

CAUSEWAY-4096 is roadmap step 1b, following the merged CAUSEWAY-3975 foundation. Main still puts bookmarks and query descriptions into JPA observation names, emits user identity automatically, and applies a hard-coded duration threshold that can remove parents of exported child spans; these policies need settling before adding more instrumentation.

## What Changes

- Define stable operation names separately from contextual display names and domain metadata; apply the policy to existing dynamic JPA, action/property and execution-publishing names.
- Exclude object identifiers, query arguments and arbitrary query descriptions from Causeway-generated names and attributes. Allow bounded type/member/named-query metadata.
- **BREAKING (telemetry):** make username and tenancy-token emission independent opt-ins, disabled by default.
- **BREAKING (telemetry):** disable Causeway duration filtering by default, retaining complete framework ancestry in the supported test configurations. Keep the existing opt-in switch and make the JPA threshold configurable, defaulting to 2 ms when filtering is enabled.
- **BREAKING (telemetry):** replace affected dynamic operation names; document migration for span searches and observation metrics without emitting duplicate observations.
- Extend focused tests and the existing Boot/agent export fixture to verify metadata and ancestry. Preserve lifecycle, activation, registry ownership and domain behavior.

## Capabilities

### New Capabilities

- `observation-policy`: Stable naming, controlled identity metadata, explicit duration configuration and migration guidance for existing observations.

### Modified Capabilities

- `main-observation-foundation`: Duration suppression becomes explicitly enabled behavior; default retention and the existing agent limitation are made explicit.

## Impact

Core configuration and configuration metadata, runtime interaction tagging, member execution and execution publishing, JPA instrumentation, the tracing compatibility fixture and the observability guide. No new SDK, exporter or production telemetry dependency is planned. Existing dashboards and metrics using changed names/default tags require migration; retained short spans can increase export volume.

This does not forward port interaction classification/correlation, mixin semantics, Wicket instrumentation, trace-root renaming, application spans, collection budgets or primer observations. Those remain later roadmap entries.
