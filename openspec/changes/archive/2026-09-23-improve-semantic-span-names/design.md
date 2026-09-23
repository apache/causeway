## Context

Causeway creates Micrometer observations with stable names such as `causeway.action.invocation` and `causeway.wicket.page.render`.
It currently assigns the same value as the contextual name, so tracing backends display generic operation names even though the observations carry canonical logical identifiers as attributes.

Micrometer Observation distinguishes the stable observation name from the contextual name used by its tracing handler.
The pinned Micrometer Tracing 1.0.12 `DefaultTracingObservationHandler` converts contextual names to lower-hyphen form and truncates them to 50 characters.
That transformation obscures Causeway logical identifiers such as `isisExtSecMan.ApplicationUser#updateEmailAddress`, so Causeway needs a narrowly specialized handler for its dedicated observation registry.

Action invocation instrumentation already resolves the owning action's `FeatureIdentifier` before opening the observation.
Mixed-in properties and collections are evaluated through underlying no-argument `prop` and `coll` actions, so instrumentation at that shared boundary must recover the domain-facing `ObjectAssociation` from the mixee specification.
Wicket entity pages already resolve the logical object type, and action prompt components retain an `ActionModel` that resolves the prompted action's `FeatureIdentifier`.
The current page-render behavior starts only when markup rendering begins, after page initialization and the component tree's configuration and `onBeforeRender` preparation have completed.
These boundaries expose bounded metamodel identifiers, unlike object titles, bookmarks, arguments, or other instance data.

## Goals / Non-Goals

**Goals:**

- Make Causeway semantic spans understandable while scanning a trace tree.
- Preserve logical identifier casing in exported span display names.
- Resolve mixed-in action invocations to their domain-facing logical member identifiers.
- Represent mixed-in property and collection evaluation as association access rather than action invocation.
- Enclose entity-page initialization and pre-render component preparation in a meaningful page-preparation span.
- Preserve stable observation names for conventions and any metric handling.
- Keep full canonical logical identifiers in existing span attributes.
- Add one enclosing render span for a Wicket action prompt without observing individual parameters.
- Give fieldset, property, collection, and action-button render spans meaningful member-level display names.
- Preserve no-op behavior, Java-agent-owned trace context, and existing render lifecycle safety.

**Non-Goals:**

- Rename `causeway.root.interaction` according to request outcome.
- Include object-instance or runtime values in fieldset, property, collection, or action-button render span names.
- Observe prompt preparation, default calculation, choices calculation, validation, authorization, or action invocation as part of prompt rendering.
- Add instance identifiers, values, user data, or localized labels to telemetry.
- Change the OpenTelemetry SDK, exporter, collector, sampling policy, or dependencies.

## Decisions

### Keep observation names stable and specialize contextual names

The stable observation names remain bounded operation categories such as `causeway.action.invocation`, `causeway.property.access`, `causeway.collection.access`, `causeway.wicket.page.prepare`, `causeway.wicket.page.render`, and `causeway.wicket.action.prompt.render`.
Only the Micrometer contextual name will include a domain identifier.

This preserves machine-oriented classification while improving the span name rendered by tracing backends.
Replacing the observation name itself was rejected because it would unnecessarily increase metric and convention cardinality.

### Use logical-identifier display names with deterministic fallback

Contextual names will use these forms:

- `act <logical-type-name>#<action-id>` for action invocation;
- `prop <logical-type-name>#<property-id>` for mixed-in property access;
- `coll <logical-type-name>#<collection-id>` for mixed-in collection access;
- `prepare <logical-type-name>` for entity-page preparation;
- `prompt <logical-type-name>#<action-id>` for action-prompt rendering;
- `render <logical-type-name>` for an entity page;
- `render fieldset <fieldset-id>` for a fieldset, using `default` for the unnamed fieldset;
- `render property <property-id>` for a property;
- `render collection <collection-id>` for a collection;
- `render action <action-id>` for an action button.

Logical type, member, and layout identifiers retain their declared casing and are not converted to lower-hyphen form.
Friendly names and localized labels remain excluded.

The display-name builder first tries the full logical type or logical member identifier.
If a type-based display name exceeds 50 characters, it removes the logical type namespace and tries the simple logical type with the same member id where applicable.
If that fallback still exceeds 50 characters, or if a member-region display name exceeds 50 characters, it truncates the result to exactly 50 characters.

The full logical type and member identity remain available in canonical attributes and are authoritative when a display name omits the namespace or is truncated.

### Resolve action invocations and association accesses to domain-facing identities

`MemberExecutorServiceDefault` will use `IdentifierUtil.logicalMemberIdentifierFor(...)` with the action interaction head and owning action before starting an ordinary action observation.
This maps a mixin action implementation such as `ApplicationUser_updateEmailAddress#act` to the domain-facing identity `isisExtSecMan.ApplicationUser#updateEmailAddress`.

When the invocation facet implements a mixed-in property or collection, `ActionExecutor` will locate the corresponding mixed-in `ObjectAssociation` from the interaction owner's specification by matching `MixedInMember.hasMixinAction(...)`.
The runtime service will then emit `causeway.property.access` or `causeway.collection.access`, use `prop` or `coll` in the contextual name, and attach the association's full logical identifier as `causeway.property.id` or `causeway.collection.id`.
It will not expose the underlying mixin type or implementation method as an action span.

