## Why

Synthetic parented collection selector actions currently work for entity-owned collections but are unnecessarily restricted away from ordinary runtime view models.
View models such as the PetClinic home page can expose entity collections that should be recordable and navigable using the same selector mechanism.

## What Changes

- Remove the runtime restriction that only entity-owned parented collections can receive synthetic selector actions.
- Allow view-model-owned collections to receive selector actions when their element type is an entity, view model, or abstract domain type.
- Preserve the existing command-log recording support opt-in, selector action styling, parameters, validation, invocation, and disabled-by-default behavior.
- No breaking changes.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: selector action synthesis applies to eligible view-model-owned collections as well as entity-owned collections.

## Impact

- Affects selector action eligibility in the metamodel synthesis path.
- Affects applications with command-log recording support enabled that expose eligible collections from view models.
- Adds or updates metamodel tests for view-model-owned collections with entity or view-model elements.
- Does not change public APIs, configuration properties, selector matching semantics, or viewer rendering logic.
