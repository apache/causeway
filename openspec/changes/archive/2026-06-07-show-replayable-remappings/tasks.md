## 1. Participant Model

- [x] 1.1 Add `ReplayableCommandParticipant` view model in the replay package with role, owning interaction id, parameter name, recorded bookmark, and actual bookmark properties.
- [x] 1.2 Add best-effort target and result object properties that use `BookmarkService` and keep bookmark values visible when lookup fails.
- [x] 1.3 Add role handling for `TARGET`, `PARAMETER`, and `RESULT` participants.

## 2. Replayable Command Collection

- [x] 2.1 Add a participants collection to `ReplayableCommand` that parses command targets from the recorded command DTO.
- [x] 2.2 Add reference parameter parsing that captures parameter names and ignores non-reference parameters.
- [x] 2.3 Use replay mapping listener lookup to find actual bookmarks for target and reference parameter participants.
- [x] 2.4 Add result participant derivation only for successfully replayed commands with recorded and actual result mapping data.
- [x] 2.5 Ensure participant derivation does not mutate the recorded command DTO and tolerates listener lookup failures.

## 3. Layout Metadata

- [x] 3.1 Add `ReplayableCommandParticipant.layout.fallback.xml` with participant fields and bookmark values.
- [x] 3.2 Add `ReplayableCommandParticipant.columnOrder.fallback.txt` if needed for table column order.
- [x] 3.3 Update `ReplayableCommand.layout.fallback.xml` to show the participants collection as a table.

## 4. Tests and Validation

- [x] 4.1 Add unit tests for target remapping participants, including unresolved actual bookmarks.
- [x] 4.2 Add unit tests for reference parameter participants with parameter name lookup and non-reference parameter omission.
- [x] 4.3 Add unit tests for result participant visibility only after successful replay.
- [x] 4.4 Run focused command log applib tests for replayable command mapping and participant behaviour.
- [x] 4.5 Run `openspec validate show-replayable-participants --strict`.