No late mutation of the root interaction or inference from descendants is required.

Renaming the root interaction was rejected because a single request can render an object, open a prompt, invoke an action, and render an Ajax response.
A generic root with meaningful semantic children represents that request more accurately.

### Enclose entity-page preparation separately from rendering

`EntityPage.onConfigure()` is the earliest page lifecycle callback that runs immediately before rendering and calls `Page.onConfigure()`, which initializes an uninitialized page.
The page-preparation observation will start before delegating to `super.onConfigure()`, remain in scope while the component tree is initialized, configured, and prepared through `onBeforeRender()`, and close after that preparation finishes.
The existing page-render behavior will then open `causeway.wicket.page.render` only for actual markup rendering, leaving the preparation and render spans as consecutive semantic siblings.

The preparation observation will use stable name `causeway.wicket.page.prepare`, contextual name `prepare <logical-type-name>`, and canonical `causeway.object.type`.
Exceptions during configuration or pre-render preparation will be recorded before closure, and detach-time cleanup will prevent a leaked scope if rendering does not proceed.

Extending the existing render span backward was rejected because it would conflate model/component preparation with actual markup rendering and obscure the duration of each phase.
A Wicket pre-`onBeforeRender` listener was rejected because Wicket invokes that listener after the page's own `configure()` call and therefore after page initialization.

### Treat an action prompt as one Wicket render region

The Wicket observation descriptor taxonomy will gain an action-prompt region with stable name `causeway.wicket.action.prompt.render`.
It will carry `causeway.object.type` and `causeway.action.id`, and its contextual name will identify the prompted action.

The reusable `WicketRenderObservationBehavior` will be attached to the enclosing `ActionParametersPanel`, whose `ActionModel` identifies the prompted action.
The existing request-local tracker will therefore provide nesting, failure cleanup, Ajax parentage, serialization safety, and no-op handling without a second lifecycle mechanism.

The observation covers the panel's actual render callback and descendant rendering only.
Individual action parameter fields remain excluded from property observations, preventing span amplification.
Instrumenting modal, sidebar, and inline prompt shells separately was rejected because presentation style should not create different semantic boundaries or duplicate prompt spans.

### Preserve contextual names in the Causeway tracing handler

Causeway will replace `DefaultTracingObservationHandler` in its dedicated observation registry with a small subclass or equivalent handler that retains the default lifecycle, parentage, tagging, error, event, and scope behavior but returns the contextual name without Micrometer's lower-hyphen conversion.
The naming helper will enforce the 50-character limit before the handler exports the span.
The Java agent remains responsible for automatic HTTP and JDBC spans, SDK ownership, sampling, and export.

Changing Micrometer globally or renaming Java-agent-owned spans was rejected because only Causeway semantic observations need logical-identifier display names.

### Preserve canonical attributes and privacy rules

Dynamic contextual names will use only static metamodel structure.
They MUST NOT use object titles, primary keys, bookmarks, property values, action arguments, user names, tenancy tokens, generated Wicket component paths, or localized friendly names.

Canonical attributes remain unchanged for existing action and render spans.
Mixed-in association implementation calls move from `causeway.action.id` to the semantically appropriate complete `causeway.property.id` or `causeway.collection.id` attribute.
The prompt span follows the same `causeway.object.type` and `causeway.action.id` conventions as existing Wicket action render observations.
The page-preparation span follows the page-render span's `causeway.object.type` convention.

## Risks / Trade-offs

- [Namespace fallback can make type names collide] → Keep the full logical type and action identity as authoritative attributes and document that display names are navigational aids.
- [A logical identifier still exceeds 50 characters after namespace fallback] → Truncate deterministically at 50 characters and retain the complete canonical attribute.
- [Page preparation can fail before actual rendering] → Record the error and close the preparation observation from configuration, pre-render, and detach cleanup paths.
- [A mixed-in association cannot be resolved from its implementation action] → Fall back to ordinary action instrumentation rather than dropping telemetry.
- [Prompt work can occur before actual rendering] → Name the span explicitly as prompt rendering and document that prompt defaults, choices, validation, and authorization may remain outside it.
- [A prompt component can be rendered more than once in an Ajax request] → Follow the existing component-render semantics and create one observation per actual enclosing prompt render callback.
- [Changing display names can affect saved backend queries based on operation name] → Preserve stable attributes and document the display-name change so queries can use canonical attributes.

## Migration Plan

Applications receive the new contextual display names, association-access spans, page-preparation span, and prompt span only when the existing `observation` profile is active.
No application configuration or persisted domain-data migration is required.
Operators should update searches that match the old generic span display names to use canonical attributes or stable observation metadata where their backend exposes it.
Rollback consists of reverting the instrumentation change; SDK ownership, sampling, and export configuration remain unchanged.

## Open Questions

No naming-policy questions remain.
Implementation validation will retain coverage for modal, sidebar, and inline prompt use of the common `ActionParametersPanel` boundary.
