## Why

Command-log recording support is intended to capture replayable user edits, but property edits can currently be skipped when the property command-publishing facet reports publishing disabled.
That leaves recordings incomplete even though replay support needs those property changes persisted as command log entries.

## What Changes

- Enable command publishing for property edits when command-log recording support is configured as `ENABLED` for the application metamodel.
- Persist command log entries for recorded property edits even when `@Property(commandPublishing)` is absent, not specified, or explicitly `DISABLED`.
- Preserve existing suppression behavior for targets that opt out of command recording.
- Preserve existing behavior when command-log recording support is `DISABLED`.

## Capabilities

### New Capabilities
- `property-edit-recording`: Defines how command-log recording support captures property edits as replayable command log entries.

### Modified Capabilities
- `command-recording-suppression`: Clarifies that suppression remains authoritative for property edits even after recording support enables property edit publishing.

## Impact

- Affects command publishing facet evaluation for properties, especially `CommandPublishingFacet#isPublishingEnabled` and recording-support overrides.
- Affects command-log persistence for property edit interactions and replay/export workflows that depend on complete command sequences.
- Adds metamodel coverage and JDO/JPA command-log regression coverage for property edits with recording support enabled and disabled.
- No new dependencies or public API removals are expected.
