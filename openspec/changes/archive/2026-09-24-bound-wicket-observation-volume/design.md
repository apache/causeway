## Context

Causeway's Wicket viewer currently creates semantic observations directly from preparation helpers, render behaviors, collection panels, and table row callbacks.
The resulting hierarchy is useful at full detail, but admission decisions are distributed and there is no request-level state that can enforce a total Causeway Wicket span bound.
Java-agent or collector sampling controls whether a trace is retained; it does not constrain the number of framework spans inside a retained request.

The delivered hierarchy already exposes page preparation and rendering, fieldsets, logical collections, table phases, visible-row preparation and rendering, logical property cells, row actions, and action prompts.
Representative traces show that `initialize collection <collectionId>` adds many sibling spans whose durations are generally hundreds of microseconds to a few milliseconds and whose work is already contained by page preparation.
The same traces demonstrate that row and cell spans can scale with visible collection size, creating a need for both admission controls and aggregate evidence when detail is reduced.

Collection aggregates and observation suppression cannot be designed independently.
Aggregates must know whether lower-level callbacks occurred without exported child spans, while the budget coordinator must expose deterministic suppression and a safe place for truncation metadata during both full-page and Ajax requests.

## Goals / Non-Goals

**Goals:**

- Give operators configurable semantic detail without changing Java-agent or collector sampling.
- Enforce a hard positive per-request maximum for Causeway Wicket observations.
- Preserve current member-level behavior when the new controls retain their compatibility defaults.
- Prefer page, collection, table, and other structural observations over row and member detail when a finite budget is exhausted.
- Preserve deterministic hierarchy when an immediate semantic parent is not emitted.
- Explain visible-row preparation and rendering growth through bounded collection-level counts, totals, and maxima.
- Remove low-value collection-initialization observations without changing collection construction or loading.
- Support full-page and Ajax requests using non-serialized request-local state.
- Preserve privacy, failure cleanup, inactive-profile behavior, and Java-agent ownership.

**Non-Goals:**

- Do not implement head sampling, tail sampling, duration sampling, SDK setup, exporting, or automatic JDBC/HTTP instrumentation.
- Do not guarantee that every structural observation is emitted after the configured hard maximum is reached.
- Do not add fieldset, property, collection, or action preparation spans.
- Do not add another per-row, per-cell, slow-row, or summary observation.
- Do not measure browser, JavaScript, DOM, layout, or paint work.
- Do not record row identity, object values, bookmarks, labels, users, tenants, filters, or generated component paths.
- Do not use row aggregates as application analytics or auditing.

## Decisions

### Use one request-scoped observation coordinator

A Wicket observation coordinator will own detail eligibility, budget admission, active semantic ancestry, suppression counters, and collection aggregate contexts for one request.
The coordinator will be stored in request-cycle metadata or an equivalent request-local facility and removed when the request completes.
It will not be stored in Wicket component fields, page state, or serialized descriptors.

Keeping detail and budget checks in individual components was rejected because independently suppressed parents would produce inconsistent hierarchy and no single component could enforce a request-wide maximum.
A general core observation budget was rejected because the requirement is specific to Causeway Wicket spans and must not count core semantic or Java-agent spans.

### Preserve compatibility through explicit defaults

The Wicket configuration model will add an observation detail enum with values `NONE`, `PAGE`, `REGIONS`, `ROWS`, and `MEMBERS`.
The default will be `MEMBERS`, preserving all existing eligible Wicket observations except the deliberately removed collection-initialization category.

The configuration will add `max-spans-per-request`, where `0` means unlimited and a positive value establishes the hard maximum.
The default will be `0` so existing deployments do not silently lose detail.
Configuration is read at startup and remains immutable during a request.

A finite default was rejected for this maintenance branch because it would silently change existing traces without representative application-specific span-count evidence.
Runtime mutation was rejected because it complicates deterministic request behavior and configuration support.

### Map existing descriptors to semantic detail levels

Descriptor categories will map as follows:

| Minimum level | Observation categories |
|---|---|
| `PAGE` | page preparation, page rendering, and action prompt |
| `REGIONS` | page categories plus fieldset, collection preparation and rendering, and collection table/header/body/footer phases |
| `ROWS` | region categories plus visible-row preparation and rendering |
| `MEMBERS` | row categories plus logical properties and eligible actions |

`NONE` suppresses all Causeway Wicket observations but does not affect interaction, action, property-access, collection-access, HTTP, or JDBC spans.
Collection initialization is removed from the descriptor taxonomy rather than assigned to a detail level.

Table phases are treated as structural regions because they explain aggregate table mechanics and provide stable parents for rows.
Action prompts remain page detail because they are top-level semantic rendering operations rather than ordinary member buttons.

### Enforce a hard budget while reserving structural capacity

Only an observation that is actually started increments the Causeway Wicket request count.
No automatic Java-agent span or non-Wicket Causeway observation participates in this count.

For a positive maximum, the coordinator will maintain an internal structural reserve equal to ten percent of the maximum, with a minimum of one and a maximum of sixteen.
Row and member observations are denied once admitting them would consume that reserve.
Page and region observations can use the reserve but are denied when the absolute maximum is reached.
This provides deterministic structural priority without exceeding the hard limit or requiring another operator setting.

An unlimited budget performs no count-based suppression.
Detail filtering occurs before budget admission and does not consume budget.
A first-come-only budget was rejected because rows or cells could consume all capacity before later table-footer or sibling collection structure appears.
Exempting structural spans from the maximum was rejected because it would violate the hard-bound requirement.

### Reparent permitted descendants to the nearest active permitted ancestor

When detail or budget suppresses an immediate semantic parent, an otherwise permitted descendant joins the nearest active emitted Wicket ancestor or the naturally current non-Wicket context.
The coordinator will never retain a scope for a suppressed observation and will never fabricate an observation solely to preserve shape.

