## 1. Discovery

- [x] 1.1 Locate the current command result bookmark extraction path for scalar action results.
- [x] 1.2 Identify the managed-object APIs for safely inspecting zero, one, or more elements of a packed list, collection, array, or iterable result.
- [x] 1.3 Identify existing tests for `CommandSubscriberForCommandLog`, `MemberExecutorServiceDefault`, safe action command publishing, and command export known-target validation that can be extended.

## 2. Result Bookmark Extraction

- [x] 2.1 Refactor scalar command result bookmark extraction into a helper that accepts one candidate managed object.
- [x] 2.2 Add singleton-container detection that inspects at most enough elements to distinguish empty, singleton, and multi-element results.
- [x] 2.3 Store the contained object's bookmark when the result container has exactly one bookmarkable domain object.
- [x] 2.4 Leave the command result unset for empty containers, multi-element containers, scalar values, and singleton containers containing non-bookmarkable values.
- [x] 2.5 Preserve the existing guard that does not overwrite an already populated command result.

## 3. Export and Replay Integration

- [x] 3.1 Verify command export YAML emits the existing `result` field when a singleton-container result bookmark was recorded.
- [x] 3.2 Verify command export YAML omits `result` for empty and multi-object container results.
- [ ] 3.3 Verify export known-target validation treats a recorded singleton-container result bookmark as an earlier known result.
- [x] 3.4 Verify replay result mapping receives recorded and actual result bookmarks for replayed commands whose recorded result came from a singleton container.

## 4. Tests and Documentation

- [x] 4.1 Add unit tests for scalar bookmarkable result behavior to guard against regressions.
- [x] 4.2 Add unit tests for singleton-list, empty-list, multi-object-list, and singleton-non-bookmarkable-list result behavior.
- [x] 4.3 Add integration or regression tests covering a safe finder that returns a singleton list followed by a command targeting that object.
- [x] 4.4 Update user-facing or developer documentation if command recording result capture behavior is documented.

## 5. Validation

- [ ] 5.1 Run the relevant commandlog applib unit tests.
- [x] 5.2 Run the relevant runtime services or metamodel tests for action result handling.
- [x] 5.3 Run OpenSpec validation for `record-single-list-result-in-command-log`.
