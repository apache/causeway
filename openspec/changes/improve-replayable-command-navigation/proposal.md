## Why

Replayable command records currently hide useful recorded participants in some states even though targets and arguments remain meaningful while reviewing recorded or exported commands.
Users also need faster row-by-row navigation and clearer table feedback about which commands produced a result.

## What Changes

- Make replayable command participant target and argument links available for recorded commands whose replay state is `UNDEFINED` or `EXPORTED`.
- Keep target links available for participants whose recorded target is a domain service, regardless of replay state.
- Add `previous` and `next` row actions on `ReplayableCommand` to navigate through the current command log ordering.
- Disable `previous` at the first command and `next` at the last command.
- Add a boolean `hasResult` property on `ReplayableCommand` that indicates whether the wrapped command log entry stores a result bookmark.
- Show `hasResult` in replayable command tables before the existing exportability indicator.

## Capabilities

### New Capabilities
- `replayable-command-participant-availability`: Defines when replayable command participant target and argument objects are available from recorded command data.
- `replayable-command-navigation`: Defines row-level previous and next navigation for replayable commands.
- `replayable-command-result-presence`: Defines result-presence reporting for replayable commands and its table placement.

### Modified Capabilities
- `replayable-command-exportability`: Updates table-column ordering expectations so result presence appears before exportability.

## Impact

- Affects the command-log replay view models in `extensions/core/commandlog/applib`.
- Affects UI metadata for `ReplayableCommand` and `ReplayableCommandParticipant` properties and actions.
- May require tests around participant lookup, command navigation boundaries, result presence, and table property ordering.
