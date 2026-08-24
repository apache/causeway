## Context

Rich GraphQL represents values declared by abstract domain types as GraphQL unions with advertised `possibleTypes`.
GraphQL permits `__typename` directly on a union, but concrete metadata and members must be selected inside `... on ConcreteType` fragments.
The foundation selection tree and executable renderer currently represent fields and arguments only, so `resultSelectionForType(...)` falls back to `__typename` and abstract collections are rejected locally.

The pinned Reference Application exposes raw-list collection fixtures through `rich__demo_ValueHolder__gqlv_union`.
The runtime rows are concrete `demo.CollectionTypeOfChildVm` objects with valid metadata, but the viewer cannot select that metadata without fragments.
A broad union can advertise many possible domain types, so blindly expanding every possible type would create large operations and excessive introspection.
Mutating actions must never be executed once to discover a result type and again to obtain its value.

## Goals / Non-Goals

**Goals:**

- Render schema-validated inline fragments through the internal selection model.
- Describe advertised concrete possible types and their metadata without application-specific type lists.
- Project bounded small unions directly.
- Probe broad abstract collection rows with `__typename` and replay the same side-effect-free window or list read once using fragments for observed concrete types.
- Merge concrete row metadata and supported requested columns and preserve hydration coverage.
- Retain cancellation, stale-response, lazy activation, windowing, partial errors, and bounded operation policy.
- Prove the Reference Application polymorphic collection without changing public GraphQL or component contracts.

**Non-Goals:**

- Repeating mutating actions or property mutations to discover result types.
- Adding GraphQL schema fields, changing union membership, or changing Causeway metamodel type inference.
- Providing sorting, filtering, paging beyond existing windows, or unbounded fragment expansion.
- Correcting opaque route encoding or long composite bookmark handling.
- Changing action dispatch, value codecs, Vaadin adapters, dependencies, CSP, or public events.

## Decisions

### Add a reserved inline-fragment selection node

Internal selections will represent fragments under a reserved `__fragments` map keyed by advertised concrete GraphQL type name.
Each fragment contains an ordinary nested selection and the renderer always includes `__typename` alongside fragments.
The renderer validates that the parent is an interface or union, the fragment name is advertised by `possibleTypes`, the concrete type is described, and every nested field is valid before emitting `... on Type { ... }`.

The rejected alternative is accepting raw GraphQL fragment strings, because that would bypass name validation, type validation, deterministic rendering, merge logic, and operation diagnostics.

### Bound direct expansion

`resultSelectionForType(...)` may directly expand an abstract type only when the advertised possible-type count is within a documented internal limit and the required concrete descriptions are available.
The fragment for each concrete object uses the existing advertised metadata-selection policy, with `__typename` retained when metadata is unavailable.
A larger or incomplete union remains typename-only unless a side-effect-free collection planner can narrow it from observed rows.

The rejected alternative is expanding every possible type, because broad domain unions can make request and introspection cost proportional to the complete metamodel.

### Probe and replay broad collection reads once

An activated abstract collection first performs the same bounded list or window operation with `__typename` only.
The context collects the distinct observed type names, rejects names not advertised by the union, enforces a maximum observed-type count, describes those concrete types and required metadata or column wrappers, and then reissues the same side-effect-free collection operation once with concrete fragments.
Only the second result becomes the published row state.
Existing request generations, abort signals, window arguments, ordering, and stale-response checks cover both stages.

If the replay contains a row type not observed by the probe, that row remains typename-only and is reported as a bounded partial projection rather than triggering an unbounded retry loop.
The rejected alternative is recursive probing until all races disappear, because changing collections could produce unbounded requests.

### Build concrete row fragments from advertised fields

Each observed concrete row fragment selects its advertised metadata subset and only requested semantic column wrappers present on that concrete type.
Wrapper and nested output types are described through the existing one-type introspection cache before operation rendering.
Rows with `id` and `logicalTypeName` use existing object-link and hydrated-context behavior.
Columns absent from one concrete type become local missing cells rather than invalidating other types.

### Keep non-repeatable outcomes bounded

Small action or property union outputs can use direct advertised fragments when their type closure is already bounded and described.
A broad mutating action result remains typename-only and successful rather than being invoked twice.
The semantic result does not invent identity when the returned projection lacks metadata.

### Preserve opaque-route separation

Concrete union rows may expose long opaque identifiers.
This change publishes the existing semantic navigation event with the exact returned identifier but does not alter route encoding, decoding, limits, or the retained `invalid-route` assertion.

## Risks / Trade-offs

- [Probe and replay observe different collection contents] → Reuse identical window arguments, limit execution to one replay, and mark unprojected replay types locally.
- [A broad union creates excessive introspection] → Introspect only distinct observed types and enforce fixed type and fragment limits.
- [Fragment selections break selection merging or hydration coverage] → Extend merge, difference, and coverage helpers with focused nested-fragment tests.
- [One concrete type lacks a requested column] → Omit that column from its fragment and preserve rows from other concrete types.
- [A malicious or stale typename is returned] → Require exact membership in the introspected possible-type set before constructing a fragment.
- [Action semantics are accidentally repeated] → Restrict probe/replay to side-effect-free object and collection reads and test that mutation execution remains single-shot.

## Migration Plan

The change is an internal operation-planning correction with no persisted-data or server-schema migration.
Deploy through the existing same-origin foundation artifact and qualify ordinary concrete collections, small unions, broad polymorphic collections, and mutating action outcomes.
Rollback is a source revert with no route, configuration, dependency, or asset migration.
