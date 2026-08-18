## Context

The framework-neutral components own GraphQL projections, domain behavior, effective object layout, menu layout, interaction state, and semantic results.
A generic viewer still needs a stable shell and a router that maps bookmarks and application entries to pages.

The customization review considered a page provider inside `<causeway-object>` but rejected it.
Every host framework already has a routing boundary that can select a custom page for a logical type or route to a generic page containing `<causeway-object>`.
HTMX expresses that boundary through server routes and replaceable HTML fragments.

The existing `sample-html` application is intentionally a low-level component acceptance fixture.
It must retain its stable path and automation contracts, but its visual defects should not become the default presentation of the new viewer.
A separate Petclinic sample supplies a cohesive application while `sample-html` adopts the same corrected design tokens and component styling.

The Petclinic source baseline is Apache-licensed `apache/causeway-app-petclinic` commit `16a10608129ca9ce8ae04d21df1462f4d69ac018`.
That application targets Causeway 2.0.0, so the copy is a provenance-preserving port of the Pet Owner, Pet, Visit, home-page, fixture, menu, and grid material to current APIs rather than an unmodified nested checkout.
The unrelated starter `SimpleObject` module, historical security and operational extensions, and obsolete infrastructure are not copied.

The current Causeway Wicket viewer uses a sticky primary-color navbar, left-aligned application menus, right-aligned secondary and user menus, contained page content, icon-and-title object headers, grouped action buttons, cards, tabs, aligned labels and fields, compact collection tables, modal prompts, visible disabled state, and responsive navbar collapse.
Those characteristics are the visual reference, but the HTMX viewer uses ordinary CSS and public custom-element hooks rather than Wicket markup or Bootstrap classes.

## Goals / Non-Goals

**Goals:**

- Provide canonical bookmark routes, deep linking, refresh, and browser history.
- Resolve custom logical-type pages before the generic object page.
- Keep route fragments free of metamodel and layout implementation knowledge.
- Preserve one route-level object context for custom and generic pages.
- Compose menu bars and configurable object-home behavior in a stable shell.
- Provide replaceable result, navigation, page, and theme policy.
- Ship an executable Petclinic sample with deterministic fixture data and both HTMX and Wicket routes over the same model.
- Deliver a cohesive responsive visual system that closely resembles the information hierarchy and interaction affordances of the Wicket viewer.
- Correct the existing `sample-html` styling without breaking its vanilla-HTML or automation contracts.

**Non-Goals:**

- Teaching `<causeway-object>` about custom pages or routers.
- Parsing grid or menu resources in the HTMX viewer.
- Translating GraphQL JSON or constructing domain operations in HTMX server handlers.
- Inventing a public home-action descriptor when the rich application-entry contract exposes only object homes.
- Pixel-identical reproduction of one Wicket theme or reuse of Wicket-generated HTML.
- Adding Bootstrap, Wicket, or Petclinic dependencies to the component foundation or generic HTMX viewer.
- Porting Petclinic's unrelated SimpleObject starter module, SecMan administration, audit, command, execution, Quartz, Flyway, PDF, calendar, or historical deployment examples.
- Reproducing authentication pages or every extension of existing viewers initially.
- Defining framework APIs for Vue or Svelte.

## Decisions

### Make routing the customization boundary

The server route resolver accepts a canonical bookmark route and determines the public logical type from the route value.
It first checks an application registry keyed by exact logical type.
A match renders the registered server fragment factory beneath the route object context.
Absence renders the generic `<causeway-object>` fragment.

`<causeway-object>` is never asked whether a custom page exists.
It remains a pure semantic object renderer that uses the effective or fallback grid.

### Use one canonical route grammar

The default viewer base path is `/htmx`, configurable through `causeway.viewer.webcomponents.htmx.base-path`.
The canonical object route is `<base-path>/object/<logical-type>/<identifier>`, where each route value is independently UTF-8 percent-encoded and decoding rejects malformed escapes, empty values, encoded path separators, control characters, dot segments, overlong values, and non-canonical round trips.
Logical route identity is the decoded public logical type and opaque identifier pair, not the encoded spelling.

Semantic object-navigation events are converted to the same canonical route.
Direct loads, refresh, back, forward, home navigation, menu-originated object results, and object links therefore converge on one resolver.

