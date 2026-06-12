## Why

Regression export and replay need a complete dotted path through recorded workflows, including navigation from a parent object into its parented child collections.
Today selecting a row from a rendered collection is a UI interaction rather than a command, so recordings lose the parent-to-child lookup step that is needed to remap objects during replay.

## What Changes

- Add the disabled-by-default `causeway.extensions.command-log.parented-collection-selector-actions-enabled` configuration property that enables synthetic parented collection selector action creation.
- Synthesize safe `ObjectAction`s in the metamodel for parented collections only when that configuration property is enabled so a recorder can navigate from a parent object to a child object through an invokable command.
- Add one mandatory parameter for the parent object and optional scalar parameters that can identify or narrow the child object within the collection.
- Return the selected collection element as the action result so command recording can identify the child object selected from the parented collection.
- Preserve existing collection rendering and direct row selection behavior for ordinary UI navigation when synthetic selector action creation is disabled.

## Capabilities

### New Capabilities
- `parented-collection-selector-actions`: Synthetic metamodel actions for parented collection navigation that make parent-to-child object selection command-recordable.

### Modified Capabilities
- `safe-action-command-publishing`: Synthetic parented collection selector actions are safe actions and participate in the existing opt-in safe action command publishing flow.

## Impact

- Affects metamodel introspection in `ObjectSpecificationDefault`, where associations and actions are assembled.
- Adds internal synthetic `ObjectAction` support for collection-derived lookup actions and their parameters.
- May affect viewers and metamodel export tests that enumerate or invoke actions when the feature is enabled.
- Requires tests for config-gated action synthesis, parameter shape, invocation semantics, and integration with safe action command publishing.
