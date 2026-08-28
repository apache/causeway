## Context

The accepted Vaadin integration uses pinned free-core closures behind Causeway-owned semantic editors.
The HTMX viewer defaults `editor-toolkit` to `vaadin`, but read-only property values still use the standard HTML value-renderer registry and `<cw-action>` still renders a native button.
Consequently, a property changes visual control family when editing begins, action buttons retain a separate native theme, and read-only routes avoid every Vaadin request.

The existing architecture already provides reusable foundations: introspection-driven value descriptors, reversible codecs, independently lazy Vaadin family loaders, strict generated-asset policy, exact CSP hashes, document policy, native fallback, semantic events, and broad native/Vaadin browser qualification.
The extension must reuse those boundaries rather than expose Vaadin as a new public application API.

## Goals / Non-Goals

**Goals:**

- Use reviewed Vaadin fields for eligible read-only scalar and enum presentation under the default toolkit.
- Use a reviewed Vaadin Button for ordinary `<cw-action>` affordances.
- Preserve Causeway ownership of labels, descriptions, disabled reasons, values, errors, GraphQL execution, interaction state, events, focus policy, and routing.
- Provide one common toolkit policy with an immediate complete native rollback.
- Reuse independently lazy field-family closures and add an independently lazy action-button closure.
- Preserve deterministic packaging, strict CSP, legal, accessibility, responsive, lifecycle, and browser-isolation gates.

**Non-Goals:**

- Expose raw Vaadin elements, events, item models, renderer APIs, theme internals, or Shadow DOM as public contracts.
- Replace object links, null, resources, LOBs, custom renderers, unsupported values, collections, Grid, or application menu bars.
- Replace property edit, save, cancel, clear, action-prompt, or shell controls in this change.
- Present protected values through a read-only field or approximate any adopted value family with disabled semantics.
- Add Vaadin Flow, Binder, server-side state, Pro components, telemetry, CDN assets, or blanket CSP permissions.
- Change GraphQL operations, value codecs, application markup, canonical routes, or persisted data.

## Decisions

### Introduce one component-toolkit policy

The HTMX viewer will add `causeway.viewer.webcomponents.htmx.component-toolkit=vaadin|native`, defaulting effectively to `vaadin`.
It governs eligible references, editors, read-only fields, and ordinary action buttons.

Resolution precedence will be:

1. An explicitly configured `component-toolkit`.
2. An explicitly configured deprecated `editor-toolkit`, interpreted as the complete component policy so existing native rollback remains complete.
3. The existing deprecated reference-widget and field-family properties, preserving their former independent editor-only compatibility behavior while read-only presentation and action buttons remain native.
4. The default Vaadin component policy.

The shell will expose the resolved component policy through a stable Causeway-owned data attribute while retaining bounded compatibility diagnostics for old properties.
Invalid values fail configuration binding.

Keeping `editor-toolkit` as the permanent master property was rejected because its name would misdescribe read-only and action presentation.
Independent presentation and editor master switches were rejected because they multiply supported combinations and weaken the one-property rollback gate.

### Qualify read-only presentation by semantic shape

The read-only adapter will participate only after application value-renderer precedence has been resolved.
An application-specific renderer, object reference, null renderer, LOB renderer, custom value, unsupported value, collection, or other non-standard result remains authoritative.

The first qualified read-only families are:

- single-line and semantic multiline text;
- Boolean values through Vaadin Checkbox's genuine read-only state rather than disabled state;
- enum and bounded scalar-choice display where the candidate provides genuine read-only semantics;
- exact and machine numeric values without JavaScript numeric coercion;
- local date, millisecond-representable local time, and millisecond-representable local date-time.

Protected values remain on the established redacted presentation.
Offset-bearing, zoned, legacy temporal, resource, custom, reference, collection, and unqualified values remain native or explicitly unsupported.

Using Vaadin fields for every scalar was rejected because disabled controls and lossy value coercion would weaken established semantics.

### Keep labels and explanations Causeway-owned

`<cw-property>` will retain its existing visible label, description placement, tooltip behavior, disabled-reason focus target, string alignment hook, and semantic host classes.
The internal Vaadin field will not render a duplicate visible label.
It will receive an accessible name and description from the Causeway label and description identifiers and will use `readonly`, not `disabled`, for visible non-editable values.

This preserves the public styling and accessibility boundary and avoids moving disabled-reason behavior into toolkit Shadow DOM.
Allowing Vaadin to replace the complete property layout was rejected because it would make toolkit label and helper internals observable and would break custom-page styling hooks.

### Add internal view and action adapters

A Causeway-owned internal read-only field adapter will accept normalized semantic value state and a family decision, load the existing family closure, instantiate the reviewed Vaadin control, and publish no application-facing toolkit event.
The standard value-renderer path will mount that adapter only when the resolved standard renderer and semantic shape are qualified.
The adapter will use revision and connection checks so a late import or upgrade cannot replace a newer value, a native fallback, or a disconnected route.

