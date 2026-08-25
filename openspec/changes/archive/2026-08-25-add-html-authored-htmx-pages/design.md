## Context

The generic HTMX viewer owns canonical routing, one disposable route object context, the stable shell, GraphQL endpoint configuration, HTMX fragment replacement, result policy, CSP, and toolkit policy.
It currently resolves application customization through injected `HtmxPageFragmentFactory` beans whose `render(...)` method returns trusted HTML.
Petclinic registers one such factory for `petclinic.HomePage`, but the Causeway element markup is consequently embedded in a Java text block rather than visible as an application page resource.
Petclinic's other logical types use the generic `<causeway-object editable>` page and consume their effective grid and collection-column resources.

The foundation already supports direct declarative composition in static HTML, and route identity is already supplied by the viewer-owned `<causeway-object-context>`.
A static page therefore needs no route interpolation and no access to metamodel, persistence, authorization, or GraphQL implementation services.

The new resource convention must work from ordinary application resources and dependency jars, remain private rather than becoming a Spring static resource, fail deterministically for conflicting definitions, preserve the Java SPI, and impose no production frontend build step.

## Goals / Non-Goals

**Goals:**

- Make `.html` files the normal application-owned HTMX page-customization mechanism.
- Register conventional files by exact public logical type without application Java configuration.
- Load trusted resources once into an immutable bounded registry and render their literal contents beneath the existing route context.
- Preserve Java fragment factories for dynamic advanced use and preserve generic `<causeway-object>` fallback for unregistered types.
- Convert all four routable Petclinic object types into visible HTML-authored examples.
- Retain Petclinic layout resources so deleting or omitting a page continues to demonstrate generic layout composition.
- Preserve GraphQL authority, route identity, interaction dispatch, lifecycle disposal, CSP, accessibility, toolkit isolation, and native rollback.

**Non-Goals:**

- A server-side template language, model binding, expression evaluation, route interpolation, HTML sanitizer, or arbitrary user-content renderer.
- A public static-page endpoint or a second client-side request for page markup.
- A replacement for effective grids, menu resources, `<causeway-object>`, or `HtmxPageFragmentFactory`.
- Automatic HTML generation, visual page design, catalogue tooling, or prototype-mode missing-member suggestions.
- Vue, Svelte, React, or another frontend framework integration.
- Declarative property- or collection-associated action nesting, which remains the separate `add-declarative-associated-action-composition` proposal.
- Converting the complete Reference Application corpus to authored pages.

## Decisions

### Discover exact logical-type filenames from one private classpath root

The viewer will discover `classpath*:/META-INF/causeway/webcomponents/pages/*.html` during application startup.
The basename before `.html` is the exact public logical type, so `petclinic.PetOwner.html` registers `petclinic.PetOwner` without a manifest, annotation, configuration bean, or naming transformation.

The fixed `META-INF` location keeps resources private from Spring's normal static-resource mappings while allowing application modules and dependency jars to contribute pages.
Only files directly beneath the page root participate, which keeps discovery bounded and avoids interpreting logical names as relative paths.
Resource URLs will be normalized only for deterministic diagnostics and never used to choose a winner.

A manifest was rejected because it separates registration from the page being registered and creates another artifact that can drift.
Public static resources were rejected because they would permit context-free retrieval and add a second browser request.
Spring template directories were rejected because they imply server-side interpolation that this contract deliberately excludes.

### Build one qualified immutable registry at startup

Classpath resources and injected `HtmxPageFragmentFactory` beans will be adapted into one internal exact-type page-definition registry.
Two definitions of any kind for the same logical type will fail startup with a bounded error naming the logical type and safe source identifiers.
Bean order, resource enumeration order, and classpath order will never determine precedence.

Resource discovery and decoding will happen once during bean construction rather than on every route.
Each page will have a fixed UTF-8 byte ceiling of 256 KiB, must be non-empty, must decode with malformed input rejected, and must not contain a NUL character.
The aggregate registry will have a documented finite entry ceiling so accidental broad classpath matches cannot create unbounded memory use.

The initial qualification will not attempt to prove full HTML validity or sanitize trusted application markup.
Browsers remain responsible for HTML parsing, while integration and browser tests qualify the shipped Petclinic pages.

Lazy route-time lookup was rejected because duplicate resources and unreadable content would fail only when a user happened to visit a route.
Silently choosing one duplicate or silently falling back from a discovered broken page was rejected because both outcomes conceal deployment errors.

### Distinguish absence from defective registration

If no exact resource page or Java factory is registered, the renderer will use the existing `<causeway-object editable>` content and therefore the effective or fallback Causeway grid.
If a resource was discovered but cannot satisfy naming, size, UTF-8, content, or uniqueness rules, startup will fail.
A broken intended customization will never become a successful-looking generic page.

This preserves fallback as an explicit absence policy rather than an error-recovery policy.
It also means a demonstration can remove, rename, or exclude an HTML resource and immediately observe the retained layout-driven page.

