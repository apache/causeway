## 1. Consolidated Petclinic utility menu

- [x] 1.1 Remove the shared Petclinic `System` unreferenced-action catch-all while retaining one authoritative tertiary utility section.
- [x] 1.2 Add bounded host menu-title projection so the utility section displays the authoritative current username, renders it as text, and falls back to Account only when no trustworthy identity is available.
- [x] 1.3 Wire ordinary and secured HTMX and Vue hosts to their established current-user context without adding authentication discovery to generic component packages.
- [x] 1.4 Add an appearance token for exact `causeway.security.LogoutMenu#logout` and render **Sign out** with a distinct accessible outlined or equivalently bounded treatment in native and Vaadin-backed menus.
- [x] 1.5 Verify adjacent actions retain ordinary presentation and that username/title/appearance mapping changes neither identity, authorization, ordering, invocation, nor CSRF-safe Logout ownership.
- [x] 1.6 Verify ordinary and secured HTMX and Vue expose one intended authorized utility menu without a duplicate or empty System or Account menu.

## 2. Vue object-result navigation

- [x] 2.1 Capture and test the normalized `UserMenu#me` semantic result to identify why its advertised view-model identity is not reaching canonical Vue navigation.
- [x] 2.2 Correct the responsible shared query/normalization or Vue policy boundary without adding label-, action-, or `UserMemento`-specific identity inference.
- [x] 2.3 Add unit coverage for entity and view-model object results with valid identity and for object-shaped results that genuinely lack identity.

## 3. Vue result ownership and lifecycle

- [x] 3.1 Refactor default Vue result handoff so local-resource, object, scalar, collection, void, and unsupported outcomes dismiss transient source result presentation after acceptance.
- [x] 3.2 Clear only obsolete viewer-owned page or shell result presentation when a later unclaimed outcome navigates or replaces it.
- [x] 3.3 Preserve application-claimed result ownership and route-generation safeguards against stale asynchronous events.
- [x] 3.4 Add Vue browser coverage that opens the username-labelled utility menu, activates **Me**, verifies canonical generic object presentation without header result chrome, then activates **Configuration** and verifies no Me result remains.

## 4. Documentation and generated outputs

- [x] 4.1 Update HTMX, Vue, security-integration, and Petclinic documentation for the single username-labelled utility menu, distinct **Sign out** treatment, and Vue result lifecycle.
- [x] 4.2 Regenerate and verify Vue package declarations, bundles, source maps, and committed Petclinic production assets when affected.

## 5. Validation

- [x] 5.1 Run focused foundation and Vue policy tests for menu-title and action-appearance projection, exact Logout identity, object identity, source dismissal, outlet replacement, and claimed-policy behavior.
- [x] 5.2 Run ordinary and secured HTMX and Vue integration and headless browser suites, including username confidentiality and native/Vaadin **Sign out** presentation.
- [x] 5.3 Run packaging, license, deterministic-asset, formatting, inspection, and strict OpenSpec checks for all affected modules.
