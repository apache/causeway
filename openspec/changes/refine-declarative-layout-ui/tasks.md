## 1. Tab Panel Presentation

- [ ] 1.1 Update shared tabgroup and tab-panel styles to use the regular contained white viewer-panel surface while preserving selected, disabled, and focus-visible states.
- [ ] 1.2 Keep wide and narrow tab presentations within their containers without horizontal document overflow or incoherent nested fieldset borders.
- [ ] 1.3 Extend foundation style and component tests for the integrated tab surface, selection, keyboard behavior, RTL behavior, and responsive containment.

## 2. Metadata Heading Action Menu

- [ ] 2.1 Replace the full-width metadata Actions disclosure with one compact ellipsis trigger anchored in the Metadata fieldset heading.
- [ ] 2.2 Implement accessible naming, expanded state, keyboard and pointer opening, Escape and outside dismissal, constrained positioning, and valid focus restoration.
- [ ] 2.3 Preserve authoritative action order and ordinary connected `<cw-action>` descendants, including icons, disabled reasons, prompts, confirmation, invocation, results, and generation-safe lifecycle behavior.
- [ ] 2.4 Omit the trigger and menu when no authorized metadata actions exist, and retire open-menu state on supersession, terminal state, or disconnection.
- [ ] 2.5 Add foundation tests for action and no-action metadata, accessibility, opening and dismissal, narrow positioning, disabled actions, ordinary action behavior, supersession, and disconnect retirement.

## 3. PetOwner Demonstration Parity

- [ ] 3.1 Recompose the HTMX PetOwner declaration as an initial Identity tab followed by a Metadata tab using valid tab-to-row-to-column grammar.
- [ ] 3.2 Apply the equivalent custom-element composition to the Vue PetOwner page without Vue-owned tab or menu state.
- [ ] 3.3 Remove the Layout help tab and placeholder content from both hosts.
- [ ] 3.4 Extend HTMX and Vue source, integration, and browser assertions for tab order, white panel presentation, pointer and keyboard selection, metadata properties, ellipsis actions, stable routes, and wide/narrow parity.
- [ ] 3.5 Regenerate Vue production assets through the established frontend build lifecycle.

## 4. Documentation and Verification

- [ ] 4.1 Update foundation, HTMX, and Vue documentation and examples for the refined tab surface, Identity/Metadata composition, and metadata heading action menu.
- [ ] 4.2 Run complete foundation JavaScript, HTMX, Vue, native/Vaadin, secured, Maven packaging, RAT/license, OpenSpec, IDE, browser accessibility, responsive, and whitespace validation.