A separate Causeway-owned action-control adapter will start from the established action state, lazy-load a new pinned Vaadin Button closure, and translate activation into the existing `<cw-action>` request path.
It will not invoke GraphQL, navigate, or own action interaction state.
Hidden actions mount no control, and disabled actions preserve the existing accessible reason outside toolkit internals.

Directly emitting raw `vaadin-*` markup from public renderers was rejected because asynchronous module failure would have no bounded native fallback and applications could accidentally depend on toolkit events.
Reusing the editor host for read-only display was rejected because it would blur interaction state and invite editor-only behavior on view pages.

### Reuse field closures and isolate the button closure

Read-only basic, numeric, and local-temporal controls will reuse the existing pinned family closures and generated policy metadata.
The action adapter will use an independent `vaadin-actions` closure containing Vaadin Button and only its reviewed free-core transitive dependencies.

A route requests a family closure when its first eligible read-only presentation or editor connects.
It requests the action closure when its first visible ordinary action connects.
Unused families remain unrequested, and native mode requests no Vaadin closure.

Adding Button to the basic field closure was rejected because action-only pages would download unrelated field controls and field-only pages would inherit unrelated button drift.

### Extend strict CSP from generated policy metadata

The default CSP hash union will include the reviewed reference, field-family, and action-button hashes allowed by the resolved component policy.
Compatibility subsets include only their former editor hashes and do not enable read-only or action adapters.
Native policy includes no Vaadin hash.
`style-src-attr 'none'`, same-origin scripts and connections, no `unsafe-inline`, and no external asset source remain mandatory.

### Fail independently and preserve current semantic state

A presentation-family load or definition failure disables only that family for the current document and rerenders the current value through its established native renderer.
An action-button failure disables only the action adapter and rerenders the established native button.
Failures do not mutate values, descriptors, validation, interaction state, semantic events, routes, or other family eligibility.

If the focused control is replaced during fallback, focus returns to the equivalent Causeway-owned affordance when one remains operable.
No failed or stale adapter may leave duplicate controls, overlays, listeners, or pending asynchronous work.

### Make default and native modes release gates

Foundation and standalone browser fixtures will verify each qualified view family and action state under default and native policies.
Petclinic will verify ordinary object pages, custom pages, view/edit transitions, actions, disabled reasons, responsive behavior, and route isolation.
The pinned Reference Application will exercise representative scalar, temporal, enum, custom, protected, disabled, hidden, service-action, object-action, route replacement, module-failure, and native-parity cases.

Qualification includes axe or equivalent accessibility checks, keyboard-only operation, visible focus, reduced motion, forced colors, theme switching, narrow and wide layouts, console and page errors, external requests, CSP violations, duplicate controls, overlay leaks, and compressed budgets.

## Risks / Trade-offs

- **Risk: Most object pages now load the basic field or action closure.** → Measure route-level requests and compressed bytes, keep closures independent, enforce budgets, and retain native mode.
- **Risk: Read-only Vaadin fields may look interactive or expose internal focus targets.** → Require genuine `readonly` semantics, remove picker affordances where necessary, and reject any family that fails keyboard or accessibility qualification.
- **Risk: Causeway labels and Vaadin field internals may produce duplicate accessible names.** → Keep one visible Causeway label, bind the internal control to its identifiers, and assert the computed accessible tree.
- **Risk: Application renderer precedence could be bypassed by toolkit selection.** → Resolve application renderers first and permit the Vaadin view adapter only for the selected standard eligible renderer.
- **Risk: Asynchronous upgrade can replace current route or value state.** → Use connection, generation, and revision guards plus deterministic failure injection.
- **Risk: Button theming can diverge from icon-only editor controls that remain native.** → Document the bounded scope, preserve shared Causeway variables, and defer those controls until separately qualified.
- **Risk: New component policy precedence can surprise deprecated-property users.** → Preserve old independent behavior only when no common property is explicit, emit diagnostics, and cover every precedence combination.

## Migration Plan

1. Add component-toolkit configuration, shell policy, precedence tests, and compatibility diagnostics without changing rendering.
2. Add and audit the deterministic Vaadin Button closure and extend generated CSP policy.
3. Add read-only family adapters behind feature selection with native fallback and application-renderer precedence.
4. Add the ordinary action-button adapter behind the same component policy.
5. Update themes, documentation, samples, and browser fixtures.
6. Run default and native release matrices, route-loading and bundle budgets, strict CSP, accessibility, lifecycle, and Reference Application qualification.
7. Deprecate `editor-toolkit` in favor of `component-toolkit` while preserving bounded compatibility.

Rollback is immediate through `causeway.viewer.webcomponents.htmx.component-toolkit=native`.
Code rollback reverts adapter selection, the action closure, CSP hashes, configuration, and documentation together without application markup or data migration.

## Open Questions

None.
