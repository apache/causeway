## Context

`ReplayableCommand` already exposes a participants collection and an `openTarget` action associated with that collection.
Parameter participants can now expose actual argument bookmarks and argument objects, but users do not have a corresponding command-level action to select and open an argument from the participants table.
The action needs to distinguish between the recorded command parameter name and the resolved argument object.

## Goals / Non-Goals

**Goals:**

- Add an `openArgument` command-level mixin associated with the `participants` collection.
- Use action sequence `2` so it appears after the existing `openTarget` action.
- Disable the action when there are no parameter participants.
- Provide a `parameterName` action parameter with choices derived from parameter participants.
- Default `parameterName` only when there is exactly one parameter participant.
- Validate the selected parameter name when its participant has no actual bookmark for the argument.
- Open the actual argument object for the selected parameter participant when it is available.

**Non-Goals:**

- Do not change participant derivation or replay mapping semantics.
- Do not rename `parameterName` or the participant role `PARAMETER`.
- Do not add persistence schema changes.
- Do not change the existing `openTarget` action behaviour.

## Decisions

- Implement `openArgument` as a separate mixin rather than adding behaviour to participant rows so argument navigation remains command-level and associated with the same participants collection as `openTarget`.
- Use `parameterName` as the action parameter because the user selects from recorded command parameter names, while the action opens the corresponding actual argument object.
- Derive choices from current participants with role `PARAMETER` so the action reflects the same participant derivation and mapping rules as the table.
- Return a default only when exactly one parameter participant exists to avoid silently choosing among multiple possible arguments.
- Validate on `parameterName` when the selected participant has no actual bookmark because an actual bookmark is the minimum evidence that an argument can be opened or at least resolved.
- Resolve and return the selected participant's argument object through the same bookmark lookup behaviour used by `ReplayableCommandParticipant#getArgument`.

## Risks / Trade-offs

- A participant may have an actual bookmark that cannot be resolved locally → The action can still fail to open an object unless validation also accounts for lookup failure during implementation.
- Multiple parameter participants might share the same parameter name → Choices should preserve the names available from participants, but duplicated names could make selection ambiguous.
- Calling `getParticipants()` from mixin support methods may repeat participant derivation → Keep the implementation simple unless profiling shows a performance issue.
