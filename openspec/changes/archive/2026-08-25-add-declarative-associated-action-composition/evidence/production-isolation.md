# Production isolation and rollback

## Changed production boundary

Production changes are limited to a toolkit-neutral member-composition helper, property and collection rendering targets, direct-child event scoping, collection declaration filtering, and structural associated-action CSS.
Petclinic changes are limited to equivalent nested HTML and removal of obsolete application-only wrapper styles.

No GraphQL schema, operation builder, action dispatch planner, parameter preparation, validation, invocation, result normalization, value codec, editor registry, autocomplete window, collection window, abstract projection, route codec, identity policy, menu resource, grid parser, persisted value, fixture, dependency, package lock, generated Vaadin closure, checksum policy, or CSP hash changed.

## Domain and interaction authority

The owner component does not read an action descriptor or action state.
It identifies only direct semantic action elements for presentation and event-boundary purposes.
Every action continues to register independently against the nearest object context and to publish the unchanged semantic action request.

Owner hidden, disabled, loading, editing, validation, collection activation, paging, and error state remain local to the primary region.
Action hidden, disabled, parameter, validation, invocation, result, cancellation, stale-result, announcement, and focus behavior remain authoritative and independent.
No member value or protected content enters composition markers, mutation observations, semantic events, errors, or diagnostics.

## Delivery and CSP

The new helper is an ordinary same-origin foundation ECMAScript module imported by property and collection components.
It adds no dependency, remote request, telemetry, CDN origin, dynamic code generation, inline handler, inline style, toolkit element, script hash, or style hash.
The external structural CSS and installable CSS string remain byte-synchronized.

Vaadin-default policy still loads each qualified family only when an eligible editor opens.
Explicit native policy uses the identical nested page and makes no Vaadin asset request.
`style-src-attr 'none'` and the reviewed exact-hash policy remain unchanged.

## Compatibility and rollback

Pages without nested actions retain their existing host hiding and semantic behavior.
Adjacent ordinary HTML action groups remain supported and are not claimed.
Generated `<causeway-object>` composition and effective grid XML remain unchanged.

Rollback removes the helper imports, restores full-host rendering, and restores Petclinic's adjacent wrappers.
It requires no GraphQL, route, grid, menu, persistence, fixture, package, or CSP migration.
