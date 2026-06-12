## Context

`ReplayableCommand` is a view model around a `CommandLogEntry` and already parses the recorded command DTO to display command metadata and YAML.
Replay execution remaps command targets and reference action parameters by asking the configured `CommandReplayMappingListener` instances for an actual bookmark before executing a copied DTO.
Successful replay also records result mappings by notifying listeners with the recorded result bookmark and the actual replay result bookmark.
The UI currently requires users to infer those remappings from raw DTO YAML or separate replay result mapping screens.

## Goals / Non-Goals

**Goals:**
- Add a `ReplayableCommandParticipant` view model for remapping rows derived from a replayable command.
- Add a collection on `ReplayableCommand` that lists recorded target, reference parameter, and result participants, with actual bookmarks populated after successful replay.
- Include target, reference parameter, and result participants in the same table.
- Resolve target and parameter actual bookmarks to objects when possible.
- Resolve result actual bookmarks only after the replayable command is in a successful replay state.
- Provide fallback layout metadata for the new view model and include the collection in `ReplayableCommand.layout.fallback.xml`.

**Non-Goals:**
- Do not change the replay mapping SPI signatures.
- Do not add persistence or schema changes.
- Do not mutate the recorded command DTO while preparing UI rows.
- Do not expose non-reference action parameters as remapping participants.

## Decisions

### Derive rows on demand from the replayable command

The remappings collection will be calculated by parsing the command DTO owned by the current `ReplayableCommand` instead of storing a second representation.
This keeps the view model consistent with the recorded command and avoids migration concerns.
The alternative was to persist participant rows when replay runs, but that would duplicate command DTO state and require schema changes.

### Use a dedicated participant view model

Each row will be a `ReplayableCommandParticipant` view model with properties for owning interaction id, role, target, parameter name, result, recorded bookmark, and actual bookmark.
This provides layout metadata and table column control independent of `ReplayableCommand`.
The alternative was to use a DTO-like inner class, but that would make fallback layout and future object actions harder to control.

### Use replay mapping lookup for target and parameter participants

Target and reference parameter rows will start from recorded bookmarks in the command DTO and ask the configured replay mapping listeners for actual bookmarks using the same lookup contract used by replay execution.
Rows without an actual replacement will still be included so that recorded participants are visible before replay.
After successful replay, rows without an explicit actual replacement will use the recorded bookmark as their actual bookmark.
Listener failures will not break the UI and will be handled consistently with replay-time lookup behaviour.

### Show result participant before replay and actual result after success

The result row will be included when the recorded result bookmark is available.
Before replay succeeds, only the recorded result bookmark is shown.
After replay succeeds, the actual result bookmark is found from replay mapping data, or falls back to the recorded result bookmark when no explicit mapping exists.
If the actual result cannot be resolved to an object, the bookmark fields still show the recorded and actual values.

### Resolve objects best-effort

The participant view model will keep bookmark values as the source of truth and use `BookmarkService` to resolve target, parameter, and result objects only when allowed by role and replay state.
This preserves usability for unresolved or external bookmarks and avoids making object resolution a precondition for displaying remappings.

## Risks / Trade-offs

- [Risk] Recomputing the collection can invoke replay mapping listeners during page rendering.
  → Mitigate by using safe, best-effort lookup and keeping the collection limited to the current command DTO participants.
- [Risk] Multiple mapping listeners could provide conflicting replacements.
  → Mitigate by following replay-time precedence or first-available behaviour so the table reflects execution behaviour.
- [Risk] Parameter names may be unavailable in older or unusual command DTOs.
  → Mitigate by leaving `parameterName` blank while still displaying role and bookmark values.
- [Risk] Result mapping lookup depends on available mapping data.
  → Mitigate by omitting the result row unless the command is successfully replayed and an actual bookmark can be found.
