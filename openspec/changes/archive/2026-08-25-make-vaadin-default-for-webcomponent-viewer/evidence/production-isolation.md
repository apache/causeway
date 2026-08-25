# Production isolation review

## Changed production boundary

The production change is limited to foundation adapter defaults, bounded reference failure text, HTMX toolkit policy resolution, resolved shell attributes, effective exact-hash CSP selection, sample configuration, and support documentation.
No Maven POM, npm manifest, package lock, generated bundle, license file, GraphQL module, metamodel module, persistence module, route codec, or route-policy JavaScript changed.

## Stable public contracts

Public `<causeway-*>` elements, GraphQL client and context operations, semantic events, canonical routes, HTMX fragment lifecycle, ordinary HTML composition, and `--causeway-*` design variables remain unchanged.
Internal Vaadin elements and value events remain below Causeway-owned adapters.
Native controls remain the explicit rollback and unsupported or failed-adapter fallback.

## Delivery and security

All assets remain pinned, deterministic, same-origin, Maven-packaged, permissively licensed, and independently route-lazy.
No Flow, Binder, Grid, Pro component, upload component, telemetry collector, CDN reference, or server-side Vaadin runtime entered production.
Default policy changes exact hash permission but does not request an asset until an eligible connected editor imports its closure.
Native policy restores the strict policy without Vaadin hashes.

## Data and authority

GraphQL remains authoritative for identity, choices, autocomplete windows, validation, interactions, paging, ordering, and persisted state.
Exact numerics remain lexical, protected values remain redacted, unsupported temporal and resource shapes remain native or explicitly unsupported, and no migration of application data or markup is required.
