# Implementation and qualification

## Implementation

`causeway-htmx.mjs` now re-enters the existing GraphQL-authoritative `resolveHome()` path after HTMX installs a route fragment.
A module-local `WeakSet` suppresses concurrent work for one landing element without changing public route-state markers.
Resolution verifies that the landing element remains connected inside the current route region after each asynchronous boundary and before policy handling, navigation, or fallback presentation.
The default policy still replaces the root history entry with the canonical object route.
Unsupported, absent, partial-error, and failed home discovery retain their bounded landing messages and stable menus.

The Petclinic browser regression navigates to a Visit, switches to the 390-pixel layout, clicks the visible brand link, waits for the canonical `petclinic.HomePage` object route, and requires the custom HTML-authored home page and route focus.
The recorded application-entry request delta is bounded to at most one because the GraphQL client may satisfy the repeated read from its existing authoritative cache.

## Qualification

- Pre-change focused browser regression: failed with a 30-second timeout on the home object route.
- Post-change focused browser regression: passed.
- Foundation Node suite: 170 passed, zero failed.
- HTMX route-policy Node suite: 5 passed, zero failed.
- Petclinic Java integration suite: passed.
- Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- HTMX RAT check: passed.
- Petclinic RAT check: passed.
- Ordinary reactor packaging: passed and produced a 53,745-byte Petclinic jar without `BOOT-INF`.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.

No GraphQL schema, operation builder, identity codec, route codec, CSP hash, toolkit adapter, dependency, lockfile, page resource, menu contract, or public web-component API changed.
