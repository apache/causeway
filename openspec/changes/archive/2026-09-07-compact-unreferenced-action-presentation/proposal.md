## Why

`<cw-unreferenced-actions>` currently introduces a block-level host and nested action-group box that can consume excessive horizontal or vertical space when it is authored inside a compact action toolbar.
The PetOwner experiment now exposes this mismatch, while generated actions should occupy the same compact wrapping flow as equivalent explicitly authored `<cw-action>` siblings.

## What Changes

- Make the ready `<cw-unreferenced-actions>` presentation layout-transparent while retaining its accessible generated action group.
- Size the generated action group to its action controls rather than to an unnecessary panel-like area, while allowing bounded wrapping in narrow containers.
- Preserve authoritative action order, labels, authorization, invocation, result, focus, and lifecycle behavior.
- Keep loading, empty, and error states non-presentational.
- Demonstrate and verify equivalent compact generated-action placement in the PetOwner HTMX and Vue hosts without moving action semantics into either host.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require generated unreferenced actions to participate as a compact, accessible, wrapping action cluster without an excessive host layout box.
- `generic-htmx-web-component-viewer`: Require an HTMX-authored action toolbar to preserve compact foundation-owned unreferenced-action presentation.
- `generic-vue-web-component-viewer`: Require equivalent Vue presentation and host neutrality for the generated action cluster.

## Impact

The change affects foundation unreferenced-action markup or styling, shared generated component styles, PetOwner sample composition, foundation presentation tests, and headless HTMX/Vue browser assertions.
It introduces no new dependency, action API, allocation rule, or host-owned rendering behavior.
