## Why

Regression export and replay need a complete dotted path through recorded workflows, including navigation from a parent object into its parented child collections.
Today selecting a row from a rendered collection is a UI interaction rather than a command, so recordings lose the parent-to-child lookup step that is needed to remap objects during replay.

## What Changes

- Synthesize safe `ObjectAction`s in the metamodel for parented collections so a recorder can navigate from a parent object to a child object through an invokable command.
- Add one mandatory parameter for the parent object and optional scalar parameters that can identify or narrow the child object within the collection.
- Return the selected collection element as the action result so existing safe action command logging, export, and replay result mapping can establish the recorded-to-replayed object correspondence.
- Hide these actions from normal application pages unless explicitly surfaced for command recording or metamodel tooling.
- Preserve existing collection rendering and direct row selection behavior for ordinary UI navigation.

## Capabilities

### New Capabilities
- `parented-collection-selector-actions`: Synthetic metamodel actions for parented collection navigation that make parent-to-child object selection command-recordable.

### Modified Capabilities
- `safe-action-command-publishing`: Synthetic parented collection selector actions are safe actions and participate in the existing opt-in safe action command publishing flow.
- `command-export-yaml-result`: Exported command YAML can include returned object metadata for logged synthetic selector actions.
- `command-replay-result-mapping`: Replay result mapping can use logged synthetic selector action results to remap later command targets and reference parameters.

## Impact

- Affects metamodel introspection in `ObjectSpecificationDefault`, where associations and actions are assembled.
- Adds internal synthetic `ObjectAction` support for collection-derived lookup actions and their parameters.
- May affect viewers, metamodel export, command export, and replay tests that enumerate or invoke actions.
- Requires tests for action synthesis, parameter shape, invocation semantics, visibility to command recording, and integration with safe action command publishing/export/replay.
