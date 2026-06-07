## Context

The command log extension already has a persistent replay result mapping entity, an abstract repository contract, JDO/JPA concrete repositories, and a command log menu action that lists all persisted replay mappings.
The repository currently supports `findAll()`, `findByRecordedBookmark(...)`, and `createAndPersist(...)`.
The persistence entities currently define named queries for all mappings and recorded-bookmark lookup only.

After identity replay results are recorded, replay mappings can support broader inspection workflows.
Users need a changed-only view to focus on non-identity mappings, and they need lookup actions by both recorded and actual bookmarks.

## Goals / Non-Goals

**Goals:**
- Add repository methods for changed mappings and actual-bookmark lookup.
- Expose command log menu actions for all mappings, changed mappings, recorded bookmark lookup, and actual bookmark lookup.
- Update both JDO and JPA named queries so the abstract repository can implement the new finders consistently.
- Keep menu actions hidden when no persistent replay mapping repository is available.

**Non-Goals:**
- Do not change how mappings are recorded during replay.
- Do not add new persistent entities or tables.
- Do not add the final ReplayableCommand UI that consumes these finders.
- Do not add write or delete menu actions for replay mappings.

## Decisions

- Keep finder methods on `CommandReplayResultMappingRepository` rather than introducing a separate query service.
  The repository is already the abstraction used by the persistent listener and menu, and extending it keeps JDO and JPA support aligned.

- Add `findChanged()` for non-identity mappings where recorded and actual bookmarks differ.
  This creates the explicit “changed mappings” view requested by users while preserving `findAll()` for the complete view.

- Add `findByActualBookmark(Bookmark)` returning a list rather than an optional value.
  Recorded bookmark remains unique, but multiple recorded bookmarks can legitimately map to the same actual bookmark.

- Add menu actions in `CommandLogMenu` next to the existing replay result mappings action.
  The all-mappings action can retain or rename the existing behavior, while new actions should use safe semantics and disabled command/execution publishing like the current listing action.

- Add an index for `actualBookmark` in JDO and JPA persistence metadata.
  Actual-bookmark lookup is user-driven and should not require scanning all rows.

## Risks / Trade-offs

- [Risk] Query syntax for changed mappings differs between JDOQL and JPQL.
  → Mitigation: Add persistence-specific named queries and test through repository integration coverage.
- [Risk] Existing UI action names may shift if the current all-mappings action is renamed.
  → Mitigation: Prefer adding clear actions while keeping existing behavior compatible where possible.
- [Risk] `actualBookmark` is not unique.
  → Mitigation: Return a list for actual-bookmark lookup and document that multiple mappings can match.
