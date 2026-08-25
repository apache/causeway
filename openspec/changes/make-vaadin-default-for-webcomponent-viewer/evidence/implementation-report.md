# Implementation report

## Common policy

`HtmxViewerProperties.EditorToolkit` accepts `VAADIN` and `NATIVE`, binds from the documented lowercase values, and resolves to Vaadin when no toolkit property is supplied.
Setter tracking distinguishes an effective default from explicit common or deprecated configuration.
Explicit common policy wins regardless of binding order.
When the common property is absent and either deprecated setter is invoked, both adapter groups use the former defaults unless their own old value was supplied.
The compatibility matrix is executable through property and controller tests.

## Foundation selection

Reference configuration now defaults enabled and field configuration defaults to basic, numeric, and local-temporal.
Explicit resolved shell attributes can disable references and supply an empty field-family value before registry selection.
Custom hosts retain the existing JavaScript configuration functions for native diagnostics and injected modules.
Registry precedence, value codecs, exact numeric lexical handling, local temporal precision limits, protected-value redaction, pending values, validation, cancellation, focus, dependent preparation, and semantic events are unchanged.
Reference load failure now exposes a bounded Causeway-owned message and native fallback, while field failures remain family scoped.

## HTMX shell and CSP

The shell emits bounded resolved policy, explicit reference mode, and explicit normalized field families.
Default CSP contains the deterministic deduplicated reviewed union for all qualified closures.
Native CSP contains no Vaadin hash.
Deprecated compatibility mode contains only the old-policy subset.
Every mode retains same-origin scripts and connections, no blanket inline permission, and `style-src-attr 'none'` whenever style-element hashes are required.
Hash permission does not import an asset; landing and unaffected routes retain zero toolkit requests.

## Samples and release modes

Petclinic and the Reference Application no longer opt in through pilot properties.
Their ordinary configuration therefore exercises the supported default.
The complete equivalent browser suites use only `causeway.viewer.webcomponents.htmx.editor-toolkit=native` for rollback.
The vanilla sample imports the foundation index directly and documents pre-import native configuration for a custom HTML host.

## Compatibility and exclusions

The old reference boolean and field-family allow-list remain callable and documented as deprecated.
No GraphQL operation, route codec, persisted model, public Causeway element, semantic event, generated bundle, dependency, license manifest, or package lock changed.
Grid behavior, uploads, unsupported temporal shapes, custom values, Flow, Binder, Pro components, server-side Vaadin state, telemetry, CDN assets, and raw application-facing Vaadin APIs remain excluded.
