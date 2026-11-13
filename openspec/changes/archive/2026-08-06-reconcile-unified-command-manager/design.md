## Context

P1 established the reusable `ReplayableCommand` eligibility policy, participant projection, result-presence metadata, and adjacent navigation. Causeway 4 still presents command review through separate `CommandExportManager` and `CommandReplayManager` view models, whose timestamp-only state and workflow-specific collections prevent later reachability, export/import, workflow, and background-gate slices from sharing one sequence context.

The maintenance branch provides a unified, baseline-bounded `CommandManager` with four read collections and baseline/limit controls. Its observable behavior is authoritative, but Causeway 4 must also preserve the legacy `EXPORTED` replay state, existing manager logical types and bookmarks, Jakarta APIs, the persistence-neutral repository split, and P1 eligibility. P2 must not pull forward the later R1/R2, E1, W1, or B1/B2 concerns.

## Goals / Non-Goals

**Goals:**

- Add one command-recording-suppressed `CommandManager` as the primary commandlog review surface.
- Give the manager stable baseline and page-limit state with safe, non-recording navigation controls.
- Present ordered `commandsInSequence`, `excluded`, `pendingOrFailed`, and `recordedOrReplayed` collections with explicit replay-state and P1 eligibility rules.
- Preserve Causeway 4's persisted `EXPORTED` state as visible historical work.
- Preserve old manager bookmarks and provide a safe route from each legacy manager to the unified manager.
- Add only the persistence-neutral repository operations and JPA named queries needed by the new read model.
- Broaden replay/retry action eligibility to the P2 state set without implementing B2's background-completion gate.

**Non-Goals:**

- Do not add export roots, participant tracking, `knownParticipants`, reference-data classification, or reachability validation from R1/R2.
- Do not redesign YAML export/import, remap exported command sequences, or validate selection from E1.
- Do not implement exclusion/restoration/deletion/movement/retimestamping workflows from W1.
- Do not add background-state reporting or replay/export completion gates from B1/B2.
- Do not remove the legacy manager logical types, change their timestamp-only mementos, migrate stored bookmarks, change command persistence, or restore commandlog JDO.

## Decisions

### Add one stateful manager without a workflow mode

Add `CommandManager` with logical type `causeway.ext.commandLog.CommandManager`. It implements `ViewModel` and `CommandRecordingSuppressed` and carries exactly two pieces of state: an inclusive baseline timestamp and a positive page limit. The default limit is 100. Its canonical memento is `<timestamp>--<limit>`; the framework constructor also accepts a timestamp-only value and supplies the default limit. Missing or malformed components fall back independently to the current timestamp or default limit, following the existing tolerant timestamp-memento convention.

No export/replay mode is encoded. The collections themselves provide the useful groupings, allowing later slices to add actions without creating divergent manager identities.

Separate mode-specific managers or a mode field were rejected because they preserve the split P2 is intended to remove. Reusing a timestamp-only memento was rejected because changing the page size would then fail to survive bookmark navigation.

### Reuse safe baseline and limit controls

Introduce small `HasBaseline` and `HasLimit` contracts and manager mixins for previous hour, next hour, change baseline, and change limit. Every action is safe, has command and execution publishing disabled, and returns a new manager retaining the other state component. A non-positive or unparsable limit uses the default rather than issuing an unbounded read.

The legacy managers keep their existing controls and mementos. Retrofitting their URLs with a limit was rejected because it would break their compatibility role.

### Define four focused review intentions over foreground commands

`commandsInSequence` reads foreground entries at or after the baseline in repository order, applies the page limit at the repository boundary, excludes only `EXCLUDED`, and applies P1 general eligibility. It therefore includes eligible Causeway 4 entries in `UNDEFINED`, `EXPORTED`, `PENDING`, `OK`, and `FAILED` states.

`excluded` reads `EXCLUDED` foreground entries at or after the baseline and applies P1 general eligibility. `pendingOrFailed` reads `PENDING` and `FAILED` entries and deliberately wraps every repository result, preserving P1's imported-work exception. `recordedOrReplayed` reads `UNDEFINED`, legacy `EXPORTED`, and `OK` entries and applies P1 general eligibility. These focused review collections are baseline-bounded but not truncated by the sequence page limit; the limit controls only `commandsInSequence`.

