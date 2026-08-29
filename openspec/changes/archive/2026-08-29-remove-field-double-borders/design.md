## Context

The foundation theme applies shared native-control rules to every `input`, `select`, and `textarea` in document light DOM.
Vaadin fields create their native form element as a light-DOM node assigned to `slot="input"`, so those broad rules cross the internal adapter boundary.
Vaadin simultaneously renders its own read-only input container, producing the solid inner border and dashed outer boundary shown in the supplied Petclinic screenshots.

## Goals / Non-Goals

**Goals:**

- Keep one coherent Vaadin-owned field boundary for toolkit-backed read-only values.
- Keep global Causeway styling on native application and fallback controls.
- Preserve genuine `readonly` state, accessibility, focus, responsive sizing, and editor behavior.

**Non-Goals:**

- Change Vaadin generated assets or third-party styles.
- Remove all field boundaries or focus indicators.
- Change public component markup, toolkit policy, or native-mode presentation.

## Decisions

### Scope application native-control rules away from toolkit internals

The application-defined global theme selectors will not target controls carrying `slot="input"` wherever they apply border, sizing, focus, padding, or textarea resizing.
The slot marker is the stable integration boundary already emitted by Vaadin field controls, and the exclusion allows Vaadin's own shadow styles to reset and present that internal input correctly.

Editing generated Vaadin assets is rejected because they are deterministic reviewed outputs and regeneration would overwrite the change.
Removing `readonly` is rejected because it would weaken semantics and permit interaction in control families that support value changes.
Removing Vaadin's outer boundary is rejected because the toolkit owns that presentation and its focus, forced-colors, and read-only treatment.

### Verify both selector scope and rendered result

Foundation stylesheet tests will assert that slotted toolkit inputs are excluded while ordinary controls remain covered.
Petclinic browser acceptance will inspect representative read-only text and telephone fields and require only one visible bordered rectangle.
Existing accessibility and native-toolkit checks remain authoritative.

## Risks / Trade-offs

- [Risk] A future toolkit uses a different slot name. → Keep the regression test tied to the reviewed adapter contract and fail visibly when integration markup changes.
- [Risk] Excluding slotted controls could remove intended application styling from unrelated custom elements. → Scope the exclusion only to `slot="input"`, which denotes an internal field input, and retain host-level component variables.
- [Risk] Border counting can vary with forced-colors or focus state. → Test the ordinary unfocused read-only presentation and retain existing forced-colors and focus qualification separately.

## Migration Plan

Ship the stylesheet selector change with no markup or configuration migration.
Rollback restores the broad native-control selectors.

## Open Questions

None.
