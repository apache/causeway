## Why

Wicket page regions now expose preparation and rendering, but collection traces do not explain table phases or work for participating rows and logical cells. Forward-port roadmap step 6 with detail and volume controls together, so operators can inspect expensive collections while keeping the number of Wicket observations predictable.

## What Changes

- Adapt CAUSEWAY-4062 collection preparation, table/header/body/footer, visible-row preparation/rendering, logical property-cell and row-action observations to main.
- Add startup-bound `causeway.viewer.wicket.observation.detail` (`NONE`, `PAGE`, `REGIONS`, `ROWS`, `MEMBERS`) and `max-spans-per-request` (nonnegative, `0` unlimited). Preserve maintenance defaults of `MEMBERS` and `0`.
- Apply admission to all Causeway Wicket observations, including existing page/member regions. Suppressed candidates open no observation scope and do not suppress their underlying work or non-Wicket observations.
- Keep numeric collection row-duration/cell aggregates and detail/budget suppression diagnostics useful when individual child observations are omitted.
- Preserve request ancestry, failure cleanup, serialization safety, pagination and static identity; add real table/Ajax export evidence.
- Integrate the capability and simple environment overrides into the existing M3 how-to and observability guide, retaining the existing launcher and Boot-first/agent structure.

## Capabilities

### New Capabilities

- `wicket-collection-observations`: Collection/table phases, participating-row and logical member callbacks, detail selection, request budgets and collection aggregate diagnostics.

### Modified Capabilities

- `wicket-region-observations`: Existing Wicket regions participate in shared admission; table members become eligible; guidance includes the new controls while preserving lifecycle and tracing ownership.

## Impact

Main's immutable CausewayConfiguration and binding metadata/tests; Wicket UI/model table integration, preparation/render helpers and request cleanup; tracing fixtures and operator documentation. Source anchors are CAUSEWAY-4062 `8e5eaab6145`, `036a28a23c2` and CAUSEWAY-4067 `5a1dc2dd17b`. No applib service or new tracing SDK/exporter is added.

The budget applies only to Causeway Wicket observations. HTTP, JDBC, domain execution, transaction and JPA observations retain their existing behavior. General duration filtering, semantic entry-span naming, other viewers and browser telemetry remain outside this change.
