## Why

Replayable command participants currently populate actual target and parameter bookmarks only when the command is already marked successful.
Users need target and parameter rows to use replay mapping lookup immediately so known mappings are visible before replay as well as after replay.

## What Changes

- Refine participant derivation so target participants ask replay mapping listeners for an actual bookmark whenever a mapping exists.
- Refine reference parameter participants to ask replay mapping listeners for an actual bookmark whenever a mapping exists.
- Populate the target or parameter actual bookmark when lookup returns a mapped bookmark.
- Leave actual bookmark empty for target and parameter participants when lookup returns no mapping and the command has not successfully replayed.
- Preserve the current successful replay fallback where unchanged target and parameter participants use their recorded bookmark as the actual bookmark.
- Keep result participant behaviour unchanged.
- Keep the replay mapping SPI signatures unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `replayable-command-remappings`: Refines target and parameter participants so available replay mappings populate their actual bookmarks independently of replay success state.

## Impact

- Affects `ReplayableCommand` participant derivation in `extensions/core/commandlog/applib`.
- Affects focused command log applib tests for participants.
- No persistence schema, public replay mapping SPI, layout, or result participant changes are expected.
