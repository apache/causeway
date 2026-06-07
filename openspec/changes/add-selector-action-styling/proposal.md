## Why

Synthetic parented collection selector actions currently expose the correct name and collection association, but they do not provide the visual affordances expected for generated navigation actions.
Adding default styling makes the generated `Select` action easier to recognize in viewers and aligns its metamodel facets with the equivalent developer-authored `@ActionLayout` declaration.

## What Changes

- Add layout facets to synthetic parented collection selector actions equivalent to `@ActionLayout(cssClass = "btn-outline-secondary", cssClassFa = "hand-point-left", ...)`.
- Preserve the existing selector action name, collection association, invocation, validation, and parameter behavior.
- Ensure the styling metadata is part of the synthesized action metamodel so viewers and layout exporters can consume it consistently.
- No breaking changes.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: synthetic selector actions gain default action layout styling metadata.

## Impact

- Affects synthetic parented collection selector action facet installation in the metamodel.
- Affects viewer rendering and layout export surfaces that already consume action `cssClass` and `cssClassFa` facets.
- Adds or updates metamodel tests for selector action layout facets.
- Does not change public application APIs, configuration properties, or selector matching semantics.
