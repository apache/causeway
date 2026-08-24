# Action dispatch implementation report

## Implemented boundary

Object and service action contexts now share an immutable introspection-driven invocation plan.
The plan records target kind, placement, mutability, selected field, mutation field, object target argument, declared arguments, result selection, extraction path, and effective outcome type.

Nested `invoke` and `invokeIdempotent` fields remain query placements.
An advertised generated top-level mutation is preferred over `invokeNonIdempotent`.
The legacy nested mutating field remains available only when no corresponding mutation exists.
Object targets are supplied only through a declared compatible target argument, and service actions never receive an object target.

Direct scalar, enum, object, collection, and void results no longer require a fabricated envelope.
An advertised `results` envelope remains supported and is extracted explicitly.
Object metadata projection is derived from the effective metadata type, so versionless view models omit `_meta.version` rather than failing operation rendering.
General union-fragment metadata projection remains deferred.

Expected planning and execution failures return bounded action-scoped results.
Protected action failures redact errors and operation variables while retaining the executable document shape for safe diagnostics.
Cancellation and stale controller generations retain their existing behavior, and only successful mutating plans trigger authoritative refresh or service change notification.

## Closed Reference Application failures

`demo.ActionChoicesMenu.choices` now executes its parameterless flat mutation and navigates to the returned `demo.ActionChoices` object without opening an error prompt.
`demo.ActionChoices.selectTvCharacter` now prepares its reference choices, submits the selected typed identity through the generated mutation, closes the prompt, and keeps the authoritative logical route.
`demo.ActionSemanticsVm.reportPropertyForSafe` now executes without a version-selection failure and completes through the host result policy.
`demo.ActionSemanticsVm.updatePropertyForIdempotent` retains invalid-value validation and now accepts a valid value without a version-selection failure.

The remaining known gaps concern general polymorphic union projections, opaque composite routes, and identity/preparation cases not resolved by selecting only advertised result metadata.
They remain outside this Priority 0 change.

## Automated coverage

The foundation operation matrix covers nested safe and idempotent fields, direct object and service mutations, mutation preference, legacy fallback, declared arguments, object targets, missing targets, unsupported plans, direct results, envelopes, versionless metadata, and protected-value redaction.
Real GraphQL integration covers the parameterless service mutation and parameterized object mutation against the pinned Reference Application.
The Reference Application browser suite converts prior visible-failure assertions into successful semantic journeys while retaining invalid, cancellation, route-disposal, accessibility, responsive, CSP, and external-isolation coverage.
Petclinic continues to cover complete-identity object actions and established public selectors and events.
