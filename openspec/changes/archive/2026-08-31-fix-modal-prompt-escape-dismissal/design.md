## Context

The interaction controller renders a prompt and requests focus on its first `[data-causeway-editor]` in a microtask.
A `<cw-reference-editor>` remains as a stable host while its Vaadin control is loaded and created asynchronously.
Its current `focus()` implementation delegates only when that internal control already exists, so an earlier focus request is silently lost.
For a reference-first modal such as Petclinic `removePet`, focus can remain on the originating action outside the controller, preventing the prompt's Escape handlers from observing the key.

The field-editor adapter already preserves this lifecycle by recording a pending focus request and applying it after upgrade.

## Goals / Non-Goals

**Goals:**

- Preserve focus requests across asynchronous reference-editor upgrade.
- Ensure a reference-first modal receives initial focus and Escape cancellation through the existing controller lifecycle.
- Verify no invocation occurs and focus returns to the originating action after cancellation.

**Non-Goals:**

- Do not add a document-global Escape listener.
- Do not change modal markup, prompt-style selection, reference values, or Vaadin overlay Escape behavior.
- Do not make Escape cancel an invocation already in progress.

## Decisions

### Preserve intent on the stable reference-editor host

`CausewayReferenceEditorElement.focus()` will record that focus was requested before attempting to delegate to the current internal control.
After successful toolkit upgrade or native fallback rendering, the host will transfer the pending request to the actual control in a microtask.
This mirrors the established field-editor lifecycle and keeps focus ownership inside the public semantic adapter.

A document-global keydown listener was rejected because multiple controllers could react to one key and an Escape outside a non-modal prompt could cancel unrelated work.
Changing the controller to retry on arbitrary timers was rejected because the editor adapter is the component that knows exactly when its internal focus target becomes available.

### Retain the existing Escape event path

Once focus reaches the reference control, the reference host's capture listener and `causeway-reference-escape` event continue to drive `cancelPrompt()`.
No new cancellation path or prompt-state transition is introduced.
This preserves cancellation events, retained values, invocation guards, and focus restoration.

### Cover the real reference-first modal

Foundation tests will verify deferred focus transfer for toolkit and fallback controls.
Petclinic Playwright coverage will open `removePet`, confirm its `DIALOG_MODAL` reference prompt, press Escape from the focused reference editor, and assert prompt removal, zero mutation, and restored action focus.

## Risks / Trade-offs

- [A stale focus request could move focus after disconnection] → Apply it only when the current generation remains connected and the control is installed.
- [Programmatic focus options cannot be retained indefinitely] → Preserve the latest options only until the current upgrade resolves, then clear them.
- [The first Escape may belong to an open reference dropdown] → Retain existing reference-adapter behavior so an open dropdown closes first and a subsequent Escape cancels the prompt.

## Migration Plan

The change is internal and additive to existing focus behavior.
Rollback restores the prior reference-host focus delegation without affecting data or public markup.

## Open Questions

None.
