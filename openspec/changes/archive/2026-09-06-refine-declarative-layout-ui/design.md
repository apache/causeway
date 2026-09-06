## Context

Declarative layout components now provide strict fieldset, row, column, tab, and metadata composition in foundation, with equivalent HTMX and Vue PetOwner examples.
The initial visual treatment leaves tab panels on the page's grey background, while ordinary fieldsets and panels use a white contained surface.
The metadata component also renders associated actions in a full-width disclosure beneath the metadata properties, which consumes too much space and differs from the compact panel action menu established by the Wicket viewer.
The sample's Metadata and Layout help tabs further separate metadata from identity instead of demonstrating a useful object-page information hierarchy.

The refinement must preserve authoritative layout membership, ordinary action descendants, strict direct-child grammar, accessibility, lifecycle isolation, and thin-host parity.

## Goals / Non-Goals

**Goals:**

- Integrate tab panels visually with ordinary white viewer panels.
- Present metadata actions from a compact ellipsis menu attached to the metadata fieldset heading.
- Keep generated metadata actions ordinary `<cw-action>` components in authoritative order.
- Demonstrate Identity and Metadata as adjacent useful tabs in both PetOwner hosts.
- Preserve responsive and accessible pointer and keyboard tab behavior.

**Non-Goals:**

- Changing layout XML membership, GraphQL metadata, authorization, invocation, prompts, or results.
- Introducing a generic replacement for application menubars or collection action headings.
- Reproducing Wicket markup, JavaScript, CSS, or implementation internals.
- Adding host-owned tab or menu state.
- Retaining the artificial Layout help sample content.

## Decisions

### Use the regular panel surface for tab content

The selected tab panel will use the same white surface, border, radius, spacing, and responsive containment vocabulary as ordinary viewer panels.
The tablist remains visually connected to its selected panel, while unselected tabs remain distinguishable and all existing focus-visible and selected-state indications remain intact.
This is preferred over making all regular panels grey because the white panel surface is already established throughout object pages.

### Attach one compact menu to the metadata fieldset heading

When authoritative metadata actions exist, `<cw-metadata>` will expose one ellipsis trigger in the Metadata heading region and a bounded dropdown panel containing those actions.
The trigger will have an explicit accessible name such as **Metadata actions**, expose expanded state, support keyboard and pointer operation, and close predictably after dismissal or lifecycle retirement.
No trigger or empty menu will render when there are no authorized actions.

This is preferred over the existing full-width **Actions** disclosure because metadata actions are secondary panel operations rather than another content section.
It is also preferred over moving actions to an application toolbar because the effective metadata fieldset remains their authoritative layout owner.

### Preserve ordinary action components inside the menu

The menu controls only presentation, opening, dismissal, focus, and anchoring.
Each menu entry remains an ordinary `<cw-action>` descendant so foundation retains authoritative identity, labels, icons, disabled reasons, prompts, confirmation, invocation, results, and lifecycle behavior.
The menu MUST NOT infer action identity or availability from labels or CSS.

### Recompose the sample without changing layout grammar

The PetOwner page will contain an **Identity** tab followed by a **Metadata** tab.
Each tab continues to contain rows, each row contains columns, and the Metadata column contains `<cw-metadata>`, preserving strict direct-child contracts.
The previous Layout help tab and placeholder fieldset are removed rather than hidden.
HTMX and Vue author equivalent custom-element structures without introducing framework state.

## Risks / Trade-offs

- [Risk] An ellipsis alone can be ambiguous to assistive technology or sighted users. → Mitigation: provide an explicit accessible name, tooltip or equivalent bounded description, expanded state, and visible focus treatment.
- [Risk] Dropdown positioning can overflow narrow containers or the document viewport. → Mitigation: anchor it to the heading, constrain its inline size, align it within the fieldset, and test wide and narrow viewports.
- [Risk] Moving action descendants into a menu can affect requirement registration or focus after invocation. → Mitigation: retain ordinary connected `<cw-action>` elements and add lifecycle, disabled-action, prompt, result, and focus regression coverage.
- [Risk] White panel styling could duplicate nested fieldset surfaces. → Mitigation: apply the surface at the selected tab-panel boundary and verify nested fieldsets remain visually coherent without redundant heavy borders.

## Migration Plan

Update foundation styles and metadata presentation, update both PetOwner declarations and tests, regenerate Vue assets, and verify native and Vaadin action presentations at wide and narrow widths.
Rollback consists of restoring the prior shared styles, metadata disclosure markup, and sample declarations; no persistent data or API migration is required.

## Open Questions

None.
