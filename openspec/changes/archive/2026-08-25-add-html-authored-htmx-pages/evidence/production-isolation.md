# Production isolation review

## Changed boundary

Production changes are limited to private classpath page discovery, qualified in-memory page definitions, unified exact-type registry construction, safe page-source evidence, and literal custom-fragment selection.
Petclinic changes replace its Java page text with four private HTML resources and add application CSS for those pages.

No GraphQL schema, document builder, scalar, value codec, action planner, autocomplete window, collection window, menu resource, grid resource, route codec, opaque identifier, persisted value, domain fixture, toolkit closure, package lock, dependency, or generated Vaadin asset changed.

## Authority

The viewer still owns the shell, canonical route, escaped opaque identity, one route context, one interaction controller, HTMX lifecycle, history, announcements, result policy, CSP, and toolkit policy.
HTML pages own only presentation composition.
Causeway semantic components still obtain identity, values, visibility, usability, choices, defaults, validation, invocation, results, paging, and navigation through authoritative GraphQL behavior.

Resource content is never interpolated with route, metamodel, persistence, authorization, or GraphQL result data.
The resource location is not publicly served.
No protected value can enter startup diagnostics or page-source evidence.

## Compatibility and rollback

Applications without private pages remain unchanged.
Existing `HtmxPageFragmentFactory` implementations remain supported and continue receiving validated route identity.
An exact mixed-source duplicate fails rather than changing precedence silently.

Removing an HTML resource from packaging restores generic `<causeway-object editable>` behavior for that logical type.
Petclinic retains every effective grid and column-order resource needed by that fallback.
Explicit native policy remains the complete toolkit rollback without changing the HTML files.

## CSP and delivery

Resource pages introduce no script, inline handler, inline style, nonce, hash, external origin, CDN, or client-side fragment request.
The shell retains exact reviewed Vaadin style hashes only under eligible policy and retains `style-src-attr 'none'` in every mode.
Vaadin closures remain independently route-lazy and native mode makes no Vaadin asset request.