### Keep HTMX above the component data plane

HTMX owns shell navigation, fragment replacement, history, loading indicators, route announcements, and route errors.
The implementation uses HTMX `2.0.6` from `org.webjars.npm:htmx.org` and the documented `htmx.ajax()` target, swap, and push options for semantic-event navigation.
Normal anchors remain present so routes work without scripted click handlers and preserve open-in-new-tab behavior.

Semantic components continue to obtain all domain state through GraphQL contexts.
HTMX does not construct GraphQL documents, inspect member wrappers, parse layouts, or convert domain values.

### Preserve one route-level object context

Every custom or generic object page is rendered beneath one route-level `<causeway-object-context>`.
Custom pages can compose standard member components, `<causeway-object>`, ordinary HTML, and application custom elements without reimplementing GraphQL execution.
Changing routes replaces the complete route context, creates a new generation, and disconnects obsolete page requirements.

The generic server fragment contains only safely escaped route values and semantic custom elements.
It does not resolve or authorize a domain object server-side.
The object context performs established GraphQL resolution and presents bounded not-found, access-denied, partial, or terminal state without disclosing object or authorization details.

### Register custom pages as server fragment factories

The initial registry accepts ordered Spring beans implementing a small `HtmxPageFragmentFactory` SPI.
Each factory declares one exact public logical type and renders trusted application-owned HTML for a validated route descriptor.
Duplicate logical-type registrations fail at startup rather than depending on bean order.

Factories receive only route identity and documented rendering helpers.
They do not receive Causeway metamodel objects, persistence sessions, GraphQL responses, or authorization internals.
Templates and client-only registries are deferred until a concrete need appears.

### Keep menus in the stable shell

The full-page shell owns one `<causeway-graphql-client>`, `<causeway-menubars>`, application branding, route announcement region, loading indicator, result region, and route-content region.
Object-fragment replacement does not recreate menu state.
Authorization, locale, or application-entry generation changes invalidate menus through their own contract rather than incidental page navigation.

The shell returns a complete document for ordinary requests and the route fragment only when `HX-Request: true` is present.
History restoration can request the complete page, and the server never returns a fragment where a complete document is required.

### Keep home behavior within the accepted object-home contract

The browser shell uses the existing targeted rich application-entry discovery to obtain an optional object home.
An object home is translated to the canonical route under replaceable home policy.
Absent, hidden, invalid, unsupported, or partially failing home entries produce a bounded local landing state.

No home service-action descriptor or invocation contract is invented.
If Causeway later exposes such a public contract, it will be added through a separate change.

### Keep result behavior replaceable

Default result policy routes object results, presents scalar and collection results in an accessible shell region, and announces void completion before refreshing the current object context when one exists.
Applications can replace behavior per scalar, object, collection, or void kind without changing component interaction semantics.
The policy never assumes that an object result must navigate when an application override is present.

### Ship the viewer as an optional server module

`viewers/webcomponents/htmx` contains the Spring configuration, route codec, fragment registry, controller, shell renderer, HTMX WebJar dependency, browser bridge, and default theme.
The viewer is enabled only when an application imports its module configuration.
Applications can override base path, brand, fragments, route policy, home policy, result policy, and CSS variables without forking semantic components.

The browser package exposes versioned assets beneath a module-owned path and uses external scripts and styles compatible with a restrictive same-origin content-security policy.
No inline script is required.

### Port Petclinic as the cohesive acceptance application

`viewers/webcomponents/sample-htmx-petclinic` copies and ports the pinned Petclinic domain under Causeway's current Jakarta, persistence, programming-model, GraphQL, and layout contracts.
The sample retains recognizable Pet Owners, Pets, Visits, home-page collections, service actions, object actions, choices, defaults, validation, effective menus, effective grids, and deterministic fixture data.
A provenance document lists the source repository, pinned commit, copied concepts, intentional omissions, and porting changes.

The sample includes GraphQL and HTMX viewers by default.
It includes the Wicket viewer as the live visual comparison at its documented `/wicket/` base path, while `/htmx` remains the generic viewer route.
Both viewers use the same model, fixture data, authorization, menu definitions, and grid definitions.

