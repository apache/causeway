## 1. Locate Replay Remapping Flow

- [x] 1.1 Identify where `ReplayableCommand` obtains the recorded `CommandDto` from `CommandLogEntry` for replay.
- [x] 1.2 Identify where target bookmarks are remapped before command execution.
- [x] 1.3 Identify where reference action parameter bookmarks are remapped before command execution.
- [x] 1.4 Identify the command DTO copy utility currently used by the replay flow.
- [x] 1.5 Identify command subscriber lifecycle sync that can copy remapped in-memory command DTOs back to replay entries.

## 2. Preserve Recorded Command DTO

- [x] 2.1 Ensure replay remapping always operates on a defensive copy of the recorded command DTO.
- [x] 2.2 Ensure target bookmark replacement writes only to the replay execution DTO.
- [x] 2.3 Ensure reference parameter bookmark replacement writes only to the replay execution DTO.
- [x] 2.4 Ensure `CommandLogEntry#getCommandDto()` retains recorded target and reference parameter bookmarks after replay remapping.
- [x] 2.5 Ensure replay failures do not leave remapped values in the recorded command DTO.
- [x] 2.6 Ensure command subscriber `onStarted(...)` and `onCompleted(...)` preserve recorded DTO data for replay entries.

## 3. Preserve Replay and Inspection Behavior

- [x] 3.1 Ensure command execution still receives remapped target bookmarks when mapping lookup provides replacements.
- [x] 3.2 Ensure command execution still receives remapped reference parameter bookmarks when mapping lookup provides replacements.
- [x] 3.3 Ensure retry starts from the original recorded command DTO rather than a previous execution DTO.
- [x] 3.4 Ensure replayable command participant inspection still shows recorded bookmarks from the stored DTO and actual bookmarks from mapping lookup or replay success.

## 4. Tests and Verification

- [x] 4.1 Add a unit test proving remapped target replay does not mutate the recorded command DTO.
- [x] 4.2 Add a unit test proving remapped reference parameter replay does not mutate the recorded command DTO.
- [x] 4.3 Add a unit test proving replay failure preserves recorded command DTO target and parameter bookmarks.
- [x] 4.4 Add or update tests proving replay execution receives the remapped execution DTO.
- [x] 4.5 Add tests proving command subscriber lifecycle sync does not fully sync replay entries.
- [x] 4.6 Run targeted commandlog applib replay mapping and subscriber tests.
- [x] 4.7 Run `openspec status --change avoid-mutating-command-dto-on-replay-remap` and address any artifact validation issues.
