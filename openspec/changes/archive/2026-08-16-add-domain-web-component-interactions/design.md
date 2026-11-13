## Context

The preceding changes provide shared GraphQL and object contexts plus a read-only semantic component vocabulary.
Existing action components publish semantic action requests, leaving the host free to provide interaction behavior.
This change supplies the standard interaction implementation while preserving those host override points.
The rich GraphQL grammar already exposes property validation, choices, autocomplete, setters, action parameter semantics, invocation, and top-level mutations according to the configured API variant.
The completed `sample-html` application now provides a responsive, accessible read-only showcase with deterministic data, stable selectors, semantic event diagnostics, a real same-origin `/graphql` endpoint, and the `run-sample-html` Maven profile.
This change will evolve that same page into the executable interaction fixture rather than introduce another sample or generic viewer.

## Goals / Non-Goals

**Goals:**

- Add consistent property editing and action invocation to generic and custom pages.
- Drive editors and prompts from introspected rich-schema capabilities.
- Keep GraphQL query-versus-mutation details inside the semantic context layer.
- Handle dependent parameter semantics, cancellation, stale responses, validation, and partial errors.
- Reconcile active object state after successful commands.
- Publish framework-neutral semantic outcomes and allow application overrides.
- Prove representative property and action interactions through the existing executable sample and real rich GraphQL endpoint.
- Preserve the sample's established read-only states, selectors, route, bookmark, responsive presentation, and no-frontend-build architecture.
- Provide repeatable manual verification for browser-only interaction, focus, layout, and network behavior.

**Non-Goals:**

- Collection editing, selection, bulk actions, or drag-and-drop.
- Optimistic client-side domain updates before server success.
- Generic routing or HTMX page composition.
- A duplicate action or property metadata API.
- Automatic inference of member dependencies not represented by GraphQL arguments.
- Changes to the rich schema without a demonstrated missing semantic.

## Decisions

### Extend object contexts with semantic commands

Components will call object-context commands such as validate property, set property, resolve action parameter state, validate action arguments, and invoke action.
The commands will accept semantic member IDs and values and will use introspected wrapper fields, arguments, input types, and top-level mutation fields to construct GraphQL operations.

Page composers and components will not select generated mutation names or API variants directly.
The context will report unsupported commands when the configured schema is query-only or omits the required field.

### Keep editors replaceable through renderer registries

Property and action-parameter editors will be resolved through a registry analogous to the read-only value-renderer registry.
The standard library will supply editors for supported scalar, enum, object-reference, and choice-based values.
Applications may register more-specific editors without replacing property or action orchestration.

Editors will exchange semantic value, pending, validation, choice, and autocomplete state with their owning property or prompt component.
They will not execute GraphQL independently.

### Implement property editing as an explicit state machine

A property interaction will move through view, preparing, editing, validating, saving, and failed states.
Opening the editor will lazily request choices or other editor semantics supported by the property wrapper.
Changing a value will start cancellable validation or autocomplete only when the schema advertises that capability.
Saving will be blocked while invalid or pending and will execute through the context command API.

The original value remains authoritative until the mutation succeeds.
Cancel restores the view state without changing the object context.

### Handle action requests through a standard controller

The existing action affordance will continue to emit a semantic action-request event.
A standard nearest interaction controller will handle unclaimed requests, inspect the action descriptor, and either invoke a parameterless action or open a semantic prompt.
Applications may prevent the default handling and provide a custom prompt or workflow while continuing to use context commands.

This preserves the simple action component and keeps modal, sidebar, inline, or framework-specific prompt presentation replaceable.

### Ship inline property editors and one accessible modal prompt

Property editing will remain local to each property so the authoritative value, pending value, validation reason, save, and cancel controls stay visibly associated.
The standard action interaction controller will provide one accessible modal prompt for parameterized actions, using native dialog semantics where available and a testable light-DOM form contract.
The controller remains presentation-neutral at its semantic boundary, so applications may claim action requests and substitute a sidebar, inline form, or framework-specific prompt.
A single default modal is preferred over multiple initial presentations because it gives custom pages a complete accessible behavior without multiplying focus and validation implementations.

### Apply a deterministic validation policy

Discrete choice changes will validate immediately when the schema advertises validation.
Free-text and autocomplete changes will use cancellable debounced validation, while blur and every save or invocation attempt will force final validation of the current generation.
Applications may override timing policy, but they may not bypass final server validation before a standard mutation or invocation.
This balances responsive feedback with protection against one network request per keystroke.

### Derive parameter negotiation from GraphQL fields and arguments

