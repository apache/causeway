## 1. Discovery and Configuration

- [ ] 1.1 Locate the existing command publishing, command DTO creation, and command log subscriber paths for action invocations.
- [ ] 1.2 Identify the existing Causeway configuration namespace that should own the safe single-result finder logging property.
- [ ] 1.3 Add the disabled-by-default boolean configuration property and bind it into the relevant runtime service or subscriber.
- [ ] 1.4 Add documentation metadata or configuration property descriptors if this repository maintains generated config docs.

## 2. Safe Finder Eligibility

- [ ] 2.1 Implement eligibility detection for safe action invocations when the new property is enabled.
- [ ] 2.2 Detect whether the completed result is exactly one bookmarkable domain object.
- [ ] 2.3 Exclude null, list, collection, array, void, scalar, and otherwise non-single-bookmarkable results.
- [ ] 2.4 Ensure idempotent and non-idempotent actions continue to follow existing command publishing behavior rather than the safe finder logging path.
- [ ] 2.5 Prevent duplicate command log entries when a safe action is already explicitly command-published.

## 3. Command Log Entry Creation

- [ ] 3.1 Create or adapt the command DTO for eligible safe finder invocations so it can be persisted and replayed.
- [ ] 3.2 Persist an eligible safe finder invocation as a command log entry when the property is enabled.
- [ ] 3.3 Store the returned object's bookmark as the command log entry result.
- [ ] 3.4 Keep disabled-by-default behavior unchanged for safe actions when the property is absent or false.

## 4. Export and Replay Integration

- [ ] 4.1 Verify command export includes logged safe finder entries in replay order.
- [ ] 4.2 Verify exported safe finder entries include returned object metadata when a result bookmark is present.
- [ ] 4.3 Verify replay executes imported safe finder command entries.
- [ ] 4.4 Verify replay result mapping notifications are emitted for safe finder entries with recorded and actual result bookmarks.
- [ ] 4.5 Verify later replay command targets and reference parameters can be remapped using mappings learned from safe finder replay results.

## 5. Tests and Documentation

- [ ] 5.1 Add unit tests for the new configuration property default and enabled values.
- [ ] 5.2 Add unit tests for safe finder eligibility and excluded result shapes.
- [ ] 5.3 Add tests ensuring explicitly command-published safe actions are not duplicated.
- [ ] 5.4 Add export tests covering safe finder command entries and returned object metadata.
- [ ] 5.5 Add replay tests covering finder result mapping notifications and subsequent input remapping.
- [ ] 5.6 Update user-facing documentation for regression recording usage and the new configuration property.

## 6. Validation

- [ ] 6.1 Run the relevant commandlog applib unit tests.
- [ ] 6.2 Run the relevant commandlog persistence integration tests for JDO and JPA if affected.
- [ ] 6.3 Run OpenSpec validation for `log-safe-single-result-finders`.
