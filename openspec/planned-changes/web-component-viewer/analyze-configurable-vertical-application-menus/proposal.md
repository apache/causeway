## Why

The archived Menu Bar tranche maps each non-empty `<cw-menubar-primary>`, `<cw-menubar-secondary>`, or `<cw-menubar-tertiary>` tier to an independent internal horizontal `<vaadin-menu-bar>` while retaining Causeway-owned menu state and action execution.
Some applications need the same authorized application menu hierarchy in a persistent vertical region on the left rather than in a horizontal bar at the top.
Vaadin Menu Bar is designed for horizontal commands and overflow, so changing its flex direction with CSS would risk incompatible keyboard, focus, overflow, and accessibility behavior.
Vaadin Side Navigation is designed for vertical navigation, but its normal model is route links while Causeway application-menu leaves invoke parameterized or parameterless service actions through the interaction controller.
An evidence-backed analysis is required before selecting Side Navigation, a Causeway-native vertical renderer, or another internal control and before defining configuration or responsive shell behavior.

## What Changes

- Record the current top-mounted `<cw-menubars>` shell layout, three-tier identity, Menu Bar projection, service-action activation, focus, accessibility, responsive, authentication, delivery, failure, and native rollback contracts.
- Evaluate a bounded placement configuration with conceptual values `top` and `left`, defaulting to the existing `top` behavior and remaining orthogonal to `component-toolkit=vaadin|native`.
- Prototype a left-side stable-shell region without changing the public `cw-*` menu vocabulary, effective menu resource, GraphQL operations, application markup, authorization, or action-result policy.
- Evaluate an independently packaged internal `vaadin-side-nav` adapter against a Causeway-native vertical renderer rather than attempting to rotate `<vaadin-menu-bar>` with CSS.
- Determine whether Side Navigation items can represent Causeway service actions without path-based routing, executable application callbacks, direct GraphQL, direct navigation, or leaked raw Vaadin APIs.
- Preserve primary, secondary, and tertiary tier identity, menu order, non-activatable section headings, action order, labels, descriptions, icon hints, hidden state, disabled state, bounded disabled reasons, and opaque current-generation action identity.
- Define wide left placement and narrow-screen collapse behavior, including drawer or complete native fallback options, without losing actions, keyboard reachability, Escape dismissal, focus return, or zero-overflow guarantees.
- Measure stable-shell payload, independent Side Navigation closure size, CSP style hashes, package and license inventory, external requests, telemetry, accessibility, themes, zoom, reduced motion, forced colors, and failure isolation.
- Verify that login, authentication challenge, failure, logged-out, empty-menu, unsupported, explicit-native, and top-only states request no vertical-adapter bytes.
- Produce a separately reviewable implementation proposal with the selected component, property name, shell layout, responsive contract, migration behavior, budgets, and qualification matrix rather than changing production behavior during this analysis.

## Capabilities

### New Capabilities

- `configurable-application-menu-placement-analysis`: Defines evidence, prototypes, semantic gates, responsive alternatives, packaging boundaries, and an implementation recommendation for top or left application-menu placement.

### Modified Capabilities

None during the analysis.
A later implementation proposal would be expected to modify `domain-web-components`, `generic-htmx-web-component-viewer`, `reference-application-viewer-regression-suite`, and the internal Vaadin application-navigation adapter capabilities selected by the analysis.

## Impact

- Adds analysis documents, disposable shell and adapter prototypes, browser fixtures, measurements, and a decision record.
- Depends on the archived `use-vaadin-menu-bar-for-application-menus` behavior and keeps horizontal top placement as the accepted baseline.
- Treats `<vaadin-menu-bar>` as the horizontal adapter and evaluates `<vaadin-side-nav>` only as a separate vertical candidate.
- Does not add a production configuration property, change the HTMX shell, package Side Navigation for runtime use, expose raw Vaadin elements, or alter application-menu action semantics.
- Retains explicit native rollback and requires any later vertical adapter to fail independently from Menu Bar, references, fields, actions, Grid, GraphQL, routing, and authentication.
