# Wicket Render Observability Roadmap

## Objective

Expose enough server-side Wicket rendering structure to explain slow entity pages while keeping observations semantically stable, privacy-safe, operationally bounded, and compatible with the existing Java-agent-owned telemetry architecture.

The roadmap deliberately starts with actual rendering because it has clean contiguous boundaries and natural component-tree nesting.
Later proposals are evidence-gated so Causeway does not add lifecycle complexity or telemetry volume without a demonstrated diagnostic gap.

## Intended Evolution

```text
Existing trace
HTTP
└── causeway.root.interaction
    └── largely opaque Wicket page work

Proposal 1
HTTP
└── causeway.root.interaction
    └── causeway.wicket.page.render
        ├── fieldset.render
        │   ├── property.render
        │   └── action.render
        └── collection.render

Proposal 2, if page time remains unattributed
HTTP
└── causeway.root.interaction
    ├── property.prepare / action.evaluate / collection.prepare
    └── causeway.wicket.page.render and nested render spans

Proposal 3, if a collection remains broad
collection.render
├── collection.load
│   └── JDBC
├── collection.page
└── collection.presentation

Proposals 4 and 5, if volume or row scaling requires control
configuration and request budget
└── bounded aggregate row diagnostics
```

## Phase 1: Establish Semantic Render Regions

**Active OpenSpec change:** `add-wicket-region-render-spans`

Add page, fieldset, regular entity-property, entity-collection, and relevant action-button render observations.
Use fixed names, safe static identifiers, natural Wicket rendering parentage, Ajax support, and request-level failure cleanup.
Exclude preparation, table cells, row actions, action parameters, and browser work.

**Exit gate:** Representative full-page and Ajax traces show correctly nested logical regions, no scope leaks, and no per-cell span amplification.

## Phase 2: Explain Pre-Render Work When Necessary

**Planned change:** `add-wicket-region-preparation-spans`

Use Phase 1 traces to measure time that remains directly under the root or page boundary.
If preparation is material, add separate contiguous observations for fieldset, property, and collection preparation and for action visibility or usability evaluation.
Do not stretch render observations across Wicket lifecycle phases.

**Entry gate:** Material unexplained duration is associated with initialization, configuration, or consent evaluation.

**Exit gate:** Traces clearly distinguish preparation from actual rendering without duplicate or overlapping duration semantics.

## Phase 3: Decompose Slow Collections

**Planned change:** `add-wicket-collection-render-detail-spans`

If collection regions dominate, separate loading, paging or materialization, and presentation work.
Retain automatic JDBC spans as children where applicable and continue to exclude individual rows and cells.

**Entry gate:** A collection region is slow and its existing child spans do not explain the duration.

**Exit gate:** Operators can tell whether the bottleneck is data access, model processing, or presentation generation.

## Phase 4: Bound Production Detail

**Planned change:** `configure-wicket-observation-detail`

Use measured span counts to decide whether page, region, and member detail levels or a per-request observation budget are needed.
Keep these controls independent from Java-agent and collector sampling.

**Entry gate:** Representative measurements or production rollout requirements show that one fixed detail level is insufficient.

**Exit gate:** An application can select diagnostic detail and enforce a hard bound on Causeway Wicket observations per request.

## Phase 5: Diagnose Row Scaling Without Span Explosion

**Planned change:** `add-wicket-slow-row-diagnostics`

Add aggregate-first row and cell timing only if collection presentation remains slow after loading has been separated.
Permit any individual slow-row observations only as explicit bounded diagnostics, without domain identifiers.

**Entry gate:** Collection-detail traces demonstrate presentation cost that scales with rows rather than query latency.

**Exit gate:** Row-scaling cost is explained with bounded overhead and no disclosure of row identity or values.

## Longer-Term Horizon

Browser-side OpenTelemetry or Real User Monitoring could eventually correlate server generation with network transfer, JavaScript initialization, DOM work, layout, and paint.
That would introduce propagation, browser security, deployment, and telemetry-ownership questions and is not part of Proposals 1 through 5.

## Decision Principles

- Add a new observation only for a contiguous operation with a clear semantic meaning.
- Prefer fixed observation names and static layout or metamodel identifiers.
- Never record domain values or object-instance, user, tenant, or row identity.
- Use trace evidence from the preceding phase as the entry gate for the next phase.
- Prefer aggregate diagnostics over per-item spans.
- Keep the OpenTelemetry Java agent responsible for SDK ownership, automatic instrumentation, sampling, and export.
- Preserve no-op behavior when observation is inactive.
