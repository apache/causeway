## Context

`ReplayableCommand` derives participant rows from the recorded command DTO, replay state, and replay mapping listeners.
Target and reference parameter rows already have recorded bookmarks before replay.
The current derivation only exposes actual target and parameter bookmarks after successful replay, even when a replay mapping listener can already provide a mapped bookmark.

## Goals / Non-Goals

**Goals:**

- Populate actual bookmark values for target participants when replay mapping lookup returns a mapping.
- Populate actual bookmark values for reference parameter participants when replay mapping lookup returns a mapping.
- Keep unmapped target and parameter actual bookmarks empty before successful replay.
- Keep successful replay fallback behaviour for unmapped target and parameter participants.
- Preserve result participant behaviour.

**Non-Goals:**

- Do not change the replay mapping SPI signatures.
- Do not change participant layout or column ordering.
- Do not change replay execution remapping behaviour.
- Do not add persistence or schema changes.

## Decisions

### Split actual bookmark derivation by role

Target and parameter participants should ask replay mapping listeners first, regardless of replay state.
If lookup returns a bookmark, that bookmark becomes the actual bookmark shown on the participant.
If lookup returns no bookmark and the command has replay state `OK`, the participant keeps the current fallback to its recorded bookmark.
If lookup returns no bookmark and the command is not `OK`, the actual bookmark remains empty.

The alternative was to keep actual values tied entirely to successful replay state, but that hides mappings that are already known before replay.

### Keep result derivation tied to successful replay

Result actual bookmarks should remain tied to successful replay because result mappings are observations produced by replay execution.
This avoids presenting speculative result actuals before the command has run.
The alternative was to apply the same eager lookup to results, but that could conflate historical result mappings with the current command execution state.

### Keep listener failure tolerance

Participant derivation should continue to tolerate listener lookup failures and still return rows with recorded bookmarks.
This keeps the UI usable even when a mapping listener cannot answer a lookup.

## Risks / Trade-offs

- [Risk] Rendering participants may call replay mapping listeners before replay.
  → Mitigate by reusing existing safe lookup handling and keeping failures non-fatal.
- [Risk] A mapping available before replay might later differ if listener state changes.
  → Mitigate by deriving rows on demand so the table reflects current listener lookup state.
