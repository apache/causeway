## Context

The shared picker bridge currently moves focus from a time input to Vaadin's shadow toggle, consumes Enter or Space, and schedules `open()` with `setTimeout` after returning focus to the input.
That delay made a state-based Playwright assertion pass but separates overlay creation from the trusted keyboard event and permits asynchronous prompt replacement to supersede the target.
A robust browser contract must verify the actual Vaadin overlay is visibly presented, not merely that a Boolean briefly becomes true.

The application theme treats `textarea:not([slot="input"])` as native.
Vaadin Text Area uses an internal `<textarea slot="textarea">`, so the selector applies native border, padding, sizing, and focus rules inside the toolkit control.
The desired outer orange boundary in the screenshot is Vaadin's own focus ring; the inner textarea boundary is the leaked application-native styling.

Bounded collection windows already carry normalized `totalCount` and `countAvailable` metadata.
The pager currently renders only `Items rangeStart–rangeEnd`, even when a safe authoritative total is present, while correctly avoiding invented totals when metadata is unavailable.

## Goals / Non-Goals

**Goals:**

- Make Enter and Space on a focused clock affordance synchronously present the real time overlay.
- Verify the overlay itself is open and visible in a real browser.
- Leave exactly the toolkit-owned multiline boundary and focus ring around Vaadin Text Area.
- Include a safe authoritative total in bounded collection range labels on every page.
- Preserve unknown-total wording and all existing interaction, paging, validation, and fallback semantics.

**Non-Goals:**

- Do not replace Vaadin's time overlay or implement a Causeway-owned time list.
- Do not remove focus indication or borders from native fallback textareas.
- Do not infer collection totals from offsets, page size, navigation flags, or loaded rows.
- Do not change GraphQL collection window metadata or requests.

## Decisions

### Open the picker synchronously during keyboard activation

The qualified toggle will consume Enter and Space and, during the trusted key event, return focus to the picker input and invoke the pinned public `open()` method.
The fallback branch used by test adapters may set `opened`, but production Vaadin controls use `open()`.
No timer or microtask will defer activation.

Returning focus to the input matches Vaadin pointer handling, which prevents toggle mousedown from blurring the input before opening.
It also lets the picker keep its overlay open rather than interpreting toggle focus as an external blur.

Calling `open()` from a timer was rejected because the control can be superseded and browser popover presentation can diverge from property state.
Synthesizing pointer events was rejected because untrusted synthetic clicks are not equivalent to user activation.

### Assert visible overlay presentation

Foundation browser audit and Petclinic Playwright coverage will require the nested time picker to report `opened`, expose its Vaadin overlay as `:popover-open`, and have a non-empty visible rectangle after keyboard activation.
The tests will stabilize any prior parameter preparation before interacting so they exercise the current generation rather than a replaced control.

Property-only tests of `opened` were rejected because they reproduced the false positive reported by the user.

### Scope native textarea styling to unslotted controls

Every application-theme selector that currently uses `textarea:not([slot="input"])` will use `textarea:not([slot])` instead.
This retains the application border, padding, sizing, and focus outline for native fallback textareas while excluding Vaadin's `slot="textarea"` implementation detail.
The Vaadin input container remains responsible for the one visible boundary and focus ring.

Broadly removing textarea borders was rejected because native toolkit rollback must remain visibly operable.

### Add totals only when authoritative and safe

The bounded pager will append ` of <totalCount>` when `totalCount` is a non-negative safe integer.
When no items are returned, established `No items` wording remains.
When total metadata is absent or invalid, the pager retains range-only wording and does not infer a value.

This applies equally to native bounded tables and bounded Grid mode because the collection host owns the pager.

## Risks / Trade-offs

- [Synchronous focus transfer could alter keyboard focus expectations] → Preserve the explicit toggle Tab stop, open only on Enter or Space, and verify reverse Tab and prompt containment separately.
- [Vaadin changes its overlay implementation] → Keep the browser assertion inside the pinned adapter audit so dependency drift fails qualification.
- [Changing textarea selectors could affect other slotted controls] → Scope only the textarea selector and verify native fallback and Vaadin multiline controls independently.
- [A malformed total could leak into UI] → Require a non-negative safe integer and retain unknown-total wording otherwise.

## Migration Plan

The behavior applies automatically to existing qualified controls and bounded collections.
No application markup, GraphQL query, configuration, or persisted data migration is required.
Reverting restores delayed keyboard activation, the broader textarea selector, and range-only pager labels.

## Open Questions

None.
