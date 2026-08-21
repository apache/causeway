# Accessibility, keyboard, and visual review

## Automated evidence

Axe-core 4.10.3 reported zero violations in all six retained scenarios:

- Generic desktop light.
- Semantic custom desktop dark with reduced motion.
- Raw-widget custom desktop light.
- Generic narrow light.
- Semantic custom narrow dark with reduced motion.
- Generic narrow forced colors.

Axe marked shadow-DOM ARIA relationships and 33 to 44 color-contrast nodes per scenario as incomplete for manual review.
Those incomplete checks are retained in `results/browser-evidence.json` and are not silently counted as passes.

The fixture initially exposed a real checkbox labeling failure when it used text content and `aria-label` incorrectly.
Changing to Vaadin's documented `label` property removed both the critical internal-input label failure and the prohibited host ARIA attribute.
Making the scrollable event log keyboard-focusable removed the serious scrollable-region failure.

## Headless keyboard journeys

The retained journey uses actual Playwright keyboard input after focusing the candidate controls.
It demonstrated:

- Typing `Owner 019`, navigating the Combo Box popup with Arrow Down, and selecting the canonical `petclinic.owner.Owner:19` identity with Enter.
- Typing `Owner 002` into Multi-Select Combo Box, selecting it with the keyboard, and retaining the canonical identity in the selected-items array.
- Closing the multi-selection popup with Escape after selection.
- Focusing the Grid's internal table, moving with Arrow Down, and retaining a Grid-focused active descendant.
- Opening the action dialog from its button, dismissing it with Escape, and restoring focus to the invoking button.
- Required-field validation with an accessible error message.
- Menu disclosure and Escape dismissal after same-origin Vaadin injection into the real Petclinic viewer.

The journey recorded no unexpected console error, page error, failed request, external network request, stale result, or residual overlay.

## Responsive and preference evidence

All retained fixture scenarios had zero page-level horizontal overflow.
The 390-pixel layout converted two-column panels to one column while keeping fields, labels, action controls, and the Grid within the viewport.
The Grid retained horizontal cell truncation and keyboard operability instead of forcing page overflow.
Long reference labels and owner names were truncated inside candidate controls and Grid cells while canonical identities remained unchanged.

The dark fixture mapped Causeway colors into Vaadin and Lumo variables and remained visually coherent across fields, disabled state, collection rows, status content, and action controls.
Reduced-motion rules disabled nonessential transition duration.
Forced-colors capture retained borders, control labels, selection structure, and zero overflow, although manual assistive-technology verification remains required for a production proposal.

## Visual observations

Vaadin provides a substantially more coherent form, date, time, selection, dialog, and Grid vocabulary than Bootstrap styling alone.
The generic and custom pages remained recognizably Causeway-owned because the application shell, content grouping, semantic labels, GraphQL state text, and public theme variables came from the harness rather than Vaadin.
Shadow DOM prevents broad global-selector collisions but also limits direct descendant styling.

The default density is suitable for business applications, although fields consume more vertical space than the current compact viewer and require a reviewed compact theme before broad adoption.
On narrow screens the Grid remains usable but only a few columns are readable at once, so collection column prioritization remains a Causeway responsibility.

## Real-viewer CSP finding

Same-origin injection into Petclinic preserved route readiness, menu readiness, Escape dismissal, page overflow, and external-request isolation.
However, four Vaadin component operations attempted inline style application and were blocked by the viewer's current `style-src 'self'` policy.
The test classifies these errors separately from unexpected browser failures and records strict style CSP compatibility as false.

This is an adoption blocker until a production design proves one reviewed approach, such as nonce propagation where applicable, Constructable StyleSheet use, removal of dynamic inline styles, or a narrowly scoped `style-src-attr` policy.
The analysis does not weaken CSP to make the prototype pass.

## Conclusion

The standalone controls provide credible accessible interaction and responsive presentation in the unrestricted local harness.
The component-level accessibility evidence is stronger than the current handcrafted-widget baseline, but incomplete shadow-DOM checks and the strict-CSP conflict require explicit production resolution.
