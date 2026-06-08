## 1. Eligibility Model

- [x] 1.1 Locate all production paths that wrap `CommandLogEntry` instances as `ReplayableCommand` view models.
- [x] 1.2 Add a centralized replayable-command eligibility predicate for command log entries.
- [x] 1.3 Keep state-changing command log entries eligible according to existing replay state filters.
- [x] 1.4 Mark safe action command log entries eligible only when they store a single result bookmark.
- [x] 1.5 Mark safe action command log entries without a result bookmark as ineligible without mutating or deleting the entry.

## 2. Collection Integration

- [x] 2.1 Apply the eligibility predicate to command replay manager collections that expose `ReplayableCommand` rows.
- [x] 2.2 Apply the eligibility predicate to command export manager active command collections.
- [x] 2.3 Apply the eligibility predicate to replayable command navigation helpers so omitted safe entries are skipped.
- [x] 2.4 Preserve command log repository persistence for void and multi-result safe action command log entries.

## 3. Tests

- [x] 3.1 Add tests showing safe action command log entries with one stored result bookmark remain replayable/exportable.
- [x] 3.2 Add tests showing safe action command log entries without a result bookmark are omitted from `ReplayableCommand` collections.
- [x] 3.3 Add tests showing safe action command log entries without a result bookmark remain persisted.
- [x] 3.4 Add tests showing state-changing command log entries retain existing replayable behavior.
- [x] 3.5 Add tests showing replayable command previous/next navigation skips ineligible safe action entries.

## 4. Verification

- [x] 4.1 Run the relevant command-log applib test suite.
- [x] 4.2 Run `openspec status --change suppress-useless-safe-replayable-commands` and confirm the change remains apply-ready.
