## ADDED Requirements

### Requirement: Host-owned action interception boundary

The foundation SHALL publish canonical target identity before any service or object action invocation and SHALL allow an enclosing host to claim and cancel the request synchronously.
A claimed or canceled request MUST NOT execute GraphQL validation or invocation and MUST NOT emit a synthetic successful action result.
The exact framework Logout operation SHALL be identified by service logical type `causeway.security.LogoutMenu` and action id `logout`, not by its label, result path, or a partial identity match.

#### Scenario: Host claims an action request

- **WHEN** a host claims or cancels a published action request using its canonical target and action identity
- **THEN** the component does not validate or invoke that action through GraphQL
- **AND** the host remains responsible for any replacement behavior, announcement, and navigation

#### Scenario: Framework Logout is requested

- **WHEN** a component publishes an action request for `causeway.security.LogoutMenu#logout`
- **THEN** the request contains sufficient exact identity for the host to recognize the reserved authentication operation before invocation
- **AND** no path string or post-invocation result is required to recognize it

#### Scenario: Application action has a logout-like name

- **WHEN** an application action is labeled Logout or returns `/logout` but does not have the exact framework service and action identity
- **THEN** the foundation does not classify it as the reserved framework Logout operation
- **AND** ordinary host and domain-action policy continues to apply

### Requirement: Local resource action-result semantics

The foundation SHALL classify an action result of exact GraphQL output type `LocalResourcePathValue` as a distinct local-resource semantic result before applying generic structured-object classification.
The result SHALL preserve only the authoritative `path` and `openUrlStrategy` fields and MUST NOT be treated as a navigable domain object, ordinary scalar, or authentication instruction.
Object and service action contexts SHALL apply the same classification contract.

#### Scenario: Typed local resource result is returned

- **WHEN** a successfully invoked action has exact output type `LocalResourcePathValue` and returns valid path and strategy fields
- **THEN** the emitted action result has the local-resource kind with those fields preserved
- **AND** the result does not acquire object identity or route semantics

#### Scenario: Structured non-resource result is returned

- **WHEN** a structured result has a different GraphQL output type
- **THEN** it retains the applicable existing object or unsupported-result behavior
- **AND** field names resembling `path` or `openUrlStrategy` do not opt it into local-resource semantics

#### Scenario: Local resource result is malformed

- **WHEN** a `LocalResourcePathValue` result lacks a non-empty path or supported opening strategy
- **THEN** the result fails closed with a bounded unsupported-result diagnostic
- **AND** no navigation request is emitted

#### Scenario: Result kinds are regression tested

- **WHEN** foundation result-normalization tests execute
- **THEN** local-resource, scalar, object, collection, void, and other structured result classifications remain deterministic
- **AND** service and object action execution produce equivalent classifications
