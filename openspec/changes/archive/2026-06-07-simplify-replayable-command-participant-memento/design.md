## Context

`ReplayableCommandParticipant` is a view model whose current memento includes interaction id, role, parameter name, recorded bookmark, and actual bookmark.
The bookmark data is already derived from the owning replayable command's `CommandLogEntry` and replay mapping lookups.
Including that derived data in the memento makes URLs noisy and can make stale bookmark values appear canonical.

## Goals / Non-Goals

**Goals:**

- Use compact readable mementos based on command interaction id and participant identity.
- Recompute recorded bookmark, actual bookmark, and object links from the owning command log entry.
- Keep the public view model constructor shape valid with the string memento first and injected services after it.
- Inject `CommandLogEntryRepository` into the view model constructor if that is the simplest way to rehydrate participant state.
- Preserve existing participant UI behaviour after rehydration.

**Non-Goals:**

- Do not change the participants collection content or ordering.
- Do not change replay mapping SPI signatures.
- Do not persist participant rows independently.
- Do not retain the old pipe-delimited bookmark-bearing memento format as the canonical URL format.

## Decisions

- Use `--` as the memento delimiter because the requested forms are readable and map directly to participant roles.
- Encode target and result participants as `[commandInteractionId]--target` and `[commandInteractionId]--result` because each command has at most one semantic target participant set and one result participant in the current UI contract.
- Encode parameter participants as `[commandInteractionId]--parameter--[parameterName]` because the parameter name distinguishes reference parameter participants.
- Rehydrate by loading the owning `CommandLogEntry` and selecting the matching participant from the replayable command's derived participant list.
- Keep non-public construction helpers available for tests and collection derivation, but ensure the framework-facing constructor remains a single public constructor with memento string first.

## Risks / Trade-offs

- Parameter names that contain `--` could make a plain split ambiguous → Parse only the first two delimiters or preserve the remaining suffix as the parameter name.
- Old URLs that include bookmark data will not be canonical → Treat this as a breaking URL-format refinement.
- Rehydration adds a repository lookup for standalone participant URLs → Keep collection-derived construction direct and use repository lookup only for memento reconstruction.
