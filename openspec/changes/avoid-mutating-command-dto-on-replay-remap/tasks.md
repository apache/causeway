## 1. Locate Replay Remapping Flow

- [ ] 1.1 Identify where `ReplayableCommand` obtains the recorded `CommandDto` from `CommandLogEntry` for replay.
- [ ] 1.2 Identify where target bookmarks are remapped before command execution.
- [ ] 1.3 Identify where reference action parameter bookmarks are remapped before command execution.
- [ ] 1.4 Identify the command DTO copy utility currently used by the replay flow.

## 2. Preserve Recorded Command DTO

- [ ] 2.1 Ensure replay remapping always operates on a defensive copy of the recorded command DTO.
- [ ] 2.2 Ensure target bookmark replacement writes only to the replay execution DTO.
- [ ] 2.3 Ensure reference parameter bookmark replacement writes only to the replay execution DTO.
- [ ] 2.4 Ensure `CommandLogEntry#getCommandDto()` retains recorded target and reference parameter bookmarks after replay remapping.
- [ ] 2.5 Ensure replay failures do not leave remapped values in the recorded command DTO.

## 3. Preserve Replay and Inspection Behavior

- [ ] 3.1 Ensure command execution still receives remapped target bookmarks when mapping lookup provides replacements.
- [ ] 3.2 Ensure command execution still receives remapped reference parameter bookmarks when mapping lookup provides replacements.
- [ ] 3.3 Ensure retry starts from the original recorded command DTO rather than a previous execution DTO.
- [ ] 3.4 Ensure replayable command participant inspection still shows recorded bookmarks from the stored DTO and actual bookmarks from mapping lookup or replay success.

## 4. Tests and Verification

- [ ] 4.1 Add a unit test proving remapped target replay does not mutate the recorded command DTO.
- [ ] 4.2 Add a unit test proving remapped reference parameter replay does not mutate the recorded command DTO.
- [ ] 4.3 Add a unit test proving replay failure preserves recorded command DTO target and parameter bookmarks.
- [ ] 4.4 Add or update tests proving replay execution receives the remapped execution DTO.
- [ ] 4.5 Run targeted commandlog applib replay mapping tests.
- [ ] 4.6 Run `openspec status --change avoid-mutating-command-dto-on-replay-remap` and address any artifact validation issues.
