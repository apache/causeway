# Production isolation verification

No Maven POM, npm package manifest, lock file, dependency version, or third-party source changed.
No public GraphQL field, argument, scalar, generated type name, lookup grammar, or operation placement changed.
The possible-type membership of an existing generated union is corrected to include every concrete type discovered during metamodel construction.

No public `<causeway-*>` element, context method, semantic event name, event detail, result kind, canonical route, browser history policy, HTMX lifecycle, or asset URL changed.
The `__fragments` node is an internal selection representation and is not application-facing markup or API.
No Vaadin input, generated bundle, checksum, exact CSP hash, qualification policy, route-lazy trigger, native fallback, or default-selection policy changed.

Broad polymorphic probing is restricted to activated side-effect-free collection reads and bounded to one replay.
Mutating interactions remain single-shot.
Rollback requires no persisted-data, route, application configuration, dependency, or asset migration.
