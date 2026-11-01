## Context

The archived menu-bar foundation already owns application-menu capability discovery, resource parsing, semantic primary, secondary, and tertiary tiers, generation-scoped refresh, independently prepared service-action state, action execution, result publication, responsive native disclosures, focus, diagnostics, and host policy.
The archived Vaadin reference, editor, read-only field, action, and Grid tranches established the common `component-toolkit=vaadin|native` policy, independently packaged free-core closures, exact-hash CSP, family-scoped failure, lifecycle revisions, and complete native rollback.
Menu Bar differs from member-local adapters because `<cw-menubars>` belongs to the stable authenticated shell and its closure can affect nearly every authenticated route.
Vaadin Menu Bar accepts nested item objects, disabled state, item-selected events, overflow, and keyboard behavior, but those objects and events cannot become application contracts or bypass Causeway interaction and navigation policy.
The implementation must preserve the existing public `cw-*` vocabulary and established native behavior while deciding whether the stable-shell payload and responsive behavior justify default internal adaptation.

## Goals / Non-Goals

**Goals:**

- Use one qualified internal Vaadin Menu Bar for each non-empty semantic primary, secondary, or tertiary tier.
- Preserve exact menu, section, action, service, visibility, usability, disabled-reason, icon-hint, description, preparation, refresh, invocation, result, and focus semantics.
- Keep Causeway application-menu state and service-action interaction as the only authorities.
- Preserve wide and narrow keyboard, pointer, overflow, dismissal, focus-restoration, and accessibility contracts.
- Reuse the common toolkit policy while isolating Menu Bar packaging, CSP, family health, and fallback from every other Vaadin family.
- Prove deterministic free-core packaging, acceptable stable-shell cost, no telemetry, no external requests, and complete explicit-native rollback.
- Preserve login and authentication-challenge chrome without loading Menu Bar assets.

**Non-Goals:**

- Expose raw `vaadin-menu-bar`, item objects, item-selected events, methods, properties, theme internals, or Shadow DOM as supported application APIs.
- Merge the three Causeway semantic tiers into one public or internal navigation hierarchy.
- Move GraphQL execution, service-action preparation, validation, authorization, invocation, action-result handling, routing, or refresh ownership into Vaadin callbacks.
- Add application routing, hyperlinks, client-owned menu definitions, speculative prefetch, or local authorization filtering.
- Add Vaadin Flow, Binder, server-side Vaadin state, commercial components, telemetry, CDN assets, or external runtime resources.
- Change public menu resources, GraphQL operations, canonical routes, authentication behavior, or application markup.
- Remove the established native renderer or its responsive disclosures.

## Decisions

### Keep the existing Causeway menu state machine authoritative

`MenuBarsContextController` continues to discover capabilities, load and parse the menu resource, prepare service-action state, cancel superseded work, publish diagnostics, refresh generations, and execute through the existing interaction controller.
The Menu Bar adapter receives only an immutable Causeway projection of an already accepted bar generation.
An item-selected callback resolves a bounded internal semantic identity and delegates to the same Causeway service-action activation path used by native buttons.
The callback never constructs GraphQL, invokes navigation, interprets action results, or mutates authoritative menu state.

The alternative of letting Vaadin items contain executable callbacks is rejected because it would create a second invocation path and make stale item objects authoritative.

### Preserve one independently qualified adapter per semantic tier

Each non-empty primary, secondary, or tertiary bar remains represented by its existing public element and receives its own internal Menu Bar control.
The adapter does not merge tiers, reorder menus, or move actions between sections.
An empty tier remains hidden exactly as it is today.
A hierarchy that cannot be represented without semantic loss keeps that complete tier native rather than mixing native and Vaadin controls within one tier.
A closure or definition failure is family-scoped and returns all connected tiers to native rendering because partial toolkit family health would be confusing and difficult to recover consistently.

The alternative of one Menu Bar for all tiers is rejected because it would erase stable tier identity, styling, source order, independent state, and host placement.

### Project immutable item descriptors with opaque semantic identities

