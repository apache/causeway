> **Execution note:** small, self-contained backport (one repository-method guard); **`medium` reasoning effort**
> is sufficient.

## Why

Child change 4 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-2**;
second-opinion G2, third-opinion F3, fourth-opinion G2 — MED-HIGH, unanimous).

`CommandLogEntryRepositoryAbstract.saveForReplay(...)` creates and persists a new command-log entry
**unconditionally**, keyed by the DTO's interaction id:

```
// extensions/core/commandlog/applib/.../dom/CommandLogEntryRepositoryAbstract.java:337-346 (identical to audited head)
public C saveForReplay(final CommandDto commandToReplay) {
    final C entity = factoryService.detachedEntity(commandLogEntryClass);
    entity.init(commandToReplay, ReplayState.PENDING, 0);
    entity.setParentInteractionId(null);
    entity.setExecuteIn(ExecuteIn.FOREGROUND);
    persist(entity);            // no findByInteractionId guard
    return entity;
}
```

Re-importing (or re-saving) a command whose interaction id is already present therefore creates a **duplicate**
`CommandLogEntry`. Because the JPA command-log primary key is the interaction id, a repeated import is liable to
**fail persistence** rather than reuse the existing row. Neither the unified nor the legacy import caller
pre-checks for an existing entry.

Maintenance (`ecp`, CAUSEWAY-4037 commit `4bc7b2c9f25`) makes the method idempotent: it looks up
`findByInteractionId(...)` first and returns the existing entry when present
(maintenance `CommandLogEntryRepositoryAbstract.java:267-283`).

## What Changes

- Make `saveForReplay(CommandDto)` idempotent: look up the existing entry by the DTO's interaction id and return
  it unchanged when present; otherwise create, initialise, and persist a new replay entry as today.
- Do not alter the created entry's initialisation (`ReplayState.PENDING`, null parent interaction id, foreground
  execute-in), the repository query set, the persistence contract, or either import caller.

## Capabilities

### Modified Capabilities

- `command-result-metadata`: replay import persistence is idempotent per interaction id — re-importing a command
  whose interaction id already exists returns the existing replay entry rather than creating a duplicate.

## Impact

- Affects commandlog applib `CommandLogEntryRepositoryAbstract.saveForReplay` only; the existing
  `findByInteractionId` query is reused (no new query, schema, or persistence-adapter change).
- Fixes both the unified importer and the retained legacy `CommandReplayManager.importCommands` path (both call
  `saveForReplay`).
- Requires repository/integration coverage for a repeated canonical and legacy import: the second import returns
  the existing entry, no duplicate row is created, and persistence does not fail.
