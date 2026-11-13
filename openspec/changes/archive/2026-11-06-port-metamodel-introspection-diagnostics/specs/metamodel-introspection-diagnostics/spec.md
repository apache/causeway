## ADDED Requirements

### Requirement: Retained introspection chain on stack overflow

When nested specification loading throws `StackOverflowError`, Causeway SHALL best-effort report the active Java-type chain in nesting order before propagating the identical original error.
Entries SHALL contain the requested introspection request, effective request/substituted type when different, a current-state snapshot when available and compact caller information when safely obtainable.
Reporting failures MUST NOT replace the original error, and duplicate reports SHALL be suppressed as the same error unwinds through nested loader boundaries on that thread.

#### Scenario: Nested loader overflow

- **WHEN** nested specification loading exhausts the stack or propagates a stack-overflow error
- **THEN** one best-effort report retains the active ordered type/request/state/caller chain
- **AND** the original error continues unchanged through existing failure handling

#### Scenario: Missing current state or failed reporting

- **WHEN** a spec is not yet available or diagnostics themselves fail
- **THEN** available type/request data remains usable without forcing introspection
- **AND** the original metamodel error is not replaced

#### Scenario: Normal successful loading

- **WHEN** loading succeeds
- **THEN** no overflow-chain report is emitted at standard log levels and loader results remain unchanged

### Requirement: Thread-isolated scoped lifecycle

Diagnostic chains SHALL be isolated per bootstrap thread and SHALL release active request state after success or failure.
Existing substitution, null/filter handling, cache identity, request downgrades, alias registration, introspection transitions and synchronization SHALL remain unchanged.

#### Scenario: Concurrent nested loads

- **WHEN** independent threads load specifications
- **THEN** each captured/reported chain contains only its own frames

#### Scenario: Failure followed by unrelated work

- **WHEN** a nested load fails and its scopes unwind before later work starts
- **THEN** no old frame or report-suppression state contaminates the later chain

#### Scenario: V4 phased request and substitution

- **WHEN** v4 downgrades a FULL request during mixin bootstrap or substitutes/filters a Java type
- **THEN** diagnostics describe that request without changing the existing loading decision or returned specification

### Requirement: Deterministic mixin traversal without semantic reordering

Registered mixin types SHALL be traversed in ascending fully qualified class-name order through the shared registry stream.
Applicability and explicit action/association ordering SHALL NOT be changed.

#### Scenario: Different insertion orders

- **WHEN** equivalent registries receive the same mixins in different insertion orders
- **THEN** their mixin streams yield the same ascending class-name sequence

#### Scenario: Shared discovery and explicit member sequence

- **WHEN** eager/on-the-fly action or association discovery consumes registered mixins
- **THEN** it receives the deterministic stream
- **AND** explicit member sequence still determines user-facing member order

### Requirement: Representative recursion-path regression coverage

Regression coverage SHALL exercise the combined recording-navigation synthesis, cyclic mixed-in association discovery and action element-type loading path across several specifications.
It SHALL establish repeated bootstrap/traversal consistency while diagnostic tests demonstrate retained nested type chains without requiring a genuine JVM stack exhaustion.

#### Scenario: Repeated representative bootstrap

- **WHEN** the representative graph boots repeatedly with command recording enabled
- **THEN** cyclic mixed-in collections and their navigation actions remain available, action return element types resolve and explicit ordering is preserved
- **AND** mixin traversal order is repeatable
