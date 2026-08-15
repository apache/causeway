## Context

The preceding changes provide shared GraphQL and object contexts plus a read-only semantic component vocabulary.
Existing action components publish semantic action requests, leaving the host free to provide interaction behavior.
This change supplies the standard interaction implementation while preserving those host override points.
The rich GraphQL grammar already exposes property validation, choices, autocomplete, setters, action parameter semantics, invocation, and top-level mutations according to the configured API variant.

## Goals / Non-Goals

**Goals:**

- Add consistent property editing and action invocation to generic and custom pages.
- Drive editors and prompts from introspected rich-schema capabilities.
- Keep GraphQL query-versus-mutation details inside the semantic context layer.
- Handle dependent parameter semantics, cancellation, stale responses, validation, and partial errors.
- Reconcile active object state after successful commands.
- Publish framework-neutral semantic outcomes and allow application overrides.

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

### Serialize mutations and sequence transient requests

Mutating commands for one object context will execute serially to avoid ambiguous local reconciliation.
Validation, choices, defaults, and autocomplete reads may overlap, but each interaction state will carry a generation and obsolete responses will be discarded.
Autocomplete will be cancellable and debounced by the standard editor.

### Map errors to prompts and members

GraphQL errors will be associated using response paths and command identity.
Property validation and mutation errors will remain with the property editor.
Parameter errors will remain with the corresponding prompt input where possible, while invocation-level errors will appear at prompt level.
Unrelated object-context data will remain available.

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

## Open Questions

- Which prompt presentations should ship initially: modal only, inline only, or a presentation-neutral controller plus one default modal?
- Should property validation run on every change, on blur, or through a configurable policy with a conservative default?
- Which action result shapes should trigger navigation automatically versus only publishing a semantic result event?
