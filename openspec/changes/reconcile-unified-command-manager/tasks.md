## 1. Manager State and Controls

- [ ] 1.1 Add `HasBaseline` and `HasLimit` contracts, tolerant baseline/limit memento parsing, and focused tests for canonical, timestamp-only, malformed, and non-positive state.
- [ ] 1.2 Add the command-recording-suppressed `CommandManager` view model with logical type, default limit, title, canonical memento, and framework constructor.
- [ ] 1.3 Add safe publishing-disabled previous-hour, next-hour, change-baseline, and change-limit mixins that preserve the manager's other state component, with unit tests.

## 2. Persistence-Neutral Collection Queries

- [ ] 2.1 Extend the command-entry repository contract and reusable implementation with excluded and recorded-or-replayed foreground queries while reusing bounded foreground and pending-or-failed reads.
- [ ] 2.2 Add Jakarta Persistence named-query wiring for `EXCLUDED` and `UNDEFINED`/`EXPORTED`/`OK` reads with established ordering and no schema change.
- [ ] 2.3 Add repository tests for inclusive baselines, replay-state membership, ordering, and bounded sequence reads.

## 3. Unified Review Collections

- [ ] 3.1 Implement `commandsInSequence` with repository-side limiting, `EXCLUDED` removal, P1 general eligibility, and replay-context-preserving wrapping.
- [ ] 3.2 Implement `excluded` and `recordedOrReplayed` with their replay-state sets and P1 general eligibility.
- [ ] 3.3 Implement `pendingOrFailed` using the direct repository result-to-wrapper path so resultless safe imported work bypasses general eligibility.
- [ ] 3.4 Add manager collection tests covering all six Causeway 4 replay states, eligibility, ordering, sequence limiting, focused-collection non-limiting, and absence of mutation.

## 4. Entry Point, Compatibility, and Presentation

- [ ] 4.1 Add the unified prototyping menu action with current-hour baseline and default limit, retain but hide the legacy launch actions, and test menu defaults and visibility.
- [ ] 4.2 Add safe publishing-disabled `openCommandManager` navigation to both legacy managers while preserving their logical types, timestamp-only constructors, collections, and existing actions; add bookmark/navigation compatibility tests.
- [ ] 4.3 Register the manager and mixins in the commandlog applib module and add fallback object/collection layouts with only P1/P2 fields and controls.

## 5. Replay State Policy

- [ ] 5.1 Add a replay-specific state predicate that accepts `PENDING`, `OK`, and `FAILED` and rejects `UNDEFINED`, `EXPORTED`, and `EXCLUDED` without changing exclusion eligibility.
- [ ] 5.2 Wire the replay-or-retry guard to the new predicate and add parameterized tests for every replay state and the deliberately absent background gate.

## 6. Verification

- [ ] 6.1 Run focused commandlog applib and JPA tests, including metamodel/layout tests for the new view model and compatibility actions.
- [ ] 6.2 Run the affected commandlog Maven verification suite and confirm the change introduces no datastore migration or JDO dependency.
- [ ] 6.3 Update affected commandlog reference documentation to describe the unified primary manager, page limit, four collections, legacy compatibility path, and P2 replay-state boundary.
