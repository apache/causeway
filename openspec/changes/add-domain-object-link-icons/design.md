## Context

Rich GraphQL already exposes an optional object-level `_meta.icon` URL when structural metadata resources are enabled.
The web-component client currently omits that field from its targeted header and object-result selections, while `<cw-object-link>` accepts only identity and title.
Breadcrumb entries expose identity and title but not icon metadata, so ancestor links cannot use the same authoritative resource URL.

The object header currently renders a plain `<h1>` title and a separate identity line.
Property references, collection rows, and breadcrumbs already converge on `<cw-object-link>`, making that component the appropriate presentation boundary.

## Goals / Non-Goals

**Goals:**

- Make the displayed object title an ordinary semantic navigation affordance targeting the current object.
- Present an available authoritative domain icon on every semantic object-link surface, including headers, object-valued properties, collection rows, and breadcrumb ancestors.
- Preserve targeted GraphQL selection, older-schema compatibility, keyboard operation, accessible naming, and icon-free fallback behavior.
- Keep icon sizing and alignment application-themeable through documented classes and variables.

**Non-Goals:**

- Do not construct icon URLs from logical type names or identifiers in the browser.
- Do not make icons authoritative for identity, routing, or accessible link naming.
- Do not change canonical route grammar, semantic navigation event payloads, or application router policy.
- Do not add icons to action, property, service, or menu affordances that are not domain-object navigation links.

## Decisions

### Use optional authoritative metadata rather than client-side URL construction

The client will select `_meta.icon` only when introspection advertises it and will propagate the returned URL through an optional `icon` attribute on `<cw-object-link>`.
This preserves GraphQL resource configuration and deployment-path semantics.
Constructing a predictable URL in JavaScript was rejected because it would bypass schema capability detection and duplicate server routing knowledge.

### Centralize icon rendering in `<cw-object-link>`

`<cw-object-link>` will render a decorative image before its title when a non-empty icon URL is supplied.
The image will have empty alternative text and will not alter the semantic navigation target or event payload, leaving the object title as the link's accessible name.
A failed image load will remove the broken visual affordance while leaving the link usable.
Duplicating image markup in headers, properties, collections, and breadcrumbs was rejected because those surfaces already share the semantic link component.

### Compose the object heading from the same semantic link

`<cw-object-header>` will place a `<cw-object-link>` inside its `<h1>` and target the current metadata identity.
Header styling will suppress the link's secondary identity text because the existing identity line remains available separately.
Publishing the standard navigation event is preferred over a literal anchor because the framework-neutral component must not impose a router URL.

### Extend targeted selections additively

Header metadata selection and reusable object-result metadata selection will include `icon` only when the described metadata type provides it.
This automatically covers property references and collection row identities, including concrete inline fragments.
Older schemas and configurations that omit structural metadata continue with identity and title only.

### Extend breadcrumb entries with policy-aware optional icon metadata

The shared `RichNavigableBreadcrumb` type will advertise nullable `icon` only when structural metadata responses are enabled, matching the existing object `_meta.icon` policy.
Ancestor traversal will derive each icon URL through the same configured `ResourcePath` mechanism used for current-object metadata.
The browser will request the breadcrumb icon field only when introspection advertises it.
Omitting breadcrumb icons was rejected because breadcrumbs use the same domain-object navigation affordance and the requested presentation is intended to be consistent wherever such links appear.

### Keep icon presentation bounded and responsive

Shared component styles will define a compact fixed logical size, preserve aspect ratio, and avoid allowing intrinsic image dimensions to resize controls or rows.
Theme variables will permit applications to adjust size and fit without replacing semantic markup.

## Risks / Trade-offs

- [Additional metadata and image requests may increase page traffic] → Select only advertised icon URLs, rely on normal browser caching, and keep images presentation-only so links render immediately.
- [Missing or defective icon resources can show broken images] → Hide the image on load failure and retain the title link unchanged.
- [Adding an optional breadcrumb field changes targeted introspection expectations] → Update exact allow-list tests and keep the field nullable and configuration-gated.
- [A button inside the heading changes heading markup] → Retain one `<h1>`, use the established keyboard-operable semantic link, and preserve title text as its accessible content.
- [Long titles and icons can crowd narrow layouts] → Keep the icon non-growing and allow title text to wrap using existing responsive link styles.

## Migration Plan

The GraphQL field and component attribute are additive.
Deploying the frontend against an older schema produces title-only links because selection is introspection-driven.
Rollback removes icon selection and presentation without changing object identity, routes, or stored data.

## Open Questions

None.
