## 1. Locate Parameter Validation Inputs

- [x] 1.1 Identify where `CommandExportKnownTargetValidator` extracts selected command targets and result bookmarks.
- [x] 1.2 Identify the `ActionDto` parameter API used to enumerate command reference parameters.
- [x] 1.3 Identify the bookmark conversion API for parameter reference OIDs.
- [x] 1.4 Confirm current export validation ordering and baseline behavior for adding known result bookmarks.

## 2. Extend Known Participant Validation

- [x] 2.1 Extend the export known-target validator to model both targets and reference parameters as export participants.
- [x] 2.2 Validate action reference parameters using the same known bookmark set as action targets.
- [x] 2.3 Accept reference parameters whose bookmarks are menu domain service roots.
- [x] 2.4 Accept reference parameters whose bookmarks were produced as earlier command results at or after the export manager baseline.
- [x] 2.5 Reject unknown reference parameters without changing command recording behavior.
- [x] 2.6 Ensure non-reference parameters are ignored by export path validation.

## 3. Improve Validation Messages

- [x] 3.1 Include the failing command identity in unknown reference parameter validation messages.
- [x] 3.2 Include the parameter name or a deterministic fallback label in unknown reference parameter validation messages.
- [x] 3.3 Include the unknown parameter bookmark and guidance to include an earlier navigation or finder command.
- [x] 3.4 Preserve existing unknown target validation message behavior for target failures.

## 4. Preserve Export Guarding

- [x] 4.1 Ensure `CommandExportManager_exportSelected.validateSelected(...)` rejects invalid selected commands with unknown reference parameters.
- [x] 4.2 Ensure `CommandExportManager_exportSelected.act(...)` guards against bypassed UI validation for unknown reference parameters.
- [x] 4.3 Ensure validation still uses the same sorted selected command list as YAML export.
- [x] 4.4 Ensure command results are added to the known set only after validating the current command.

## 5. Tests and Verification

- [x] 5.1 Add unit tests for reachable reference parameters, unknown reference parameters, root service reference parameters, later-result rejection, and baseline exclusion.
- [x] 5.2 Add unit tests proving scalar and non-reference parameters are ignored by export path validation.
- [x] 5.3 Add export action tests for invalid selected commands with unknown reference parameters.
- [x] 5.4 Add a test proving command recording remains unaffected by parameter knownness validation.
- [x] 5.5 Run targeted commandlog applib tests for the validator and export action.
- [x] 5.6 Run `openspec status --change require-known-action-parameters` and address any artifact validation issues.
