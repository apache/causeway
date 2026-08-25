## Why

The HTMX Petclinic brand link returns to the generic landing fragment but home resolution runs only during initial module startup, leaving the user stuck on the welcome card instead of returning to the configured Petclinic home object.
Brand navigation must apply the same GraphQL-authoritative home policy after every route-fragment replacement.

## What Changes

- Re-run bounded home resolution when HTMX installs a landing fragment, including brand-link navigation.
- Prevent concurrent or duplicate home-resolution work for the same landing fragment.
- Preserve canonical replacement history, route focus, menu availability, GraphQL authority, and native full-page fallback.
- Add route-policy and Petclinic browser regression coverage for navigating away and clicking the brand logo to return home.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Require every newly installed landing fragment to resolve the configured object home, not only the initial document load.

## Impact

The change affects the HTMX shell lifecycle in `causeway-htmx.mjs` and focused HTMX/Petclinic tests.
It does not change public web-component contracts, GraphQL operations, route encoding, CSP, toolkit policy, or application page resources.
