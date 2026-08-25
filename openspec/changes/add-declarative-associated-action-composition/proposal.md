## Why

Causeway grid resources can associate actions structurally with a property or collection by nesting action references beneath that member.
The current `<causeway-object>` grid interpreter preserves this relationship and renders associated actions immediately after their owning member.
Directly authored HTML does not expose an equivalent supported composition contract because `<causeway-property>` replaces its light-DOM content and `<causeway-collection>` currently treats only `<causeway-collection-column>` children as declarative configuration.
HTML-authored application pages therefore cannot express established Causeway member-associated action placement using the natural semantic element hierarchy.

## What Changes

- Support direct declarative `<causeway-action>` children beneath `<causeway-property>` and `<causeway-collection>` without inferring association from arbitrary descendants or adjacent markup.
- Recognize parser-late direct declarations deterministically while preserving each action as one connected semantic element rather than cloning or regenerating it.
- Preserve declaration order and render one responsive associated-action region immediately after the owning member's primary presentation.
- Keep every associated action independently governed by its own GraphQL visibility, usability, parameter, validation, invocation, and result semantics.
- Keep an independently visible or enabled action available when its owning property or collection is hidden or disabled.
- Treat association as presentation structure only and do not let the owning member fabricate, authorize, invoke, hide, disable, or otherwise control the action.
- Preserve associated action declarations, focus, pending interaction state, context generation, and stale-request protection across property or collection loading, rerendering, editing, validation, collection activation, paging, and toolkit fallback.
- Allow declarative collection columns and associated actions to coexist beneath one collection without either child vocabulary consuming or duplicating the other.
- Prevent lifecycle rerendering or declaration capture from creating duplicate live action elements or duplicate action requests.
- Keep generated `<causeway-object>` composition semantically equivalent and reuse a common associated-action presentation contract where practical.
- Document natural HTML compositions such as:

```html
<causeway-property member="name" editable>
  <causeway-action member="updateName"></causeway-action>
</causeway-property>

<causeway-collection member="pets" active>
  <causeway-collection-column member="name"></causeway-collection-column>
  <causeway-collection-column member="species"></causeway-collection-column>
  <causeway-action member="addPet"></causeway-action>
  <causeway-action member="removePet"></causeway-action>
</causeway-collection>
```

- Add deterministic component, interaction, accessibility, responsive-presentation, and browser coverage for property- and collection-associated actions.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds declarative member-associated action composition to property and collection elements while preserving established semantic action and GraphQL authority contracts.

## Impact

- Extends the public semantic child vocabulary of `<causeway-property>` and `<causeway-collection>` without introducing application-facing toolkit elements.
- Aligns directly authored HTML with the associated-action structure already supported by effective Causeway grid resources.
- Affects property and collection light-DOM lifecycle handling, associated-action grouping styles, documentation, fixtures, and component tests.
- May permit the object-layout renderer and direct HTML path to share associated-action configuration and presentation helpers, but does not require a change to the effective-grid resource contract.
- Does not add GraphQL fields, duplicate action invocation, server-side layout requirements, frontend-framework coupling, Vaadin Flow, or a parallel domain-state channel.
- Provides a stable target syntax for future prototype-mode page-authoring suggestions and copyable snippets.
