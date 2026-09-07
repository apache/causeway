## Context

The allocation coordinator correctly generates authoritative remaining actions as ordinary `<cw-action>` descendants inside an accessible `role="group"`.
The generated group currently sits inside a block-level `<cw-unreferenced-actions>` host, so placing the component among explicitly authored toolbar actions introduces an additional layout box that can take more space than the controls require.
The PetOwner page now demonstrates both unreferenced collections and actions, making compact action-toolbar composition part of the visible host-parity contract.

## Goals / Non-Goals

**Goals:**

- Make generated remaining actions align and wrap like explicitly authored action siblings in compact toolbars.
- Retain one accessible named group and ordinary `<cw-action>` ownership.
- Preserve non-presentational loading, empty, and error states.
- Keep the behavior framework-neutral and equivalent in HTMX and Vue.
- Keep the PetOwner declaration valid while preserving the user's move toward unreferenced action and collection composition.

**Non-Goals:**

- Change action allocation, authorization, ordering, identity, invocation, prompt, result, or navigation semantics.
- Flatten the accessible action group or make a host reconstruct generated actions.
- Introduce an action overflow menu or change ordinary button styling.
- Make generated actions inherit application-specific PetOwner CSS selectors.

## Decisions

### Make the ready custom-element host layout-transparent

The shared foundation stylesheet will make a ready `<cw-unreferenced-actions>` host participate with `display: contents`, while the generated inner `role="group"` remains the accessible and visual action cluster.
This removes the redundant block box without discarding the semantic group or changing generated descendants.
Using only `inline-block` on the custom-element host was rejected because it retains a nested sizing boundary and makes wrapping depend on two boxes.
Rendering actions without the inner group was rejected because it would remove the component's accessible name and semantic ownership boundary.

### Keep the inner action group intrinsically compact and bounded

The generated `.causeway-unreferenced-action-group` will use the existing wrapping action layout, no panel margin or padding, intrinsic inline sizing where the containing layout permits it, and a maximum inline size of its container.
Controls will retain the established action gap and wrap only when available inline space requires it.
No fixed dimensions or PetOwner-specific spacing values will be introduced.

### Preserve state and interaction contracts

Loading, empty, and error hosts will continue to use `display: none` and will generate no visible group.
Ready-state style changes will not alter allocation signatures, generated action markup, lifecycle retirement, focus, or event dispatch.
The inner actions remain ordinary connected `<cw-action>` components served by the existing context and interaction controller.

### Demonstrate valid direct toolbar composition in both hosts

The HTMX PetOwner template will place `<cw-unreferenced-actions>` as a direct action-toolbar child rather than inside another action or a column panel.
Authored actions that require nested custom result declarations will remain explicit unless the existing generic result path is intentionally used, avoiding malformed or semantically detached result markup.
The Vue PetOwner template will use equivalent semantic placement so browser geometry and interaction tests exercise host parity.
Unreferenced collections remain in their authored layout region because their presentation is already acceptable.

### Verify geometry in a real browser

Foundation DOM tests will continue to verify generated group semantics and will add stable ready-state class or selector assertions.
Headless HTMX and Vue browser tests will assert that generated action controls remain adjacent to toolbar actions, that the generated group does not create panel-like blank space, and that narrow layouts wrap without horizontal document overflow.
Both native and Vaadin presentation paths will be covered where the existing suite supports them because action control implementations can differ while container geometry must remain equivalent.

## Risks / Trade-offs

- **`display: contents` can change selector and box behavior** → Keep accessibility semantics on the inner group, avoid host-box geometry dependencies, and test computed layout in supported browsers.
- **Intrinsic sizing can overflow with unusually long action labels** → Retain wrapping and cap the group at the available container width.
- **Sample cleanup could accidentally change special action results** → Keep result-bearing explicit actions structurally valid and test their established result behavior separately from generated actions.
- **Generated controls can differ between native and Vaadin adapters** → Assert bounded container relationships rather than brittle pixel-perfect button dimensions.

## Migration Plan

Apply the shared presentation change without changing the public custom-element API.
Clean the PetOwner declarations and add equivalent Vue composition and tests.
Rollback consists of restoring the previous host display and generated-group sizing rules; no data or API migration is required.

## Open Questions

None.
