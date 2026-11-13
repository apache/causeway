## Context

The current `window(offset, size)` implementation validates the request before reading the member, invokes the Causeway collection association, materializes its complete iterable value, applies the configured element comparator, computes the exact total, and slices the requested rows.
This design is semantically correct and bounds GraphQL output, but it cannot prevent an ORM from loading all matching rows.

A Causeway collection accessor is a domain semantic operation rather than a portable database query plan.
It may return a persistent lazy collection, a computed list, a filtered view, a sorted result, or data obtained from another system.
Visibility and ordering can depend on the current interaction, while an exact count can require different backend work from retrieving one window.

## Goals / Non-Goals

**Goals:**

- Establish reproducible persistence and materialization evidence for the current path.
- Identify a safe capability boundary for bounded backend retrieval.
- Preserve the archived collection-windowing semantics exactly where optimization is used.
- Retain deterministic fallback whenever pushdown capability or semantic equivalence is unavailable.
- Separate row retrieval, ordering, count, authorization, transaction, and consistency concerns.
- Produce an evidence-backed implementation roadmap with explicit framework and application impact.

**Non-Goals:**

- Changing production collection fetching during the analysis.
- Parsing arbitrary GraphQL sort expressions into persistence queries.
- Treating `@CollectionLayout(paged=...)` as a transport or persistence directive.
- Assuming every collection is JPA-backed or queryable.
- Replacing the established unargumented `get` field.
- Claiming snapshot or cursor stability across requests.

## Decisions

### Analyse before defining an SPI

The analysis does not begin by adding `CollectionWindowProvider` or another public abstraction.
It first traces how persistent and computed associations are represented and where authorization, ordering, transaction, and identity information remain available.
Any recommended SPI must demonstrate that an application can implement it without duplicating hidden framework rules or bypassing domain semantics.

### Treat semantic equivalence as the optimization gate

A backend-aware path is valid only when it returns the same authorized elements in the same configured order as the materializing path for the same execution-time state.
A strategy that cannot prove supported ordering, owner identity, member visibility, element identity, or transaction safety must fall back.
Fallback is normal behavior rather than an error.

### Evaluate multiple retrieval boundaries

The analysis compares at least these candidates:

1. transparent optimization of persistence-managed associations where the framework retains a query representation;
2. an opt-in application or repository provider addressed by logical owner type and semantic collection ID;
3. a domain programming-model contract for explicitly windowable collections;
4. action or repository results that already expose bounded query semantics;
5. retention of full materialization for computed and unsupported collections.

The comparison covers API coupling, portability, discoverability, authorization, testing, migration, and the risk of running domain collection logic twice.

### Separate rows from totals

A source may retrieve bounded rows efficiently while an exact count remains expensive or unavailable.
The existing nullable `totalCount` contract is retained, and candidate designs must not fetch every row merely to manufacture a count.
Continuation can rely on bounded look-ahead where the selected strategy can do so safely.

### Measure backend work explicitly

Fixtures record generated SQL or equivalent backend calls, rows transferred, domain objects materialized, count queries, wall-clock latency, allocation or memory evidence, and fallback reason.
Response row count alone is not accepted as evidence of retrieval pushdown.
Measurements include warm and cold behavior and state enough dataset size and environment detail to be reproducible.

### Keep persistence integrations optional

A recommended JPA or JDO adapter belongs with the relevant persistence integration or behind an optional capability boundary.
The GraphQL model must continue to operate for in-memory, computed, and non-relational collections without acquiring a mandatory ORM dependency.

## Risks / Trade-offs

- [A collection getter has side effects or computed semantics] → Do not bypass it unless the programming model explicitly authorizes an equivalent bounded provider.
- [Database ordering differs from Java comparator ordering] → Require a proven ordering mapping or use the materializing fallback.
- [Counting dominates request cost] → Preserve nullable totals and evaluate bounded look-ahead separately.
- [Authorization is applied after retrieval] → Keep interaction visibility checks and prove whether row-level filtering affects offset semantics.
- [Persistence sessions are lazy and transaction-scoped] → Run prototypes inside the established Causeway interaction and record session behavior.
- [An SPI duplicates repositories] → Compare application ergonomics and reject an abstraction that merely relocates query code without semantic benefit.

## Migration Plan

The analysis is additive and changes no runtime behavior.
Any later implementation keeps materialization as the compatibility fallback and introduces optimization behind explicit capability discovery or configuration.
The public GraphQL `window` shape remains unchanged unless separate evidence justifies another proposal.

## Open Questions

- Whether any current metamodel or persistence adapter retains a safely reusable query representation for collection associations.
- Whether row-level authorization can alter offsets after a database-level slice.
- Whether a portable provider should return domain objects, bookmarks, managed identities, or a backend-neutral result envelope.
- How configured Causeway comparators can be mapped to backend ordering without semantic drift.
- Whether exact totals should use a separate count capability, bounded look-ahead, or remain absent by default for optimized sources.
