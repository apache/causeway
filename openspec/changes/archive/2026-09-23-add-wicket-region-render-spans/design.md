## Context

The OpenTelemetry Java agent currently creates automatic HTTP and JDBC spans, while Causeway adds `causeway.root.interaction` and `causeway.action.invocation` through `CausewayObservationIntegration`.
Entity-page work below the root interaction is otherwise opaque even though the Wicket component tree already has stable semantic boundaries for the page, fieldsets, properties, collections, and action links.

Wicket separates component construction, initialization, configuration, visibility evaluation, and markup rendering.
This change measures actual markup rendering and the descendant work performed during that rendering; it does not claim to measure earlier preparation callbacks.

Wicket pages and behaviors can be serialized between requests, Ajax requests can render only selected subtrees, and `Behavior.afterRender(...)` is not guaranteed after a rendering exception.
The design must therefore avoid serializing active telemetry state and must provide request-end failure cleanup.

The maintenance branch favors a small targeted implementation, no new dependencies, and unchanged application behavior when observation is inactive.

## Goals / Non-Goals

**Goals:**

- Make server-side entity-page rendering visible as a page span with nested spans for logical regions.
- Preserve Wicket component-tree parentage for full-page and Ajax partial renders.
- Use stable span names and bounded, type-level metadata.
- Integrate through the existing Micrometer Observation bridge and Java-agent-owned trace context.
- Close scopes correctly after successful rendering and defensively after failures.
- Keep inactive-mode overhead small and behavior functionally unchanged.

**Non-Goals:**

- Measure component construction, initialization, configuration, visibility, usability, or authorization evaluation.
- Instrument action execution, which already has `causeway.action.invocation`.
- Create spans for collection cells, collection rows, row actions, action parameters, Bootstrap layout containers, or browser-side work.
- Record domain values, rendered labels, object titles, bookmarks, user identities, tenancy data, or generated Wicket component paths.
- Add an OpenTelemetry SDK, exporter, sampling policy, or third-party dependency.
- Add observation-detail configuration or a per-request span budget in this initial change.

## Decisions

### Instrument semantic Wicket components rather than generic layout components

The implementation will attach render observation behavior at the established semantic component boundaries represented by `EntityPage`, `PropertyGroup`, regular entity-property scalar panels, `EntityCollectionPanel`, and relevant `ActionLink` instances.
Rows, columns, tabs, and generic component factories will not be observed because they add noise without identifying a domain region.

A global rule that observes every Wicket component was rejected because it would create high span volume, unstable names, and spans for framework implementation details.

### Measure actual rendering as a contiguous operation

Each observation will start immediately before the selected component's actual Wicket render callback and stop immediately afterward.
Wicket's normal recursive rendering will make descendant region observations children of the active enclosing region observation.

A single observation stretching from component preparation through rendering was rejected because sibling components are prepared and rendered in different orders, causing overlapping durations and incorrect scope nesting.
Separate preparation observations are reserved for a follow-up change if real traces show significant unattributed time.

### Use one reusable serializable behavior and immutable descriptors

A reusable Wicket behavior will own the common observation lifecycle, while each instrumented component will supply an immutable descriptor containing its region kind and safe identifiers.
Active observations, scopes, integration services, and request registrations will be transient and resolved per request so page serialization cannot retain telemetry state.

Explicit attachment at semantic construction points is preferred over class-name inspection by a global component-instantiation listener.
It keeps inclusion rules visible, permits model-aware filtering, and avoids observing extension components accidentally.

### Use stable observation names and bounded attributes

The observation names will be:

- `causeway.wicket.page.render`
- `causeway.wicket.fieldset.render`
- `causeway.wicket.property.render`
- `causeway.wicket.collection.render`
- `causeway.wicket.action.render`

The span display name will remain stable rather than embedding application member identifiers.
Applicable low-cardinality attributes will identify the logical object type and fieldset, property, collection, or action member.
Identifiers will come from layout or metamodel definitions and will describe application structure rather than a particular object instance.

The implementation will reuse the module metadata convention supplied by `CausewayObservationIntegration.withModuleName(...)` with the Wicket viewer namespace.

### Bound member-level instrumentation

Property observations will apply only to regular entity properties and will exclude scalar action parameters and compact/table-cell rendering.
Action observations will apply to action links rendered in entity object-form regions and will exclude service menus and row-level collection actions.
Collection observations will cover the entity collection region as a whole and allow automatic JDBC spans to appear beneath it when loading occurs during rendering.

These rules prevent a collection table from producing a span for every cell and row action.

### Make failure cleanup request-scoped

Starting an observation will register its closure in request-local render state.
Successful completion will close and unregister it.
If normal behavior completion is skipped, the Wicket request-cycle exception or detach path will mark the active observation with the rendering failure when available and close remaining closures in reverse start order.
Cleanup will be idempotent so normal completion followed by request detach is harmless.

Storing cleanup state in global or session scope was rejected because request processing is thread-bound and Wicket sessions can outlive individual traces.

### Preserve opt-in activation

All observations will use `CausewayObservationIntegration`, whose registry is `ObservationRegistry.NOOP` unless the existing `observation` profile is active.
No separate exporter or SDK configuration will be introduced.
The instrumentation will avoid attaching or starting unnecessary active state when the integration reports no-op operation where practical.

## Risks / Trade-offs

- [Render spans do not explain expensive preparation or visibility evaluation] → Name them explicitly as render spans, document the limitation, and reserve preparation observations for a follow-up informed by real traces.
- [A page with many logical members creates many spans] → Exclude table cells, row actions, parameters, and structural layout components; rely on the existing opt-in profile and agent sampling for the initial release.
- [A render exception skips the behavior completion callback] → Track active closures in request-local state and drain them during request exception or detach handling.
- [Wicket page serialization captures observation objects] → Keep all active telemetry and service references transient and retain only immutable safe descriptors.
- [Extension-provided property or collection renderers differ from core components] → Attach at stable enclosing semantic boundaries and test representative alternate presentations without attempting to instrument every extension component.
- [Some expensive work remains directly under the page or root span] → Use the trace evidence to decide whether the preparation or collection-detail follow-up is warranted.

## Migration Plan

No application migration is required.
Applications without the `observation` profile continue to use the no-op registry.
Applications with observation enabled receive the additional Wicket spans after upgrading and can suppress their export through existing agent sampling if necessary.
Rollback consists of reverting this instrumentation; it does not change persisted Wicket page formats beyond serializable behavior metadata or any domain data.

## Open Questions

- Confirm the exact metamodel method used to produce a stable logical object-type identifier consistently across entity, property, collection, and action models during implementation.
- Confirm whether Wicket's request exception callback always exposes the originating render failure before detach; otherwise cleanup will close the span without an error annotation in that fallback path.
- Confirm through an integration trace which collection presentations perform data loading inside the observed render callback rather than during earlier preparation.
