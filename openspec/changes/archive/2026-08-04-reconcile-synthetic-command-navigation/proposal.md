## Why

Command recording can now include ordinary safe actions and property edits, but navigation through parented collections and scalar references still has no invokable action representation in the Causeway 4 metamodel.
Maintenance recordings depend on deterministic synthetic navigation actions so those user transitions can be published, exported, and eventually replayed without changing ordinary domain models.

## What Changes

- Synthesize deterministic safe navigation actions for eligible parented collections and scalar reference properties when command-log recording support is enabled.
- Suppress synthetic action creation when recording support is disabled or the owning type implements `CommandRecordingSuppressed`.
- Give synthetic actions framework metadata, associated-member layout metadata, stable `__causeway_navigate_to_` identifiers, the display name `Navigate To`, and secondary navigation styling.
- Derive optional parented-collection selector filters from eligible displayed child columns in collection-column order, including constrained reference and tri-valued boolean filters.
- Validate selector filters against the live collection and invoke only when exactly one child matches.
- Disable collection selectors for empty collections and scalar-reference navigation for null references while retaining the actions in the metamodel.
- Return the uniquely selected collection element or referenced object through the normal action invocation and command-result pipeline.
- Preserve ordinary collection/reference traversal and existing metamodel behavior when recording support is disabled.

## Capabilities

### New Capabilities

- `synthetic-command-navigation`: Defines configuration-gated parented-collection selectors, scalar-reference navigation actions, their metadata and parameters, usability and validation, invocation results, publication compatibility, and suppression.

### Modified Capabilities

None.

## Impact

- `core/metamodel`: Causeway 4 programming-model post-processing, synthetic action construction, marker and behavior facets, parameter derivation, layout metadata, validation, and invocation.
- `core/mmtest`: Metamodel and behavior coverage for entity and view-model owners, suppression, metadata, parameter selection and ordering, matching, usability, and invocation.
- `core/runtimeservices`: Command execution compatibility for synthetic selector arguments and result capture where the existing action execution path requires adaptation.
- `core/config` documentation: Clarifies that recording support also opts eligible navigation associations into synthetic action creation.
- No new public configuration property is introduced, and no replay mapping, unified command manager, reference-data, export-reachability, workflow, or background-completion behavior is included.