The host projects each accepted bar into frozen descriptors containing stable generation, tier, menu, section, action, service logical type, action ID, label, description, icon hint, disabled state, and bounded disabled reason.
Top-level items correspond to Causeway menus in source order.
Labeled sections become nested grouping items and unlabeled sections contribute their actions in order without manufacturing selectable commands.
Only leaf action items are activatable.
Adapter-private item objects carry opaque lookup keys into the current projection, and stale or unknown keys are ignored.
Values, GraphQL variables, prepared argument snapshots, full errors, and interaction-controller internals are excluded from item objects and diagnostics.

The alternative of identifying selected actions by visible label is rejected because labels are not stable or unique and may be localized.

### Preserve Causeway-owned action activation and focus intent

The existing bar element owns activation, closes transient menu state, requests action execution through the generated interaction controller, and publishes established semantic outcomes.
Before activation, the host records a semantic focus intent containing tier and action identity rather than retaining a Vaadin Shadow DOM node.
After preparation refresh, action completion, cancellation, adapter rerender, or responsive fallback, focus restoration resolves that intent against the current semantic projection or a safe tier-level target.
Disabled items never invoke, and their existing bounded reason remains available through accessible description and native fallback.

The alternative of restoring a recycled internal item element is rejected because Menu Bar may recreate or overflow internal controls.

### Qualify responsive use without weakening the native contract

A tier qualifies only when policy is Vaadin, family health is available, the element is connected and visible, the generation is current, the tier is non-empty, every hierarchy node is representable, and current width can preserve every authorized action through reviewed Menu Bar overflow behavior.
The adapter uses a container observer and a revision token so width changes are applied outside observer delivery and stale asynchronous work cannot restore a superseded mode.
A narrow tier uses Vaadin only after browser evidence proves nested overflow, internationalized overflow labeling, Escape behavior, focus return, order, and zero horizontal overflow.
If that proof fails for a hierarchy or width, the complete tier uses the established native bar disclosure.
Responsive transitions reuse accepted semantic state and issue no GraphQL request.

The alternative of assuming Vaadin overflow is always equivalent is rejected because tier labels, nested sections, disabled reasons, and focus restoration can differ at narrow widths.

### Add one independent Menu Bar family loader and lifecycle

A private adapter module owns dynamic import, custom-element definition waiting, timeout, projection, listener installation, rendering, cleanup, and family health.
Every upgrade captures host connection, menu generation, adapter revision, responsive revision, policy revision, and family revision.
Disconnect, tier replacement, refresh, policy change, width disqualification, or family failure invalidates pending work before native rendering.
Cleanup removes listeners, item arrays, observers, focus intents, and generated controls exactly once.
One family failure emits one bounded value-free diagnostic, marks Menu Bar unavailable, rerenders connected tiers natively, and leaves references, fields, actions, Grid, GraphQL, routing, and authentication unchanged.
A later explicit connected retry can recover the family without reloading the route.

The alternative of embedding adapter behavior directly throughout `menubar-element.mjs` is rejected because it would obscure native authority and make lifecycle isolation difficult to test.

### Reuse the common component policy without another public switch

Default or explicit `component-toolkit=vaadin` enables qualified Menu Bar together with existing adapters.
Explicit `component-toolkit=native` disables Menu Bar, contributes no Menu Bar CSP hashes, and requests no Menu Bar bytes.
When `component-toolkit` is absent, the documented deprecated `editor-toolkit=vaadin|native` compatibility mapping continues to control the complete component policy and therefore Menu Bar.
Older pilot properties remain limited to their original families and do not enable Menu Bar.
No Menu-Bar-specific application property is introduced.

The alternative of an independent Menu Bar toggle is rejected because the common policy intentionally provides one operational rollback while family qualification already supplies automatic fallback.

### Load only for qualified authenticated stable-shell menus

The HTMX shell publishes a same-origin Menu Bar closure URL and family policy only under the resolved Vaadin component policy.
The closure is imported after an authenticated stable shell has connected a non-empty representable tier, rather than on login, authentication challenge, static shell startup, or routes with no effective menu.
Because most authenticated applications have menus, its normal cost is treated as stable-shell cost and receives a stricter compressed budget and request-count review than route-local adapters.
Module import remains deduplicated across tiers and routes.

The alternative of unconditional shell import is rejected because it would penalize login, failure, empty-menu, unsupported, and explicit-native states.

