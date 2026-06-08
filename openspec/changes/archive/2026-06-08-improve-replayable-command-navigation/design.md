## Context

`ReplayableCommand` is a view model wrapper around `CommandLogEntry` and derives properties, participants, and row actions from the wrapped command record.
`ReplayableCommandParticipant` currently resolves target, argument, and result objects through an `actualBookmark` calculated by `ReplayableCommand`.
That calculation primarily reflects replay remapping and replay success, which is too restrictive for recorded or exported commands that still have valid recorded bookmarks.

Command export review also depends on scanning an ordered command list.
Users can move and export commands from manager screens, but there is no direct row-level way to step to adjacent command records.

## Goals / Non-Goals

**Goals:**
- Preserve replay-state safety for replay execution while improving read-only navigation and participant inspection.
- Resolve domain service targets from recorded bookmarks independent of replay state.
- Resolve target and reference argument participants from recorded bookmarks while the command is a recorded/exported command with replay state `UNDEFINED` or `EXPORTED`.
- Add `previous` and `next` actions that navigate between adjacent command log entries in the command ordering used by replayable command lists.
- Add a non-persisted `hasResult` property and place it before exportability in tables.

**Non-Goals:**
- Do not change replay-or-retry enablement rules.
- Do not change export validation rules or exportability semantics.
- Do not persist new participant, navigation, or result-presence state.
- Do not introduce a new command-log table or external dependency.

## Decisions

1. Resolve record-state participants from recorded bookmarks when replay mapping is not applicable.
Alternative considered: keep using only replay mappings and successful replay state.
That would continue hiding valid recorded targets and arguments during record/export review, so it does not satisfy the change.

2. Treat domain service target bookmarks as stable roots for participant lookup in every replay state.
Alternative considered: special-case only `UNDEFINED` and `EXPORTED`.
Domain services are valid stable targets for both replay and record states, so the broader rule is simpler and consistent with export-root behavior.

3. Keep result participant resolution tied to successful replay mappings or stored results, while exposing result presence separately through `hasResult`.
Alternative considered: use `hasResult` to make result objects navigable in every state.
A stored result bookmark indicates presence, but object availability can still depend on local lookup and replay mapping, so presence and navigation should remain separate concepts.

4. Implement `previous` and `next` as row actions on `ReplayableCommand` that locate adjacent `CommandLogEntry` records through repository/query support in the same ordering already used for command lists.
Alternative considered: make navigation a collection action on `CommandExportManager`.
Row actions work from both command-detail and table contexts and do not couple navigation to one manager view.

5. Implement `hasResult` as a derived Boolean property on `ReplayableCommand` based on the wrapped `CommandLogEntry#getResult()` value.
Alternative considered: infer result presence from the participant collection.
Using the command log entry directly avoids constructing participants merely to render a table column.

## Risks / Trade-offs

- [Risk] Adjacent-command lookup could accidentally use a different ordering than command list rendering.
  → Mitigation: centralize or reuse the existing command ordering comparator/query and cover boundary scenarios in tests.
- [Risk] Looking up recorded bookmarks in more replay states could expose stale local objects.
  → Mitigation: make properties optional and return `null` when bookmark lookup fails, while leaving recorded bookmark values visible.
- [Risk] Table column ordering could vary by framework metadata conventions.
  → Mitigation: set explicit `@PropertyLayout(sequence = ...)` values so `hasResult` precedes `exportable`.

## Migration Plan

No data migration is required because all new behavior is derived from existing command DTO, replay state, and result bookmark data.
Rollback consists of removing the new derived property and row actions and restoring the previous participant actual-bookmark resolution conditions.

## Open Questions

None.
