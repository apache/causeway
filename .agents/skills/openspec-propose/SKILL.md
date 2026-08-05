---
name: openspec-propose
description: Propose a new change with all artifacts generated in one step. Use when the user wants to quickly describe what they want to build and get a complete proposal with design, specs, and tasks ready for implementation.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.6.0"
---

Propose a new change - create the change and generate all artifacts in one step.

I'll create a change with artifacts:
- proposal.md (what & why)
- design.md (how)
- tasks.md (implementation steps)

When ready to implement, run /opsx:apply

---

## Local Workflow Customizations

The steps labeled **Local Customization** are repository-specific additions layered on top of the upstream OpenSpec skill. Keep them grouped when merging future upstream updates so the local workflow policy stays easy to review and reapply.

**Store selection:** If the user names a store (a store is a standalone OpenSpec repo registered on this machine) or the work lives in one, run `openspec store list --json` to discover registered store ids, then pass `--store <id>` on the commands that read or write specs and changes (`new change`, `status`, `instructions`, `list`, `show`, `validate`, `archive`, `doctor`, `context`). Other commands do not take the flag. Hints printed by commands already carry the flag; keep it on follow-ups. Without a store, commands act on the nearest local `openspec/` root.

**Input**: The user's request should include a change name (kebab-case) OR a description of what they want to build.

**Steps**

1. **If no clear input provided, ask what they want to build**

   Use the **AskUserQuestion tool** (open-ended, no preset options) to ask:
   > "What change do you want to work on? Describe what you want to build or fix."

   From their description, derive a kebab-case name (e.g., "add user authentication" → `add-user-auth`).

   **IMPORTANT**: Do NOT proceed without understanding what the user wants to build.

2. **Local Customization: enforce one active change at a time**
   ```bash
   openspec list --json
   ```
   - If no active changes exist, continue.
   - If an active change already exists with the same name, continue that change instead of creating another one.
   - If an active change exists with a different name, do **not** create a new proposal. Tell the user this workflow keeps one active OpenSpec change at a time and ask them to archive the existing change before starting a new one.

3. **Local Customization: clear pending workflow commit before new proposal when needed**

   Inspect git status before creating a new proposal.

   - If there is no active change and workflow-related OpenSpec files are uncommitted (for example previously synced specs or archived change moves), stage only those relevant files and commit them before proceeding.
   - If unrelated uncommitted work would be swept into that commit, pause and ask the user how to proceed rather than bundling unrelated changes.

4. **Local Customization: compact session before a new proposal when helpful**

   Before creating a new proposal (that is, no active change exists yet), prefer running `/compact` when conversation context is long or has drifted topics.

   - Keep this conditional, not mandatory.
   - If unresolved decisions or blockers are still only in chat and not captured in OpenSpec artifacts, capture those key points first, then compact.
   - If context is already concise and focused, proceed without compacting.

5. **Create the change directory (only when needed)**
   ```bash
   openspec new change "<name>"
   ```
   This creates a scaffolded change in the planning home resolved by the CLI with `.openspec.yaml`.

6. **Get the artifact build order**
   ```bash
   openspec status --change "<name>" --json
   ```
   Parse the JSON to get:
   - `applyRequires`: array of artifact IDs needed before implementation (e.g., `["tasks"]`)
   - `artifacts`: list of all artifacts with their status and dependencies
   - `planningHome`, `changeRoot`, `artifactPaths`, and `actionContext`: path and scope context. Use these instead of assuming repo-local paths.

7. **Create artifacts in sequence until apply-ready**

   Use the **TodoWrite tool** to track progress through the artifacts.

   Loop through artifacts in dependency order (artifacts with no pending dependencies first):

   a. **For each artifact that is `ready` (dependencies satisfied)**:
      - Get instructions:
        ```bash
        openspec instructions <artifact-id> --change "<name>" --json
        ```
      - The instructions JSON includes:
        - `context`: Project background (constraints for you - do NOT include in output)
        - `rules`: Artifact-specific rules (constraints for you - do NOT include in output)
        - `template`: The structure to use for your output file
        - `instruction`: Schema-specific guidance for this artifact type
        - `resolvedOutputPath`: Resolved path or pattern to write the artifact
        - `dependencies`: Completed artifacts to read for context
      - Read any completed dependency files for context
      - Create the artifact file using `template` as the structure and write it to `resolvedOutputPath`
      - Apply `context` and `rules` as constraints - but do NOT copy them into the file
      - Show brief progress: "Created <artifact-id>"

   b. **Continue until all `applyRequires` artifacts are complete**
      - After creating each artifact, re-run `openspec status --change "<name>" --json`
      - Check if every artifact ID in `applyRequires` has `status: "done"` in the artifacts array
      - Stop when all `applyRequires` artifacts are done

   c. **If an artifact requires user input** (unclear context):
      - Use **AskUserQuestion tool** to clarify
      - Then continue with creation

8. **Show final status**
   ```bash
   openspec status --change "<name>"
   ```

**Output**

After completing all artifacts, summarize:
- Change name and location
- List of artifacts created with brief descriptions
- What's ready: "All artifacts created! Ready for implementation."
- Prompt: "Run `/opsx:apply` or ask me to implement to start working on the tasks."

**Artifact Creation Guidelines**

- Follow the `instruction` field from `openspec instructions` for each artifact type
- The schema defines what each artifact should contain - follow it
- Read dependency artifacts for context before creating new ones
- Use `template` as the structure for your output file - fill in its sections
- For `tasks` artifacts, include only implementation work items; never include workflow operations such as commits, sync, archive, or running propose/apply/archive commands
- **IMPORTANT**: `context` and `rules` are constraints for YOU, not content for the file
  - Do NOT copy `<context>`, `<rules>`, `<project_context>` blocks into the artifact
  - These guide what you write, but should never appear in the output

**Guardrails**
- Create ALL artifacts needed for implementation (as defined by schema's `apply.requires`)
- Always read dependency artifacts before creating a new one
- Never create a second active OpenSpec change while another remains unarchived
- If no active change exists but OpenSpec workflow files are uncommitted, commit that prior workflow checkpoint work before creating a new proposal
- Before starting a new proposal, prefer `/compact` when context is long or topic-drifted; capture unresolved decisions in artifacts first
- If context is critically unclear, ask the user - but prefer making reasonable decisions to keep momentum
- If the requested change already exists as the only active change, continue it instead of creating a duplicate
- Verify each artifact file exists after writing before proceeding to next
