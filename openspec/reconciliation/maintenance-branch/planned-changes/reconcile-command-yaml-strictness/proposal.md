## Why

Child change 8 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-11**; third-opinion
F9, meta-analysis 2 A9 — MED, code-confirmed with a runtime probe). Implements the importer half of product
decision **D-D** (MA-15: keep the legacy managers, harden the legacy importer).

`main` already has a strict replay-import decoder, `CommandDtoUtils.fromYamlForReplay(...)`, which accepts the
wrapped `CommandExportDto` multi-document form and the plain `CommandDto` multi-document form, rejects a YAML
list root, and **fails on unparseable input** (`api/applib/.../CommandDtoUtils.java:195-233`,
`ifFailureFail()`). The unified importer uses it (`CommandManager_importCommands.java:64`).

But the retained **legacy** importer still uses the lenient general-purpose `CommandDtoUtils.fromYaml(...)`, which
does `YamlUtils.tryReadAsList(...).getValue().orElseGet(Collections::emptyList)` (`CommandDtoUtils.java:178-182`)
— a failed parse `Try` has no value, so **malformed input becomes an empty list, silently**:

```
// extensions/core/commandlog/applib/.../dom/replay/CommandReplayManager.java:152-160 (@Action restrictTo=PROTOTYPING, file upload)
final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);   // lenient — swallows parse errors
commandDtos.forEach(commandLogEntryRepository()::saveForReplay);
```

A malformed YAML upload through the legacy replay manager therefore silently "imports zero commands" and reports
success. (`CommandDtoUtils` and both importers are byte-identical between the audited head and current HEAD.)

## What Changes

- Route the legacy `CommandReplayManager.importCommands` through the strict `fromYamlForReplay` decoder (the same
  one the unified importer uses), so it accepts the canonical wrapped and legacy multi-document forms, rejects a
  YAML list root, and **fails on unparseable input instead of importing nothing**.
- Adjust the legacy importer to persist each decoded carrier's embedded command (`saveForReplay(carrier.getCommand())`)
  and derive its `moveBaselineToOldest` timestamp from the decoded commands, mirroring the unified importer.
- Do not change the general-purpose public `CommandDtoUtils.fromYaml(...)` API contract (kept per the existing
  spec's "existing plain `CommandDto` YAML APIs … MUST remain unchanged"); this change is scoped to the replay
  **import entry points**.

## Capabilities

### Modified Capabilities

- `command-result-metadata`: every replay import entry point — the unified importer **and** the retained legacy
  replay-manager importer — uses the strict decoder, so malformed replay YAML is rejected rather than silently
  treated as an empty import.

## Impact

- Affects commandlog applib `CommandReplayManager.importCommands` (decoder swap + carrier handling). No change to
  `api/applib` `CommandDtoUtils` (both decoders already exist).
- Closes the last live surface that silently swallows a malformed upload; completes decision D-D (the legacy
  managers themselves are retained as documented v4 compatibility).
- Coordinates with slice 4 (`reconcile-replay-import-idempotency`), which ADDs to the same `command-result-metadata`
  capability; the two touch different requirements and are applied sequentially.
- Requires coverage: a malformed YAML upload through the legacy importer now fails with a clear error (imports
  nothing *and reports failure*, not success); a valid canonical/legacy stream imports correctly through the
  legacy path.
