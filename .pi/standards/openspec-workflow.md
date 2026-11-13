# OpenSpec Workflow Guidance

These guidelines apply **only when a repository chooses to use OpenSpec**. They do not require OpenSpec as the default workflow for all work in the repository.

## Workflow Rules

1. Keep **one active change at a time**. Archive the current change before starting a new proposal.
2. Treat **phase boundaries as commit checkpoints**:
   - commit proposal/planning artifacts before moving into apply
   - commit before archive actions when the change has relevant uncommitted work
   - commit again after spec sync and archive actions complete
3. OpenSpec **implementation tasks must stay implementation-only**:
   - do not include workflow actions such as git commits, spec sync, archive, or running propose/apply/archive commands as tasks
4. During archive, **always sync delta specs into main specs**. Do not skip sync as part of the normal archive path.
5. During archive, if there is only one active change, **use it without prompting**.
6. During archive, if tasks remain incomplete, **ask for confirmation** before proceeding.
7. Prior to archive, **ask the user if they want to build and ensure tests pass**. If affirmative, then perform these actions and proceed with the archive thereafter.  If the build or tests fail, **ask for confirmation** before proceeding with the archive.
8. Before starting a **new** proposal or a **fresh** oneshot run, prefer a **session compact** when the thread is long or has drifted topics. Keep this conditional (not mandatory): if there are unresolved decisions or blockers that are not yet captured in OpenSpec artifacts, capture those first, then compact.

## Reusable Skill Customization Pattern

The OpenSpec-derived skills in this repository carry local workflow behavior in clearly labeled sections named `Local Workflow Customizations`.

That convention exists to make future merges against upstream OpenSpec skill updates easier:

- upstream/base behavior remains recognizable
- repository-specific workflow rules stay grouped together
- maintainers can review or reapply local policy without hunting through the whole file

## Intended Consumption

- **This repository** uses these rules through its local OpenSpec-derived skills and repo instructions.
- **Consuming repositories** can pick up the same behavior by syncing the shared OpenSpec-derived skills and reading this standards document from the mounted library.
