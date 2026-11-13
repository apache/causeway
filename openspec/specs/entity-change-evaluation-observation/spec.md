# entity-change-evaluation-observation Specification

## Purpose

Identify the aggregate cost of evaluating candidate property changes while preserving lazy evaluation, retry, failure, publication and tracing ownership behavior.

## Requirements

### Requirement: Aggregate entity property-change evaluation observation

With active observation and enlisted candidate records, the tracker SHALL create one `causeway.entitychange.evaluate` observation, displayed as `evaluate property changes`, around obtaining post-values and comparing pre/post values.
The observation SHALL cover all candidates and complete before publication callbacks.

#### Scenario: Candidates are evaluated

- **WHEN** multiple enlisted records are evaluated
- **THEN** one aggregate observation encloses their post-value access and comparison
- **AND** candidates with no publishable difference still contribute to that observation

#### Scenario: Empty candidate collection

- **WHEN** no property-change records are enlisted
- **THEN** no evaluation observation is created

#### Scenario: Evaluation is triggered lazily

- **WHEN** a public tracker accessor triggers evaluation before transaction completion
- **THEN** that actual evaluation is observed
- **AND** later access to the memoized result does not create another evaluation observation

### Requirement: Retry and failure lifecycle

The observation SHALL preserve existing defensive retry, handled-accessor failure and escaping-failure behavior without leaking scope.

#### Scenario: Concurrent modification

- **WHEN** the original candidate traversal triggers concurrent-modification recovery
- **THEN** the original attempt and defensive-copy retry execute within one evaluation observation

#### Scenario: Handled accessor failure

- **WHEN** existing behavior converts a non-deadlock accessor failure to an unknown post-value
- **THEN** evaluation and publication semantics remain unchanged
- **AND** telemetry does not turn that handled error into an escaping failure

#### Scenario: Escaping failure

- **WHEN** evaluation throws an escaping exception
- **THEN** the observation records it, stops and restores its prior context
- **AND** the identical original exception is rethrown
- **AND** subsequent work cannot inherit a stale evaluation scope

### Requirement: Natural ancestry and distinct publication boundaries

Evaluation SHALL inherit current tracing context without fabricated action parentage and SHALL enclose its synchronous JDBC work without replacing or duplicating existing instrumentation.
It SHALL preserve existing transaction, publishing, JPA and audit observation boundaries.

#### Scenario: Current action or background context

- **WHEN** evaluation starts with an action, interaction, request or background context current
- **THEN** that natural context parents the evaluation observation and its synchronous SQL descendants

#### Scenario: Audit follows evaluation

- **WHEN** transaction completion evaluates candidates and then publishes changes to the audit subscriber
- **THEN** evaluation finishes before audit writing begins
- **AND** evaluation and audit observations are siblings under their natural current parent

### Requirement: Static private evaluation metadata

Evaluation SHALL add only static operation/display names and framework bean/module metadata.
It MUST NOT add entity types, property identifiers, targets, bookmarks, pre/post values, user/tenant identities, transaction/interaction IDs, sequence numbers, candidate/changed/retry counts or other change payload data.

#### Scenario: Telemetry inspection

- **WHEN** candidate payloads or counts vary
- **THEN** observation names and added attributes remain static and free of candidate data

### Requirement: Inactive evaluation behavior

Inactive observation SHALL preserve post-value access, changed-record results, publication and existing retry behavior without exported Causeway evaluation spans or a replacement tracing pipeline.

#### Scenario: No-op registry

- **WHEN** evaluation uses a no-op registry
- **THEN** the same evaluation closure runs with unchanged results and failures

#### Scenario: Inactive profile or absent exporter

- **WHEN** evaluation runs with observation inactive or without an exporting tracing pipeline
- **THEN** evaluation and publication still run normally
- **AND** no Causeway evaluation span is exported
