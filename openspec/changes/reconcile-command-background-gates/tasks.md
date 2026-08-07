## 1. Pending Background Detection

- [x] 1.1 Add shared commandlog-applib replay support that detects `findBackgroundAndNotYetStarted()` entries through `ReplayContext` and returns one consistent wait-for-execution-and-commit message.
- [x] 1.2 Add focused tests for empty and non-empty pending results and preserve the existing JPA query definition of `ExecuteIn.BACKGROUND` with null `startedAt` without adding repository or schema APIs.

## 2. Recording Completion Gate

- [x] 2.1 Guard new foreground-entry creation in `CommandSubscriberForCommandLog` when recording support is enabled, querying pending background work before persistence and raising the counted user-facing wait exception.
- [x] 2.2 Prove the foreground scheduling command and pre-persisted background lifecycle remain accepted, a later foreground command is rejected while work is pending, and retry succeeds after background execution commits.
- [x] 2.3 Prove recording-support disabled, commandlog disabled, and paused recording paths retain their established behavior without applying the B1 gate.

## 3. Replayable Command Gate

- [x] 3.1 Extend replay-or-retry disablement to apply the shared pending-background gate after the established `PENDING`, `OK`, or `FAILED` state predicate.
- [x] 3.2 Add focused tests for enabled replay after background completion, the shared wait message while pending, unchanged non-replay-state disablement, and direct invocation that performs no replay while gated.

## 4. Unified Manager Replay Workflows

- [x] 4.1 Add and register `replayOrRetryNext` on `pendingOrFailed`, including background checks in disablement and execution, oldest-row selection, current-sequence membership, known-participant validation, and unchanged manager state.
- [x] 4.2 Add and register bounded `replayOrRetryMultiple` on `pendingOrFailed` with limits 5, 10, 20, 40, 80, 160, 320, and all, default 10, established manager ordering, and no separate known-participant gate.
- [x] 4.3 Stop bounded replay before starting when background work is already pending and after each item on failure or newly pending work, preserving completed replay states and allowing remaining work to continue after background execution commits.
- [x] 4.4 Add focused tests for empty collections, ordering and every bound, defaulting, direct invocation, known and unknown next-command participants, stop-on-failure, stop-on-background, continuation, and unchanged baseline/limit/memento.

## 5. Legacy Compatibility and JPA Evidence

- [x] 5.1 Apply the shared pending-background disablement and direct guards to retained legacy replay-next and selected-replay actions, including pre-loop and post-item checks without changing identifiers, parameters, mementos, ordering, or failure behavior.
- [x] 5.2 Add compatibility tests proving row, unified-manager, and legacy-manager entry points cannot bypass B2 and regain their established eligibility behavior after pending work completes.
- [x] 5.3 Add Causeway 4 JPA integration coverage across separate committed transactions for scheduling, recording rejection, background completion, replay pause, and replay continuation.
- [x] 5.4 Confirm no new named query, datastore schema, configuration property, replay state, JDO source, or commandlog JDO adapter is introduced.

## 6. Presentation, Documentation, and Verification

- [x] 6.1 Expose replay-next and replay-multiple with `pendingOrFailed` in the unified-manager fallback layout while retaining import and all E1/W1 controls.
- [x] 6.2 Add presentation tests for action association, ordering, styling, prototyping restriction, publishing suppression, and the completed B1/B2 layout boundary.
- [x] 6.3 Document the global pending-background definition, recording failure behavior, replay pause/continuation workflow, batch limits, and Causeway 4 JPA-only adaptation.
- [x] 6.4 Run focused commandlog applib and JPA tests, the affected commandlog reactor under JDK 21, strict OpenSpec validation, JDO-absence and persistence-scope checks, and repository whitespace checks.
