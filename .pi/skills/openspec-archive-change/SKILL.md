---
name: openspec-archive-change
description: Archive a completed change in the experimental workflow. Use when the user wants to finalize and archive a change after implementation is complete.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.6.0"
---

Archive a completed change in the experimental workflow.

## Local Workflow Customizations

The steps labeled **Local Customization** are repository-specific additions layered on top of the upstream OpenSpec skill. Keep them grouped when merging future upstream updates so the local workflow policy stays easy to review and reapply.

**Store selection:** If the user names a store (a store is a standalone OpenSpec repo registered on this machine) or the work lives in one, run `openspec store list --json` to discover registered store ids, then pass `--store <id>` on the commands that read or write specs and changes (`new change`, `status`, `instructions`, `list`, `show`, `validate`, `archive`, `doctor`, `context`). Other commands do not take the flag. Hints printed by commands already carry the flag; keep it on follow-ups. Without a store, commands act on the nearest local `openspec/` root.

**Input**: Optionally specify a change name. If omitted, check if it can be inferred from conversation context. If vague or ambiguous you MUST prompt for available changes.

**Steps**

1. **Select the change**

   Run `openspec list --json` to get available changes.

   - If a change name was provided, use it.
   - If no change name was provided and exactly one active change exists, use it automatically.
   - If multiple active changes exist, use the **AskUserQuestion tool** to let the user select.
   - If no active changes exist, stop and tell the user there is nothing to archive.

   Show only active changes (not already archived) when prompting. Include the schema used for each change if available.

2. **Check artifact completion status**

   Run `openspec status --change "<name>" --json` to check artifact completion.

   Parse the JSON to understand:
   - `schemaName`: The workflow being used
   - `planningHome`, `changeRoot`, `artifactPaths`, and `actionContext`: path and scope context
   - `artifacts`: List of artifacts with their status (`done` or other)

   **If any artifacts are not `done`:**
   - Display warning listing incomplete artifacts
   - Use **AskUserQuestion tool** to confirm user wants to proceed
   - Proceed if user confirms

3. **Check task completion status**

   Read the tasks file (typically `tasks.md`) to check for incomplete tasks.

   Count tasks marked with `- [ ]` (incomplete) vs `- [x]` (complete).

   **If incomplete tasks found:**
   - Display warning showing count of incomplete tasks
   - Use **AskUserQuestion tool** to confirm user wants to proceed
   - Proceed if user confirms

   **If no tasks file exists:** Proceed without task-related warning.

4. **Local Customization: create a pre-archive commit when required**

   Inspect git status before syncing or moving the change.

   - If the selected change has no relevant uncommitted work, continue.
   - If the selected change has uncommitted implementation work, planning artifacts that missed the apply checkpoint, delta specs, or other directly related archive inputs, stage only relevant files and create a pre-archive commit before continuing.
   - If unrelated uncommitted work would be swept into that commit, pause and ask the user how to proceed rather than bundling unrelated changes.
   - This pre-archive commit is where any missing apply-to-archive checkpoint commit is caught up; do not add archive as an implementation task.

5. **Local Customization: run local build and test validation before archive when applicable**

   After any needed pre-archive commit and before syncing specs or moving the change:

   - Determine the applicable local validation commands for this repository.
   - Run the relevant local build and test checks when the repository provides them.
   - If both build and tests pass, continue.
   - If no local build or test workflow applies to this repository, note that and continue.
   - If one or more applicable checks fail, show a concise summary of the failures and use the **AskUserQuestion tool** to confirm whether the user wants to continue with archive anyway.

6. **Sync delta specs directly**

   Use `artifactPaths.specs.existingOutputPaths` from status JSON to check for delta specs. If none exist, proceed without sync prompt.

   **If delta specs exist:**
   - Compare each delta spec with its corresponding main spec at `openspec/specs/<capability>/spec.md`
   - Determine what changes will be applied (adds, modifications, removals, renames)
   - Apply those changes directly during this workflow using the same intelligent merging rules as `openspec-sync-specs`
   - Do **not** prompt to skip sync and do **not** launch a subagent for sync

7. **Perform the archive**

   Create an `archive` directory under `planningHome.changesDir` if it doesn't exist:
   ```bash
   mkdir -p "<planningHome.changesDir>/archive"
   ```

   Generate target name using current date: `YYYY-MM-DD-<change-name>`

   **Check if target already exists:**
   - If yes: Fail with error, suggest renaming existing archive or using different date
   - If no: Move `changeRoot` to the archive directory

   ```bash
   mv "<changeRoot>" "<planningHome.changesDir>/archive/YYYY-MM-DD-<name>"
   ```

8. **Local Customization: commit the archive result**

   After spec sync and the archive move complete, stage the resulting main-spec updates and archived change move, then create a post-archive commit.

9. **Display summary**

   Show archive completion summary including:
   - Change name
   - Schema that was used
   - Archive location
   - Build and test validation status (passed / failed but user confirmed / not applicable)
   - Whether specs were synced (if applicable)
   - Whether pre-archive and post-archive commits were created
   - Note about any warnings (incomplete artifacts/tasks)

**Output On Success**

```
## Archive Complete

**Change:** <change-name>
**Schema:** <schema-name>
**Archived to:** the archive path derived from `planningHome.changesDir`/YYYY-MM-DD-<name>/
**Specs:** ✓ Synced to main specs (or "No delta specs")

All artifacts complete. All tasks complete.
```

**Guardrails**
- Auto-select the change when there is exactly one active change; otherwise prompt
- Use artifact graph (openspec status --json) for completion checking
- Don't block archive on warnings - just inform and confirm
- Preserve .openspec.yaml when moving to archive (it moves with the directory)
- Show clear summary of what happened
- Always sync delta specs during archive; do not offer a skip path
- Do not use a subagent for archive-time spec sync
- Ask for confirmation before archiving when tasks remain incomplete
- Before archive, run applicable local build and test validation; ask for confirmation if an applicable check fails
- Create a pre-archive commit when the selected change has relevant uncommitted work, and a post-archive commit after sync and archive complete
- Do not treat commit/sync/archive workflow actions as implementation tasks
