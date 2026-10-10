## Context

Roadmap step 6 combines CAUSEWAY-4062 collection detail (`8e5eaab6145`, `036a28a23c2`) with CAUSEWAY-4067 admission and summaries (`5a1dc2dd17b`). Baseline is `aa9c7e80db7`, after Wicket region archival. Main has immutable configuration records, DomainObjectPage, ParentedCollectionPanel and a relocated `components/table/CausewayAjaxDataTable` using DataRow. Maintenance code is a behavior reference, not a patch to replay.

## Goals / Non-Goals

Expose actual collection preparation, table phases, visible rows and logical property/action cells; provide predictable Wicket volume and useful summaries; preserve UI work, static identity, scope safety and both tracing owners.

Do not add applib APIs, SDKs, exporters, entry-span nominations/renaming, other viewer instrumentation, browser timing or general duration filtering. Admission cannot suppress enclosing Wicket request-cycle, JDBC, HTTP, transaction, JPA or domain-execution spans.

## Decisions

### Startup-bound detail and request budget

Add an observation record under Wicket configuration, with `detail` and `max-spans-per-request`. Preserve maintenance defaults: `MEMBERS`, `0` (unlimited). Reject negative budgets and invalid/null detail at binding/startup; use main record accessors and generated metadata.

| Detail | Eligible observations, cumulative |
|---|---|
| NONE | None of the Causeway semantic Wicket observations |
| PAGE | Page preparation/rendering and action prompts |
| REGIONS | Fieldsets, collections and table/header/body/footer phases |
| ROWS | Participating row preparation/rendering |
| MEMBERS | Logical properties and eligible object-form/table actions |

A request-owned coordinator admits existing and new Wicket candidates together. A positive budget counts starts cumulatively across preparation, rendering, all collections and Ajax subtrees, with no refund on closure. Reserve `min(16, max(1, ceil(budget / 10)))` places for PAGE/REGIONS candidates using overflow-safe arithmetic. ROWS/MEMBERS cannot consume the reserve; structural observations may, while the absolute budget remains hard. Small budgets cannot guarantee every structural candidate survives.

Detail rejection precedes budget rejection. Suppressed candidates execute normally without creating an observation or opening a scope. Emitted descendants use the nearest active emitted ancestor/current framework context. Logical aggregation state can exist without a telemetry scope; summaries export only when an eligible carrier was admitted.

### Observe real lifecycle work

Parented collection before-render preparation encloses table configuration and provider work already performed by Wicket. Row-population callbacks receive row preparation children; fetches before row population remain collection work. Initial UI construction stays under page preparation/current ancestry, without a collection-initialize span. Preparation closes before markup.

Add fixed operations `causeway.wicket.collection.prepare`, `causeway.wicket.collection.row.prepare`, `causeway.wicket.collection.table.render`, `causeway.wicket.collection.table.header.render`, `causeway.wicket.collection.table.body.render`, `causeway.wicket.collection.table.footer.render` and `causeway.wicket.collection.row.render`. Row rendering belongs under table body. Logical property/action callbacks reuse existing member operations. Header/navigation/count/toggle/presentation widgets are not member spans; table markup outside phase scopes remains table self-time.

Instrument actual participating paginated rows, never enumerate off-page elements, force model resolution, fetch or count for telemetry. Preserve sorting, row reuse and Ajax lifecycle. Standalone result/parameter tables without canonical parented collection identity remain excluded.

### Static identity and numeric summaries

Retain main's full canonical collection/member identifiers and case-preserving 50-character displays. Row contexts identify element logical type and collection, never row index, bookmark, instance title, value or generated component path. Table action identity includes its domain-facing parameter signature without changing physical invocation identity.

Collection preparation/render carriers report `causeway.wicket.collection.row.count`, `causeway.wicket.collection.row.duration.total.nanos` and `causeway.wicket.collection.row.duration.max.nanos`. Render carriers additionally report `causeway.wicket.collection.logical-cell.count` and `causeway.wicket.collection.suppressed.children`. These count participating callbacks for their respective phase, not distinct entities, total collection size or physical columns. Omitted row/member observations still contribute. Failed callbacks contribute once; use monotonic time and nonnegative, overflow-safe aggregates.

Report positive `causeway.wicket.suppressed.detail` and `causeway.wicket.suppressed.budget` counts on the nearest admitted page/region carrier. Count each candidate once under its first rejection reason. No carrier means no synthetic summary span.

### Preserve cleanup and tracing ownership

Extend the existing preparation/render tracker with shared admission and aggregation; preserve its reverse-order, attempt-all, idempotent cleanup and original-failure semantics. Do not replace it with maintenance cleanup that stops on the first closure failure. Clear counters, aggregation frames and active scopes before enclosing interaction/request scopes close. Serializable components retain static descriptors only; live request state stays transient/request-owned.

Reuse CausewayObservationIntegration in Boot and agent modes. Exclude maintenance semantic trace nominations, which belong to roadmap step 7. Profile-off/no-op integration must preserve rendering and introduce no alternate registry or exporter.

## Risks / Trade-offs

- Default MEMBERS/unlimited adds detail and can increase volume → document reduced detail and finite budgets, and prove hard limits.
- Main table lifecycle differs → characterize its actual DataRow/provider/toolbars and test a real table, including Ajax and pagination.
- Aggressive budgets omit structural parents → prove nearest surviving ancestry; summaries do not manufacture spans.
- Instrumentation can change lazy loading or mask errors → assert provider calls unchanged and test callback/cleanup failures and serialization.

## Validation and documentation

Cover configuration defaults/invalid input, each detail level, reserve boundaries, cumulative budgets without refunds, reset, no-op integration, summary arithmetic and failure cleanup. Exercise main's actual table/property/action callbacks in full-page and Ajax tests; export traces through production Boot and agent fixtures, checking categories, static attributes, limits and ancestry. Include focused Wicket/config tests and relevant tracing regressions; record actual evidence and limitations in validation.md.

Integrate direct descriptions into the existing M3 how-to and observability guide. Preserve Boot-first then agent-managed flow and existing launcher; show environment overrides `CAUSEWAY_VIEWER_WICKET_OBSERVATION_DETAIL` and `CAUSEWAY_VIEWER_WICKET_OBSERVATION_MAX_SPANS_PER_REQUEST`, not scenario flags. Explain summaries, Wicket-only budget scope and how to inspect representative collection traces.
