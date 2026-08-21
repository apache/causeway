# Semantic adapter and integration assessment

## Boundary under test

The desired boundary keeps application code, GraphQL contexts, semantic events, HTMX routing, and public `<causeway-*>` hosts independent of the selected visual implementation.
Toolkit details may exist inside renderers, but they must not become required application markup or domain behavior.

## Comparative assessment

| Concern | Bootstrap CSS | Web Awesome | Open Props plus native |
|---|---|---|---|
| Public Causeway tags | Preserved | Preserved only through explicit wrappers | Preserved |
| Semantic events | Causeway remains authoritative | Requires event translation per toolkit control | Causeway remains authoritative |
| Focus and disabled state | Existing Causeway behavior retained | Toolkit properties and lifecycle must map to Causeway state | Existing behavior plus native Dialog and Popover |
| Styling boundary | Global selectors and classes | Shadow DOM, custom properties, slots, and CSS parts | Global token packs plus Causeway selectors |
| Application variables | Map Causeway variables to Bootstrap variables or compiled Sass | Map Causeway variables to `--wa-*` tokens and parts | Map Causeway variables to selected Open Props |
| HTMX fragment lifecycle | CSS-only integration is naturally compatible | Custom elements reconnect safely but adapter lifecycle requires testing | Native and Causeway elements reconnect without framework state |
| Automation | Existing selectors remain | Tests must cross Causeway hosts and toolkit shadow parts where necessary | Existing selectors remain |
| Incremental adoption | Very easy but collision-prone | Explicit component-by-component migration | Very easy at token level |

## Bootstrap assessment

Bootstrap can improve existing light-DOM markup without replacing Causeway behavior.
The least-coupled form is a compiled Causeway theme that consumes Bootstrap variables, mixins, or selected CSS layers while retaining Causeway class names.
Adding Bootstrap classes directly to every renderer is faster initially but creates public-looking markup coupling and makes future removal expensive.
Bootstrap dropdown, modal, collapse, and tab JavaScript should not be adopted while Causeway already owns those semantic state machines.
Running both would create competing focus, dismissal, and lifecycle behavior.

The real-viewer injection demonstrated that complete Bootstrap CSS changes generic button radius and font before purposeful adoption.
A production proposal must use CSS layers, a selective Sass build, or strongly scoped imports rather than dropping the entire stylesheet into the shell.

## Web Awesome assessment

Web Awesome provides complete visual controls with strong audited behavior, but it does not theme existing Causeway buttons, inputs, menus, dialogs, or tabs automatically.
Each adopted control needs a renderer adapter that maps Causeway state into toolkit attributes and properties and translates toolkit events back into Causeway semantic events.
Focus delegation, asynchronous component definition, form association, validation messages, slots, parts, and open or close lifecycle events all require explicit tests.

A representative mapping is:

```text
<causeway-action>
    internal renderer
        <wa-button>
            shadow button
```

Applications continue addressing `<causeway-action>`, while the renderer owns `wa-button` and maps disabled reason, variant, busy state, invocation, focus, and result semantics.
The same pattern can apply to prompts, inputs, selects, tabs, dropdowns, status callouts, and progress indicators.
Collections and property grids still need Causeway-owned layout and ordinary table or grid semantics because Web Awesome does not replace the domain composition model.

The browser-ready autoloader is unsuitable for production because the fixture loaded 132 candidate requests.
The analysis produced a selective twelve-component bundle at 70,009 bytes gzip for JavaScript and 14,124 bytes gzip for toolkit CSS, proving that Maven-packaged bundling is feasible while also showing a material payload cost.
Further component reduction or code splitting would be mandatory for any implementation proposal.

## Open Props and native assessment

Open Props can remain entirely behind `--causeway-*` values and Causeway selectors.
It introduces no toolkit element or event vocabulary.
Native Dialog and Popover can replace selected custom lifecycle code while keeping Causeway controls and semantic events.

The strongest mapping is:

```text
--causeway-* public contract
        ↓
curated Causeway theme
        ↓
selected Open Props values

Causeway menu or prompt host
        ↓
native popover or dialog lifecycle
```

The complete normalize and buttons packs should not be loaded globally because the prototype and real-viewer check showed substantial typography and control changes.
A production proposal should import selected prop packs or copy resolved values into the generated Causeway theme.
Popover's Baseline 2025 status requires either an explicit modern-browser policy or a small fallback using the current disclosure implementation.

## Global CSS and shadow DOM trade-off

Bootstrap and Open Props can style current light DOM immediately, which minimizes renderer work but increases collision risk.
Web Awesome isolates visual implementation and avoids broad collisions, but every adopted component adds an adapter and can complicate inspection, CSS overrides, automation, and server-rendered first paint.

## HTMX compatibility

All candidates preserved route readiness, menu readiness, Escape dismissal, and zero page overflow in the bounded real Petclinic check.
CSS-oriented strategies require no HTMX lifecycle integration.
Web Awesome custom elements upgraded successfully when inserted after the route was ready, which supports fragment replacement, but production adoption still needs disconnect, reconnect, stale-response, and focus tests within actual HTMX swaps.

## Contract conclusion

Bootstrap CSS and Open Props can preserve the semantic boundary with a theme-only implementation.
Web Awesome can preserve the boundary only through deliberate internal adapters and selective adoption.
No candidate justifies exposing toolkit tags or events as public Causeway contracts.
