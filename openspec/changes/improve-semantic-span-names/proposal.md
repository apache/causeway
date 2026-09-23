## Why

Causeway's semantic spans currently use generic or implementation-facing display names, forcing operators to open span attributes to discover which domain type or member a span represents.
Mixed-in properties and collections are currently exposed as invocations of their underlying `prop` or `coll` actions rather than as accesses to the domain-facing associations.
Page initialization, component configuration, visibility checks, and other pre-render preparation also occur before the existing page-render observation starts, leaving related property and collection accesses without a meaningful semantic owner.
The framework already has stable logical type and member identifiers at the relevant boundaries, so it can make traces easier to scan without exposing object-instance data.

## What Changes

- Give action-invocation spans contextual names of the form `act <logical-type-name>#<action-id>`, using `act` consistently with the Causeway programming model while preserving `causeway.action.invocation` as the stable observation name and `causeway.action.id` as the canonical attribute.
- Resolve mixed-in actions to their domain-facing logical member identifiers rather than exposing implementation identifiers.
- Recognize invocations that implement mixed-in properties and collections and represent them as domain-facing association accesses rather than actions.
- Give mixed-in property accesses contextual names of the form `prop <logical-type-name>#<property-id>`, with stable observation name `causeway.property.access` and canonical attribute `causeway.property.id`.
- Give mixed-in collection accesses contextual names of the form `coll <logical-type-name>#<collection-id>`, with stable observation name `causeway.collection.access` and canonical attribute `causeway.collection.id`.
- Add a Wicket entity-page preparation observation named `prepare <logical-type-name>` around initialization, component configuration, visibility and usability evaluation, and other work completed before actual markup rendering.
- Preserve `causeway.wicket.page.prepare` as the stable page-preparation observation name and `causeway.object.type` as its canonical attribute.
- Make property and collection accesses performed during page preparation descendants of that preparation span.
- Give Wicket entity-page render spans contextual display names based on the rendered domain object's full logical type while preserving `causeway.wicket.page.render` as the observation name and `causeway.object.type` as the canonical attribute.
- Add one bounded Wicket action-prompt render observation identified by the prompted action, without creating spans for individual parameter fields.
- Give Wicket fieldset, property, collection, and action-button render spans contextual display names based on their static layout or metamodel member identifiers.
- Preserve logical-identifier casing in exported Causeway span names and enforce a deterministic 50-character limit with namespace fallback for logical type and member identifiers.
- Keep the root-interaction display name stable.
- Continue to prohibit object titles, bookmarks, primary keys, argument values, user identities, tenancy identifiers, and other instance-specific data from span names and attributes.
- Keep canonical attributes authoritative for filtering and correlation when display names omit a namespace or are truncated.

## Capabilities

### New Capabilities

- `semantic-span-display-naming`: Defines human-readable contextual names for action invocation and mixed-in association access spans while retaining stable observation names, canonical attributes, and bounded cardinality.

### Modified Capabilities

- `wicket-region-render-observation`: Adds meaningful page-preparation, entity-page, action-prompt, and member-region observations to the Wicket lifecycle taxonomy.

## Impact

The change affects core runtime member instrumentation, Wicket page lifecycle and render observation attachment points, focused observation tests, Java-agent compatibility validation, and the Micrometer tracing operations documentation.
It changes action contextual names from `invoke` to `act`, reclassifies mixed-in property and collection implementation calls as association accesses, adds a page-preparation observation, and makes pre-render association accesses descendants of that observation.
It does not change activation, sampling, SDK ownership, exporters, dependencies, or Java-agent-owned HTTP and JDBC instrumentation.
