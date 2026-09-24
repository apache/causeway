## 1. Configuration and Request Coordinator

- [ ] 1.1 Add Wicket observation configuration for `NONE`, `PAGE`, `REGIONS`, `ROWS`, and `MEMBERS` detail with compatibility default `MEMBERS`.
- [ ] 1.2 Add `max-spans-per-request` configuration with `0` as the unlimited compatibility default and reject invalid negative values.
- [ ] 1.3 Implement request-cycle-local coordinator state for admitted span count, structural reserve, active semantic ancestors, suppression counters, and collection aggregate contexts.
- [ ] 1.4 Classify every existing Wicket observation descriptor by minimum detail level and remove collection initialization from the descriptor taxonomy.
- [ ] 1.5 Implement detail filtering before budget admission and count only actually started Causeway Wicket observations.
- [ ] 1.6 Implement the finite-budget structural reserve and guarantee that no request exceeds its positive configured maximum.
- [ ] 1.7 Track suppression by detail and budget using fixed-key counters and finalize them on the nearest active structural Wicket observation.
- [ ] 1.8 Ensure request completion and failure cleanup clears counters, active ancestors, scopes, and aggregate state without serializing coordinator objects.

## 2. Observation Admission and Hierarchy

- [ ] 2.1 Route page preparation, page rendering, and action-prompt creation through the coordinator as `PAGE` detail.
- [ ] 2.2 Route fieldset, collection preparation and rendering, and table/header/body/footer creation through the coordinator as `REGIONS` detail.
- [ ] 2.3 Route visible-row preparation and rendering through the coordinator as `ROWS` detail.
- [ ] 2.4 Route logical property and eligible action rendering through the coordinator as `MEMBERS` detail.
- [ ] 2.5 Reparent admitted observations whose immediate semantic parent is suppressed to the nearest active emitted Wicket ancestor or naturally current non-Wicket context.
- [ ] 2.6 Apply identical admission, budget, parentage, and cleanup semantics to full-page and Ajax responses, including Ajax responses without a page-render span.
- [ ] 2.7 Preserve existing failure recording and closure behavior for every admitted observation and create no scope for suppressed candidates.

## 3. Remove Collection Initialization

- [ ] 3.1 Remove the `causeway.wicket.collection.initialize` descriptor, contextual-name construction, and synchronous wrapper around parented collection UI construction.
- [ ] 3.2 Preserve collection panel construction and model setup unchanged so construction-time JDBC work inherits page preparation or the nearest natural context.
- [ ] 3.3 Remove initialization-specific fixture expectations, tests, documentation, and Java-agent compatibility assertions without weakening collection-preparation coverage.

## 4. Aggregate Collection Diagnostics

- [ ] 4.1 Add request-local collection preparation and render accumulators using an injectable monotonic ticker and primitive count, total, and maximum values.
- [ ] 4.2 Record every applicable visible-row preparation callback in the current emitted collection-preparation aggregate whether or not its row observation is admitted.
- [ ] 4.3 Record every applicable visible-row render callback and logical-cell callback in the current emitted collection-render aggregate whether or not child observations are admitted.
- [ ] 4.4 Finalize fixed-key preparation aggregates on `causeway.wicket.collection.prepare` and render aggregates on `causeway.wicket.collection.render` before each observation stops.
- [ ] 4.5 Report suppressed row/member child count on the applicable emitted collection observation and create no synthetic aggregate carrier when the collection observation is absent.
- [ ] 4.6 Close timers and discard temporary aggregate state on callback failure, component detach, request failure, and observation inactivity while preserving original behavior.

## 5. Focused Tests

- [ ] 5.1 Test configuration binding, default compatibility behavior, all detail-level mappings, invalid budgets, and startup-bound policy stability.
- [ ] 5.2 Test unlimited and finite budgets, exact hard-limit enforcement, structural reserve behavior, and exclusion of core and Java-agent spans from counting.
- [ ] 5.3 Test deterministic nearest-active-ancestor parentage for every detail level and for budget-suppressed immediate parents.
- [ ] 5.4 Test full-page and Ajax request isolation, sequential request reset, failure cleanup, component detach, and serialization safety.
- [ ] 5.5 Test that collection initialization observations are absent while collection construction, failure propagation, page-preparation ownership, and automatic JDBC parentage remain unchanged.
- [ ] 5.6 Test row preparation counts and total/maximum durations with zero, one, multiple, failed, and detail-suppressed row callbacks.
- [ ] 5.7 Test row rendering and logical-cell counts and total/maximum durations with admitted, detail-suppressed, and budget-suppressed child observations.
- [ ] 5.8 Test bounded suppression and aggregate metadata privacy, including absence of row indexes, object identities, bookmarks, values, labels, users, tenants, filters, and component paths.
- [ ] 5.9 Extend pagination, item-reuse, standalone-table, no-op registry, and inactive-profile tests to cover the coordinator and aggregate paths.

## 6. Compatibility and Operations Evidence

- [ ] 6.1 Extend the Java-agent compatibility fixture to exercise full detail, reduced detail, finite-budget truncation, collection aggregates, and removal of collection initialization.
- [ ] 6.2 Assert that HTTP and JDBC spans remain Java-agent-owned, omitted semantic parents do not create duplicate spans, and the configured Wicket maximum is never exceeded.
- [ ] 6.3 Capture representative full-page, Ajax, and collection-heavy span counts for each detail level and for a finite budget.
- [ ] 6.4 Update the tracing operations guide with configuration examples, category mapping, compatibility defaults, budget semantics, suppression metadata, aggregate interpretation, and collection-initialization migration guidance.
- [ ] 6.5 Record the supplied low-value collection-initialization evidence and post-change hierarchy and volume evidence in `validation.md`.
- [ ] 6.6 Run focused Wicket and Java-agent compatibility suites, run `git diff --check`, validate the OpenSpec change strictly, and record commands and results in `validation.md`.
