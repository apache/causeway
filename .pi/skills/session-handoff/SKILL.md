---
name: session-handoff
description: Summarize the current coding session into SESSION_HANDOFF.md at the repository root so a future session can quickly load the most relevant context. Use when the user asks to write, update, create, or refresh a session handoff, session summary, or SESSION_HANDOFF.md.
license: MIT
compatibility: Requires repository root access and git CLI.
---

# Session Handoff

Create or refresh `SESSION_HANDOFF.md` at the repository root.
The purpose is to let a new session quickly learn the most relevant current context without rereading the whole conversation.

## Inputs

The user may provide additional focus areas or instructions.
If they do, prioritize those details in the handoff.
If no focus is provided, infer the relevant context from the current session, git state, OpenSpec state, recent commands, and important decisions.

## Steps

1. Identify the repository root.

   Run:
   ```bash
   git rev-parse --show-toplevel
   ```

2. Gather concise state.

   Use commands such as:
   ```bash
   git branch --show-current
   git status --short
   git log --oneline -5
   openspec list --json
   ```

   If OpenSpec is relevant, also inspect the active or recently archived change:
   ```bash
   openspec status --change "<change>" --json
   ```

   Use context-mode tools for large outputs.
   Do not paste large raw command output into the handoff.

3. Read the existing `SESSION_HANDOFF.md` if present.

   Preserve any still-relevant information.
   Remove stale information that has been resolved or superseded.

4. Write a concise handoff.

   Use the template below.
   Keep it practical and action-oriented.
   Prefer bullets for scanability.
   Follow the repository documentation style: one sentence per line for prose.

5. Verify the file exists and briefly summarize what was written.

## Template

```markdown
# Session Handoff

## Current branch

- `<branch-name>`

## Current status

- <working tree status, active change, or archived/completed status>
- <validation/test status if known>

## Key decisions and context

- <decision or context item>
- <decision or context item>

## Files changed or important paths

- `<path>` — <why it matters>
- `<path>` — <why it matters>

## Commands already run

- `<command>` — <result>
- `<command>` — <result>

## Remaining work / next steps

- <next action or "None known">

## Notes for the next session

- <gotchas, user preferences, or implementation constraints>
```

## Quality bar

The handoff should answer these questions for a new session:

- What branch and change are we on?
- What has been implemented or decided?
- What files matter most?
- What validations have already run?
- What remains to do?
- What should the next assistant avoid redoing?

## Guardrails

- Do not include secrets, tokens, or private credentials.
- Do not include huge diffs or full command logs.
- Do not claim tests passed unless they were actually run in this session or visible in reliable project state.
- If the working tree is dirty, say exactly what is dirty at a high level.
- If context is uncertain, label it as uncertain rather than guessing.
