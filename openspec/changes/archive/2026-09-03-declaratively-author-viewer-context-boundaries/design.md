## Context

The semantic component library already implements `<cw-graphql-client>` as a descendant provider and `<cw-object-context>` as the owner of one object snapshot.
Those elements work independently of HTMX, but `HtmxPageRenderer` currently creates both the shared client provider and every route object context in Java text blocks.
Resource pages therefore describe only inner presentation, while the planned Vue and Svelte adapters also claim ownership of creating route contexts.

The target architecture treats HTML or framework templates as the composition authority.
A host router selects a page and binds canonical route values, but it does not manufacture the semantic data-plane hierarchy.
The shared client remains stable above menus and route pages so schema descriptions and application-entry state are reused.

## Goals / Non-Goals

**Goals:**

- Make the GraphQL-client and route object-context elements visible in declarative source.
- Reduce HTMX Java rendering to bounded template selection, safe value binding, routing, HTTP concerns, and lifecycle policy.
- Establish the same ownership boundary for planned Vue, Svelte, and Angular viewers.
- Preserve a stable shared client, deterministic context disposal, canonical routes, authentication, accessibility, and result behavior.
- Fail closed when an authored template violates its structural contract.

**Non-Goals:**

- Sharing one physical page-template format among HTML, Vue, Svelte, and Angular applications.
- Introducing a component-library page registry or making `<cw-object>` inspect router state.
- Moving GraphQL operations, member state, validation, or interaction state into framework stores.
- Implementing the Vue, Svelte, or Angular viewers in this change.
- Adding a general-purpose server-side expression or template language.

## Decisions

### Treat shell and object pages as declarative templates

The HTMX module will load a bounded private shell template containing the stable `<cw-graphql-client>`, `<cw-menubars>`, result outlet, and route outlet.
Object-page resources and factory results will contain their own route-page element, exactly one route-level `<cw-object-context>`, and their interaction controller.
The generic fallback will be the same kind of declarative object-page template containing `<cw-object editable>`.

This makes semantic ownership inspectable in source and prevents the Java renderer from deciding component ancestry.
Keeping generated wrappers was rejected because it preserves an HTMX-only composition contract that sibling viewers would need to imitate.

### Use a closed set of escaped binding tokens

Declarative HTMX templates need runtime values that static resources cannot know.
The renderer will recognize only documented exact tokens for values such as GraphQL endpoint, logical type, object identifier, base path, shell metadata, and route content.
Text and attribute values will use context-appropriate escaping, while the few structural insertion points will accept only already-validated trusted template fragments.
Unknown reserved tokens, duplicate structural slots, and unresolved required tokens will fail closed.

A general expression engine was rejected because it increases authority, dependency, injection surface, and coupling.
Client-side post-connect binding was rejected because a context could connect with incomplete identity and because direct full-page rendering must remain deterministic without an HTMX timing dependency.

### Validate semantic roots before response assembly

The shell template must contain exactly one `<cw-graphql-client>`, one route-content slot beneath it, and the stable menu and result boundaries required by viewer policy.
Each selected object page must contain exactly one route-level `<cw-object-context>` with the exact logical-type and object-id binding tokens for resource pages or the exact validated route values for factories.
Nested row-preview contexts remain legal but do not count as route-level contexts.
The selected page must also contain its route-state boundary and interaction controller according to the documented page contract.

Validation operates on bounded trusted resources and produces stable diagnostic codes without exposing resource paths or authored content.
Silently wrapping legacy content was rejected because it hides ownership errors and perpetuates the old contract.

### Keep one stable client provider in the shell

The shell provider remains connected while HTMX replaces object pages beneath the route outlet.
Menus and route contexts therefore share its executor and schema-description cache.
Authentication continues to install a replaceable executor on that authored provider, rather than changing the provider element or descendant APIs.

Putting a client provider in every object page was rejected because it would recreate caches, separate menus from object pages, and complicate cancellation and authentication policy.

### Keep routers responsible for values and lifecycle

HTMX binds escaped values before returning markup.
Vue binds attributes in its route component template, Svelte binds them in its route page, and Angular binds them in a standalone routed component template.
All adapters select exact logical-type pages before generic fallback, translate semantic navigation and result events, and ensure obsolete route contexts disconnect.
They do not create the `<cw-graphql-client>` or `<cw-object-context>` nodes imperatively.

### Preserve framework-native pages

Declarative ownership does not require one shared serialization format.
HTMX applications author private HTML resources, while Vue, Svelte, and Angular applications author their native templates containing public Causeway elements.
This preserves framework lifecycle and tooling without moving page discovery into the semantic component library.

## Risks / Trade-offs

- [Existing HTMX inner-content-only pages become invalid] → Provide a mechanical migration, bounded startup diagnostics, and updated sample resources.
- [Token replacement could become an injection surface] → Support only exact reserved tokens, escape all route/configuration values, and validate that no required token remains.
- [Naive element counting could confuse nested contexts or comments] → Centralize bounded template inspection and test comments, attributes, nested preview contexts, duplicates, and malformed contracts.
- [A custom shell could omit nodes assumed by JavaScript] → Validate stable shell markers and make browser bootstrap fail safely when optional policy regions are absent.
- [Framework plans drift again] → State one shared declarative ownership invariant in the programme README and reference it from every viewer design and spec.
- [Stable shell templating broadens the migration] → Keep existing CSS hooks, IDs, accessibility markup, authentication slots, and response behavior unchanged.

## Migration Plan

1. Add declarative default shell and generic object-page templates plus bounded loader and binder tests.
2. Change object-page validation and renderer assembly to bind authored contexts rather than add wrappers.
3. Migrate HTMX test fixtures and all four Petclinic pages to the declarative page contract.
4. Move the existing shell hierarchy from the Java text block into the private shell template without changing rendered behavior.
5. Update browser and integration tests to assert source ownership, one stable client, one route context, lifecycle cleanup, authentication, and direct/HTMX navigation.
6. Update planned Vue and Svelte artifacts and add Angular artifacts using the same invariant.

Rollback restores the previous renderer and inner-content page resources together.
Mixed old and new object-page contracts are deliberately unsupported because implicit fallback would make ownership ambiguous.

## Open Questions

None.
