## 1. Establish the Execution Baseline

- [ ] 1.1 Add deterministic synchronous, delayed, future-returning, nested, list, DataLoader, failing, query, and mutation fixtures.
- [ ] 1.2 Trace request thread, execution-strategy dispatch, data fetchers, interactions, transactions, persistence calls, completion stages, and result assembly.
- [ ] 1.3 Verify current concurrency and ordering across independent HTTP requests, sibling query roots, nested fields, and top-level mutations.
- [ ] 1.4 Attribute latency to parsing, validation, domain work, persistence I/O, completion waiting, batching, and stitching.

## 2. Define the Safety Boundary

- [ ] 2.1 Inventory user, authorization, interaction, locale, transaction, persistence, diagnostics, tracing, and cancellation context that must cross a task boundary.
- [ ] 2.2 Identify which root and nested operation shapes are independent and which share mutable or thread-confined state.
- [ ] 2.3 Define serial mutation and dependent-interaction invariants before asynchronous fetchers are introduced.
- [ ] 2.4 Compare bounded root concurrency, finer-grained asynchronous fetchers, DataLoader batching, query consolidation, and unchanged synchronous execution.

## 3. Prototype Bounded Query Concurrency

- [ ] 3.1 Build a disposable prototype using a dedicated bounded executor and per-task Causeway interaction and persistence lifecycle.
- [ ] 3.2 Preserve deterministic GraphQL field ordering, partial data, bounded errors, authorization, and nested source dependencies.
- [ ] 3.3 Test cancellation, timeout, queue saturation, rejection, transaction cleanup, client disconnect, and executor shutdown.
- [ ] 3.4 Confirm top-level mutations remain serial and no managed persistence object or mutable interaction crosses threads unsafely.

## 4. Measure and Recommend

- [ ] 4.1 Benchmark latency, throughput, tail latency, thread and queue use, database connections, statement concurrency, and resource consumption across operation shapes.
- [ ] 4.2 Compare platform-thread, supported virtual-thread, batching, and synchronous baselines under documented environments and dataset sizes.
- [ ] 4.3 Publish the recommended concurrency boundary, configuration, metrics, rollback, rejected alternatives, and separately reviewable implementation changes.
- [ ] 4.4 Run analysis-document, prototype, concurrency-safety, security, formatting, and strict OpenSpec validation checks.
