---
name: openspec-oneshot
description: Run a full OpenSpec workflow from one request. Use when the user wants proposal, implementation, commits, spec sync, and archive handled as one resumable flow.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: local
  version: "1.0"
  inventedHere: true
---

Run a one-shot OpenSpec workflow.

This repository-defined workflow is published as the `openspec-oneshot` skill and remains a local customization layered on top of upstream OpenSpec patterns.

The goal is to take one request and drive it through the normal OpenSpec workflow:
1. Create or continue the change proposal
2. Commit planning artifacts when apply is about to start
3. Apply the change and implement tasks
4. Sync delta specs and archive the change (including pre-archive checkpoint commit when needed)
5. Commit the archive result

If any stage is blocked, stop there, explain the blocker, and let the user resolve it. When the skill is run again, resume from the next incomplete stage instead of starting over.

## Local Workflow Customizations

The steps labeled **Local Customization** are repository-specific additions layered on top of the upstream OpenSpec workflow. Keep them grouped when merging future upstream updates so the local workflow policy stays easy to review and reapply.

**Store selection:** If the user names a store (a store is a standalone OpenSpec repo registered on this machine) or the work lives in one, run `openspec store list --json` to discover registered store ids, then pass `--store <id>` on the commands that read or write specs and changes (`new change`, `status`, `instructions`, `list`, `show`, `validate`, `archive`, `doctor`, `context`). Other commands do not take the flag. Hints printed by commands already carry the flag; keep it on follow-ups. Without a store, commands act on the nearest local `openspec/` root.

**Input:** The user may provide freeform change text, a change name, or simply ask to resume the oneshot flow.

## Workflow

### 1. Resolve whether this is a new run or a resume

Start with:
```bash
openspec list --json
```

- If there is exactly one active change and the user asked to resume, continue that change.
- If there is exactly one active change and the user gave no new change description, treat it as a resume.
- If there is exactly one active change and the user gave new change text for a different effort, stop and tell them this workflow keeps one active change at a time; ask them to archive the existing change first.
- If there are multiple active changes, use the **AskUserQuestion tool** to let the user choose which change to continue.
- If there is no active change, treat the request as a fresh oneshot run and derive a kebab-case change name from the user's request text. If the request is too vague, ask:
  > "What change do you want to work on? Describe what you want to build or fix."

Always announce which change is being used and whether the skill is starting fresh or resuming.

### 1.5 Local Customization: compact session before a fresh oneshot run when helpful

Before starting a **fresh** oneshot run (no active change), prefer running `/compact` when conversation context is long or has drifted topics.

- Keep this conditional, not mandatory.
- If unresolved decisions or blockers are still only in chat and not captured in OpenSpec artifacts, capture those key points first, then compact.
- If context is already concise and focused, proceed without compacting.

### 2. For a fresh run, create planning artifacts until apply-ready

Create the change if needed:
```bash
openspec new change "<name>"
```

Then use the normal proposal flow:
```bash
openspec status --change "<name>" --json
openspec instructions <artifact-id> --change "<name>" --json
```

- Create every required planning artifact until the change is apply-ready.
- Read dependency artifacts before creating later artifacts.
- Use the user's original freeform text as the starting point for the proposal.
- If clarification is required, pause and ask the user rather than inventing a critical requirement.

### 3. Determine the current stage before acting

Inspect the current state:
```bash
openspec status --change "<name>" --json
openspec instructions apply --change "<name>" --json
git --no-pager status --short
```

Use that information to decide the next stage:
- **Planning incomplete** → finish missing artifacts first.
- **Planning complete, tasks not complete** → enter apply flow.
- **Tasks complete, change still active** → enter archive flow (including any needed pre-archive checkpoint commit).
- **No active change remains** → report that the oneshot flow is already complete.

### 4. Local Customization: commit planning artifacts before apply

Before implementation begins:
- Inspect git status.
- If the selected change's planning artifacts are uncommitted, stage only those relevant files and create a commit before proceeding into apply.
- If unrelated uncommitted work would be swept into the commit, stop and ask the user how to proceed.

### 5. Apply the change

Use the normal apply workflow:
```bash
openspec instructions apply --change "<name>" --json
```

- Read all `contextFiles` returned by the CLI.
- Work pending tasks one by one.
- Keep changes minimal and focused.
- Mark each completed task in the tasks file immediately.
- Continue until all tasks are complete or a blocker is hit.

### 6. Archive the change with direct spec sync

Use the same archive-time behavior as the normal archive workflow:
- Inspect `artifactPaths.specs.existingOutputPaths` from `openspec status --change "<name>" --json`.
- If delta specs exist, sync them directly into `openspec/specs/<capability>/spec.md`.
- Inspect tasks for incomplete checkboxes; if any remain, warn and ask for confirmation before archiving.
- Create a pre-archive commit when relevant archive inputs are uncommitted. This includes uncommitted implementation work when apply finished without a checkpoint commit.
- After any needed pre-archive commit, run the applicable local build and test checks for the repository when they exist.
- If an applicable build or test check fails, warn and ask for confirmation before archiving anyway.
- Move the change directory into `openspec\changes\archive\YYYY-MM-DD-<name>\`.
- Create the post-archive commit for the synced specs and archived change move.

### 7. Pause cleanly on blockers

Stop and wait for the user when:
- proposal creation needs clarification
- an apply task is unclear
- implementation reveals a design issue that should update the artifacts first
- a required commit would capture unrelated work
- a git or OpenSpec command fails
- archive cannot proceed safely

When pausing, explain:
- the current change
- the current stage
- what blocked progress
- what needs to be resolved before rerunning the skill

### 8. Resume from the next incomplete stage

When the user reruns the skill after resolving a blocker:
- inspect the active change with `openspec list --json`, `openspec status --change "<name>" --json`, `openspec instructions apply --change "<name>" --json`, and `git --no-pager status --short`
- determine the first unfinished stage
- continue from there instead of recreating proposal artifacts or reopening completed tasks

Examples:
- Planning artifacts complete but tasks remain unchecked → resume in apply
- Tasks complete but implementation changes are uncommitted → continue to archive and create the pre-archive checkpoint commit before sync/move
- Specs synced but change directory not yet archived → resume in archive

## Output Style

During execution, show the stage explicitly:

```text
## OpenSpec One-Shot: <change-name>

Stage: Planning
...

Stage: Apply
...

Stage: Archive
...
```

On pause, explain where the workflow stopped and that rerunning `openspec-oneshot` will resume from that stage once the blocker is resolved.

On success, summarize:
- change name
- commits created
- whether specs were synced
- archive location

## Guardrails

- Follow the normal OpenSpec workflow rather than inventing a parallel lifecycle.
- Keep one active change at a time.
- Commit only at the workflow checkpoints the repository expects (before apply when needed, before archive when needed, and after archive completes).
- Before starting a fresh oneshot run, prefer `/compact` when context is long or topic-drifted; capture unresolved decisions in artifacts first.
- Before archive, run applicable local build and test validation and require confirmation to continue when those checks fail.
- Always sync delta specs during archive; do not offer a skip path.
- Never treat commit/sync/archive workflow operations as implementation tasks.
- Pause on blockers instead of guessing.
- Resume from the current active change state instead of creating duplicate changes.
