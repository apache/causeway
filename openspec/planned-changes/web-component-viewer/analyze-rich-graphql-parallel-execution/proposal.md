## Why

The GraphQL viewer already installs a custom GraphQL Java `AsyncExecutionStrategy` for queries and mutations, but its generated data fetchers normally return synchronous values inside `InteractionService` calls.
GraphQL Java can compose `CompletionStage` results concurrently, yet the execution strategy alone does not establish that blocking domain or persistence work runs on different threads.
Naively offloading arbitrary fields could share non-thread-safe persistence sessions, lose Causeway interaction or security context, reorder mutations, exhaust a common pool, or increase database pressure without improving latency.
A measured analysis should determine the current execution timeline and identify whether a bounded safe concurrency boundary can reduce latency for independent query subtrees.

## What Changes

- Instrument the current GraphQL request, execution-strategy, data-fetcher, Causeway interaction, transaction, persistence, and result-assembly timeline.
- Verify actual behavior for synchronous fetchers, `CompletionStage` fetchers, nested fields, lists, DataLoader batches, partial errors, cancellation, queries, and mutations.
- Compare bounded top-level query concurrency, asynchronous fetchers, DataLoader batching, virtual-thread or platform-thread executors, and retention of synchronous execution.
- Define propagation and isolation requirements for user identity, interaction context, authorization, locale, transaction, persistence session, diagnostics, and cancellation.
- Require top-level mutation fields and dependent domain interactions to retain serial semantics.
- Prototype the safest viable query boundary with a dedicated bounded executor and per-task interaction lifecycle.
- Benchmark latency, throughput, database concurrency, thread usage, queueing, cancellation, and failure behavior under representative operations.
- Produce separately reviewable implementation proposals rather than changing production threading during this analysis.

## Capabilities

### New Capabilities

- `rich-graphql-parallel-execution-analysis`: Defines reproducible concurrency evidence, safety boundaries, prototype requirements, and a roadmap for bounded parallel rich GraphQL query execution.

### Modified Capabilities

None.

## Impact

- Adds timing probes, thread and interaction traces, deterministic delay fixtures, load tests, disposable prototypes, decision records, and implementation recommendations.
- Investigates `AsyncExecutionStrategyResolvingWithinInteraction`, `GraphQlSourceForCauseway`, generated data fetchers, DataLoader integration, and Causeway interaction and persistence boundaries.
- May justify later changes to query and mutation execution strategies, executor configuration, context propagation, or fetcher return types.
- Does not change production query or mutation execution, make all fields concurrent, or promise a latency improvement before evidence exists.
