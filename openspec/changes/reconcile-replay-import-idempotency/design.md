## Context

`saveForReplay(CommandDto)` is the replay-import persistence primitive shared by the unified importer
(`CommandManager_importCommands`) and the retained legacy importer (`CommandReplayManager.importCommands`). On
`main` it always creates a new entry (`CommandLogEntryRepositoryAbstract.java:337-346`). The command-log JPA
identity is the interaction id, so a second import of the same command either duplicates the row (in a store that
tolerates it) or fails the insert.

The repository already exposes an interaction-id finder (`findByInteractionId`). Maintenance's `saveForReplay`
uses it as a guard and returns the existing entry, making import idempotent (`ecp` `:267-283`, CAUSEWAY-4037
`4bc7b2c9f25`). `CommandLogEntryRepositoryAbstract` is byte-identical between the audited head and current HEAD.

## Goals / Non-Goals

**Goals:**

- Re-importing/re-saving a command whose interaction id already exists returns the existing replay entry without
  creating a duplicate or failing persistence.
- A first-time replay save is unchanged.

**Non-Goals:**

- No change to how a fresh replay entry is initialised, to the repository query set, or to either import caller.
- No new query, schema, persistence-adapter, or JDO change.
- Not deduplicating across *different* interaction ids or reconciling the *content* of an existing entry against
  a re-imported DTO — idempotency is by interaction-id presence only, matching maintenance.

## Decisions

### Guard `saveForReplay` on `findByInteractionId`

At the top of `saveForReplay`, resolve the DTO's interaction id and call `findByInteractionId(...)`; when an entry
is present, return it unchanged; otherwise fall through to the existing create/init/persist. This is the minimal,
maintenance-faithful fix and keeps the caller contract (returns the `CommandLogEntry` for the interaction id).

Rejected — pre-checking in each import caller: duplicates the guard across the unified and legacy paths and leaves
the repository primitive itself unsafe for any future caller.

### Return the existing entry as-is

Do not mutate or re-initialise an existing entry from the re-imported DTO. Maintenance returns the existing entry
unchanged; re-initialising could clobber a partially-replayed entry's state.

## Acceptance evidence

- A repository/integration test importing the same canonical (result-bearing) stream twice: the second import
  returns the existing entries, creates no duplicate rows, and does not fail persistence.
- The same for a legacy multi-document stream via the legacy importer.
- A first-time import test confirming unchanged create/init behaviour (`PENDING`, null parent interaction id,
  foreground execute-in).
