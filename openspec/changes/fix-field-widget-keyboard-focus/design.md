## Context

Vaadin documents that its built-in clear button is intentionally not keyboard-focusable and that Escape performs clearing while the field owns focus.
The Causeway field wrapper already reserves Escape for cancelling property editing, so retaining the internal clear button leaves the visible `×` pointer-only.
Vaadin text fields and text areas support public suffix slots, allowing Causeway to supply a normal native button without reaching into toolkit shadow internals.

The property controller replaces its light-DOM interaction markup when debounced validation starts and finishes.
It currently queues restoration for both editors and actions.
A fast second render can occur while the replacement Save control has not yet received queued focus, leaving the document body active and losing the intended tab position.

## Goals / Non-Goals

**Goals:**

- Make every visible clear affordance supplied by qualified field adapters reachable and activatable through normal keyboard navigation.
- Give the clear affordance a bounded accessible name and keep clearing within existing pending-value semantics.
- Preserve the editor, clear, Save, and Cancel tab order.
- Keep Save or Cancel focused across validation-driven markup replacement.
- Preserve strict CSP, native fallback, protected values, and GraphQL authority.

**Non-Goals:**

- Change Vaadin's internal shadow-DOM clear button implementation.
- Add a clear affordance to required, protected, checkbox, or selection controls that do not currently qualify for one.
- Change Escape cancellation, property validation timing, action-prompt behavior, or reference autocomplete controls.
- Expose raw Vaadin APIs to application markup.

## Decisions

### Use a Causeway-owned native suffix button

For an optional non-protected Vaadin field that supports the public clear-button capability, the adapter will disable the non-tabbable built-in control and append a native button in Vaadin's public `suffix` slot.
The button will have a Causeway class, `type="button"`, and an accessible name derived from the bounded semantic label.
It will be shown only while the field has a value.

Activation will clear the internal control, emit the same bubbling input path used by ordinary editing, and return focus to the field after the now-empty suffix is hidden.
This keeps the owning property or action interaction authoritative and avoids a second value state machine.

Alternatives were to mutate a private shadow part or merely document Escape.
Private-part mutation is toolkit-version fragile, while Escape conflicts with Causeway's established cancellation contract and does not make the visible control reachable.

### Restore native action controls synchronously

When property rerendering observes focus on Save or Cancel, it will focus the corresponding newly inserted native button immediately after replacement.
Editor restoration will remain deferred because lazy custom-element upgrade and selection restoration require its existing microtask path.

This removes the transient body-focus window between validation renders without trapping focus or changing user-selected external focus.

### Keep styling semantic and CSP-safe

The standard theme will style only the Causeway-owned clear suffix class.
No inline style, shadow-root mutation, extra asset, CSP exception, or Vaadin theme API is required.

### Qualify lifecycle and browser tab order

Foundation tests will verify suffix semantics, keyboard activation, pending-value events, value-dependent visibility, and synchronous action focus restoration across consecutive renders.
Petclinic Playwright will verify the real Vaadin editor-to-clear-to-Save-to-Cancel sequence and Save persistence after debounced validation.
The native policy remains covered to prove the change does not weaken rollback behavior.

## Risks / Trade-offs

- **Risk:** Slotted button styling could conflict with toolkit styling.
  **Mitigation:** Use a narrowly scoped Causeway class and verify the packaged pinned toolkit in browser qualification.
- **Risk:** Hiding the clear button after activation could strand focus.
  **Mitigation:** Move focus back to the internal field synchronously after clearing.
- **Risk:** Programmatic value updates could leave clear visibility stale.
  **Mitigation:** Refresh visibility on initial upgrade, wrapper value assignment, and internal input/change paths.
- **Risk:** Immediate action restoration could steal focus after intentional departure.
  **Mitigation:** Restore only when the active element captured immediately before replacement was an owned Save or Cancel control.
- **Risk:** Unrelated local PetOwner work could enter workflow commits.
  **Mitigation:** Keep that file untouched and stage only explicit change files.
