## Context

`GraphQlSourceForCauseway` configures the same `AsyncExecutionStrategyResolvingWithinInteraction` as both query and mutation execution strategy.
That strategy extends GraphQL Java's `AsyncExecutionStrategy` and wraps field resolution in an authenticated or anonymous Causeway `InteractionService` call.
Its call to `super.resolveFieldWithInfo` can return a completed value or `CompletableFuture`, but generated Causeway data fetchers usually perform synchronous domain and persistence work before returning.

GraphQL Java's async strategy composes `CompletionStage` values and assembles the final result in GraphQL field order.
The data fetcher or batch loader chooses where asynchronous work runs, so selecting `AsyncExecutionStrategy` does not by itself move synchronous work to another thread.
HTTP requests may already execute concurrently at the server level, which is separate from concurrency among fields inside one GraphQL operation.

Causeway interactions, transaction state, persistence sessions, security context, and managed domain objects may be thread-confined.
Nested field fetchers also depend on source objects produced by parent fields, so arbitrary field-level offloading is not equivalent to parallel independent root queries.

## Goals / Non-Goals

**Goals:**

- Establish the actual current execution timeline and thread usage.
- Identify independent query work that could safely overlap.
- Preserve Causeway interaction, authorization, transaction, persistence, locale, and diagnostic semantics.
- Keep mutation and dependent interaction ordering serial.
- Use bounded, configurable, observable concurrency rather than an implicit common pool.
- Compare latency improvements with throughput, contention, queueing, and database impact.
- Produce an evidence-backed implementation roadmap.

**Non-Goals:**

- Enabling production multithreading during the analysis.
- Running every nested data fetcher concurrently.
- Sharing one `EntityManager`, persistence session, transaction, or mutable interaction across threads.
- Parallelizing top-level mutation fields.
- Treating result stitching as the only or necessarily dominant latency cost.
- Replacing GraphQL Java's result ordering or partial-data semantics.

## Decisions

### Correct the initial hypothesis before optimizing

The analysis starts from two separate questions:

1. whether independent field work is currently overlapped;
2. whether result assembly itself is materially expensive.

Timing probes distinguish parser, validation, field dispatch, domain work, persistence I/O, completion waiting, DataLoader dispatch, and final result assembly.
The proposal does not encode the unverified claim that the server is simply single-threaded.

### Treat queries and mutations differently

GraphQL queries may contain independent sibling subtrees that can safely overlap if each receives an isolated interaction and persistence lifecycle.
Top-level mutation fields must execute serially in document order, and domain operations with shared mutable state must not be overlapped merely because they return futures.
Any production proposal must evaluate `AsyncSerialExecutionStrategy` or an equivalent mutation strategy before asynchronous fetchers can make current mutation configuration observably concurrent.

### Prototype at a coarse safe boundary first

The first candidate is bounded concurrency among demonstrably independent top-level query roots or lookups.
Each task re-establishes user identity and other required request context, opens its own Causeway interaction and transaction, resolves its own persistent identity, and returns detached GraphQL-safe results or completion values.
Nested fields that depend on a managed parent remain within that task unless evidence proves a finer boundary safe.

### Compare concurrency with batching

Parallel queries can increase database connections and duplicate work.
DataLoader batching, query consolidation, projection reduction, caching, or sequential execution may outperform additional threads for some operation shapes.
The decision matrix compares these alternatives rather than treating maximum concurrency as the objective.

### Use a dedicated bounded executor

Prototype asynchronous work does not use `ForkJoinPool.commonPool()` implicitly.
It uses an application-managed executor with configurable maximum concurrency, bounded queueing, rejection behavior, thread naming, metrics, shutdown, and context propagation.
The analysis compares platform threads and virtual threads where supported by the project's Java baseline, but neither is selected without evidence.

### Isolate field interactions

A user memento may be copied as immutable identity context, while mutable interaction, transaction, persistence, and request-local objects are created independently per concurrent task.
Managed domain objects and lazy collections are not passed across task boundaries unless the relevant persistence implementation proves that operation safe.
Authorization is re-evaluated inside each task's Causeway interaction.

### Preserve cancellation and bounded failure

Client disconnect, request cancellation, timeout, executor rejection, and one-field failure must not leave work running without ownership.
The prototype records whether cancellation reaches queued and active work and whether transactions and interactions close correctly.
GraphQL partial-data and deterministic response ordering remain unchanged.

## Risks / Trade-offs

- [Parallel fields share persistence state] → Restrict concurrency to independently re-resolved roots with separate interactions and sessions.
- [Database contention erases latency gains] → Measure connection usage, lock behavior, throughput, and tail latency at increasing concurrency.
- [Mutations become concurrent] → Require serial mutation execution before introducing asynchronous mutation-capable fetchers.
- [Thread-local context is lost] → Enumerate context explicitly and test user, locale, transaction, diagnostics, and authorization propagation.
- [Common-pool work is unbounded] → Use a dedicated bounded executor with observable queue and rejection policy.
- [Cancellation only abandons the response] → Trace interruption, future cancellation, interaction closure, and transaction rollback.
- [Fine-grained futures add overhead] → Compare coarse roots, DataLoader batching, and unchanged synchronous execution.

## Migration Plan

The analysis changes no production threading behavior.
Any implementation proposal starts disabled or conservatively configured, preserves serial mutations, and defines rollback to the existing execution path.
Configuration, metrics, executor lifecycle, and compatibility are reviewed independently from the analysis artifacts.

## Open Questions

- Which generated root fields are genuinely independent once Causeway interaction and persistence semantics are considered.
- Whether Spring GraphQL or Reactor request context contains state that must be bridged explicitly.
- Whether virtual threads are compatible with the repository's supported Java baseline and persistence stack.
- Whether DataLoader batching can remove the dominant latency without additional field threads.
- Whether concurrent read-only interactions require separate transactions and database connections for every root.
- How request deadlines and cancellation should propagate into blocking domain and persistence calls.
