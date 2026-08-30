## Context

A property-associated `<cw-action>` remains a direct child of `<cw-property>` while the rendered property row lives inside the generated `.causeway-member-primary` wrapper.
Current structural styles make the primary wrapper consume the full flex row, so associated actions begin at the host's logical start rather than at the property field column.
The Petclinic owner page separately authors `<cw-object-header>` and an object-action toolbar as consecutive grid rows, so the delete action necessarily appears below the title.

## Goals / Non-Goals

**Goals:**

- Align property-associated actions with the effective property field column at wide widths.
- Collapse associated actions to the same logical start as a stacked field at narrow widths.
- Compose the Petclinic owner title and object-level action in one application-owned responsive heading row.
- Preserve action semantics, focus, invocation, toolkit selection, and component ownership.

**Non-Goals:**

- Change GraphQL action metadata or invocation behavior.
- Move Petclinic-specific title composition into `<cw-object-header>` or `<cw-object>`.
- Change collection-associated action placement.
- Introduce a new public component or application layout API.

## Decisions

### Use host-grid placement for property-associated actions

The foundation structural stylesheet will make a `<cw-property>` with associated actions use a grid whose label and field tracks match the rendered property grid.
The generated `.causeway-member-primary` will span the complete grid, while each associated action begins in the field track.
The cohesive theme will mirror its effective property columns on the associated-action host and will reset action placement to the first track when property content stacks responsively.
This keeps placement in component-owned styles and avoids application-specific offsets.

A margin or padding offset was rejected because the effective label track may be responsive or expressed as `minmax(...)`, making arithmetic offsets brittle.
Moving authored actions into the generated property markup was rejected because it would complicate light-DOM ownership, mutation observation, and semantic event ancestry for a cosmetic change.

### Keep object title composition application-owned

The Petclinic owner page will wrap `<cw-object-header>` and its delete-action toolbar in a semantic application heading container.
Petclinic application CSS will lay out that container as a wrapping flex row, remove the inner header's standalone bottom spacing and border in that context, and place the shared divider on the wrapper.
At narrow widths the row may wrap while retaining title-before-action document order.

Changing `<cw-object-header>` to accept or discover unrelated actions was rejected because object-level placement is application layout policy and existing pages may require other arrangements.

### Verify structure at foundation and Petclinic levels

Foundation tests will assert synchronized structural styles and wide/narrow property-associated-action placement hooks.
Petclinic resource or browser coverage will assert that the owner heading contains the object header followed by the delete action and that the property-associated update action remains nested beneath its property.

## Risks / Trade-offs

- [Risk] Theme and structural property-column definitions can drift and misalign associated actions. → Reuse the same variables and assert both baseline and cohesive-theme selectors in component-style tests.
- [Risk] Several actions associated with one property may occupy additional grid rows. → Preserve logical order, spacing, and wrapping behavior, and keep the field-column start for every action.
- [Risk] A long title and action may not fit on one line. → Allow the application heading container to wrap in document order without overlap or horizontal overflow.

## Migration Plan

No migration is required.
Applications using documented semantic markup receive the corrected property-associated placement through foundation styles.
The Petclinic title-row change is isolated to its packaged HTML and application stylesheet and can be rolled back independently.

## Open Questions

None.
