# Add Wicket Slow Row Diagnostics

**Roadmap position:** Proposal 5, only after collection-detail spans demonstrate unexplained row-scaling cost.

## Why

A collection may spend most of its time converting and rendering rows even when its query is fast.
Unconditional per-row and per-cell spans would create excessive telemetry, so row diagnostics need an aggregate-first and explicitly bounded design.

## Proposed Changes

- Add aggregate diagnostics for collection row and cell processing beneath the collection presentation observation.
- Record bounded counts and aggregate duration statistics that explain scaling without identifying individual rows.
- Consider opt-in slow-row observations only when a row exceeds a configured diagnostic threshold and the request remains within its observation budget.
- Ensure any slow-row observation uses an ordinal or anonymous sequence rather than a domain identifier.
- Integrate with observation-detail and per-request budget controls from Proposal 4.
- Document the intended use as temporary high-detail diagnosis rather than routine business telemetry.

Potential aggregate metadata includes:

```text
causeway.wicket.collection.rowCount
causeway.wicket.collection.cellCount
causeway.wicket.collection.rows.totalDuration
causeway.wicket.collection.rows.maxDuration
```

## Non-Goals

- Do not emit one observation for every row or cell by default.
- Do not record row values, entity identifiers, bookmarks, table filter values, or user-visible labels.
- Do not use aggregate telemetry as an application analytics or auditing mechanism.
- Do not duplicate JDBC query spans or collection-loading observations.

## Prerequisites and Evidence Gate

Collection-detail traces must show that loading is acceptably fast while collection presentation time grows materially with row count.
Observation-detail controls and a request budget should be available before any opt-in slow-row spans are considered.

## Design Questions

- Can aggregate timing be collected with negligible overhead when tracing is inactive or unsampled?
- Should slow-row thresholds be absolute durations, relative outliers within the page, or collector-side policy?
- Can custom table and icon presentations expose row boundaries consistently?
- Are duration aggregates better represented as span attributes, span events, or Micrometer metrics in this maintenance branch?

## Expected Exit Gate

A representative large collection explains row-scaling cost with bounded aggregate telemetry, and optional slow-row diagnosis cannot expose domain identity or exceed the configured request budget.
