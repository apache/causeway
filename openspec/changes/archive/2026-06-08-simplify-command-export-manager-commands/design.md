## Context

`CommandExportManager` currently stores baseline, page limit, and mode in its view-model memento.
The mode toggles between an export view backed by `notYetExported` and an unexport view backed by `exported`.
Exporting selected commands still updates their replay state to `EXPORTED`, and existing per-command actions can move commands or make them exportable again.
The newer workflow needs users to arrange commands and preserve dotted-path reachability across the full baseline-bounded sequence, so hiding exported commands behind a separate mode makes the page less useful.

## Goals / Non-Goals

**Goals:**
- Provide one export-manager collection named `commands` for all foreground commands at or after the baseline.
- Remove mode and toggle behavior from the export manager UI and memento.
- Keep export marking by setting exported command log entries to replay state `EXPORTED` after successful YAML creation.
- Show replay state in the unified command list so users can distinguish exported and not-yet-exported entries without switching modes.
- Update actions and tests to use the unified collection as their choice source and association target.

**Non-Goals:**
- Do not preserve backward compatibility for old view-model mementos, collection names, or UI toggle behavior.
- Do not change command YAML structure or command replay import behavior.
- Do not change the existing dotted-path validation rules except to evaluate selections in the unified command-list workflow.

## Decisions

- Replace `Mode`, `withMode`, `getNotYetExported`, `getExported`, and their hide/support methods with a single `getCommands` collection.
  Alternative considered: keep the mode but make it default to all commands.
  That preserves obsolete concepts and continues to split the user workflow, so the mode should be removed.

- Persist only baseline and limit in the export-manager memento.
  Alternative considered: keep a third memento segment and ignore it.
  The request explicitly says not to worry about backward compatibility, so the simpler memento shape is acceptable.

- Use repository queries that return all foreground commands since or before the baseline without filtering on replay state.
  Alternative considered: combine exported and not-yet-exported query results in memory.
  A single repository-level query avoids merge-order mistakes and better reflects the collection semantics.

- Keep export actions from re-exporting commands only if explicitly required by validation or selection rules.
  Alternative considered: filter exported commands out of the selected entries as a safety measure.
  That would contradict the unified list, so the action should operate on the selected commands from `commands` and then mark them `EXPORTED` after successful export.

- Rename layout and column-order fallback resources to target `commands`.
  Alternative considered: leave old resources in place until manually cleaned later.
  The UI contract changes now, so stale resources should be removed or replaced in the same change.

## Risks / Trade-offs

- Users may accidentally re-export commands whose replay state is already `EXPORTED` → The list displays replay state and the generated YAML still reflects the explicit selection.
- Removing memento compatibility can reset bookmarked export-manager URLs → This is accepted because backward compatibility is out of scope.
- Repository query changes can affect pagination boundaries → Add tests for next and previous page behavior over mixed replay states.
- Dotted-path validation can become confusing if users select only part of the visible sequence → Keep validation scoped to the selected export sequence while target choices and move operations use the unified baseline-bounded command set.
