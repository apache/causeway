## Context

`ReplayableCommand` recently removed the target summary fields and the old target-opening mixins because participant rows now carry target information.
The participants table is still the right place for target details, but users benefit from a command-level navigation action that opens the current actual target when that actual target is known and resolvable.

## Goals / Non-Goals

**Goals:**

- Reintroduce an `openTarget` mixin for `ReplayableCommand`.
- Associate the action with the `participants` collection using sequence `1`.
- Resolve the target from the target participant's actual bookmark rather than from legacy target summary fields.
- Disable the action when there is no actual target available to open.

**Non-Goals:**

- Do not reintroduce `targetType` or `targetId` properties.
- Do not reintroduce the table-row `openTargetTR` action.
- Do not change participant row layout, column order, or derivation except as needed to locate the actual target.
- Do not change replay mapping SPI or replay execution behaviour.

## Decisions

### Use a participants-associated mixin

The action should be associated with the `participants` collection so it appears next to the data that justifies opening the target.
Using sequence `1` gives predictable placement among collection-associated actions.
The alternative was to place the action in a separate field set or domain-object action area, but that would weaken the relationship with participant data.

### Open only actual target values

The action should open the target participant's actual target object, not the recorded command target.
This matches the participant table semantics and avoids reintroducing the older recorded-target navigation behaviour.
The alternative was to open the command log entry target, but that can point at recorded state rather than the actual replay participant.

### Disable when no actual target is available

The action should be disabled when no target participant actual bookmark exists or when the bookmark cannot resolve to an object.
This avoids presenting an action that would return null.
The alternative was to leave the action enabled and return null, but that gives a poor UI signal.

## Risks / Trade-offs

- [Risk] Resolving the actual target during disable checks can perform a bookmark lookup while rendering the page.
  → Mitigate by limiting lookup to the target participant and using best-effort existing bookmark lookup semantics.
- [Risk] Multiple target participants could exist for a command.
  → Mitigate by using the first target participant, consistent with the existing command title and target handling conventions.
