## Why

An expanded application-menu panel can remain visibly open after keyboard focus tabs out of its semantic menubar and into route content.
This leaves a stale floating panel disconnected from the user's current focus and obscures page content.

## What Changes

- Collapse expanded menu panels when focus leaves their owning semantic menubar.
- Keep panels open while focus moves among disclosures and actions inside the same menubar.
- Preserve existing action selection, outside-click, Escape, sibling-opening, narrow-bar, and focus-restoration behavior.
- Add focused component and Petclinic browser regression coverage for forward Tab navigation out of an expanded menu.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Extend transient application-menu dismissal to close expanded panels when keyboard focus leaves the owning menubar.

## Impact

The change affects focus handling in the semantic menubar component and focused foundation/Petclinic tests.
It does not change GraphQL operations, action dispatch, menu resources, route behavior, CSP, toolkit policy, or public component vocabulary.
