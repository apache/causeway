# Custom-page and extension-tier assessment

## Evidence

The fixture exercised three page modes:

- Generic semantic page with Causeway-owned Vaadin adapters.
- Router-selected custom HTML page using the same stable Causeway wrappers.
- Router-selected custom HTML page configuring allowlisted raw Vaadin widgets directly.

Every mode retained exactly one route object context, used no Flow runtime, made no external request, and survived repeated replacement without listener, overlay, context, or DOM growth.
The real Petclinic injection preserved route readiness, menu readiness, Escape dismissal, and page overflow.

## Option 1: Internal-only Vaadin

Applications use `<causeway-*>`, semantic events, GraphQL contexts, canonical routes, and `--causeway-*` variables only.
Causeway owns data-provider mapping, lifecycle, disabled state, validation, accessibility integration, theme mapping, and toolkit upgrades.

Advantages:

- Long-lived toolkit-neutral public contract.
- Standard pages require no Vaadin knowledge.
- One place to resolve GraphQL, CSP, and lifecycle concerns.
- Future toolkit replacement remains possible.

Costs:

- Causeway must implement and test adapters for every adopted widget family.
- Advanced application pages cannot immediately use every bundled Vaadin feature through semantic wrappers.

## Option 2: Supported raw-widget profile

The viewer bundles and documents selected `<vaadin-*>` tags, module loading, theme, CSP, and version policy for custom pages.
Applications configure data providers, events, accessibility, lifecycle, and GraphQL behavior themselves.

Advantages:

- Maximum immediate widget breadth for custom pages.
- Applications can build specialized HTML pages without waiting for Causeway wrappers.

Costs:

- Vaadin version becomes an application compatibility surface.
- Raw pages must understand data-provider callbacks, shadow DOM, theme parts, CSP, stale requests, and route disposal.
- The viewer inherits support questions for low-level toolkit APIs.
- A future Vaadin major upgrade becomes a page migration event.

## Option 3: Application-owned third-party widgets

The viewer guarantees only semantic Causeway elements and ordinary browser composition.
Applications may bundle Vaadin or any other widget library in their own custom pages under their own dependency and CSP policy.

This preserves maximum freedom but loses shared delivery, theme, deduplication, and framework-provided integration.

## Recommendation

Use internal-only Vaadin for the first supported Causeway widget pilot.
Keep stable Causeway wrappers as the only standard domain behavior contract.
Do not initially declare raw `<vaadin-*>` tags a supported viewer API.

Custom HTML remains fully extensible: applications can use ordinary HTML, Causeway semantic elements, and application-owned scripts or widgets.
If later demand justifies a bundled raw profile, specify it separately with an explicit allowlist and version policy after the CSP and release model are proven.

This recommendation preserves the main architectural distinction from Wicket and a putative server-side Vaadin viewer: custom object pages remain browser-composable and do not require Java Vaadin APIs.
