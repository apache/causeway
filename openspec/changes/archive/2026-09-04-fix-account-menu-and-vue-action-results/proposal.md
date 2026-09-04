## Why

The Petclinic HTMX and Vue shells currently expose both `System` and `Account` utility menus even though `System` only duplicates **Me**, and the utility menu does not identify the signed-in user or distinguish the consequential **Sign out** action.
Vue also mishandles tertiary service-action results by leaving source result chrome visible, failing to navigate to `UserMemento`, and retaining stale results across later navigation.
These regressions make the secured Vue experience materially less coherent than the HTMX reference and leave both hosts with misleading utility navigation.

## What Changes

- Consolidate Petclinic utility actions into one authoritative utility menu for ordinary and secured HTMX and Vue, removing the superfluous `System` catch-all and separate `Account` label.
- Label that menu with the current username supplied by authoritative application or authentication context, with a neutral Account fallback only when no authenticated display identity is available.
- Give exact `causeway.security.LogoutMenu#logout`, presented as **Sign out**, a distinct accessible outlined treatment in native and Vaadin-backed menus.
- Preserve exact authorization, action identity, ordering, and host-owned Logout behavior for **Me**, **Configuration**, and **Sign out**.
- Make Vue navigate complete `UserMemento` object results to their canonical object route, matching the existing HTMX behavior.
- Ensure Vue dismisses the menu action's transient source result presentation once default host result handling begins.
- Ensure object-result navigation and subsequent actions clear obsolete shell/page result presentation so a prior unsupported or transient result cannot remain above the newly selected object.
- Add native and Vaadin-backed browser regressions for the username-labelled consolidated menu, distinct **Sign out** treatment, **Me**, **Configuration**, source-panel dismissal, canonical navigation, and stale-result replacement.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Require the Petclinic reference shell to expose one username-labelled utility menu, with distinct **Sign out** presentation, and no duplicate System or Account menu.
- `generic-vue-web-component-viewer`: Require the same consolidated username-labelled utility menu and deterministic Vue service-action result ownership, object navigation, source dismissal, and stale-result cleanup.
- `htmx-viewer-local-secman-authentication`: Require the secured HTMX utility menu to use the authenticated username, contain the authorized utility action set, and distinguish **Sign out** without duplicate menus.
- `vue-viewer-local-secman-authentication`: Require the equivalent secured Vue utility menu and correct **Me**/**Configuration** result behavior without duplicate or stale presentation.

## Impact

The change affects the shared Petclinic menu layout, bounded menu-title and action-appearance projection, HTMX/Vue host identity wiring, Vue semantic result policy and lifecycle, Vue package declarations/build output if public contracts change, ordinary and secured HTMX/Vue acceptance tests, and viewer/sample documentation.
The generic presentation layer remains authentication-neutral: applications or authentication integrations supply display identity, and components neither discover users nor infer Logout from text.
The change does not alter domain action invocation, GraphQL authorization, semantic result normalization, CSRF-safe Logout ownership, canonical route encoding, or unrelated applications that author different menu layouts and presentation policies.
