## Why

The Wicket viewer can render an action prompt in a sidebar rather than a modal dialog, preserving the user's view of the object that motivated the action.
A richer version of that pattern could use the lower part of the prompt panel for explicitly selected contextual properties or collections that help the user choose action parameters or verify the consequences of invocation.

The semantic composition is the inverse of member-associated actions.
Instead of nesting actions beneath a property or collection, authored HTML would nest relevant read-only properties or collections beneath an action declaration:

```html
<causeway-action member="bookVisit">
  <causeway-property member="lastVisit"></causeway-property>
  <causeway-collection member="visits" active size="5">
    <causeway-collection-column member="visitAt"></causeway-collection-column>
    <causeway-collection-column member="reason"></causeway-collection-column>
  </causeway-collection>
</causeway-action>
```

The current `<causeway-action>` replaces its light DOM with the action affordance, while the shared interaction controller independently renders parameter prompts as modal `<dialog>` markup.
Direct contextual children would therefore be destroyed, and the controller has no supported contract for projecting authored semantic content into a prompt-owned region.
A follow-on change must define that ownership and lifecycle deliberately rather than treating arbitrary nested markup as a visual trick.

## What Changes

- Introduce a declarative action-prompt context concept in which selected direct `<causeway-property>` and `<causeway-collection>` children describe supporting object information for an action prompt.
- Initially scope contextual members to object actions whose source action and contextual members share one authoritative object identity.
- Keep service actions, parameter-selected targets, action results, and cross-object context outside the initial contract unless a later prototype proves an unambiguous identity model.
- Treat contextual member selection as application-authored presentation only; it does not change action visibility, usability, parameter choices, defaults, validation, invocation, concurrency, or results.
- Keep nested contextual properties read-only in the initial scope even if ordinary property components support editing elsewhere, avoiding optimistic-concurrency and prompt-validity ambiguity while an action is pending.
- Preserve normal collection visibility, usability, bounded paging, ordering, row identity, and lazy loading semantics for contextual collections.
- Perform no contextual member or collection GraphQL work while the action prompt is closed.
- When the prompt opens, resolve each contextual component against the exact object context already associated with the source action rather than interpolating identity or introducing a second domain-state channel.
- Render one labelled supporting-information region after the parameter form and before or above the prompt's submit/cancel controls according to the accepted accessibility prototype.
- Support a wide-screen sidebar presentation inspired by Wicket while retaining a bounded narrow-screen presentation such as a drawer or modal panel.
- Keep the interaction controller authoritative for prompt preparation, parameters, validation, submission, cancellation, stale-result protection, announcements, focus restoration, and result handling.
- Preserve each contextual declaration across prompt rerendering without duplicate live components, duplicate GraphQL reads, lost paging state, or protected-value disclosure.
- Hide or omit a contextual member only according to its own GraphQL state; the action declaration does not fabricate visibility or usability.
- Allow ordinary action declarations with no contextual children to retain their current prompt markup, behavior, request shape, and delivery cost.
- Add executable prototypes comparing at least these projection strategies before selecting an implementation:
  - keep stable contextual child nodes beneath the source action and let the prompt presentation visually host that stable region;
  - capture a constrained declarative configuration and instantiate prompt-local semantic components;
  - provide a controller-owned semantic portal contract that preserves node identity without unsupported DOM relocation.
- Reject strategies that serialize domain values into events, clone protected content, require application-authored GraphQL, disconnect and reconnect context consumers on every prompt update, or couple applications to Wicket or raw toolkit elements.
- Add deterministic component, interaction, accessibility, responsive, request-count, lifecycle, CSP, and browser evidence before promotion from prototype to supported behavior.

## Prototype Questions and Acceptance Gates

- Determine whether the action source or the interaction controller owns the supporting-information DOM while keeping one interaction state machine.
- Determine how the source action advertises contextual declarations without adding non-serializable or sensitive data to public semantic events.
- Determine whether the existing modal `<dialog>` remains a supported policy beside sidebar presentation or whether prompt layout becomes a viewer-owned responsive choice.
- Determine the accessible reading and focus order among title, parameter form, supporting information, validation errors, submit, and cancel controls.
- Determine whether navigation from a contextual object link cancels the prompt, asks for confirmation, or is excluded from the initial interaction contract.
- Confirm that hidden, disabled, protected, resource, unsupported, and authorization-sensitive members reveal no additional information through markup, events, errors, diagnostics, or request summaries.
- Confirm that opening, validating, rerendering, cancelling, invoking, navigating, and replacing the route produce bounded requests with no duplicate contextual subscriptions.
- Confirm that a contextual collection remains bounded and does not become an implicit complete-object or complete-collection projection.
- Confirm that default Vaadin and explicit native editor policies affect action parameters only and do not change contextual-member authority or delivery.
- Confirm that pages without contextual declarations incur no additional introspection, GraphQL operations, JavaScript modules, stylesheet hashes, or prompt markup.

## Capabilities

### New Capabilities

- `action-prompt-context-composition`: Defines declarative, bounded, read-only object context presented with an action prompt while preserving one authoritative action interaction lifecycle.

### Modified Capabilities

- `domain-web-components`: Extends action light-DOM composition with constrained property and collection declarations without changing the independent semantic contracts of those components.
- `generic-htmx-web-component-viewer`: Adds an optional Wicket-inspired sidebar or responsive prompt presentation while retaining viewer-owned shell, route, CSP, and interaction policy.

## Impact

- Follows `add-declarative-associated-action-composition`, which establishes the stable direct-child lifecycle techniques needed before extending `<causeway-action>` in the inverse direction.
- Affects action light-DOM handling, interaction-controller prompt ownership, prompt presentation policy, accessibility, focus restoration, contextual collection lifecycle, and application documentation.
- Requires a real object-action fixture where contextual properties and a bounded collection materially assist parameter entry or invocation review.
- May require a narrow internal declaration model or semantic portal API, but must not expose GraphQL documents, route identities, protected values, metamodel objects, or persistence state to page authors.
- Does not add GraphQL schema fields, Wicket runtime dependencies, server-side prompt rendering, template interpolation, arbitrary HTML projection, telemetry, CDN assets, Vaadin Flow, or application-facing raw Vaadin elements.
- Provides a possible future authoring syntax for catalogue examples, prototype diagnostics, and a semantic page designer only after the interaction and security gates are satisfied.

## Sequencing

1. Complete and archive `add-declarative-associated-action-composition`.
2. Prototype prompt ownership and contextual projection without changing the public contract.
3. Review the prototype against action interaction, protected-value, route-lifecycle, and responsive-accessibility evidence.
4. Expand this draft into design, specifications, and implementation tasks only if one strategy passes every acceptance gate.
