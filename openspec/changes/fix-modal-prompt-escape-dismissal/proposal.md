## Why

A modal action prompt whose first parameter uses the asynchronously upgraded reference editor can leave focus on the originating action.
Escape is then delivered outside the prompt controller and fails to dismiss the dialog, unlike inline and sidebar prompts.

## What Changes

- Preserve an initial focus request made before the reference editor's toolkit control finishes upgrading.
- Transfer deferred focus into the upgraded reference control so modal focus containment and Escape cancellation work immediately.
- Add foundation and Petclinic browser regression coverage for dismissing a reference-first modal prompt with Escape.
- Preserve parameter values, cancellation events, mutation safety, and originating-action focus restoration.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require deferred toolkit reference-editor focus to preserve modal prompt initial-focus and Escape-cancellation semantics.
- `generic-htmx-web-component-viewer`: Verify Petclinic's reference-first modal action prompt is dismissible with Escape without invocation.

## Impact

The change affects the foundation reference-editor adapter, interaction-focused tests, and Petclinic Playwright coverage.
It introduces no schema, route, domain, or third-party dependency changes.
