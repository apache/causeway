## 1. Shared Preview Disclosure

- [ ] 1.1 Replace the font-dependent preview triangle with one `aria-hidden` inline SVG chevron whose direction is driven by the existing `aria-expanded` state.
- [ ] 1.2 Add shared component styling for a modestly larger icon, clear current-color stroke, expanded downward rotation, and logical collapsed direction without changing the button or preview-column dimensions.
- [ ] 1.3 Preserve existing accessible labels, focus restoration, keyboard activation, row identity, and preview lifecycle behavior.

## 2. Foundation Verification

- [ ] 2.1 Extend preview markup and lifecycle tests to verify the SVG hook, hidden decorative semantics, and synchronized collapsed and expanded states.
- [ ] 2.2 Extend component-style tests to verify icon sizing, stroke presentation, state-driven rotation, logical direction, and unchanged disclosure-control dimensions.
- [ ] 2.3 Run the complete foundation JavaScript suite under native component presentation.

## 3. Host Acceptance and Documentation

- [ ] 3.1 Extend HTMX and Vue Petclinic browser assertions to verify useful icon dimensions and distinct collapsed and expanded directions without horizontal overflow.
- [ ] 3.2 Update foundation preview documentation to describe the clearer state indicator without assigning behavior to either host.
- [ ] 3.3 Run affected Maven, HTMX, Vue, native/Vaadin, OpenSpec, IDE, packaging, and whitespace validation.
