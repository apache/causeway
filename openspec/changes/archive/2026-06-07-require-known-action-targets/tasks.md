## 1. Locate Export Integration Points

- [x] 1.1 Identify how `CommandExportManager` stores and exposes its baseline timestamp or state.
- [x] 1.2 Identify where `CommandExportManager_exportSelected` resolves, sorts, validates, and exports selected `CommandLogEntry` records.
- [x] 1.3 Identify the command DTO target bookmark extraction API used by replay/export for action targets.
- [x] 1.4 Identify the metamodel or service registry API that reliably classifies `@DomainService` menu service action targets as export roots.

## 2. Implement Export Known Target Validation

- [x] 2.1 Add a focused known-target validator in the command-log applib export/replay layer.
- [x] 2.2 Make the validator operate on the export manager baseline and selected export sequence, not on command recording completion.
- [x] 2.3 Accept action commands whose targets are menu domain service roots.
- [x] 2.4 Accept action commands whose target bookmark appears as the result bookmark of an earlier command at or after the export manager baseline.
- [x] 2.5 Reject unknown non-root targets during export validation without blocking recording of future commands.
- [x] 2.6 Ensure the validation message identifies the failing command and unknown target bookmark.

## 3. Integrate Navigation Results

- [x] 3.1 Ensure logged safe action result bookmarks within the baseline-bounded export range are visible to the known-target validator as known targets for later selected commands.
- [x] 3.2 Ensure synthetic parented collection navigate-to command results establish known child targets for later selected commands.
- [x] 3.3 Ensure synthetic scalar reference navigate-to command results establish known referenced-object targets for later selected commands.
- [x] 3.4 Ensure safe actions without result bookmarks do not establish unrelated known targets.

## 4. Guard Export of Invalid Sequences

- [x] 4.1 Wire export-time validation into `CommandExportManager_exportSelected.validateSelected(...)` so invalid selections are disabled before invocation.
- [x] 4.2 Also guard `CommandExportManager_exportSelected.act(...)` so callers that bypass UI validation cannot emit invalid YAML.
- [x] 4.3 Ensure export validation uses the same ordering semantics as command export and does not fabricate path metadata.
- [x] 4.4 Surface an export validation error that identifies the unknown target bookmark and the affected command.

## 5. Tests and Documentation

- [x] 5.1 Add unit tests for root menu service targets, previously returned targets, unknown targets, baseline exclusion, and locally resolvable but unreached targets.
- [x] 5.2 Add tests proving safe finder actions and synthetic navigate-to actions establish known targets through result bookmarks within the export manager baseline.
- [x] 5.3 Add export action tests for valid reachable selections and invalid selected command log data.
- [x] 5.4 Add a test proving recording is not blocked by known-target validation.
- [x] 5.5 Run targeted commandlog applib tests and any affected metamodel tests.
- [x] 5.6 Run `openspec status --change require-known-action-targets` and address any artifact validation issues.
