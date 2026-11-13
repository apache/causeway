## Context

Roadmap step 5 extracts Wicket rendering from CAUSEWAY-4059 and the relevant semantic naming changes from CAUSEWAY-4062. The preceding core member naming change is archived. Planning baseline is local `a073787d229`; no remote freshness or runtime validation is claimed during proposal creation.

Main no longer has maintenance's EntityPage, EntityCollectionPanel or ScalarPanelAbstract structure. The relevant entry points include DomainObjectPage (which builds its tree in onInitialize), PropertyGroup, ParentedCollectionPanel, ActionLink and ActionParametersPanel. Property scalar hooks and model identifiers must be mapped to main's actual implementation. WebRequestCycleForCauseway closes interaction layers at request end and already handles Wicket exceptions. Existing request-cycle observation closure order must be preserved.

## Goals / Non-Goals

**Goals:** semantic page preparation/render regions; fieldset, property, collection and action-control rendering; prompt rendering; full/Ajax ancestry; static identity; serialization and failure safety; evidence in Boot and agent exports; integrated operator guidance.

**Non-Goals:** table phases, rows/cells/row actions, detail modes, budgets, aggregates, duration filtering, HTTP/root renaming or nominations, other viewers, audit/evaluation work, priming and new application APIs.

## Decisions

### Map semantic regions to main's components

Use serializable descriptors and a shared render behavior rather than separate ad hoc observations in every component. Descriptors carry region kind and static strings only. Start region scopes around actual rendering callbacks; do not force lazy model evaluation simply to produce metadata. Regular domain properties and object-form action controls are included; action parameter widgets, associated-parameter action links and table cells are excluded from this member-level port. Hidden/non-rendered regions emit no rendering span. Parented collections contribute one collection region; their table internals remain step 6.

Keep operation categories from maintenance: causeway.wicket.page.prepare, causeway.wicket.page.render, causeway.wicket.fieldset.render, causeway.wicket.property.render, causeway.wicket.collection.render, causeway.wicket.action.render and causeway.wicket.action.prompt.render. Rendering controls and executing domain members are separate work and retain separate categories.

### Keep display and canonical identity separate

Reuse CausewayObservationNaming's 50-character, case-preserving compaction policy. Page names use prepare/render plus logical type; fieldsets and member regions use render plus their region/member identity; prompts use prompt plus the contributed domain action. Extend the helper only for the region formatting actually required. Include full causeway.object.type and, where applicable, causeway.fieldset.id, causeway.property.id, causeway.collection.id or causeway.action.id. Action attributes retain declared parameter signatures and use the domain-facing contribution, preserving physical execution identity. A default fieldset uses a stable static marker. Never use object title, bookmark, argument value, page instance ID or localized label in these descriptors.

### Model preparation as a real lifecycle phase

Adapt to DomainObjectPage rather than copying EntityPage.onConfigure. Include initial page construction and subsequent page/descendant configuration and before-render preparation where main performs it. Preparation must finish before page markup rendering begins; it must not remain open across requests. Establish the precise hook order with lifecycle tests before choosing the boundary. A callback may already have its own child observation, but instrument each preparation phase once and avoid double observations when initialization precedes configuration.

### Own active scopes in the request

Resolve CausewayObservationIntegration at use time and retain only transient active state on page behaviors/helpers. RequestCycle metadata tracks open closures; normal callbacks unregister them, exceptions and request completion unwind outstanding scopes in reverse opening order before the interaction/request parents close. Cleanup is idempotent and continues if one close fails, preserving the original work failure. Preparation scopes receive the same fallback cleanup guarantees as rendering scopes. Detach is an additional safety net, not the primary owner of a scope that spans request callbacks. Serialized/deserialized pages retain descriptors but no registry, tracer, active observation, scope or request tracker.

The maintenance tracker and preparation helper are useful provenance, but their error-unwind loops and differing ownership must be reviewed rather than copied as the lifecycle contract.

### Preserve tracing ownership and document the new capability

Use existing Boot/agent observation integration without installing handlers, SDKs or exporters. Extend the compatibility harness with real Wicket lifecycle/component rendering where practical, supplemented by focused callback/serialization tests. Synthetic non-Wicket spans alone are insufficient evidence for hook ordering. Reuse the existing Petclinic launcher, Jaeger and metrics scripts. Merge examples and verification steps into the existing M3 how-to and observability guide without before/after comparisons or another configuration scenario. Keep HTMX guidance accurate: the added regions are Wicket-specific.

## Risks / Trade-offs

- [Preparation hook differs on main] → characterize first render, reused pages and Ajax callback order, then instrument actual work.
- [Failed child rendering skips completion] → request-local LIFO cleanup including preparation; failure followed by success tests on a reused worker.
- [Telemetry leaks into page serialization] → static descriptors plus transient active fields, with serialization while active and after completion tests.
- [Mixin/fieldset identity is misleading or private] → use canonical metamodel identities and stable fieldset identifiers; test contributed actions, signatures and privacy.
- [Additional spans increase volume] → retain member-level inclusion exclusions; leave table detail and configurable budgets to step 6.
- [Cleanup replaces the work exception] → explicit multi-failure tests and preserve original error semantics while closing remaining scopes.

## Migration Plan

No new application dependency or flag is required beyond the existing observation setup. Existing core member, HTTP and interaction spans remain. Deploy with ordinary Wicket UI upgrades; rollback removes only the added regions, with no persisted data migration.

## Open Questions

No user decision is required. Exact main lifecycle hooks and representative sample prompt/collection operations are implementation discovery tasks, to be recorded with validation evidence.
