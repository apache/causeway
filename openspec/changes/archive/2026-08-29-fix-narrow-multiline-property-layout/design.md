## Context

`CausewayPropertyElement` correctly resolves multiline depth from canonical `multi-line`, legacy `multiline`, or GraphQL `metadata.multiLine`.
Its editor receives that effective value, but the theme's layout selectors inspect only `cw-property[multiline]`.
For a canonical Petclinic `multi-line="5"` property, the multiline row rules therefore do not match.
At narrow width, theme declarations target the nested value and edit control even though the direct grid item is their `.causeway-property-field` wrapper.
The wrapper therefore retains incompatible wide placement while its differently sized flex children remain center-aligned, producing the observed roughly 2.4-pixel top offset and failing the explicit alignment assertion.

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

### Select the framework-owned shell and direct field grid item

Wide, container-narrow, and viewport-narrow multiline rules will target `.causeway-property[data-multi-line]` and explicitly place the direct label, description, and `.causeway-property-field` grid items.
At narrow width, the field occupies the explicit third row across the available columns and top-aligns its value and bounded edit control.
This uses the element that is actually the grid container and the child that is actually its grid item, avoiding dependence on ancestor attribute spelling or ineffective rules for nested flex children.

Increasing geometry tolerance was rejected because the failure exposed incorrect wrapper placement and alignment rather than harmless rounding.
Adding a cosmetic edit-button margin was rejected because it would mask the missing grid-item contract and could misalign other states.

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
