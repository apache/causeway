## Why

Secured HTMX and Vue currently remove the framework Logout menu action and render a separate username-and-form block in the shell header.
That replacement can leave the tertiary menu empty and hidden, diverges from the ordinary application-menu hierarchy, and duplicates navigation chrome solely because authentication is enabled.

## What Changes

- Preserve the GraphQL-authoritative primary, secondary, and tertiary menu structure in secured HTMX and Vue shells.
- Present the exact `causeway.security.LogoutMenu#logout` entry as **Sign out** within its existing tertiary menu location when a host authentication integration claims it.
- Keep logout execution host-owned by routing activation to the existing current-CSRF POST form rather than GraphQL invocation.
- Remove the separate visible secured-shell username and Sign out block while retaining a non-visual native logout form as the secure submission mechanism.
- Add a bounded host menu-action presentation hook that can relabel an exact action without changing its identity, order, authorization, disabled state, or invocation contract.
- Retain fail-closed suppression of framework Logout when no host authentication integration is active and leave similarly named application actions unchanged.
- Add native and Vaadin regression coverage for menu preservation, Sign out placement, exact interception, CSRF-safe submission, and ordinary unsecured behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Allow a host to apply bounded presentation-only relabeling to an exact semantic menu action while preserving authoritative menu and action semantics.
- `generic-htmx-web-component-viewer`: Distinguish unavailable framework Logout suppression from an installed host logout policy that keeps the menu affordance and claims it before GraphQL.
- `generic-vue-web-component-viewer`: Let an authenticated application opt the exact framework Logout action into menu presentation with a host-owned label and pre-invocation policy.
- `htmx-viewer-local-secman-authentication`: Move the secured Sign out affordance into the preserved tertiary menu and remove separate visible authentication chrome.
- `vue-viewer-local-secman-authentication`: Move the secured Sign out affordance into the preserved tertiary menu while retaining the hidden native CSRF form and bounded authentication context.

## Impact

The change affects foundation menu projection and semantic menu elements, HTMX authentication metadata and bootstrap policy, Vue viewer menu policy integration, both Petclinic secured shells, their CSS and tests, generated Vue frontend assets, and authentication/viewer documentation.
It introduces no new runtime dependency, endpoint, credential field, session behavior, GraphQL operation, or domain metamodel change.
