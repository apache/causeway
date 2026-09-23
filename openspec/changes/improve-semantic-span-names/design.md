## Context

Causeway creates Micrometer observations with stable names such as `causeway.action.invocation` and `causeway.wicket.page.render`.
It currently assigns the same value as the contextual name, so tracing backends display generic operation names even though the observations carry canonical logical identifiers as attributes.

Micrometer Observation distinguishes the stable observation name from the contextual name used by its tracing handler.
The pinned Micrometer Tracing 1.0.12 `DefaultTracingObservationHandler` converts contextual names to lower-hyphen form and truncates them to 50 characters.
That transformation obscures Causeway logical identifiers such as `isisExtSecMan.ApplicationUser#updateEmailAddress`, so Causeway needs a narrowly specialized handler for its dedicated observation registry.

Action invocation instrumentation already resolves the owning action's `FeatureIdentifier` before opening the observation.
Wicket entity pages already resolve the logical object type, and action prompt components retain an `ActionModel` that resolves the prompted action's `FeatureIdentifier`.
These are bounded metamodel identifiers, unlike object titles, bookmarks, arguments, or other instance data.

## Goals / Non-Goals

**Goals:**

- Make Causeway semantic spans understandable while scanning a trace tree.
- Preserve logical identifier casing in exported span display names.
- Resolve mixed-in action invocations to their domain-facing logical member identifiers.
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

The stable observation names remain `causeway.action.invocation`, `causeway.wicket.page.render`, and `causeway.wicket.action.prompt.render`.
Only the Micrometer contextual name will include a compact domain identifier.

This preserves machine-oriented classification while improving the span name rendered by tracing backends.
Replacing the observation name itself was rejected because it would unnecessarily increase metric and convention cardinality.

### Use logical-identifier display names with deterministic fallback

Contextual names will use these forms:

- `invoke <logical-type-name>#<action-id>` for action invocation;
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

### Resolve action invocations to their domain-facing identity

`MemberExecutorServiceDefault` will use `IdentifierUtil.logicalMemberIdentifierFor(...)` with the action interaction head and owning action before starting the observation.
This maps a mixin implementation such as `ApplicationUser_updateEmailAddress#act` to the domain-facing identity `isisExtSecMan.ApplicationUser#updateEmailAddress` and uses that same canonical value for `causeway.action.id`.
No late mutation of the root interaction or inference from descendants is required.

Renaming the root interaction was rejected because a single request can render an object, open a prompt, invoke an action, and render an Ajax response.
A generic root with meaningful semantic children represents that request more accurately.

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

Canonical attributes remain unchanged for existing spans.
The new prompt span follows the same `causeway.object.type` and `causeway.action.id` conventions as existing Wicket action render observations.

## Risks / Trade-offs

- [Namespace fallback can make type names collide] → Keep the full logical type and action identity as authoritative attributes and document that display names are navigational aids.
- [A logical identifier still exceeds 50 characters after namespace fallback] → Truncate deterministically at 50 characters and retain the complete canonical attribute.
- [Prompt work can occur before actual rendering] → Name the span explicitly as prompt rendering and document that preparation, choices, defaults, validation, and authorization may remain outside it.
- [A prompt component can be rendered more than once in an Ajax request] → Follow the existing component-render semantics and create one observation per actual enclosing prompt render callback.
- [Changing display names can affect saved backend queries based on operation name] → Preserve stable attributes and document the display-name change so queries can use canonical attributes.

## Migration Plan

Applications receive the new contextual display names and prompt span only when the existing `observation` profile is active.
No application configuration or persisted domain-data migration is required.
Operators should update searches that match the old generic span display names to use canonical attributes or stable observation metadata where their backend exposes it.
Rollback consists of reverting the instrumentation change; SDK ownership, sampling, and export configuration remain unchanged.

## Open Questions

No naming-policy questions remain.
Implementation validation will retain coverage for modal, sidebar, and inline prompt use of the common `ActionParametersPanel` boundary.
