## Context

`<cw-breadcrumbs>` currently renders a labelled navigation landmark containing the current object even when rich GraphQL reports an empty ancestor list.
That current-only presentation is not navigable and is visually redundant with the object header.
The same component also owns useful loading and failure diagnostics that must remain visible.

## Goals / Non-Goals

**Goals:**

- Omit ready-state breadcrumb presentation when there are no valid navigable ancestors.
- Preserve descendant breadcrumb hierarchy, navigation, accessibility, escaping, and responsive behavior.
- Preserve visible loading and error diagnostics.
- Support state transitions between root-only and descendant breadcrumb data without leaving stale host visibility.

**Non-Goals:**

- Stop requesting breadcrumb metadata for root objects.
- Change rich GraphQL breadcrumb generation or navigable-parent annotations.
- Remove the current item from descendant breadcrumb trails.
- Change authored Petclinic page structure.

## Decisions

### Hide the component host only for a ready state with zero valid ancestors

The renderer will normalize the supplied ancestor entries with the existing defensive validation.
If no valid entries remain, it will clear `innerHTML`, set the host's `hidden` property, and return without creating a navigation landmark.
This treats an entirely malformed ancestor list as having no usable parents rather than exposing a misleading current-only trail.

Alternative considered: render an empty `nav` or visually hide only the list.
Both leave unnecessary accessibility or layout structure and do not satisfy omission cleanly.

### Explicitly restore host visibility for every presented state

Loading, unsupported, partial-error, terminal-error, and ready descendant states will set `hidden` to false before rendering.
This allows a reused component to move safely from root-only data to a descendant or diagnostic state.

Alternative considered: rely on a fresh element for each route.
The framework-neutral component contract supports state updates independently of HTMX route replacement, so visibility restoration belongs in the renderer.

### Keep current-object presentation for descendant trails

When at least one valid ancestor exists, the component will retain the ordered ancestor links and escaped final current item marked `aria-current="page"`.
Only the non-navigable root-only case changes.

## Risks / Trade-offs

- [Risk] Host visibility remains stale after a state transition. → Set `hidden` explicitly in every render branch and test root-to-descendant and diagnostic transitions.
- [Risk] Malformed ancestor data accidentally leaves a current-only landmark. → Base the decision on the filtered valid ancestor list.
- [Risk] Browser acceptance waits indefinitely for the former owner current item. → Add a dedicated no-breadcrumb wait and retain the existing descendant helper for pet and visit routes.

## Migration Plan

1. Change the foundation renderer and focused unit coverage.
2. Update Petclinic owner-page browser assertions while retaining descendant navigation checks.
3. Run foundation, Petclinic compilation, focused Playwright, and strict OpenSpec validation.

Rollback restores the former current-only ready-state branch and its acceptance assertions.

## Open Questions

None.
