## Why

Causeway 4 can replay imported command DTOs, but it cannot apply interaction-advisor policy during executor-driven invocation or carry bookmark identity changes from one replayed result into later replay inputs.
Now that recording, result metadata, safe-action publishing, and synthetic navigation are reconciled, D2, M1, and M2 are the next dependency-ordered foundation for reliable replay.

## What Changes

- Add immutable command-executor configuration selecting whether action and property visibility, usability, and validity advisors are enforced, invoked but ignored, or skipped during DTO execution.
- Add a commandlog replay-mapping SPI that looks up replacement bookmarks for replay targets and reference-valued action parameters and observes recorded-to-actual result bookmarks after successful replay.
- Remap a deep copy of each recorded command DTO so stored audit data, retries, and failures retain the imported target and parameter identities.
- Notify replay-mapping listeners inside the replay transaction only when both recorded and actual result bookmarks exist, propagating notification failures to roll back replay.
- Provide a conditionally configured in-memory listener with idempotent mapping, configurable conflict handling, and application-defined listener override.
- Keep persistent mappings, replayable-command participant UI, export remapping, unified manager behavior, reference data, reachability validation, workflows, and background gates outside this change.

## Capabilities

### New Capabilities

- `command-execution-advisor-policy`: Configurable interaction-advisor behavior for action and property execution through `CommandExecutorService`.
- `command-replay-mapping`: Replay-time target and reference-parameter remapping, result observation, and the conditional in-memory mapping listener.

### Modified Capabilities

None.

## Impact

- `core/config`: immutable command-executor and commandlog replay-mapping configuration plus generated configuration documentation.
- `core/runtimeservices`: action/property advisor checks in `CommandExecutorServiceDefault` with focused executor tests.
- `extensions/core/commandlog/applib`: public replay-mapping SPI, replay DTO remapping service, replay lifecycle integration, in-memory listener, Spring Boot 4 conditional wiring, and tests.
- Existing D1 command DTO deep copying and bookmark metadata are reused; existing C2/C3/C4 replay inputs and results gain mapping behavior without changing their publication or result formats.