The prompt will build parameters in schema order from the introspected action parameter wrappers.
Defaults, hidden state, disabled state, choices, autocomplete, and validation will be requested only where corresponding wrapper fields exist.
When a parameter changes, the controller will invalidate later parameter state whose GraphQL semantic fields accept preceding parameters as arguments and will recompute those fields with the current pending argument set.

No separate dependency graph will be invented beyond the dependencies observable in the schema argument grammar.

### Hide API variant and operation placement

Safe action invocation may be represented within the rich object query, while mutating actions and property updates are represented by top-level mutation fields when the configured API variant permits them.
The context will introspect available query and mutation fields and select the semantically valid operation.
It will not use a mutating query merely because a non-spec-compliant variant exposes one when an appropriate top-level mutation is available.

Components receive semantic unsupported, validation, success, or interaction-error outcomes rather than GraphQL operation-placement details.

### Reconcile conservatively after successful commands

After a successful property update or action invocation, the owning object context will invalidate and execute its complete active read projection unless the returned object payload can safely satisfy that projection.
A returned object with the same bookmark may hydrate selected fields before refresh.
A different object, collection, scalar, or void result will be normalized as a semantic interaction result.

Broad active-projection refresh is preferred initially because Causeway supporting methods can make any visible member state depend on the changed object.

### Publish results without automatic navigation

Object, collection, scalar, and void results will always produce a typed bubbling and composed semantic result event.
The standard controller may render a local result summary, but it will never navigate automatically, including for object results.
Host routing remains responsible for deciding whether an object bookmark should replace the current page, open elsewhere, or remain only as a result.
This preserves the framework-neutral navigation boundary established by read-only object links.

### Serialize mutations and sequence transient requests

Mutating commands for one object context will execute serially to avoid ambiguous local reconciliation.
Validation, choices, defaults, and autocomplete reads may overlap, but each interaction state will carry a generation and obsolete responses will be discarded.
Autocomplete will be cancellable and debounced by the standard editor.

### Map errors to prompts and members

GraphQL errors will be associated using response paths and command identity.
Property validation and mutation errors will remain with the property editor.
Parameter errors will remain with the corresponding prompt input where possible, while invocation-level errors will appear at prompt level.
Unrelated object-context data will remain available.

### Extend the deterministic executable sample

The existing root object `causeway.webcomponents.sample.SampleObject:s_sample-1` will remain the interaction target.
Existing hidden and deliberately disabled members will retain their semantics, while already enabled scalar and enum properties will exercise standard editors, cancellation, validation, and server-authoritative save behavior.
Additional deterministic action and supporting-method semantics will cover a safe parameterless scalar result, a parameterized mutating result, choices or autocomplete where exposed by the rich grammar, and representative object, collection, scalar, and void normalization in fixtures and integration coverage.
The page will add an interaction controller, prompt and result outlet, semantic interaction diagnostics, and additive stable selectors without introducing HTMX, routing, or generic metadata-driven composition.

Random-port integration tests will verify actual rich-schema capabilities and operations.
Manual verification will launch the sample through `mvn -f viewers/webcomponents/pom.xml -Prun-sample-html`, exercise pointer and keyboard flows, inspect query-versus-mutation network placement, repeat narrow and wide light and dark presentation checks, and require no GraphQL or console failures.

## Risks / Trade-offs

- [Rich parameter semantics can produce many requests] → Load semantics lazily, batch compatible fields, debounce autocomplete, and recompute only later parameters whose fields accept changed arguments.
- [Broad refresh after mutations costs an extra operation] → Prefer correctness first and later merge complete mutation payloads when measurements justify it.
- [Schema API variants differ] → Introspect available query and mutation capabilities and test all supported variants.
- [Application prompt customization can bypass accessibility] → Keep the standard controller accessible and document the semantic command and event obligations of replacements.
- [Object-reference editors can require large choices] → Prefer advertised autocomplete where available and document the cost of complete choice lists.
- [Out-of-order asynchronous semantics can corrupt prompts] → Tag every transient request with its interaction generation and discard obsolete responses.

## Migration Plan

The interaction layer is additive.
Existing read-only pages continue to render unchanged until edit affordances or a standard interaction controller are enabled.
Existing semantic action-request events remain the extension point, and applications can opt out of standard handling per event or subtree.
The sample enables the standard interaction layer explicitly and retains all read-only hooks so existing automation remains valid.

## Open Questions

No planning-level questions remain for this slice.
Implementation may still reveal a concrete rich-schema grammar gap; any server contract extension will be proposed separately rather than invented inside the component client.
