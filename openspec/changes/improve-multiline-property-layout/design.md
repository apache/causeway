## Context

The standard theme lays out a read-only property as a three-column grid for label, value, and edit action.
The description currently claims the value and action columns, so normal grid auto-placement moves the value into the label column on the next row and stretches the edit button across the value column.
The Petclinic Notes property exposes this defect because it combines `multiline`, a visible description, a long read-only value, and an editable affordance.

## Goals / Non-Goals

**Goals:**

- Keep the multiline value in the established value column at wide widths.
- Place supporting description text beneath the label rather than above the value.
- Keep the edit affordance compact and aligned with the value area.
- Retain a readable stacked arrangement at narrow widths.
- Preserve semantic markup, accessibility relationships, GraphQL authority, editing behavior, and toolkit independence.

**Non-Goals:**

- Redesign property editing controls or validation.
- Change descriptions for collections, actions, or action parameters.
- Change application-authored HTML, Petclinic domain metadata, GraphQL operations, or associated-action composition.
- Introduce JavaScript layout measurement or inline styles.

## Decisions

### Scope layout placement through the semantic multiline host attribute

The standard theme will use `causeway-property[multiline]` as the stable semantic selector and assign explicit grid positions to the existing label, description, value, and built-in edit classes.
This avoids changing renderer markup or applying a multiline-specific trade-off to compact scalar properties.
The existing public `multiline` attribute already controls multiline editing and is therefore the appropriate presentation signal.

An alternative was to reorder the generated DOM.
That would make visual layout depend on auto-placement, would not by itself put the description under the label, and would create unnecessary accessibility and test churn.

### Use explicit wide and narrow grid areas without moving nodes

At wide widths the label and description occupy successive rows in the label column, the value occupies the value column, and the edit affordance occupies the action column with content-sized alignment.
At narrow container and viewport breakpoints the label, description, value, and action retain logical vertical order while the compact action shares the value row when space permits.
The DOM and its `aria-labelledby` and `aria-describedby` references remain unchanged.

An alternative was flexbox or JavaScript measurement.
Explicit CSS grid placement is deterministic, responsive, CSP-safe, and consistent with the existing theme.

### Treat Edit as a compact secondary affordance

The built-in `.causeway-property-edit` button will use content-sized alignment and compact secondary-control dimensions.
It remains a native labelled button with the existing action hook, focus behavior, and accessible name.
Associated `<causeway-action>` children remain independent and unchanged.

### Verify both style contract and executable geometry

Foundation tests will assert the documented multiline selectors and responsive placements in the packaged theme.
Petclinic Playwright will verify label/value columns, description-under-label placement, compact edit width, and no horizontal overflow under Vaadin-default and explicit-native policies.

## Risks / Trade-offs

- **Risk:** Explicit rows could conflict with narrow responsive rules.
  **Mitigation:** Add multiline-specific narrow placement after the existing responsive declarations and verify geometry at representative widths.
- **Risk:** A compact action could become too small for touch or keyboard use.
  **Mitigation:** Retain the existing border, focus, label, and control semantics while reducing only the oversized stretch and excess dimensions.
- **Risk:** Generic theme changes could leak into application-owned associated actions.
  **Mitigation:** Scope selectors to the built-in edit class beneath a multiline property and leave associated action nodes untouched.
- **Risk:** The local uncommitted PetOwner default-action change could be accidentally committed.
  **Mitigation:** Do not edit or stage that unrelated file during this workflow.
