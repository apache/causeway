## Why

Void actions normally refresh the current object route, but that behavior leaves a deleted object page in a terminal error state when the action removed its target.
The generic HTMX Web Components viewer should recover from this specific post-action condition by returning the user to the viewer home page.

## What Changes

- Track when the viewer is refreshing an object route after a successful void action.
- If that refresh reports that the object no longer exists, navigate to the viewer home route instead of leaving the deleted object page visible.
- Preserve current-route refresh behavior for successful void actions whose target still exists.
- Add browser regression coverage using `PetOwner#delete`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Define home-route fallback when a void action deletes the object represented by the current route.

## Impact

The change affects the generic HTMX viewer's browser-side action-result and object-state routing policy, plus the Petclinic Playwright journey for destructive object actions.
It does not change GraphQL action-result semantics or application domain APIs.
