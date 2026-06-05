## Why

The command-log recording support currently uses two separate boolean properties for related regression recording behavior.
A single enum property is clearer for users, keeps recording support configuration centralized, and leaves room for future recording modes beyond a binary boolean pair.

## What Changes

- Replace `causeway.extensions.command-log.safe-action-command-publishing` with `causeway.extensions.command-log.recording-support`.
- Replace `causeway.extensions.command-log.parented-collection-selector-actions-enabled` with the same `recording-support` property.
- Define `recording-support` as an enum with values `ENABLED` and `DISABLED`.
- Default `recording-support` to `DISABLED`.
- When `recording-support` is `ENABLED`, enable safe action command publishing and synthetic parented collection selector action creation.
- When `recording-support` is `DISABLED`, retain the current disabled-by-default behavior for both features.
- **BREAKING**: remove or stop using the two boolean command-log properties in favor of the new enum property.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `safe-action-command-publishing`: replace the safe action publishing boolean switch with enum-based command-log recording support.
- `parented-collection-selector-actions`: replace the selector action creation boolean switch with enum-based command-log recording support.

## Impact

- Affects `CausewayConfiguration.Extensions.CommandLog` configuration shape and generated external configuration keys.
- Affects metamodel selector action gating.
- Affects command publishing facet enablement for safe actions.
- Affects command-log extension documentation.
- Requires tests to verify `recording-support=DISABLED` disables both behaviors and `recording-support=ENABLED` enables both behaviors.
