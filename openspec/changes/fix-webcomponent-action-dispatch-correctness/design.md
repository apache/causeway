## Context

The public rich GraphQL schema exposes action invocation in more than one executable shape.
Safe and idempotent actions are reached through nested object or service action-wrapper fields, while mutating actions normally use generated flat fields on the mutation root.
Some supported API variants also expose a legacy nested `invokeNonIdempotent` field.

The object and service contexts currently discover and execute these shapes independently.
The object context treats any nested invocation field, including `invokeNonIdempotent`, as sufficient and therefore can skip an available top-level mutation.
Both contexts assume that nested invocation returns an envelope with `results`, and otherwise request a `target` field even when the effective return type does not advertise one.
Those assumptions can make operation rendering throw before execution, after which the standard controller can report only `Action invocation failed`.

The pinned Reference Application preserves an executable parameterless service action that demonstrates the nested safe-dispatch defect.
Its object actions also provide parameter, validation, mutation, outcome, cancellation, and stale-response fixtures for closing the operation-shape matrix.
Public GraphQL remains authoritative, and this change must preserve Causeway elements, semantic events, typed value codecs, route policy, mutation serialization, strict CSP, and native or Vaadin editor independence.

## Goals / Non-Goals

**Goals:**

- Use one introspection-driven dispatch plan for object and service actions.
- Prefer the advertised top-level mutation for mutating actions and use a legacy nested mutating field only when no mutation exists.
- Execute nested safe or idempotent fields without assuming a non-advertised result envelope or target field.
- Map only declared arguments, add object target identity only through the declared target argument, and preserve exact codec values.
- Normalize envelope and direct scalar, enum, object, collection, and void outcomes through the existing semantic result contract.
- Return bounded capability, planning, GraphQL, transport, and invocation errors without exposing values or implementation exceptions.
- Convert retained Reference Application known-gap assertions into successful action journeys and update the reviewed inventory only from focused evidence.

**Non-Goals:**

- Defining versionless identity or concurrency-token semantics.
- Correcting general polymorphic union metadata projections or collection-row fragments.
- Changing opaque bookmark route encoding or length handling.
- Adding or renaming GraphQL operations, fields, arguments, or result types.
- Adding paged autocomplete, Grid operations, Vaadin adapters, or a Vaadin-default policy.
- Reviving invalid menu references from the copied upstream corpus.

## Decisions

### Build a shared immutable invocation plan

A shared planner will consume the targeted object or service description, action descriptor, optional mutation root, generated field name, and target kind.
It will return an immutable plan describing placement, mutability, selected field, declared arguments, target argument when applicable, result shape, extraction path, and outcome type.
Both `ObjectContextController` and `ServiceActionContextController` will execute this plan through the existing GraphQL client methods.

This is preferred to patching the two contexts separately because their current drift created different behavior for the same public action semantics.
It also makes operation-shape fixtures reusable without moving GraphQL document construction into components.

### Select placement by action semantics and advertised fields

The planner will consider nested `invoke` and `invokeIdempotent` fields safe or idempotent query candidates.
It will inspect the mutation root independently for the generated flat mutation field.
An advertised top-level mutation wins for a mutating action even when `invokeNonIdempotent` also exists.
The legacy nested `invokeNonIdempotent` field remains a compatibility fallback only when no top-level mutation is advertised.
If no executable candidate exists, preparation and invocation return `UNSUPPORTED` without sending an invalid operation.

This preserves compatibility with older API variants without using a mutating query when the compliant mutation is available.
The alternative of removing the legacy path immediately would turn an established compatibility case into a breaking change.

### Derive arguments and object target from the selected field

Pending parameter values continue through the toolkit-neutral codecs and `normalizeInteractionInput`.
Only arguments declared by the selected invocation field are emitted.
For an object mutation, the planner identifies the target argument by its generated input type or advertised target name and supplies the current bookmark identity there.
Service mutations never manufacture an object target.
A missing required target or parameter mapping is a bounded planning failure before execution.

This avoids maintaining a second parameter schema and prevents speculative `_target` or raw-string arguments.

### Model nested result shape explicitly

The planner will inspect the selected field's effective output type.
When that type advertises a `results` field, the operation selects and extracts that field.
When the selected field returns a scalar or enum directly, it requests no child selection and extracts the field value directly.
When it returns a described object directly, it uses the existing safe result-selection rules and requests only advertised fields.
When an advertised envelope provides another established result or target field, it is used only when introspection confirms the field and its type.
Void output produces the existing void semantic result.

The planner will never synthesize `{ target }` merely because `results` is absent.
General union-fragment projection remains a later focused change; this change may select `__typename` where that is the only currently valid direct projection, but it will not claim navigable object identity unless metadata was actually returned.

### Keep execution and presentation responsibilities separate

The GraphQL contexts own planning, execution, extraction, error normalization, mutation serialization, and authoritative refresh notification.
The standard interaction controller continues to own prompt state, focus, cancellation, semantic events, and result presentation.
A context planning or execution failure returns an interaction result with a stable safe code and message, allowing the controller to avoid collapsing expected failures into its generic exception fallback.
Unexpected exceptions remain caught at the controller boundary and are not exposed verbatim.

### Qualify through a closed operation-shape matrix

Foundation fixtures will cover object and service targets across nested safe, nested idempotent, top-level mutating, legacy nested mutating fallback, missing capability, parameterless, parameterized, envelope result, direct result, scalar, object, collection, and void cases.
Integration tests will execute representative public GraphQL operations before browser tests prove the semantic controller path.
The Reference Application service action currently asserted as `Action invocation failed` will instead assert successful typed outcome and host navigation or presentation as applicable.
Object-action fixtures will prove valid dispatch while versionless failures remain explicitly classified for the next change.

## Risks / Trade-offs

- **[Risk] Existing API variants expose an unexpected envelope field.** → Record the discovered type and fail boundedly rather than guessing, then add a reviewed planner rule and fixture if it is a public supported shape.
- **[Risk] Correct dispatch exposes the separate union-projection defect.** → Assert invocation success separately from navigable metadata and retain general union projection as the next focused contract change.
- **[Risk] Mutation classification differs from field naming.** → Prefer actual mutation-root presence and descriptor fields over name heuristics, and test safe, idempotent, and mutating fixtures independently.
- **[Risk] Shared planning changes previously working Petclinic behavior.** → Preserve current operation names and semantic results and run the full foundation, Petclinic, and Reference Application browser suites in both parameterless and parameterized paths.
- **[Risk] Error details disclose submitted or protected values.** → Emit stable bounded codes and messages, retain codec redaction, and add negative assertions against operation variables, errors, events, and rendered markup.

## Migration Plan

1. Capture the effective nested safe and flat mutation shapes from targeted introspection in deterministic fixtures and evidence.
2. Introduce the shared planner and unit-test it without changing public context methods.
3. Route object and service invocation through the planner while retaining the legacy nested mutation fallback.
4. Replace known-gap action assertions with successful integration and browser journeys.
5. Run production module, Reference Application, CSP, accessibility, and isolation gates.

The change is behavior-correcting and requires no application migration.
Rollback consists of reverting the shared planner integration; no schema, persisted data, route, dependency, or packaged asset migration is involved.

## Open Questions

No architectural question blocks implementation.
Implementation evidence must identify the exact direct or enveloped result shape used by each Reference Application target before its known-gap assertion is converted.
