## Why

Synthetic selector actions currently expose eligible scalar child properties as filter parameters even when those properties are not shown as columns in the parented collection.
This can make the synthetic action prompt diverge from the collection view that the action represents and can expose parameters users would not otherwise use when selecting a row.

## What Changes

- Limit synthetic selector action child filter parameters to properties that appear as columns of the associated parented collection.
- Order synthetic selector action child filter parameters according to the collection column order.
- Preserve the mandatory disabled parent parameter before the child filter parameters.
- Preserve existing exclusions for non-scalar, blob, clob, and technical metadata child properties.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: synthetic selector action parameter selection and ordering now follow the associated collection columns.

## Impact

- Affects synthetic parented collection selector action parameter discovery in the metamodel.
- Affects tests covering selector parameter generation and ordering.
- Does not add new configuration properties, APIs, dependencies, or viewer behavior.