### Package and qualify an independent deterministic free-core closure

A dedicated `foundation/vaadin-menubar` package pins Vaadin Menu Bar and only the production transitive closure required by its browser entry point.
Ordinary Maven builds verify checked-in generated assets and legal metadata without running npm.
Explicit regeneration installs pinned inputs, audits production dependencies, builds deterministic bytes, records checksums and exact style hashes, rejects telemetry and development-only modules from runtime paths, and refreshes licenses.
The closure has independent raw and compressed budgets and does not absorb reference, field, action, or Grid bundles.
CSP remains exact-hash with `style-src-attr 'none'`, no nonce weakening, and no `unsafe-inline`.

The alternative of adding Menu Bar to an existing closure is rejected because stable-shell cost, failure, CSP, and rollback must remain independently reviewable.

### Release-qualify behavior before default adaptation

Foundation tests cover projection, qualification, hierarchy, activation, disabled state, lifecycle revisions, family failure, recovery, responsive switching, and native parity.
Browser matrices cover primary, secondary, tertiary, nested and labeled sections, hidden and disabled actions, parameterized actions, action results, overflow, keyboard, pointer, Escape, focus restoration, refresh, cancellation, stale responses, authentication exclusions, themes, reduced motion, forced colors, zoom, CSP, external requests, and route replacement.
Default and explicit-native Petclinic and Reference Application journeys remain mandatory.
The family is accepted only if no unexpected axe, CSP, console, page, external-request, duplicate-control, stale-item, clipping, overlay, focus, order, or overflow failures remain.

## Risks / Trade-offs

- [Stable-shell payload is paid on most authenticated pages] → Keep a separate closure, enforce compressed and request budgets, lazy-load only after a qualified non-empty tier connects, and retain native rendering if cost is not justified.
- [Vaadin overflow or nested keyboard behavior differs from established semantics] → Qualify representative hierarchies at wide and narrow widths and keep the complete tier native whenever equivalence is not proven.
- [Section grouping can create selectable or misleading items] → Represent section labels only as non-action grouping nodes and permit activation only for opaque leaf action identities.
- [Stale item objects can invoke obsolete actions] → Bind every item to generation and revision, resolve against the current immutable projection, and ignore stale or unknown keys.
- [Disabled reasons can disappear inside toolkit internals] → Project bounded reasons to accessible descriptions and preserve the complete native fallback.
- [Three internal controls can duplicate overflow or focus state] → Keep tier-local controls, semantic focus intents, and family-scoped lifecycle cleanup with cross-tier browser tests.
- [Responsive observers can loop or race asynchronous import] → Apply width state outside observer callbacks and gate every completion by connection, generation, policy, responsive, adapter, and family revisions.
- [One family failure could break the stable shell] → Fail closed to established native menus while leaving all data, interaction, route, and authentication state intact.
- [Toolkit diagnostics could leak menu or action data] → Emit only family, phase, bounded classification, revision, and tier metadata.
- [Pinned free-core dependencies can still include telemetry code] → Audit import reachability and runtime requests, reject activation, and verify zero external requests under production CSP.

## Migration Plan

1. Record current native hierarchy, action, focus, responsive, accessibility, request, and visual baselines.
2. Build and qualify the independent Menu Bar closure without changing runtime selection.
3. Add immutable projection, adapter, qualification, lifecycle, family health, and focused tests behind internal configuration.
4. Extend common policy publication, same-origin packaging, exact-hash CSP, diagnostics, themes, samples, and authenticated stable-shell lazy delivery.
5. Run complete default and native foundation, HTMX, Petclinic, Reference Application, accessibility, responsive, security, budget, vulnerability, legal, and RAT gates.
6. Enable qualified default adaptation only after retained evidence demonstrates semantic parity and acceptable stable-shell cost.
7. Roll back operationally with `causeway.viewer.webcomponents.htmx.component-toolkit=native` without GraphQL, route, persisted-data, authentication, menu-resource, or application-markup changes.

## Open Questions

No blocking design questions remain after promotion.
Implementation evidence must still determine the accepted Menu Bar version, exact dependency closure, style hashes, raw and compressed budgets, and whether narrow Vaadin overflow qualifies every representative hierarchy or remains selectively native.
