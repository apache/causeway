## ADDED Requirements

### Requirement: Stack-overflow introspection chain

When nested specification loading results in a `StackOverflowError`, the framework SHALL report the active metamodel introspection chain before propagating the original error.
The report SHALL identify each requested Java type in nesting order and SHALL include its requested introspection state and current introspection state when available.
The report SHALL include compact caller information sufficient to distinguish relevant metamodel loading paths when that information can be obtained safely.

#### Scenario: Nested specification loading exhausts the JVM stack

- **WHEN** metamodel creation encounters a `StackOverflowError` during nested specification loading
- **THEN** one diagnostic report lists the active specification types in nesting order
- **THEN** each listed entry includes the requested introspection state and any available current state
- **THEN** the original `StackOverflowError` continues through the existing failure handling

#### Scenario: The same error unwinds through multiple loader invocations

- **WHEN** one `StackOverflowError` crosses multiple nested specification-loader boundaries
- **THEN** the framework emits no more than one introspection-chain report for that error on that thread

#### Scenario: Metamodel creation succeeds

- **WHEN** metamodel creation completes without a stack overflow
- **THEN** no stack-overflow introspection-chain report is emitted at standard log levels

### Requirement: Thread-isolated diagnostics

The framework SHALL track nested introspection independently for each metamodel bootstrap thread.

#### Scenario: Concurrent tasks load specifications

- **WHEN** two metamodel bootstrap threads perform nested specification loads concurrently
- **THEN** each diagnostic chain contains only entries created by its own thread

### Requirement: Deterministic mixin traversal

The framework SHALL traverse registered mixin types in ascending fully qualified class-name order whenever the metamodel discovers mixed-in actions or associations.
The deterministic traversal SHALL NOT change mixin applicability or the framework's explicit member-ordering rules.

#### Scenario: Registry insertion order differs

- **WHEN** equivalent registries contain the same mixin classes inserted in different orders
- **THEN** `streamMixinTypes()` returns the same ascending fully qualified class-name sequence for both registries

#### Scenario: Mixed-in actions and associations are discovered

- **WHEN** metamodel introspection discovers mixed-in actions and mixed-in associations from the registry
- **THEN** both discovery paths consume the same deterministic mixin sequence
- **THEN** the resulting members retain their existing applicability and explicit ordering semantics

### Requirement: Representative recursion regression coverage

The test suite SHALL exercise the combined metamodel path involving navigation-action synthesis, mixed-in association discovery, and action element-type introspection across multiple specifications.

#### Scenario: Representative nested graph is introspected repeatedly

- **WHEN** the representative metamodel fixture is bootstrapped repeatedly with command-log recording support enabled
- **THEN** each run follows the same mixin traversal order
- **THEN** the diagnostic mechanism can identify the participating specification types in their nested order
