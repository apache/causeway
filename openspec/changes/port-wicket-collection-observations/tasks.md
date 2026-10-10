## 1. Main lifecycle and configuration

- [ ] 1.1 Characterize main's parented table, DataRow/provider, property/action and full-page/Ajax callbacks with focused tests; identify preparation boundaries without extra provider work.
- [ ] 1.2 Add immutable Wicket observation configuration and metadata for detail and max-spans-per-request; verify defaults, all levels and invalid binding.

## 2. Shared admission and lifecycle safety

- [ ] 2.1 Implement request-owned hierarchical admission and cumulative budget with overflow-safe structural reserve; integrate existing page/region/member helpers without refunding closed observations.
- [ ] 2.2 Preserve attempt-all LIFO cleanup, original failures, no-op behavior and serialization safety; verify counters and aggregation state reset before enclosing scopes close.

## 3. Collection detail and summaries

- [ ] 3.1 Add static collection/table/row descriptors and eligible logical property/action-cell policy, retaining canonical identities, signatures and display compaction without instance data.
- [ ] 3.2 Observe parented collection preparation and actual row population, preserving initial page construction ancestry and provider/pagination behavior.
- [ ] 3.3 Observe table/header/body/footer and row rendering plus logical property/action callbacks; exclude standalone tables and decorative widgets and retain real Ajax ancestry.
- [ ] 3.4 Add row timing/count, logical-cell and suppression summaries, including omitted/failed callbacks, overflow-safe arithmetic and no synthetic carriers.

## 4. Verification and operator guidance

- [ ] 4.1 Verify all detail levels, hard budget/reserve boundaries, multiple collections, no refunds, reset, omitted-parent ancestry, summary correctness, failure cleanup and serialization through actual table callbacks.
- [ ] 4.2 Extend production Boot/agent exported-trace fixtures with main's real table full-page/Ajax lifecycle, reduced detail and finite budgets; verify static attributes, categories, scope closure and unaffected non-Wicket observations.
- [ ] 4.3 Integrate collection detail, summaries and environment overrides into the M3 how-to and observability guide, preserving Boot-first/agent flow and existing launcher without scenario flags or before/after comparisons.
- [ ] 4.4 Run focused configuration/Wicket tests, relevant tracing regressions and documentation rendering; record commands, results and material limitations in validation.md.
