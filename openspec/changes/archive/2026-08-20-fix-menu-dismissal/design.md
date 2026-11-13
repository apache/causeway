## Context

Application menu panels are rendered and owned by `CausewayMenubarElement` in the framework-neutral web-component foundation.
The component already controls each disclosure button's `aria-expanded` state and the corresponding panel's `hidden` state, while service-action requests are consumed by the interaction controller and may open a prompt, present a result, or navigate the HTMX route region.
Dismissal therefore needs to happen at the menu component boundary without coupling the semantic component to HTMX routing or to any particular action outcome.

## Goals / Non-Goals

**Goals:**

- Make selecting any enabled service action close its containing menu deterministically.
- Make Escape close the active menu without dispatching an action and restore focus to the menu disclosure.
- Keep disclosure state and panel visibility synchronized through one close path.
- Protect the behavior with component-level and Petclinic browser acceptance tests.

**Non-Goals:**

- Change service-action preparation, invocation, validation, prompt, result, or navigation semantics.
- Change menu data loading, layout parsing, responsive bar collapse, or server-rendered shell structure.
- Introduce an HTMX-specific menu contract or a new dependency.

## Decisions

### Keep dismissal ownership in the semantic menubar component

`CausewayMenubarElement` will remain responsible for closing its transient menu panels because it owns the disclosure and panel DOM state.
The HTMX viewer will observe the resulting action or navigation as it does today and will not reach into menu internals.

An HTMX document-level workaround was rejected because it would leave standalone and other viewer consumers with inconsistent behavior and would duplicate the component's accessibility state transitions.

### Reuse one idempotent close operation

Action selection and Escape handling will use the same close operation that sets the disclosure to `aria-expanded="false"`, hides its controlled panel, and optionally restores focus.
The enabled action's semantic request will still be dispatched exactly once, with dismissal ordered so synchronous consumers cannot leave the panel visually open.

Directly changing CSS visibility was rejected because it could desynchronize visual state from `aria-expanded` and `hidden`.

### Preserve focus hand-off semantics

Escape will restore focus to the disclosure that opened the menu.
Action selection may use that disclosure as fallback focus, while an action prompt, route transition, or result handler remains free to move focus according to its existing interaction contract.

A global Escape listener was rejected because key handling should remain scoped to the active menubar and multiple menubars must not close one another unexpectedly.

### Test both component state and browser-observable behavior

The foundation test will verify action dispatch, disclosure state, panel visibility, Escape cancellation, and focus restoration.
The Petclinic Playwright test will exercise `Pet Owners > Create` and Escape against the rendered application menu so event propagation and prompt focus are covered in a real browser.

## Risks / Trade-offs

- [Risk] Closing before an action consumer completes could move focus briefly before a prompt takes focus. → Keep disclosure focus as a fallback and assert the final prompt focus in browser acceptance.
- [Risk] Re-rendering menu state during action preparation could reopen a panel. → Keep expansion as transient local UI state and add assertions that wait through prompt or result presentation.
- [Risk] Escape could accidentally collapse the responsive bar as well as the menu. → Give an expanded menu first priority and stop after closing only that menu panel.