Treating `EXPORTED` as excluded or invisible was rejected because Causeway 4 has persisted that state as normal historical work. Reclassifying it would silently hide commands during migration.

### Extend repository semantics rather than filtering complete tables

Add persistence-neutral repository methods for excluded and recorded-or-replayed foreground reads. Reuse the existing pending-or-failed query and bounded foreground query. Extend the reusable repository base and JPA named-query declarations to express the three-state `UNDEFINED`/`EXPORTED`/`OK` read with the established timestamp and interaction-id ordering. No entity field, index, or schema changes are introduced.

Loading all commands and filtering in a view model was rejected because collection semantics and ordering belong at the repository boundary and may operate over large command logs.

### Make the unified entry point primary while retaining compatibility shims

Add one prototyping menu action that opens `CommandManager` with the current hour as baseline and the default limit. Keep the existing `exportManager` and `replayManager` menu action identifiers registered but hidden, so customized metadata and direct callers are not broken while the standard menu exposes one manager entry point.

Keep `CommandExportManager` and `CommandReplayManager`, their logical type names, timestamp-only framework constructors, collections, and existing workflow actions. Add a safe, publishing-disabled `openCommandManager` action to each that transfers its baseline and supplies the default limit. The new manager's timestamp-only constructor provides the inverse compatibility path for links that supply only legacy state; existing bookmarks are not rewritten automatically.

Deleting the old types was rejected because serialized view-model bookmarks include their logical types. Leaving all three menu launchers visible was rejected because it would not establish the unified surface as primary.

### Separate replay eligibility from exclusion eligibility

The replay/retry action is enabled for `PENDING`, `OK`, and `FAILED` commands. It is disabled for `UNDEFINED`, legacy `EXPORTED`, and `EXCLUDED`. This predicate is independent of the current exclusion predicate, which remains limited to its existing P2-external workflow behavior. No background check is added; B2 will conjunct its completion gate later.

Reusing `isPendingOrFailed` was rejected because P2 explicitly allows replaying an `OK` command. Broadening the shared exclusion predicate at the same time was rejected because exclusion workflow belongs to W1.

### Keep presentation useful but free of later-slice controls

The fallback manager layout shows baseline and limit controls in a header area and places `pendingOrFailed` and `recordedOrReplayed` under a replay-oriented tab, with `commandsInSequence` and `excluded` as sequence-review collections. Collection tables use the P1 columns needed to identify and assess a command—interaction id, timestamp, member, replay state, and result presence—and do not expose `knownParticipants` or later export/workflow actions.

The module registers the manager and its mixins explicitly. All manager reads and controls remain non-mutating and command-recording-suppressed.

## Risks / Trade-offs

- [Legacy menu actions remain registered but hidden] → Retain their action identifiers and implementations, and cover both hidden support and direct invocation with compatibility tests.
- [A page limit can split commands sharing a timestamp] → Preserve the repository's timestamp/interaction-id ordering and treat the limit as a display page, not an export transaction boundary.
- [The four collections intentionally overlap] → Name and document them as review perspectives, and test state membership independently rather than implying partitioning.
- [Causeway 4's `EXPORTED` state does not exist in the same form in maintenance] → Include it in general and recorded-history views but never make it replayable.
- [Tolerant memento parsing can conceal invalid input] → Fall back independently and test canonical, timestamp-only, blank, malformed timestamp, malformed limit, and non-positive limit cases.
- [Broader replay of `OK` commands can repeat domain behavior] → Keep replay explicit, retain existing confirmation/transaction behavior, and change only the action's state guard.

## Migration Plan

Deploy commandlog applib and JPA changes together. The standard menu will show the new manager; old `CommandExportManager` and `CommandReplayManager` bookmarks continue to deserialize and their screens offer `openCommandManager`. Existing commands, including those in `EXPORTED`, require no data migration. Custom menus may continue invoking the registered legacy action identifiers during a transition, although those actions are hidden in the standard UI.

Rollback removes the new manager, mixins, and repository query wiring and restores visibility of the two legacy launchers. Because P2 adds no persistent fields and rewrites no bookmarks or replay states, rollback requires no datastore repair.

## Open Questions

None. The earlier P1 question about separate manager mementos is resolved by retaining both legacy logical types and mementos as shims while making the unified manager and its baseline/limit memento the forward path.
