## Why

Synthetic parented collection selector actions currently only expose scalar child properties as filter parameters.
This prevents command recording and replay from selecting collection rows by reference properties even when the UI already constrains the reference through bounded, choices, or autocomplete semantics.

## What Changes

- Allow eligible reference properties on collection elements to become optional synthetic selector action parameters when the property is backed by a selectable value facet.
- Treat the presence of a bounded, choices, or autocomplete facet as sufficient evidence that a reference property can be selected by the user through a dropdown-like interaction.
- Preserve existing exclusions for child collections, blob or clob properties, technical metadata properties, and reference properties without the required selectable facets.
- Extend selector matching so supplied reference parameter values identify child rows by exact reference equality.

## Capabilities

### New Capabilities

### Modified Capabilities
- `parented-collection-selector-actions`: Selector actions can expose selectable reference object child properties as filters, not only value-type child properties.

## Impact

- Affects synthetic selector action parameter discovery in the metamodel.
- Affects selector validation and invocation matching for reference-typed parameters.
- Requires tests around bounded, choices, and autocomplete reference properties, plus negative coverage for unconstrained references.
- Does not add new configuration properties or change the command DTO shape.
