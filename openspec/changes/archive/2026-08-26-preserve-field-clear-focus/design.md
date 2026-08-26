## Context

Typing in a debounced property editor schedules authoritative validation.
When the user tabs to the Causeway-owned clear suffix, the property replaces its interaction markup first for `validating` and again for the resulting `editing` state; the brief status flash near “Editing” confirms this lifecycle.
The clear button is nested inside an asynchronously upgraded field adapter, so it does not exist when the property immediately attempts ordinary post-render focus restoration.
The first replacement therefore leaves body focus active, and the second replacement has no active owned element from which to infer the original clear-button intent.

## Goals / Non-Goals

**Goals:**

- Keep keyboard focus on Clear through both validation renders and lazy field upgrade.
- Continue from Clear to Save on the next Tab.
- Preserve external focus departure and avoid a focus trap.
- Keep the existing clear, validation, cancellation, and protected-value semantics.

**Non-Goals:**

- Remove the visible validating/editing status transition.
- Change validation debounce or GraphQL authority.
- Change Save/Cancel restoration, native editor behavior, or action-prompt focus policy.
- Introduce global focus listeners or private Vaadin shadow access.

## Decisions

### Store a prompt-local clear focus intent on the property

The property will record clear focus when a `focusin` event identifies its Causeway clear suffix.
A `focusin` on another owned editor or action replaces or clears that intent.
A genuine `focusout` beyond the property clears it, while focusout caused by the property's own rendering does not.

This distinguishes the temporary body-focus gap created by node replacement from intentional external navigation.
Inferring focus only from `document.activeElement` was rejected because the second validation render observes the body.

### Add an adapter-level focusClear method

`<causeway-field-editor>` will expose an internal `focusClear()` method that records a request before upgrade and focuses the suffix once it is installed and visible.
The property can therefore issue the request synchronously against replacement markup without waiting for toolkit details.
If the field is empty or does not qualify for clearing, the request safely expires without redirecting focus elsewhere.

A MutationObserver or timer in the property was rejected because it would duplicate adapter lifecycle knowledge and make focus timing nondeterministic.

### Preserve intent across consecutive owned renders

While the active element is temporarily the document body and clear focus intent is pending, each interaction render forwards the same request to the newest adapter.
Once the clear button receives focus, normal focus events maintain the intent; moving to Save, Cancel, the field, or outside clears it.

### Test the complete asynchronous sequence

Foundation tests will cover a pre-upgrade clear-focus request, visibility gating, property intent lifecycle, consecutive validation renders, and external departure.
Petclinic Playwright will pause on Clear long enough for validation to complete, assert that focus remains there, then Tab to Save.

## Risks / Trade-offs

- **Risk:** Retained intent could steal focus after genuine departure.
  **Mitigation:** Clear it on non-rendering focusout beyond the property and on focusin to any other owned control.
- **Risk:** An empty replacement has no clear button.
  **Mitigation:** Focus only a visible clear suffix and discard the adapter request when upgrade completes without one.
- **Risk:** Multiple renders can target disconnected adapters.
  **Mitigation:** Existing generation and connection checks remain authoritative; each replacement owns only its own request.
- **Risk:** Status flashing could be mistaken for the focus defect.
  **Mitigation:** Keep status semantics unchanged and verify focus independently through active-element assertions.
