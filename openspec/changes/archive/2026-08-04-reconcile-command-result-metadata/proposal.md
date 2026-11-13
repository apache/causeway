## Why

Causeway 4 records command results only for persistable scalar entities and its command YAML utilities expose only the legacy `CommandDto` shape.
The maintenance line adds portable result bookmarks and broader result capture that later replay mapping, projections, validation, and export/import workflows require.

## What Changes

- Expand command result capture to retain the bookmark of a bookmarkable scalar entity or view model.
- Treat a framework-supported result container containing exactly one bookmarkable object as the same single command result, while leaving empty, multi-object, and non-bookmarkable results unset.
- Preserve an existing command result when later nested or mixin execution also returns a value.
- Add result-bearing command transfer types for an embedded `CommandDto` and optional bookmark metadata represented as `type` and `id`.
- Add deep-copy support for command DTOs so later replay processing can adapt execution input without mutating recorded command data.
- Add multi-document YAML serialization support for result-bearing command transfer values while retaining the existing `CommandDto` YAML APIs.
- Defer recording-aware publishing, synthetic navigation, replay mapping, replayable-command presentation, reachability validation, and validated export/import workflows to their dependency-ordered changes.

## Capabilities

### New Capabilities

- `command-result-metadata`: Captures one bookmarkable command result and provides portable result-bearing command DTO, copy, and YAML foundations.

### Modified Capabilities

None.

## Impact

- `core/runtimeservices`: command result extraction during action execution, including singleton result containers and bookmarkable view models.
- `api/applib`: public command DTO utility types, bookmark metadata conversion, deep-copy support, and YAML serialization foundations.
- `extensions/core/commandlog/applib`: focused integration with command log result data where needed to verify the transfer contract without introducing later manager workflows.
- Public API additions require API documentation and compatibility tests.
- Existing command publishing eligibility and legacy `CommandDto` YAML behaviour remain unchanged.
