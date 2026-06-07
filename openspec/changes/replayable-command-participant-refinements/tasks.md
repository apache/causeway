## 1. Participant Actual Bookmark Lookup

- [ ] 1.1 Update target participant actual bookmark derivation to use replay mapping lookup before checking replay state.
- [ ] 1.2 Update reference parameter participant actual bookmark derivation to use replay mapping lookup before checking replay state.
- [ ] 1.3 Preserve successful replay fallback to recorded bookmark when no target or parameter mapping exists.
- [ ] 1.4 Preserve result participant actual bookmark behaviour.

## 2. Tests and Validation

- [ ] 2.1 Add or update tests for mapped target actual bookmark population before successful replay.
- [ ] 2.2 Add or update tests for mapped parameter actual bookmark population before successful replay.
- [ ] 2.3 Add or update tests for unmapped target and parameter actual bookmarks before and after successful replay.
- [ ] 2.4 Run focused command log applib tests for replayable command mapping and participant behaviour.
- [ ] 2.5 Run `openspec validate replayable-command-participant-refinements --strict`.
