## 1. Marker API

- [ ] 1.1 Add a public marker interface for command recording suppression in an applib package that core runtime and extension modules can both use.
- [ ] 1.2 Add minimal API documentation explaining that the marker opts targets out of command recording support but does not suppress normal invocation.

## 2. Core Recording Suppression

- [ ] 2.1 Update normal action command publishing preparation to leave commands unready when the action target implements the marker interface.
- [ ] 2.2 Update property edit command publishing preparation to leave commands unready when the property target implements the marker interface.
- [ ] 2.3 Update synthetic parented collection selector action synthesis to skip adding selector actions when the collection owner type implements the marker interface.
- [ ] 2.4 Confirm synthetic selector invocation result-capture logic remains covered for unmarked owner types.

## 3. Command Log Helper Opt-Out

- [ ] 3.1 Have `CommandReplayManager` implement the marker interface.
- [ ] 3.2 Have `CommandExportManager` implement the marker interface.
- [ ] 3.3 Have `ReplayableCommand` implement the marker interface.
- [ ] 3.4 Have `ReplayableCommandParticipant` implement the marker interface.
- [ ] 3.5 Have command-log entry entity and view types implement the marker interface.

## 4. Verification

- [ ] 4.1 Add or update unit tests proving marked action and property targets are not recorded while unmarked targets remain recordable.
- [ ] 4.2 Add or update synthetic selector tests proving marked owner types do not receive synthetic selector actions and unmarked owner types still do.
- [ ] 4.3 Add or update command-log extension tests proving export and replay helper object interactions are suppressed during recording support.
- [ ] 4.4 Run focused tests for core metamodel/runtime command publishing and command-log applib behavior.
- [ ] 4.5 Run `openspec validate suppress-recording-for-replay-objects --strict`.
