# Production isolation review

## Changed production boundary

Production changes are limited to public metamodel facet utility accessors, rich GraphQL wrapper construction, one shared metadata object, one generalized package-local scalar field helper, and GraphQL documentation.
No persistence mapping, domain value, GraphQL input type, operation argument, mutation behavior, resource controller, collection-window implementation, application-entry operation, route, web-component, HTMX shell, Vaadin adapter, dependency, package lock, or generated asset changed.

## Stable authority

GraphQL input nullability remains authoritative for requiredness.
Existing validation, hidden, disabled, choices, autocomplete, invocation, collection window, and resource policies remain authoritative for dynamic behavior.
Canonical local metadata is descriptive and does not authorize an interaction or validate a candidate.

Effective grid resources continue to own page structure, grouping, ordering, icons, CSS, action positions, and fallback placement.
Effective menu resources continue to own application navigation structure and ordering.
The shared metadata type contains no aggregate member list or metamodel serialization.

## Security and localization

Resolvers read only canonical static facets and return no domain value, protected value, disabled reason, authorization rule, or imperative object-dependent text.
Known hidden wrappers expose only the static schema identity already discoverable through targeted introspection.
Request-time evaluation preserves the active translation context and does not cache one locale globally.

## Compatibility

All additions are optional response selections beneath established wrappers.
Existing generated type names, field descriptions, operation documents, resource fields, and response shapes remain valid when `metadata` is not selected.
No data migration or client regeneration is required unless a client chooses to consume the additive field.
