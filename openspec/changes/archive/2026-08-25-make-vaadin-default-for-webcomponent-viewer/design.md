## Context

The foundation now has qualified internal Vaadin adapters for single and multiple references plus basic, numeric, and millisecond-representable local-temporal values.
Each closure is pinned, same-origin, independently lazy, exact-hash CSP qualified, license reviewed, vulnerability checked, and covered in default-candidate and explicit-native browser suites.
The current runtime still initializes both adapter systems disabled and the HTMX viewer exposes two pilot-era controls: a reference boolean and a field-family allow-list.
This final adoption change must alter selection policy without changing the Causeway component, GraphQL, route, persistence, value-codec, or interaction boundaries.

## Goals / Non-Goals

**Goals:**

- Make all qualified packaged Vaadin adapters the default internal implementation.
- Provide one documented `vaadin|native` rollback property.
- Preserve bounded compatibility for explicitly configured old properties.
- Keep every closure route-lazy and failure-isolated.
- Keep exact-hash CSP, deterministic packaging, accessibility, and native parity as release gates.
- Replace production pilot terminology with supported-default terminology.

**Non-Goals:**

- Adding or changing GraphQL operations, value codecs, semantic events, canonical routes, or public Causeway elements.
- Adding Grid query behavior, upload adapters, unsupported temporal shapes, custom-value adapters, Flow, Binder, Pro components, server-side Vaadin state, telemetry, or CDN assets.
- Removing native controls or the deprecated properties in this change.
- Combining independently built reference and field closures or eagerly loading them.

## Decisions

### One common server policy with explicit-precedence tracking

Add `HtmxViewerProperties.EditorToolkit` with `VAADIN` and `NATIVE`, represented externally as `vaadin` and `native`.
The property defaults effectively to Vaadin, while its setter records whether the common property was explicitly configured.
This explicit marker is necessary because Spring configuration binding otherwise cannot distinguish a default value from an application-supplied value.

An explicitly configured common property wins over both deprecated properties.
If the common property is absent and either deprecated property setter was invoked, the viewer enters compatibility mode and preserves the old complete policy: references default false and field families default empty unless their corresponding deprecated value was supplied.
If neither new nor deprecated configuration is explicit, references and all three qualified field families are enabled.
This avoids surprising applications that still configure only one old property while ensuring a clean installation receives the new default.

Alternatives considered were allowing each legacy value to override only its own half of the new default, which would unexpectedly enable the other half for old configurations, and treating primitive defaults as explicit, which would prevent the default flip.

### Render resolved policy, not configuration ambiguity

The HTMX shell emits `data-causeway-editor-toolkit`, an explicit resolved reference mode, and an explicit resolved field-family value.
Foundation modules consume the resolved per-adapter values and do not reproduce server precedence logic.
The diagnostic common attribute never becomes application-facing control markup.

Outside HTMX, foundation adapter configuration defaults to Vaadin and all qualified field families.
Existing configuration functions remain available for explicit native tests, custom hosts, injected modules, and diagnostics.
Native policy disables adapters before registry selection and therefore requests no toolkit closure.

### Default policy changes selection only

Editor-registry precedence, semantic eligibility, codecs, pending state, validation, operation planning, cancellation, focus restoration, protected-value redaction, and family-scoped failure handling remain unchanged.
Changing the default therefore does not create a second interaction state machine or a toolkit-specific public contract.
Unsupported values and failed families continue to select the native or explicit unsupported implementation.

### CSP permits reviewed closures without loading them

The controller computes CSP from the resolved policy.
A default response permits only the already reviewed exact style hashes for references and all three field families, keeps `style-src-attr 'none'`, and adds no blanket inline source.
Hash permission is not an asset request: JavaScript imports remain triggered only when an eligible adapter connects.
Native policy emits none of the Vaadin hashes.
Compatibility mode emits only hashes for its resolved old policy.

### Deprecation is visible but non-breaking

Java accessors and configuration metadata retain the old property names and document them as deprecated.
Documentation gives the common property precedence and migration table.
No runtime warning is added because configuration can be instantiated repeatedly in tests and infrastructure, and noisy logs would not improve deterministic behavior.
Removal requires a later compatibility change.

### Qualification compares supported default and rollback

Petclinic and the pinned Reference Application run once with no toolkit override and once with `editor-toolkit=native`.
Legacy precedence receives focused unit and shell tests rather than duplicating every browser journey.
Default route-isolation assertions prove that permitted CSP hashes do not cause eager network requests.
The existing deterministic bundle, npm audit, RAT, accessibility, strict-CSP, inventory, and exact-value gates remain release requirements.

## Risks / Trade-offs

- [Default controls alter visual and keyboard details] → Keep semantic assertions, axe, focus, Escape, theme, forced-colors, and explicit-native comparison gates.
- [Old partial configuration could acquire newly enabled adapters] → Enter complete old-policy compatibility mode whenever either deprecated setter is invoked and the common property is absent.
- [Default CSP contains more hashes] → Permit only generated reviewed exact hashes and prove native policy removes all of them while assets remain lazy.
- [A closure fails after becoming default] → Retain document-scoped reference fallback and family-scoped field fallback with pending state and native rerendering.
- [Foundation consumers relied on disabled JavaScript defaults] → Treat this as the declared breaking policy change and retain one-call native configuration before interaction rendering.
- [Deprecated properties remain maintenance surface] → Centralize their interpretation in resolved policy methods and document a later removal boundary.

## Migration Plan

1. Add and test resolved common and compatibility policy in HTMX properties.
2. Change foundation defaults and shell attributes to the resolved policy without changing adapter eligibility.
3. Generate CSP from resolved policy and verify default, native, subset compatibility, and deduplication.
4. Remove candidate opt-ins from samples and run default and explicit-native suites.
5. Update production and configuration documentation from pilot to supported-default terminology.
6. Retain deprecated properties for the compatibility period.
7. Roll back any deployment immediately with `causeway.viewer.webcomponents.htmx.editor-toolkit=native`.

## Open Questions

None.
The prerequisite qualification changes resolved scope, budgets, browser behavior, CSP hashes, and fallback policy before this default flip.