### Use one Wicket-inspired visual system without Bootstrap coupling

The default theme defines public CSS custom properties for primary navigation, page background, surfaces, borders, text, muted text, links, focus, success, warning, danger, spacing, radii, shadows, label width, content width, and narrow breakpoint.
It styles stable shell landmarks and documented component light-DOM hooks for menus, object headers, action groups, layout cards, tabs, properties, collections, tables, editors, prompts, results, loading, disabled state, diagnostics, and errors.

The wide layout follows Wicket's information hierarchy: sticky primary-color navbar, contained content, title and actions on one header line where space permits, card-backed groups, aligned property labels, compact tables, and bounded modal prompts.
The narrow layout collapses navigation accessibly, stacks columns and property labels, keeps tables scrollable within their region rather than the page, and preserves document and focus order.

Light and dark palettes meet contrast requirements, visible focus is never removed, reduced-motion preferences disable decorative transitions, and forced-colors mode retains boundaries and focus.
The theme is ordinary CSS and does not require Bootstrap classes or JavaScript.

### Repair the vanilla sample without changing its role

`sample-html` keeps `/sample-html/index.html`, `/graphql`, bookmark `s_sample-1`, existing test selectors, custom-element registration, ESM loading, readiness state, and low-level examples.
Its page structure and styles are reorganized to use the shared design tokens, contained shell, consistent headings, cards, grids, spacing, forms, tables, prompts, responsive behavior, and accessible disclosures.
Domain/member state remains rendered by components.
No HTMX route or Petclinic domain behavior is introduced into this vanilla sample.

### Verify visual similarity by behavior and hierarchy

Acceptance compares the Wicket and HTMX Petclinic routes at desktop and mobile widths rather than asserting pixel identity.
The comparison records screenshots and checks equivalent presence and ordering of brand, primary menus, secondary/tertiary menus, object title, action affordances, groups, tabs, properties, collections, tables, prompts, loading, disabled state, and error presentation.

Automated browser checks also assert no horizontal page overflow, non-overlapping controls, usable target sizes, stable focus, bounded modal dimensions, readable line lengths, responsive table containment, light/dark contrast, and zero browser console errors.
Lighthouse accessibility, best-practices, and SEO targets remain 100 for the executable HTMX Petclinic routes and repaired vanilla sample.

## Risks / Trade-offs

- [The pinned Petclinic application targets Causeway 2.0.0] → Port only the cohesive domain slice, record provenance and deviations, and compile it against the current reactor rather than preserving obsolete infrastructure.
- [Wicket similarity could become pixel-copy coupling] → Compare information hierarchy, affordances, responsive behavior, and visual tokens while keeping independent semantic markup.
- [A shared theme could leak host assumptions into components] → Keep behavior unstyled by host frameworks, publish CSS variables and selectors, and make the theme optional.
- [Server fragments can duplicate shell state] → Keep menus and global state outside the replaceable object region.
- [Custom fragments can bypass semantic contracts] → Require route context composition and document that domain state remains component-owned.
- [Identifiers contain reserved characters] → Independently encode route segments and enforce canonical round-trip parsing.
- [HTMX lifecycle can leave obsolete responses] → Couple fragment generation to route identity, abort superseded requests, and dispose replaced object contexts.
- [History cache can restore stale authorization-sensitive markup] → Configure history restoration to request the server and retain no durable domain values in shell snapshots.
- [Viewer routes may diverge from Vue and Svelte] → Maintain shared canonical route and fallback fixtures across all generic viewers.
- [Including Wicket makes the sample heavier] → Isolate Wicket to the demonstration module and keep the reusable HTMX module free of Wicket dependencies.

## Migration Plan

The viewer is additive and opt-in.
Applications include and import the HTMX module, configure the base path if required, and optionally register custom fragment factories or policy beans.
Existing component-only applications can adopt selected routes incrementally.
Rollback removes the optional module or route mapping without changing GraphQL or component contracts.
The repaired `sample-html` retains all established external paths and selectors.

## Open Questions

No blocking design questions remain for the initial implementation.
Client-only template registries, home service actions, authenticated shell chrome, and broader Petclinic extensions remain explicit follow-on work if evidence requires them.
