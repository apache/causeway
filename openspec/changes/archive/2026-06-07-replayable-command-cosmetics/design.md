## Context

`ReplayableCommand` originally exposed `targetType`, `targetId`, and `openTarget` so users could identify and navigate the recorded command target.
The newer `Participants` collection exposes target participants with recorded bookmark, actual bookmark, and resolved target object support.
Keeping both presentations duplicates target information and creates a less focused page.

## Goals / Non-Goals

**Goals:**

- Remove the legacy target summary properties from the replayable command view model.
- Remove the legacy target navigation action from the replayable command view model.
- Update fallback layout metadata so the Target field set no longer references removed members.
- Keep target inspection and navigation available through the `Participants` table.

**Non-Goals:**

- Do not change participant row derivation.
- Do not change replay mapping lookup or result notification behaviour.
- Do not change command DTO rendering or replay execution.
- Do not introduce persistence or schema changes.

## Decisions

### Remove members instead of hiding them

The target summary members should be removed from `ReplayableCommand` rather than only hidden in fallback layout metadata.
This avoids maintaining duplicate APIs for a cosmetic view and prevents unreferenced members from appearing in generated or custom layouts.
The alternative was to hide only the fallback layout entries, but that would still leave the properties and action available to the metamodel.

### Leave title member derivation intact unless it depends on removed properties

`ReplayableCommand.title()` may continue to summarize the command using command DTO data if it remains useful for lists and breadcrumbs.
If implementation requires helper methods to replace removed getters, those helpers should be programmatic and not exposed as properties.
The alternative was to remove all target data from the title, but that would reduce object identification outside the participants table.

### Keep participant table unchanged

The participants collection already provides the replacement target inspection surface.
This change should not alter its rows, column order, object lookup, or bookmark population behaviour.
The alternative was to further redesign participants, but that would expand a cosmetic cleanup into a behaviour change.

## Risks / Trade-offs

- [Risk] Custom layouts or tests may reference the removed target members.
  → Mitigate by updating fallback layout and focused tests, and by treating the removal as an intentional UI cleanup.
- [Risk] Removing `openTarget` changes a direct navigation affordance.
  → Mitigate by relying on participant target object links, which now carry the same target information with bookmark context.
