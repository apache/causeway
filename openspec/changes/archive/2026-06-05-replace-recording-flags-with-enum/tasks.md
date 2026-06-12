## 1. Configuration Model

- [x] 1.1 Add `RecordingSupport` enum with `ENABLED` and `DISABLED` values under `CausewayConfiguration.Extensions.CommandLog`.
- [x] 1.2 Add `recordingSupport` configuration property defaulting to `DISABLED`.
- [x] 1.3 Remove or stop using `safeActionCommandPublishing` and `parentedCollectionSelectorActionsEnabled` boolean configuration properties.
- [x] 1.4 Update generated configuration documentation to describe `causeway.extensions.command-log.recording-support` and remove the old boolean properties.

## 2. Behavior Wiring

- [x] 2.1 Update safe action command publishing facet logic to enable safe action command publishing when `recording-support=ENABLED`.
- [x] 2.2 Update parented collection selector action creation gating to synthesize selector actions when `recording-support=ENABLED`.
- [x] 2.3 Update all tests and helper code that configure the old boolean properties to use `recordingSupport`.

## 3. Test Coverage

- [x] 3.1 Verify default `recording-support=DISABLED` does not publish safe action commands and does not synthesize selector actions.
- [x] 3.2 Verify `recording-support=ENABLED` publishes safe action commands and synthesizes selector actions.
- [x] 3.3 Verify selector action command publishing tests use `recording-support=ENABLED` rather than separate booleans.

## 4. Validation

- [x] 4.1 Run focused command-log and selector action tests.
- [x] 4.2 Run `openspec validate replace-recording-flags-with-enum --strict`.
