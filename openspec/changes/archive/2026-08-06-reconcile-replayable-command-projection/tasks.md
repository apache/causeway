## 1. Eligibility and Ordered Lookup Foundations

- [x] 1.1 Add a reusable replayable-command eligibility policy that retains state-changing and unclassifiable entries while excluding safe actions without recorded results.
- [x] 1.2 Extend `ReplayContext` compatibly with `BookmarkService` and `ApplicationFeatureRepository`, updating module wiring and existing fixtures without changing replay transaction behavior or depending on core metamodel internals.
- [x] 1.3 Add persistence-neutral foreground-before and foreground-since repository operations and reusable query behavior, with the corresponding Jakarta Persistence named queries and no schema changes.
- [x] 1.4 Add focused applib and JPA tests for state-changing, result-bearing safe, resultless safe, and unclassifiable eligibility plus ordered foreground repository lookup.

## 2. Participant Projection

- [x] 2.1 Add the documented public `ReplayableCommandParticipant` view model, roles, role-specific optional object properties, human-readable title, and identity-only memento parsing and serialization.
- [x] 2.2 Enrich `ReplayableCommand` with non-persisted `hasResult` and derived target, reference-parameter, and result participants while omitting scalar parameters.
- [x] 2.3 Resolve target and parameter actual bookmarks through `ResultRemappingService`, using explicit mappings in any state and recorded-bookmark fallback only after replay state `OK`.
- [x] 2.4 Gate result actual bookmarks on replay state `OK`, then use the configured mapping or recorded-bookmark fallback, without depending directly on persistent mapping storage.
- [x] 2.5 Rehydrate participant bookmarks and object links from the owning command and current mapping state, resolving local objects best-effort through `BookmarkService`.
- [x] 2.6 Add focused tests for participant derivation, mapping and replay-state combinations, object resolution, result presence, titles, readable mementos, and rehydration.

## 3. Navigation and Presentation

- [x] 3.1 Add previous/next replayable-command behavior that scans foreground ordering, skips ineligible entries, preserves `ReplayContext`, and disables safely at boundaries.
- [x] 3.2 Add and register safe command-recording-suppressed previous/next mixins with focused action and non-mutation tests.
- [x] 3.3 Add participant fallback layout and table column order, and update replayable-command layout/columns to show `hasResult` and Participants before controls while removing redundant target presentation.
- [x] 3.4 Register the participant view model and new mixins in the commandlog applib module and add focused metadata/layout/order tests.

## 4. Existing Manager Adapters

- [x] 4.1 Apply the shared eligibility policy to existing export-manager collections without changing their logical type, baseline, memento, export actions, or replay-state filters.
- [x] 4.2 Apply the shared eligibility policy to the existing completed-or-excluded replay collection while leaving pending-or-failed repository results unfiltered.
- [x] 4.3 Add regression tests proving resultless safe commands are omitted from general existing-manager projections, retained in pending-or-failed work, and never deleted or mutated.

## 5. Documentation and Verification

- [x] 5.1 Document result presence, participant roles, recorded/actual bookmark semantics, object availability, mementos, eligibility, adjacent navigation, and the temporary use of separate managers until P2.
- [x] 5.2 Run focused commandlog applib and JPA tests with JDK 21, including existing replay mapping, persistent mapping, export manager, and replay manager regressions.
- [x] 5.3 Run the affected aggregate Maven build, IDE compilation and inspections, strict OpenSpec validation, and repository checks.
- [x] 5.4 Confirm the implementation does not introduce the P2 unified manager or R1/R2, E1/W1, B1/B2 behavior and does not restore commandlog JDO.
