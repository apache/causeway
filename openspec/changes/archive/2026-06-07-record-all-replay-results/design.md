## Context

`CommandReplayMappingListener` currently combines two related replay responsibilities.
Before replay execution, `remap(CommandLogEntry, Bookmark)` lets a listener return a replacement bookmark for a recorded command target or reference-valued parameter.
After successful replay, `onReplayResultMapped(Bookmark, Bookmark, CommandLogEntry)` tells the listener about the recorded and actual result bookmarks.

The replay execution path already notifies the SPI when both recorded and actual result bookmarks are available, including identity results.
The in-memory and persistent built-in listeners currently discard identity result notifications before storing anything.
That made sense when the store represented only non-identity remappings, but the next UI goal needs the stored data to represent every object encountered as a replay result.

## Goals / Non-Goals

**Goals:**
- Rename the SPI methods to `lookup(...)` and `onReplayResult(...)` so their names match their broadened responsibilities.
- Store or persist identity result notifications as first-class replay result records.
- Keep existing lookup behavior so later replay inputs can still be replaced by the actual bookmark associated with a recorded bookmark.
- Keep existing conflict policy behavior for repeated notifications with different actual bookmarks.
- Update tests and call sites to use the renamed SPI methods.

**Non-Goals:**
- Do not add the ReplayableCommand UI changes in this proposal.
- Do not change command export format or imported command DTO semantics.
- Do not add new tables, entities, or repository contracts beyond changing what the existing replay result mapping repository stores.
- Do not introduce compatibility bridge methods for the old SPI method names unless implementation constraints require a short transitional step.

## Decisions

- Treat replay result records as observations rather than only remappings.
  This means `onReplayResult(...)` stores the first observed actual bookmark for a recorded result bookmark even when both bookmarks are equal.
  The alternative was to add a separate observation listener or repository, but that would duplicate the existing recorded-to-actual shape and delay the UI work.

- Rename `remap(...)` to `lookup(...)` rather than introducing a second method.
  The method returns the actual bookmark already associated with a recorded bookmark, so `lookup` describes both identity and non-identity records without implying that a value must differ.
  The alternative was `resolve(...)`, but that can imply object resolution and this SPI deliberately works with bookmarks without resolving objects.

- Rename `onReplayResultMapped(...)` to `onReplayResult(...)` and update call sites directly.
  The new name avoids suggesting that a mapping only exists when bookmarks differ.
  This is a breaking SPI change, but the proposal already changes method semantics and custom implementors need to review their implementations.

- Preserve conflict handling unchanged after the first stored observation.
  A repeated notification with the same actual bookmark remains idempotent.
  A repeated notification with a different actual bookmark still follows the configured conflict policy and keeps the original actual bookmark.

## Risks / Trade-offs

- [Risk] Custom `CommandReplayMappingListener` implementations will fail to compile after the rename.
  → Mitigation: Mark the proposal as breaking and update all repository implementations and tests in the same change.
- [Risk] Persisting identity results increases the number of rows shown by replay result mapping list actions.
  → Mitigation: This is intentional for the upcoming UI requirement, and no schema change is required.
- [Risk] Code or docs may still use mapping terminology after the rename.
  → Mitigation: Search for old method names and update comments, tests, and relevant specs during implementation.
