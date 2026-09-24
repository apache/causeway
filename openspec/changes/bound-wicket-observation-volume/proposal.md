## Why

Full-detail Wicket tracing now explains page, collection, table, row, and logical-member work, but complex pages can produce hundreds or thousands of Causeway spans inside one sampled request.
Operators need one coherent control plane that bounds that volume while retaining collection-level scaling evidence, and representative traces show that routine `initialize collection <collectionId>` spans usually contribute only sub-millisecond noise without enough diagnostic value to justify their span cost.

## What Changes

- Add configurable Wicket observation detail levels for disabled, page, region, row, and full member detail.
- Add a configurable hard maximum for Causeway Wicket observations created during one full-page or Ajax request.
- Preserve current full member detail and an unlimited budget as compatibility defaults while allowing production deployments to select lower detail and a finite bound.
- Coordinate admission, suppression, nearest-active-ancestor parentage, structural-span priority, and bounded truncation reporting through one request-scoped Wicket observation policy.
- Add bounded collection-level row and logical-cell counts plus preparation and rendering duration totals and maxima so row-scaling cost remains visible when lower-level observations are disabled or suppressed.
- Keep preparation and rendering aggregates separate and avoid creating any additional per-row or per-cell observations.
- **BREAKING (telemetry):** Remove `causeway.wicket.collection.initialize` and contextual `initialize collection <collectionId>` observations because representative traces show many routine sibling spans taking approximately hundreds of microseconds to a few milliseconds while the enclosing page preparation span already provides adequate ownership.
- Preserve collection construction, model setup, persistent loading, failures, and Java-agent JDBC instrumentation unchanged; work formerly beneath collection initialization inherits the nearest naturally active page-preparation or request context.
- Keep all controls independent of Java-agent HTTP/JDBC instrumentation, context propagation, sampling, and export.
- Retain static structural metadata and exclude row identity, domain values, bookmarks, labels, users, tenants, generated component paths, and application analytics.

## Capabilities

### New Capabilities

- `wicket-observation-volume-control`: Defines Wicket detail levels, per-request span budgets, deterministic suppression and truncation reporting, and bounded collection row aggregates.

### Modified Capabilities

- `wicket-region-render-observation`: Removes collection-initialization observations and makes existing Wicket observations conditional on the configured detail level and remaining per-request budget while preserving lifecycle, hierarchy, metadata, and Java-agent ownership requirements.

## Impact

- Affects Wicket observation descriptors, policies, preparation helpers, render behaviors, entity collection panels, collection tables, row callbacks, request-cycle state, configuration properties, and compatibility fixtures.
- Removes one existing exported span category and updates tests and operations documentation accordingly.
- Adds request-scoped non-serializable accounting while keeping persisted Wicket component state limited to serializable static descriptors.
- Adds no tracing SDK, exporter, sampler, or external dependency.
- Consolidates and supersedes `configure-wicket-observation-detail`, `add-wicket-row-aggregate-diagnostics`, and the remaining Wicket observability roadmap planning documents.
