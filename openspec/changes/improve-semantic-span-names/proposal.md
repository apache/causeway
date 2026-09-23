## Why

Causeway's semantic spans currently use generic display names, forcing operators to open span attributes to discover which domain type or action a span represents.
The framework already has stable logical type and member identifiers at the relevant boundaries, so it can make traces easier to scan without exposing object-instance data or destabilizing observation names.

## What Changes

- Give action-invocation spans contextual display names based on the domain-facing logical member identifier, including correct mixin resolution, while preserving `causeway.action.invocation` as the observation name and `causeway.action.id` as the canonical attribute.
- Give Wicket entity-page render spans contextual display names based on the rendered domain object's full logical type while preserving `causeway.wicket.page.render` as the observation name and `causeway.object.type` as the canonical attribute.
- Add one bounded Wicket action-prompt render observation identified by the prompted action, without creating spans for individual parameter fields.
- Give Wicket fieldset, property, collection, and action-button render spans contextual display names based on their static layout or metamodel member identifiers.
- Preserve logical-identifier casing in exported Causeway span names and enforce a deterministic 50-character limit with namespace fallback for logical type and member identifiers.
- Keep the root-interaction display name stable.
- Continue to prohibit object titles, bookmarks, primary keys, argument values, user identities, tenancy identifiers, and other instance-specific data from span names and attributes.
- Keep canonical attributes authoritative for filtering and correlation when display names omit a namespace or are truncated.

## Capabilities

### New Capabilities

- `semantic-span-display-naming`: Defines human-readable contextual names for coarse Causeway semantic spans while retaining stable observation names, canonical attributes, and bounded cardinality.

### Modified Capabilities

- `wicket-region-render-observation`: Adds meaningful entity-page contextual names and a bounded action-prompt render observation to the existing Wicket render taxonomy.

## Impact

The change affects core runtime action instrumentation, Wicket render observation descriptors and attachment points, focused observation tests, and the Micrometer tracing operations documentation.
It changes exported span display names for action invocations and Wicket page, prompt, fieldset, property, collection, and action-button rendering but does not change activation, trace parentage, sampling, SDK ownership, dependencies, or canonical span attributes.
