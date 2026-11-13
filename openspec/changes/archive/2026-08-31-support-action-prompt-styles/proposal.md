## Why

Parameterized actions currently always use one fixed modal presentation, even though Causeway already models inline, modal-dialog, and sidebar prompt intent.
Applications also need a framework-neutral way to override that presentation directly on an authored `<cw-action>` without changing action semantics.

## What Changes

- Expose canonical action prompt-style metadata through the shared rich GraphQL member metadata contract.
- Add an optional reflected `prompt-style` attribute/property to `<cw-action>` with authored presentation taking precedence over canonical metadata.
- Normalize effective prompt presentation to `INLINE`, `DIALOG_MODAL`, or `DIALOG_SIDEBAR`, with unsupported, generic, or inapplicable values falling back safely to a modal dialog.
- Render `INLINE` prompts in place of the associated property's primary presentation for directly nested property actions, restoring the property and action controls on completion or cancellation.
- Render `DIALOG_MODAL` prompts as accessible movable modal dialogs and `DIALOG_SIDEBAR` prompts as accessible vertical sidebars.
- Preserve authoritative parameters, preparation, validation, confirmation, invocation, cancellation, stale-generation protection, focus restoration, and at-most-once behavior across all styles.
- Demonstrate and verify the three effective styles in the Petclinic HTMX sample.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `rich-graphql-member-metadata`: Expose nullable canonical prompt-style metadata for actions.
- `domain-web-components`: Add declarative prompt-style precedence and accessible inline, movable-modal, and sidebar action-prompt presentation.
- `generic-htmx-web-component-viewer`: Demonstrate and verify all three prompt styles in Petclinic using ordinary authored components and canonical interactions.

## Impact

The change affects rich GraphQL metadata and tests; object, service, and application-menu metadata selections; action presentation normalization; the action and interaction-controller elements; member composition and prompt styling; fixtures and foundation tests; usage documentation; and Petclinic HTML and Playwright acceptance.
No new third-party dependency or action invocation API is introduced.
