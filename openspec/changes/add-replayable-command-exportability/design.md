## Context

`CommandExportManager_exportSelected` validates selected commands only when the user invokes the export action or when selected action parameters are validated.
That validation checks that command targets and reference parameters are export roots or results from earlier commands in the baseline-bounded export sequence.
The export manager already exposes a unified `commands` collection of `ReplayableCommand` view models, but each row does not tell the user whether it will pass export validation at its current position.

The requested implementation uses `Scratchpad` as a request-scoped handoff from `CommandExportManager#getCommands()` to the `ReplayableCommand` instances constructed while rendering the collection.
This keeps the replayable command memento unchanged and allows commands created outside this export-manager interaction to report an unknown exportability value.

## Goals / Non-Goals

**Goals:**

- Expose a nullable Boolean property on `ReplayableCommand` that indicates `true`, `false`, or unknown exportability.
- Compute exportability in the context of a `CommandExportManager` baseline and command ordering.
- Reuse the existing known-target and known-reference-parameter validation semantics so row-level feedback matches export action validation.
- Keep the first implementation simple and easy to test, even if it recomputes context per row.

**Non-Goals:**

- Do not redesign export validation or change the exported YAML format.
- Do not persist exportability on `CommandLogEntry`.
- Do not change command replay states or the meaning of `EXPORTED` and `UNDEFINED`.
- Do not introduce a global cache or cross-request state.

## Decisions

1. Use a well-known `Scratchpad` key owned by `CommandExportManager`.

`CommandExportManager#getCommands()` will put `this` into `Scratchpad` before constructing `ReplayableCommand` instances.
`ReplayableCommand` will inject or otherwise obtain `Scratchpad` in its constructor path and look up the key.
This keeps the context scoped to the current interaction and avoids adding export-manager state to the replayable command memento.

Alternative considered: pass `CommandExportManager` directly to a new constructor.
That would be simpler for direct construction but would spread export-manager-specific constructor overloads and make framework construction from mementos less consistent.

2. Represent exportability as `Boolean`, not `boolean`.

`true` means the command is valid in the current export-manager sequence up to and including itself.
`false` means the export-manager context is available and validation fails for that sequence prefix.
`null` means the command was created without export-manager context, or the system cannot evaluate exportability in that interaction.
This matches the requested tri-state behavior and avoids showing a false negative outside the export manager.

Alternative considered: use an enum with labels such as `EXPORTABLE`, `NOT_EXPORTABLE`, and `UNKNOWN`.
An enum would be more descriptive, but the requested API shape is a nullable Boolean and is enough for table display and sorting.

3. Compute exportability using the baseline-bounded command prefix.

For a replayable command created in the export manager context, gather the current command log entries from the manager's commands collection or repository ordering, retain entries at or after the baseline through the current command, and call the known-target validator on that prefix.
If the validator returns no failure, the command is exportable.
If it returns a failure, the command is not exportable.

Alternative considered: validate only the single command against all earlier results already collected in a reusable accumulator.
That would be more efficient, but a prefix validation reuses existing behavior with less risk for the first version.

4. Extract shared validation helpers if necessary.

The export action currently owns recording-support checks and export-root checks that row-level exportability also needs.
Implementation can keep the existing validator and add package-visible helpers on `CommandExportManager` or a small support class so both action validation and replayable-command exportability use the same rules.
This avoids duplicating `@DomainService` export-root checks or recording-support behavior.

Alternative considered: duplicate the export-root predicate in `ReplayableCommand`.
That risks divergence and makes tests harder to maintain.

## Risks / Trade-offs

- Recomputing prefixes per row can be O(n²) for large pages → The current page limit bounds this work, and the initial implementation can be optimized later if needed.
- `Scratchpad` context could leak within the same request if not keyed narrowly → Use a namespaced static key and overwrite it at the beginning of `getCommands()`.
- Exportability could disagree with export action validation if rules diverge → Keep validation logic shared and add tests that compare row-level values to export validation outcomes.
- Commands created outside the export manager might show a blank exportability value → This is intentional because their export context is unknown.
