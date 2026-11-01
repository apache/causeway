## Context

The stable shell brand link is a canonical HTMX route link to the viewer root.
The server correctly returns a landing fragment for that route, and `resolveHome()` correctly maps the GraphQL application entry to the configured object home.
However, `resolveHome()` currently runs only once when the shell module loads, so a later HTMX replacement with a landing fragment never starts home resolution.

## Goals / Non-Goals

**Goals:**

- Resolve the configured object home whenever HTMX installs a landing fragment.
- Preserve the current GraphQL application-entry operation and canonical replacement navigation.
- Prevent duplicate resolution for one connected landing fragment.
- Cover the reported brand-link path in executable Petclinic browser tests under both toolkit policies.

**Non-Goals:**

- Do not special-case Petclinic identity, object ids, or page resources in the generic viewer.
- Do not change the server root route into an application-specific redirect.
- Do not change menu, GraphQL, route codec, history, CSP, or toolkit contracts.

## Decisions

### Re-enter home resolution from the route swap lifecycle

The HTMX `afterSwap` handler will call the existing `resolveHome()` function after a new route fragment is installed.
This uses the same path for initial and subsequent landing fragments and keeps the brand link an ordinary canonical route link.

Changing the server to return the configured home object directly was rejected because application-entry identity is GraphQL-authoritative and can be unavailable, hidden, invalid, unsupported, or overridden by application policy.
Special-casing the brand click was rejected because any navigation that installs a landing fragment requires the same behavior.

### Deduplicate by landing-element identity

A module-local `WeakSet` will track landing elements with home resolution in flight.
Repeated lifecycle callbacks for the same element will therefore issue no duplicate application-entry operations, while a newly installed landing element remains independently resolvable.

Mutating the public route-state marker solely as an in-flight lock was rejected because it would couple implementation synchronization to observable presentation state.

### Verify through the real shell

The Petclinic Playwright matrix will navigate away from the home object, click the visible brand link, and require the custom home object page and canonical object URL to return.
The existing matrix runs under Vaadin-default and explicit-native policies, proving toolkit independence.
Focused source-level tests will retain the shell's canonical brand-link and landing-fragment contracts.

## Risks / Trade-offs

- [Risk] An HTMX lifecycle event can run after its landing element has been detached. → Home resolution checks element connectivity before applying fallback state or navigating.
- [Risk] Calling resolution from more than one lifecycle path can duplicate GraphQL work. → The `WeakSet` lock scopes one in-flight operation to one landing node.
- [Risk] A failed home lookup could interfere with menus. → Existing bounded landing error text remains local and menus remain in the stable shell.

## Migration Plan

Deploy the generic HTMX JavaScript and browser regression together.
Rollback restores the prior module and test without changing routes, persisted data, page resources, or GraphQL schema.

## Open Questions

None.
