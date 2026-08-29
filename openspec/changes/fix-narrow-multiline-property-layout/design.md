## Context

`CausewayPropertyElement` correctly resolves multiline depth from canonical `multi-line`, legacy `multiline`, or GraphQL `metadata.multiLine`.
Its editor receives that effective value, but the theme's layout selectors inspect only `cw-property[multiline]`.
For a canonical Petclinic `multi-line="5"` property, the multiline row rules therefore do not match.
At narrow width, generic grid declarations move the value but leave the edit control auto-placed, producing an implicit-grid offset of roughly 2.4 pixels and failing the explicit alignment assertion.

## Goals / Non-Goals

**Goals:**

- Make property layout consume the same effective multiline state as rendering and editing.
- Give every multiline shell state explicit wide and narrow grid placement.
- Remove overlap and retain bounded controls and no document overflow.
- Cover canonical, legacy, and metadata-derived multiline inputs.

**Non-Goals:**

- Relax the browser assertion to conceal incorrect grid placement.
- Change multiline editor selection or row-count semantics.
- Modify Vaadin components, generated toolkit assets, or application-local CSS.
- Change property label-position behavior unrelated to multiline layout.

## Decisions

### Publish effective multiline state on the generated property shell

Each rendered `.causeway-property` shell will carry `data-multi-line="N"` when the effective multiline value is positive.
The shell is regenerated for loading, error, view, disabled, and edit states, so the hook remains synchronized with canonical HTML, compatibility HTML, and metadata.

Selecting both host attribute spellings in CSS was rejected because it would still miss metadata-only multiline state.
Reflecting metadata back onto application-authored `<cw-property>` was rejected because internal presentation should not mutate public source declarations.

### Select the framework-owned shell in layout CSS

Wide, container-narrow, and viewport-narrow multiline rules will target `.causeway-property[data-multi-line]` and explicitly place label, description, value, and edit control.
This uses the element that is actually the grid container and avoids dependence on ancestor attribute spelling.

Increasing geometry tolerance was rejected because the failure exposed auto-placement rather than harmless rounding.
Adding a cosmetic edit-button margin was rejected because it would mask the missing grid-row contract and could misalign other states.

### Assert effective state and geometry

Foundation tests will verify shell attributes for canonical, compatibility, and metadata inputs and stylesheet selectors for all responsive rules.
Petclinic Playwright will retain the strict ordering checks and add bounded relative-alignment diagnostics where useful.

## Risks / Trade-offs

- [Risk] A generated data attribute becomes stale after metadata or authored attributes change. → Derive it during every existing shell render from `#effectiveMultiLine(state)`.
- [Risk] Selector replacement changes wide layout. → Preserve the same columns and rows and test wide and narrow paths.
- [Risk] Error or loading shells omit the hook. → Centralize attribute generation and apply it to every shell template.

## Migration Plan

No application migration is required.
Both `multi-line` and `multiline` remain accepted, and metadata-derived multiline behavior becomes consistent with them.
Rollback restores the host selector and removes the generated shell hook.

## Open Questions

None.
