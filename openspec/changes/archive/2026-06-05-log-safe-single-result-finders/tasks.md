## 1. Discovery and Configuration

- [x] 1.1 Locate the existing command publishing, command DTO creation, and command log subscriber paths for action invocations.
- [x] 1.2 Identify the existing Causeway configuration namespace that should own the safe action command publishing property.
- [x] 1.3 Add the disabled-by-default boolean configuration property and bind it into the relevant runtime service or subscriber.
- [x] 1.4 Add documentation metadata or configuration property descriptors if this repository maintains generated config docs.

## 2. Safe Finder Eligibility

- [x] 2.1 Implement command publishing facet eligibility for safe action invocations when the new property is enabled.
- [x] 2.2 Avoid using result shape as part of safe action command publishing eligibility.
- [x] 2.3 Allow null, list, collection, array, void, scalar, and otherwise non-bookmarkable results to be command logged when the safe action publishing property is enabled.
- [x] 2.4 Ensure idempotent and non-idempotent actions continue to follow existing command publishing behavior rather than the safe action command publishing property.
- [x] 2.5 Prevent duplicate command log entries when a safe action is already explicitly command-published.

## 3. Command Log Entry Creation

- [x] 3.1 Create or adapt the command DTO for eligible safe action invocations so it can be persisted and replayed.
- [x] 3.2 Persist an eligible safe action invocation as a command log entry when the property is enabled.
- [x] 3.3 Store the returned object's bookmark as the command log entry result when available.
- [x] 3.4 Keep disabled-by-default behavior unchanged for safe actions when the property is absent or false.

## 4. Export and Replay Integration

- [x] 4.1 Verify command export includes logged safe finder entries in replay order.
- [x] 4.2 Verify exported safe finder entries include returned object metadata when a result bookmark is present.
- [x] 4.3 Verify replay executes imported safe action command entries.
- [x] 4.4 Verify replay result mapping notifications are emitted for safe finder entries with recorded and actual result bookmarks.
- [x] 4.5 Verify later replay command targets and reference parameters can be remapped using mappings learned from safe finder replay results.

## 5. Tests and Documentation

- [x] 5.1 Add unit tests for the new configuration property default and enabled values.
- [x] 5.2 Add tests for safe action command publishing eligibility and result shapes without exclusion.
- [x] 5.3 Add tests ensuring explicitly command-published safe actions are not duplicated.
- [x] 5.4 Add export tests covering safe action command entries and returned object metadata.
- [x] 5.5 Add replay tests covering finder result mapping notifications and subsequent input remapping.
- [x] 5.6 Update user-facing documentation for regression recording usage and the new configuration property.

## 6. Validation

- [x] 6.1 Run the relevant commandlog applib unit tests.
- [x] 6.2 Run the relevant commandlog persistence integration tests for JDO and JPA if affected.
- [x] 6.3 Run OpenSpec validation for `log-safe-single-result-finders`.