### Keep route context and interaction ownership in the viewer

An HTML resource contains only the content placed inside the viewer-generated route `<causeway-object-context>`.
It does not contain `<html>`, the global `<causeway-graphql-client>`, a second object context, the route interaction controller, or route identity attributes.

The renderer will continue to escape the canonical logical type and opaque object identifier when creating the outer context.
The literal page content will then consume that nearest context exactly as current factory content does.
HTMX replacement will disconnect one context and all resource-page consumers together, preserving cancellation and stale-result protection.

A full application-owned shell template was rejected because endpoint paths, context paths, CSP, menus, canonical history, loading state, announcements, and result policy belong to the generic router rather than each logical-type page.

### Treat resources as trusted packaged application code

Resource HTML has the same trust boundary as current Java text blocks and application JavaScript.
The loader will not sanitize, rewrite, interpolate, or execute a second policy language over it.
Strict response CSP remains unchanged, including `style-src-attr 'none'`, so page authors use ordinary elements, classes, documented component attributes, the configured application stylesheet, and `--causeway-*` variables rather than inline handlers or styles.

Documentation will state that user-supplied HTML must never be copied into this location without an application-controlled sanitization boundary outside the viewer.

### Make Petclinic a complete HTML-authored page example

Petclinic will contribute exactly these private resources:

```text
META-INF/causeway/webcomponents/pages/
├── petclinic.HomePage.html
├── petclinic.PetOwner.html
├── petclinic.Pet.html
└── petclinic.Visit.html
```

The existing `PetClinicHomeFragmentFactory` will be removed.
The four pages will use application HTML regions, headings, classes, object headers, explicit properties, actions, collections, and collection columns while inheriting all state and interactions from the route context.
Top-level and member-adjacent actions can use ordinary sibling HTML regions in this change; the separate associated-action composition proposal can later replace those regions with natural nested action declarations.

The object `*.layout.xml`, collection `*.columnOrder.txt`, and `menubars.layout.xml` resources will remain unchanged and tested as fallbacks.
The Spring Boot application, HTMX module import, common viewer properties, application stylesheet, Wicket comparison viewer, domain model, fixture data, and root redirect are not page-rendering customizations and remain.

### Preserve a stable seam for later authoring diagnostics

The internal resolved-page result will distinguish resource, factory, and generic sources without exposing classpath URLs or page contents as public route data.
Tests and safe rendered state may identify that an authored resource was selected, but production pages will not gain complete-member introspection or diagnostic controls in this change.

The later diagnostics change can use this source classification to inject a prototype-only authoring assistant for resource pages.
That assistant remains responsible for unrepresented-member summaries and copyable snippets.

## Risks / Trade-offs

- [Classpath wildcard discovery behaves differently between exploded directories and jars] → Qualify both packaging forms with integration tests and use Spring's existing classpath resource abstraction rather than filesystem APIs.
- [Logical type names may contain characters that cannot be represented as one resource filename] → Document the conventional filename-safe logical-name contract and retain the Java factory SPI for exceptional names without transforming route identity.
- [Trusted HTML can contain unsafe application code] → Keep resources private, preserve strict CSP, document the trust boundary, and never present the loader as a sanitizer.
- [A resource can be valid UTF-8 but malformed HTML] → Keep qualification dependency-free and prove shipped pages in real browsers while surfacing component and browser failures rather than silently falling back.
- [Page resources can consume excessive startup memory] → Enforce per-page and aggregate count bounds and load each immutable string exactly once.
- [Explicit Petclinic pages can drift from domain members] → Keep missing or incompatible components visible, retain integration coverage, and defer richer summaries and snippets to prototype-mode diagnostics.
- [Maintaining HTML and layout resources duplicates presentation] → Treat HTML as the preferred custom page and layout as intentional generic fallback evidence rather than requiring structural equivalence.
- [Associated actions cannot yet use natural nested markup] → Use accessible adjacent HTML groups initially and keep the independent composition proposal explicit.

## Migration Plan

The feature is additive for existing applications.
Applications with no conventional page resources continue to use Java factories and generic object pages unchanged.
Applications can migrate one exact logical type at a time by adding its HTML resource and can roll back by removing that resource from the packaged application.
Petclinic will migrate all four types together while retaining every existing layout fallback.

No GraphQL schema, persisted state, route, browser bookmark, or application data migration is required.

## Open Questions

- Confirm the aggregate discovered-page ceiling after measuring ordinary application and multi-module packaging; the design requires a finite bound but does not need it to be configurable initially.
- Confirm whether safe rendered route evidence should use `data-page-kind="resource"` or retain `data-page-kind="custom"` with a separate source attribute so existing acceptance hooks remain compatible.
- Confirm the exact filename-safe logical-name validation from established Causeway metamodel rules before finalizing startup diagnostics.