For example, Ajax rendering of a row without an emitted page or collection observation can attach the row to the current request or interaction context if its detail level and budget permit it.
When the detail level excludes rows, member descendants are also excluded by the monotonic level mapping, avoiding property or action spans without their row category.

### Report suppression with bounded fixed-key metadata

The nearest active structural Wicket observation will receive fixed-key counters for observations suppressed by detail and by budget.
If no Wicket observation is active, the coordinator records counters only for internal accounting and exports no synthetic summary span.
Collection spans may also carry a fixed-key count of their suppressed row or member descendants.

The exported keys will be fixed and numeric, for example:

[source,text]
----
causeway.wicket.observation.suppressed.detail.count
causeway.wicket.observation.suppressed.budget.count
causeway.wicket.collection.child_observation.suppressed.count
----

Suppressed counts do not identify descriptor names, members, rows, or values.
Events were rejected because fixed numeric attributes finalized before the enclosing span stops are simpler to query and add no event-volume concern.

### Aggregate row work at emitted collection boundaries

Existing row preparation and render callbacks will record monotonic start time and elapsed duration in the current collection aggregate context regardless of whether the corresponding row observation is admitted.
Logical cell callbacks will increment a count without recording identity.
No additional observation is created.

Preparation aggregates will be finalized on `causeway.wicket.collection.prepare` and render aggregates on `causeway.wicket.collection.render`.
Candidate fixed-key attributes are:

[source,text]
----
causeway.wicket.collection.row.prepare.count
causeway.wicket.collection.row.prepare.duration.total.ns
causeway.wicket.collection.row.prepare.duration.max.ns
causeway.wicket.collection.row.render.count
causeway.wicket.collection.row.render.duration.total.ns
causeway.wicket.collection.row.render.duration.max.ns
causeway.wicket.collection.logical_cell.render.count
causeway.wicket.collection.child_observation.suppressed.count
----

Durations use `System.nanoTime()` or an injectable monotonic ticker and are exported as integer nanoseconds.
Counts represent callbacks completed or attempted according to the lifecycle-safe boundary documented by tests; they never represent total domain collection size or application analytics.

Aggregates are exported only when their enclosing collection observation is emitted.
At `PAGE` or `NONE`, no collection span exists and no synthetic aggregate carrier is created.
If a collection observation itself is denied by budget, its internal measurements are discarded after cleanup.

Aggregating from exported child span durations was rejected because children may be suppressed, unsampled, or unavailable to framework code.
Adding threshold-triggered slow-row spans was rejected because regular row spans already exist at higher detail and aggregates provide bounded evidence at lower detail.

### Remove collection initialization instead of suppressing it by default

`WicketRenderObservationDescriptor.Region.COLLECTION_INITIALIZATION` and the synchronous wrapper around collection panel UI construction will be removed.
Collection construction and model setup continue unchanged.
Any JDBC work during that construction naturally inherits page preparation when active, otherwise the current interaction or request context.

Retaining the descriptor behind a new detail level was rejected because the supplied trace shows broad routine amplification with little diagnostic value, and a hidden compatibility mode would preserve code and tests for a category no longer intended as part of the semantic model.

### Preserve lifecycle safety and serialization boundaries

Admission returns either a started observation handle or an explicit suppressed handle with no scope.
Every started preparation or render observation closes on normal completion, failure, detach, or request cleanup according to its existing lifecycle.
Failure is recorded only on the observation whose guarded operation fails.
Aggregate timers close in `finally` paths so failures do not leak request state.

Only static serializable descriptors remain on Wicket components.
The coordinator, active observations, scopes, counters, and aggregate accumulators remain request-local and non-serializable.

## Risks / Trade-offs

- **[Risk] A finite budget can omit a diagnostically important late structural region.** → Reserve bounded structural capacity and expose budget-suppression counts, while documenting that the hard maximum takes precedence.
- **[Risk] Reparenting can make a reduced-detail hierarchy differ from full detail.** → Always attach to the nearest active permitted ancestor and test every level for full-page and Ajax requests.
- **[Risk] Timing callbacks without child spans adds overhead.** → Use monotonic primitive accumulation only while an emitted collection aggregate context is active and verify inactive/no-op overhead paths.
- **[Risk] Numeric attributes could be interpreted as business analytics.** → Define them strictly as current-response framework callback diagnostics and exclude collection size, row identity, or domain values.
- **[Risk] Removing collection initialization loses a narrow JDBC attribution boundary.** → Retain page-preparation parentage and rely on the supplied evidence that routine initialization spans add little value; collection preparation continues to identify later data-provider work.
- **[Risk] Request cleanup defects could leak counts across Ajax or pooled threads.** → Bind state to the Wicket request cycle, clear it on completion, and add sequential-request, failure, and serialization tests.
- **[Risk] Existing dashboards may query `causeway.wicket.collection.initialize`.** → Document the telemetry removal explicitly and provide migration guidance to use page preparation and collection preparation.

## Migration Plan

Deployments that do not configure the new properties retain `MEMBERS` detail and an unlimited Wicket span budget.
Dashboards and saved queries using `causeway.wicket.collection.initialize` must migrate to `causeway.wicket.page.prepare` for construction-time work or `causeway.wicket.collection.prepare` for later collection data-provider work.

Operators can first set a finite maximum while retaining `MEMBERS`, inspect suppression counters and aggregates, and then reduce detail to `ROWS`, `REGIONS`, `PAGE`, or `NONE` as operational needs require.
Rollback restores the previous framework version and collection-initialization span category; no application data or persistent Wicket page migration is required because coordinator state is request-local.

## Open Questions

None.
