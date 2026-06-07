## Context

`ReplayableCommand` now exposes participants-associated `openTarget` and `openArgument` actions.
Result participants can expose actual result bookmarks and result objects after replay succeeds.
The participants collection should provide the same direct navigation affordance for actual results as it does for targets and arguments.

## Goals / Non-Goals

**Goals:**

- Add an `openResult` command-level mixin associated with the `participants` collection.
- Use action sequence `3` so it appears after `openTarget` and `openArgument`.
- Disable the action when no actual result object is available.
- Return the actual result object when a result participant resolves locally.

**Non-Goals:**

- Do not change result participant derivation or replay result mapping semantics.
- Do not add action parameters for result selection.
- Do not change the existing `openTarget` or `openArgument` action behaviour.
- Do not add persistence schema changes.

## Decisions

- Implement `openResult` as a separate mixin to keep target, argument, and result navigation actions independent and consistently associated with `participants`.
- Use sequence `3` because `openTarget` uses `1` and `openArgument` uses `2`.
- Disable based on actual result object availability rather than only actual bookmark availability because the action opens an object.
- Reuse `ReplayableCommandParticipant#getResult` for object lookup so the action follows the same best-effort resolution logic as the participant row.

## Risks / Trade-offs

- A result participant may have an actual bookmark that cannot resolve locally → The action remains disabled because there is no object to open.
- Multiple result participants are not expected by current participant derivation → The action can open the first available actual result object if implementation ever sees more than one.
