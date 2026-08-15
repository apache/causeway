## Why

The GraphQL web-component foundation validates shared context and coordinated scalar-property reads, but applications need a complete read-only vocabulary for composing useful domain-object pages without reimplementing Causeway semantics.
A reusable object, property, value, action, collection, and navigation layer is the next step toward both custom pages and a generic viewer.

## What Changes

- Introduce a semantic read-only component vocabulary for domain objects, properties, values, action affordances, collections, collection columns, and object links.
- Discover member capabilities and value shapes from the cached rich GraphQL schema description rather than a duplicate metadata endpoint.
- Introduce a value-renderer registry with standard renderers for GraphQL scalars, enums, object references, and supported Causeway value shapes.
- Render property visibility, usability, descriptions, values, nulls, and errors through the shared object context.
- Render action visibility and usability as semantic affordances while deferring parameter prompts and invocation to a later change.
- Load collection contents lazily, support declarative column requirements, and hydrate row object contexts from the coordinated collection result.
- Publish framework-neutral semantic navigation and action-request events.
- Define accessible loading, empty, hidden, disabled, unsupported-value, and partial-error behavior.
- Provide plain-HTML examples that compose the components without HTMX or another frontend framework.
- Extend the executable `sample-html` application as the acceptance fixture for the complete read-only vocabulary, using deterministic domain data, the real rich GraphQL endpoint, packaged modules, stable browser hooks, and the existing readiness contract.

## Capabilities

### New Capabilities

- `domain-web-components`: Provides framework-neutral semantic components for read-only Causeway domain objects, properties, values, actions, collections, and object navigation.

### Modified Capabilities

None.

## Impact

- Expands the browser component package created by `graphql-web-component-context`.
- Establishes public component names, attributes, properties, slots, events, accessibility behavior, and renderer-extension contracts.
- Uses the existing context, schema cache, requirement model, and rich GraphQL endpoint.
- Expands `viewers/webcomponents/sample-html` without changing its stable route, original sample bookmark, same-origin architecture, or no-frontend-build constraint.
- Does not add property editing, action invocation, generic page composition, collection paging, Playwright, or an HTMX dependency.
