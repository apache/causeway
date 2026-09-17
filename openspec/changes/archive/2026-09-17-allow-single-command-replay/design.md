## Context

`CommandManager_replayOrRetryMultiple` processes pending-or-failed commands in manager order up to a selected `Limit`.
The smallest available limit is currently five even though the existing bounded replay algorithm supports any positive bound.
Replay-next is a distinct operation with its own eligibility rules, so selecting a one-command bound for replay-multiple must retain replay-multiple semantics rather than redirecting to replay-next.

## Goals / Non-Goals

**Goals:**

- Allow an operator to select a replay-multiple bound of one.
- Preserve the default bound of ten and all existing larger bounds.
- Preserve replay-multiple ordering, failure handling, and background-completion gates.

**Non-Goals:**

- Change replay-next eligibility or presentation.
- Change which commands are pending, failed, or otherwise replayable.
- Change the replay algorithm, persistence model, manager state, or default limit.

## Decisions

### Add one to the existing replay-multiple limit model

The existing `Limit` enum will gain a `ONE` value whose numeric limit is one.
Reusing the existing limit model keeps selection, title rendering, stream bounding, and manager return behavior unchanged.
A separate action was considered, but rejected because replay-multiple already expresses the desired reviewed bounded-replay workflow.

### Preserve replay-multiple semantics

Selecting one will invoke the same replay-multiple path used by every other bound.
It will not delegate to replay-next because replay-next has intentionally different eligibility behavior.
The default will remain ten so existing user behavior and bookmarks are unaffected.

## Risks / Trade-offs

- [Risk] Users could confuse one-command replay-multiple with replay-next. → Preserve the existing action identity and document that only the selected bound changes.
- [Risk] A new enum value could alter ordinal-based consumers. → Treat enum names rather than ordinals as the supported representation and add the value at the start of the displayed numeric choices.
- [Trade-off] The change exposes two ways to replay one command. → Retain both because they represent different eligibility and review workflows.
