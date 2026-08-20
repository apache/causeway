## Context

The foundation currently renders effective-grid member references as a flat sequence even when one member reference contains an associated action.
The editor registry selects scalar controls from GraphQL input types but does not receive the effective-grid `multiLine` hint.
Property descriptions and disabled reasons are visible text, while application-menu disclosures close only when toggled again, when a sibling opens, or when Escape is pressed.
Petclinic already declares Causeway `associateWith` metadata, but its effective layout does not place all associated actions beneath their properties or collections.

The effective grid remains the canonical structural presentation source, GraphQL remains the semantic state and interaction protocol, and all generated markup remains framework-neutral light DOM.

## Goals / Non-Goals

**Goals:**

- Make menu disclosure dismissal match ordinary application navigation expectations.
- Keep both property descriptions and disabled reasons discoverable to pointer, keyboard, and assistive-technology users.
- Preserve effective-grid action nesting in the object composition plan.
- Visually group top-level and member-associated actions with consistent gaps.
- Render multiline string properties as bounded textareas when the effective grid requests `multiLine`.
- Demonstrate and verify the behavior in Petclinic and foundation tests.

**Non-Goals:**

- Add GraphQL member metadata or a duplicate association API.
- Infer property or collection associations from Java annotations in the browser.
- Change route, mutation, authorization, or collection-window semantics.
- Reproduce Wicket markup or Bootstrap classes.
- Introduce modifier-key-only information that is unavailable to touch or assistive-technology users.

## Decisions

### Menu activation closes transient disclosure state

Selecting an enabled service action will close its containing menu immediately after publishing the semantic action request.
Clicking outside a bar will close any expanded menu in that bar, while sibling opening and Escape retain their existing behavior.
The semantic request will identify the disclosure as the focus-restoration target so cancellation or non-navigation completion does not focus a control hidden inside a closed panel.

Keeping the panel open until the action completes was rejected because parameterized prompts can remain open for an arbitrary period and should not compete visually with navigation.
Closing only on a second disclosure click was rejected because it does not match conventional disclosure navigation.

### Disabled reasons use a dedicated accessible indicator

The property description remains the property's default explanatory text and native title.
A disabled property will add a separate focusable reason indicator with a bounded tooltip label and a visually hidden description referenced by the value.
The visible paragraph currently used for disabled reasons will be removed.

A Shift-modified tooltip was rejected because modifier-only discovery is unavailable on touch devices, unreliable with native title behavior, and difficult to expose consistently to assistive technology.
Replacing the description title with the disabled reason was rejected because it would hide stable domain documentation whenever usability changes.

### Effective-grid nesting remains hierarchical

The safe grid parser will retain child member nodes beneath an explicitly referenced property or collection instead of flattening them into the surrounding region.
The renderer will wrap nested action children in a semantic associated-action group immediately after the owning member.
Unassociated action runs remain in top-level action groups.
Explicit claims and deterministic allocation continue preventing duplicate rendering.

Browser-side inference from action names or GraphQL fields was rejected because effective-grid metadata is already the canonical source and inference can misplace authorized actions.

### Multiline editing is a bounded presentation hint

The parser will accept `multiLine` only on property member references, normalize a positive integer to a maximum of 50 rows, and pass it to `<causeway-property>`.
The property editor context will select a higher-priority multiline string editor that renders a textarea with the requested row count.
Invalid values will produce bounded layout diagnostics and fall back to the ordinary single-line editor.

Adding a GraphQL field was rejected because `multiLine` is structural presentation metadata already carried by the effective grid.
Using the property name `notes` as an editor heuristic was rejected because the behavior must remain semantic and reusable.

### CSS sources remain synchronized

The packaged stylesheet and its JavaScript string mirror will add flex wrapping and gaps for action groups, textarea sizing, and a focusable disabled-reason tooltip.
The existing stylesheet-equivalence test remains the guard against drift.

## Risks / Trade-offs

- [Outside-click listeners could leak after component removal] → Register on connection, remove on disconnection, and cover reconnect behavior.
- [Closing a menu can hide the original action control before focus restoration] → Restore focus to the still-visible disclosure rather than the hidden action.
- [Nested layout nodes could alter deterministic allocation] → Preserve the existing explicit-claim and allocated-member sets and add focused plan tests.
- [Native title behavior differs between browsers] → Keep accessible hidden text and focusable reason markup as the semantic contract rather than relying on title alone.
- [Large textarea row counts could distort layouts] → Validate and cap rows at 50.

## Migration Plan

No data or schema migration is required.
Applications with effective-grid property or collection nodes containing actions will receive the improved associated placement automatically.
Applications can opt into multiline editing by supplying the established `multiLine` grid attribute.
Rollback consists of reverting the component, layout, and sample changes.

## Open Questions

None.
