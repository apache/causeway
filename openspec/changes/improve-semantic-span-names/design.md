## Context

Causeway creates Micrometer observations with stable names such as `causeway.action.invocation` and `causeway.wicket.page.render`.
It currently assigns the same value as the contextual name, so tracing backends display generic operation names even though the observations carry canonical logical identifiers as attributes.

Micrometer Observation distinguishes the stable observation name from the contextual name used by its tracing handler.
In the pinned Micrometer Tracing 1.0.12 implementation, the tracing handler reads the contextual name when the observation stops, converts uppercase characters to lower-hyphen form, and truncates the result to 50 characters.

Action invocation instrumentation already resolves the owning action's `FeatureIdentifier` before opening the observation.
Wicket entity pages already resolve the logical object type, and action prompt components retain an `ActionModel` that resolves the prompted action's `FeatureIdentifier`.
These are bounded metamodel identifiers, unlike object titles, bookmarks, arguments, or other instance data.

## Goals / Non-Goals

**Goals:**

- Make coarse Causeway semantic spans understandable while scanning a trace tree.
- Preserve stable observation names for conventions and any metric handling.
- Keep full canonical logical identifiers in existing span attributes.
- Add one enclosing render span for a Wicket action prompt without observing individual parameters.
- Preserve no-op behavior, Java-agent-owned trace context, and existing render lifecycle safety.

**Non-Goals:**

- Rename `causeway.root.interaction` according to request outcome.
- Dynamically name fieldset, property, collection, or action-button render spans.
- Observe prompt preparation, default calculation, choices calculation, validation, authorization, or action invocation as part of prompt rendering.
- Add instance identifiers, values, user data, or localized labels to telemetry.
- Change the OpenTelemetry SDK, exporter, collector, sampling policy, or dependencies.

## Decisions

### Keep observation names stable and specialize contextual names

The stable observation names remain `causeway.action.invocation`, `causeway.wicket.page.render`, and `causeway.wicket.action.prompt.render`.
Only the Micrometer contextual name will include a compact domain identifier.

This preserves machine-oriented classification while improving the span name rendered by tracing backends.
Replacing the observation name itself was rejected because it would unnecessarily increase metric and convention cardinality.

### Use compact operation-first display names

Contextual names will use these conceptual forms:

- `invoke <action-member> on <simple-logical-type>` for action invocation;
- `render <simple-logical-type>` for an entity page;
- `prompt <action-member> on <simple-logical-type>` for action-prompt rendering.

The implementation will derive the simple type from the final segment of the logical type name and will use the action's logical member name rather than a friendly or localized label.
Display tokens will be normalized to lower-hyphen form before assignment so that the resulting name is predictable under Micrometer's normalization.
The operation and action member appear before the type so that the most diagnostic information survives Micrometer's 50-character limit.

The full logical type and action identity remain available in `causeway.object.type` and `causeway.action.id` and are authoritative when a compact display name is ambiguous or truncated.
Using full logical identities directly in contextual names was rejected because namespace prefixes can consume the complete display-name limit before the member name appears.

### Name the action invocation where its identity is already known

`MemberExecutorServiceDefault` will assign the contextual name before starting the existing action observation.
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

### Preserve canonical attributes and privacy rules

Dynamic contextual names will use only static metamodel structure.
They MUST NOT use object titles, primary keys, bookmarks, property values, action arguments, user names, tenancy tokens, generated Wicket component paths, or localized friendly names.

Canonical attributes remain unchanged for existing spans.
The new prompt span follows the same `causeway.object.type` and `causeway.action.id` conventions as existing Wicket action render observations.

## Risks / Trade-offs

- [Compact type names can collide across namespaces] → Keep the full logical type and action identity as authoritative attributes and document that display names are navigational aids.
- [Micrometer truncates contextual names to 50 characters] → Put the operation and action member first, keep names compact, and test long identifiers without relying on the display name for exact filtering.
- [Prompt work can occur before actual rendering] → Name the span explicitly as prompt rendering and document that preparation, choices, defaults, validation, and authorization may remain outside it.
- [A prompt component can be rendered more than once in an Ajax request] → Follow the existing component-render semantics and create one observation per actual enclosing prompt render callback.
- [Changing display names can affect saved backend queries based on operation name] → Preserve stable attributes and document the display-name change so queries can use canonical attributes.

## Migration Plan

Applications receive the new contextual display names and prompt span only when the existing `observation` profile is active.
No application configuration or persisted domain-data migration is required.
Operators should update searches that match the old generic span display names to use canonical attributes or stable observation metadata where their backend exposes it.
Rollback consists of reverting the instrumentation change; SDK ownership, sampling, and export configuration remain unchanged.

## Open Questions

- Confirm during implementation whether the final logical-type segment should recognize separators other than `.` for all supported logical naming conventions.
- Confirm with a representative modal, sidebar, and inline prompt that `ActionParametersPanel` is the single common render boundary and does not produce duplicate observations.
