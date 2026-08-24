# Production isolation review

Production changes are limited to the foundation semantic editor registry and adapter, verified packaged field assets and legal metadata, HTMX family configuration and exact CSP policy, sample opt-in configuration, and regression fixtures.

The change adds no GraphQL operation, metamodel mutation, persistence query, route interpretation, custom-page API, application-facing Vaadin element, Flow state, Binder, Pro component, Grid behavior, telemetry, CDN request, or server-side Vaadin runtime.
The existing reference bundle checksum and reference semantic adapter remain unchanged.
The default field-family allow-list is empty, so ordinary applications retain native editors and request no new asset.

Causeway remains authoritative for pending values, codecs, validation, action dependencies, semantic events, protected-value redaction, focus recovery, GraphQL variables, and canonical navigation.
Unsupported resource and custom values remain visibly classified rather than becoming successful-looking controls.
The later `make-vaadin-default-for-webcomponent-viewer` change remains responsible for any default-policy flip.
