## Why

Selecting a service action from an open application menu currently leaves the menu panel visible, obscuring the resulting prompt or page and making the interaction appear unfinished.
Users also need a predictable keyboard way to abandon an open menu without choosing an action.

## What Changes

- Close an expanded application menu immediately after an enabled menu action is selected.
- Close the active application menu when the user presses Escape and return focus to its disclosure control.
- Preserve existing menu opening, sibling-menu switching, outside-click dismissal, action dispatch, and responsive navigation behavior.
- Add automated component and browser coverage for action-selection and Escape dismissal.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Define transient application-menu dismissal behavior after action selection and Escape.

## Impact

- Semantic web-component menu behavior in `viewers/webcomponents/foundation`.
- HTMX Petclinic browser acceptance coverage in `viewers/webcomponents/sample-htmx-petclinic`.
- No public API, route grammar, dependency, or server-side domain behavior changes are expected.
