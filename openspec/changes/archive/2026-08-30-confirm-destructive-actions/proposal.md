## Why

Actions declared with `NON_IDEMPOTENT_ARE_YOU_SURE` currently reach the Web Components interaction controller without their destructive confirmation semantic, so the standard controller can invoke them immediately.
Petclinic also advertises owner deletion while relying on a fixture-specific disable rule rather than the pinned original application's visit-based veto.

## What Changes

- Expose a side-effect-free `areYouSure` flag for action wrappers through shared rich GraphQL member metadata.
- Carry that canonical flag through object and service action presentation without allowing HTML presentation overrides to weaken it.
- Require an accessible explicit confirmation before invoking an are-you-sure action, after successful parameter validation for parameterized actions and directly after activation for parameterless actions.
- Preserve cancellation, focus restoration, disabled-action gating, stale-response protection, and authoritative GraphQL validation and invocation behavior.
- Demonstrate the behavior with Petclinic owner deletion while restoring the pinned original policy that disables Delete when related visits exist.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `rich-graphql-member-metadata`: Expose canonical are-you-sure action semantics as narrow local metadata.
- `domain-web-components`: Require the standard interaction controller to confirm destructive action invocation accessibly at the correct point in parameterless and parameterized flows.
- `generic-htmx-web-component-viewer`: Restore Petclinic's original visit-based owner-deletion veto and verify the destructive confirmation journey.

## Impact

The change affects rich GraphQL metadata generation, Web Components action reads and interaction state, shared action-prompt markup and styling, foundation tests, Petclinic domain behavior, integration tests, and Playwright acceptance.
The metadata addition is backward compatible because existing GraphQL documents and response shapes remain unchanged unless clients select the new field.
