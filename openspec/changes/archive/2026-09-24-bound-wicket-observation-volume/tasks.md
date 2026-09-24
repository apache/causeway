## 1. Configuration and Request Coordinator

- [x] 1.1 Add Wicket observation configuration for `NONE`, `PAGE`, `REGIONS`, `ROWS`, and `MEMBERS` detail with compatibility default `MEMBERS`.
- [x] 1.2 Add `max-spans-per-request` configuration with `0` as the unlimited compatibility default and reject invalid negative values.
- [x] 1.3 Implement request-cycle-local coordinator state for admitted span count, structural reserve, active semantic ancestors, suppression counters, and collection aggregate contexts.
- [x] 1.4 Classify every existing Wicket observation descriptor by minimum detail level and remove collection initialization from the descriptor taxonomy.
- [x] 1.5 Implement detail filtering before budget admission and count only actually started Causeway Wicket observations.
- [x] 1.6 Implement the finite-budget structural reserve and guarantee that no request exceeds its positive configured maximum.
- [x] 1.7 Track suppression by detail and budget using fixed-key counters and finalize them on the nearest active structural Wicket observation.
- [x] 1.8 Ensure request completion and failure cleanup clears counters, active ancestors, scopes, and aggregate state without serializing coordinator objects.

## 2. Observation Admission and Hierarchy

- [x] 2.1 Route page preparation, page rendering, and action-prompt creation through the coordinator as `PAGE` detail.
- [x] 2.2 Route fieldset, collection preparation and rendering, and table/header/body/footer creation through the coordinator as `REGIONS` detail.
- [x] 2.3 Route visible-row preparation and rendering through the coordinator as `ROWS` detail.
- [x] 2.4 Route logical property and eligible action rendering through the coordinator as `MEMBERS` detail.
- [x] 2.5 Reparent admitted observations whose immediate semantic parent is suppressed to the nearest active emitted Wicket ancestor or naturally current non-Wicket context.
- [x] 2.6 Apply identical admission, budget, parentage, and cleanup semantics to full-page and Ajax responses, including Ajax responses without a page-render span.
- [x] 2.7 Preserve existing failure recording and closure behavior for every admitted observation and create no scope for suppressed candidates.

## 3. Remove Collection Initialization

- [x] 3.1 Remove the `causeway.wicket.collection.initialize` descriptor, contextual-name construction, and synchronous wrapper around parented collection UI construction.
- [x] 3.2 Preserve collection panel construction and model setup unchanged so construction-time JDBC work inherits page preparation or the nearest natural context.
- [x] 3.3 Remove initialization-specific fixture expectations, tests, documentation, and Java-agent compatibility assertions without weakening collection-preparation coverage.

## 4. Aggregate Collection Diagnostics

- [x] 4.1 Add request-local collection preparation and render accumulators using an injectable monotonic ticker and primitive count, total, and maximum values.
- [x] 4.2 Record every applicable visible-row preparation callback in the current emitted collection-preparation aggregate whether or not its row observation is admitted.
- [x] 4.3 Record every applicable visible-row render callback and logical-cell callback in the current emitted collection-render aggregate whether or not child observations are admitted.
- [x] 4.4 Finalize fixed-key preparation aggregates on `causeway.wicket.collection.prepare` and render aggregates on `causeway.wicket.collection.render` before each observation stops.
- [x] 4.5 Report suppressed row/member child count on the applicable emitted collection observation and create no synthetic aggregate carrier when the collection observation is absent.
- [x] 4.6 Close timers and discard temporary aggregate state on callback failure, component detach, request failure, and observation inactivity while preserving original behavior.

## 5. Focused Tests

- [x] 5.1 Test configuration binding, default compatibility behavior, all detail-level mappings, invalid budgets, and startup-bound policy stability.
- [x] 5.2 Test unlimited and finite budgets, exact hard-limit enforcement, structural reserve behavior, and exclusion of core and Java-agent spans from counting.
- [x] 5.3 Test deterministic nearest-active-ancestor parentage for every detail level and for budget-suppressed immediate parents.
- [x] 5.4 Test full-page and Ajax request isolation, sequential request reset, failure cleanup, component detach, and serialization safety.
- [x] 5.5 Test that collection initialization observations are absent while collection construction, failure propagation, page-preparation ownership, and automatic JDBC parentage remain unchanged.
- [x] 5.6 Test row preparation counts and total/maximum durations with zero, one, multiple, failed, and detail-suppressed row callbacks.
- [x] 5.7 Test row rendering and logical-cell counts and total/maximum durations with admitted, detail-suppressed, and budget-suppressed child observations.
- [x] 5.8 Test bounded suppression and aggregate metadata privacy, including absence of row indexes, object identities, bookmarks, values, labels, users, tenants, filters, and component paths.
- [x] 5.9 Extend pagination, item-reuse, standalone-table, no-op registry, and inactive-profile tests to cover the coordinator and aggregate paths.

## 6. Compatibility and Operations Evidence

- [x] 6.1 Extend the Java-agent compatibility fixture to exercise full detail, reduced detail, finite-budget truncation, collection aggregates, and removal of collection initialization.
- [x] 6.2 Assert that HTTP and JDBC spans remain Java-agent-owned, omitted semantic parents do not create duplicate spans, and the configured Wicket maximum is never exceeded.
- [x] 6.3 Capture representative full-page, Ajax, and collection-heavy span counts for each detail level and for a finite budget.
- [x] 6.4 Update the tracing operations guide with configuration examples, category mapping, compatibility defaults, budget semantics, suppression metadata, aggregate interpretation, and collection-initialization migration guidance.
- [x] 6.5 Record the supplied low-value collection-initialization evidence and post-change hierarchy and volume evidence in `validation.md`.
- [x] 6.6 Run focused Wicket and Java-agent compatibility suites, run `git diff --check`, validate the OpenSpec change strictly, and record commands and results in `validation.md`.
