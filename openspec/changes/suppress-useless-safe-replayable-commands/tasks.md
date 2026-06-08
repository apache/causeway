## 1. Eligibility Model

- [ ] 1.1 Locate all production paths that wrap `CommandLogEntry` instances as `ReplayableCommand` view models.
- [ ] 1.2 Add a centralized replayable-command eligibility predicate for command log entries.
- [ ] 1.3 Keep state-changing command log entries eligible according to existing replay state filters.
- [ ] 1.4 Mark safe action command log entries eligible only when they store a single result bookmark.
- [ ] 1.5 Mark safe action command log entries without a result bookmark as ineligible without mutating or deleting the entry.

## 2. Collection Integration

- [ ] 2.1 Apply the eligibility predicate to command replay manager collections that expose `ReplayableCommand` rows.
- [ ] 2.2 Apply the eligibility predicate to command export manager active command collections.
- [ ] 2.3 Apply the eligibility predicate to replayable command navigation helpers so omitted safe entries are skipped.
- [ ] 2.4 Preserve command log repository persistence for void and multi-result safe action command log entries.

## 3. Tests

- [ ] 3.1 Add tests showing safe action command log entries with one stored result bookmark remain replayable/exportable.
- [ ] 3.2 Add tests showing safe action command log entries without a result bookmark are omitted from `ReplayableCommand` collections.
- [ ] 3.3 Add tests showing safe action command log entries without a result bookmark remain persisted.
- [ ] 3.4 Add tests showing state-changing command log entries retain existing replayable behavior.
- [ ] 3.5 Add tests showing replayable command previous/next navigation skips ineligible safe action entries.

## 4. Verification

- [ ] 4.1 Run the relevant command-log applib test suite.
- [ ] 4.2 Run `openspec status --change suppress-useless-safe-replayable-commands` and confirm the change remains apply-ready.
