## Why

The HTMX viewer supports application-owned custom fragments, but Petclinic currently hides its only custom page inside a Java text block while its remaining object pages rely on generated `<causeway-object>` composition.
A convention-based HTML page contract is needed to prove that an application can customize Causeway presentation through ordinary `.html` files containing public `<causeway-*>` components without application-specific HTMX page-rendering code or a parallel domain-state channel.

## What Changes

- Add private classpath discovery for trusted HTML page resources beneath a documented HTMX viewer location.
- Register each page by the exact public logical type encoded in its filename, for example `petclinic.PetOwner.html`.
- Resolve one exact logical-type HTML page before the existing Java fragment-factory and generic-object paths, while rejecting duplicate registrations rather than choosing by classpath order.
- Load page resources as bounded UTF-8 literal HTML without template expressions, route interpolation, metamodel access, persistence access, or public static-resource exposure.
- Render the selected HTML beneath the viewer-owned route `<causeway-object-context>` and beside the existing route interaction controller so its semantic components inherit identity, GraphQL state, interaction, result, navigation, and disposal behavior.
- Preserve the existing `<causeway-object editable>` path when no matching HTML page or Java fragment factory exists.
- Treat an absent page as ordinary fallback while failing clearly for a discovered page that is unreadable, duplicate, empty, oversized, invalidly named, or otherwise cannot satisfy the trusted-resource contract.
- Refactor Petclinic to provide `petclinic.HomePage.html`, `petclinic.PetOwner.html`, `petclinic.Pet.html`, and `petclinic.Visit.html` as its only application-specific HTMX page customizations.
- Remove the Petclinic Java custom-fragment factory while retaining application bootstrap, domain code, common viewer configuration, stylesheet configuration, and the root-route redirect.
- Retain Petclinic `*.layout.xml`, `*.columnOrder.txt`, and `menubars.layout.xml` resources so missing-page and generic-viewer demonstrations continue to use authoritative Causeway layout fallbacks.
- Demonstrate ordinary HTML grouping, headings, classes, properties, actions, references, collections, explicit collection columns, and responsive application styling through the four Petclinic files.
- Preserve strict CSP, canonical opaque routes, GraphQL authority, HTMX full-page and fragment lifecycles, route-lazy Vaadin delivery, explicit native rollback, accessibility, and framework-neutral semantic events.
- Add source, packaged-resource, duplicate-resolution, fallback, integration, and browser evidence proving that Petclinic page markup is visible in `.html` files and no application page is rendered by Petclinic Java code.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Extends exact-type custom-page resolution with convention-registered classpath HTML pages and makes Petclinic an executable HTML-authored customization proof while retaining Java-factory compatibility and generic layout fallback.

## Impact

- Extends the HTMX page registry and renderer with trusted classpath page discovery, deterministic qualification, bounded loading, exact logical-type selection, and fallback classification.
- Adds four private HTML page resources to the Petclinic sample and removes its page-specific fragment factory.
- Updates HTMX viewer and Petclinic tests, browser acceptance, documentation, packaging checks, RAT coverage, and reproducible evidence.
- Retains existing layout and column-order resources, the public `<causeway-*>` vocabulary, GraphQL operations, route formats, Java fragment-factory SPI, generic object composition, menus, CSS variables, and toolkit policies.
- Introduces no server-side template engine, client framework, JavaScript build step, GraphQL schema field, Vaadin Flow dependency, CDN asset, telemetry channel, or application-facing raw `<vaadin-*>` API.
- Establishes the HTML page model needed by the later prototype-mode page-authoring diagnostics, Vue and Svelte viewer compatibility work, catalogue, and semantic page-designer analysis.
