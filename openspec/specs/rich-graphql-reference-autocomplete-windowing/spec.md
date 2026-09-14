# Rich GraphQL Reference Autocomplete Windowing Specification

## Purpose

Define additive, bounded rich GraphQL autocomplete windows for properties and action parameters while retaining the existing autocomplete operation and Causeway semantics.

## Requirements

### Requirement: Additive rich autocomplete windows
Rich GraphQL SHALL expose `autoCompleteWindow` beside every advertised rich property or action-parameter `autoComplete` field.
The existing autocomplete field, arguments, output, authorization, and execution behavior MUST remain compatible.

#### Scenario: Property advertises autocomplete
- **WHEN** a rich property wrapper exposes `autoComplete(search)`
- **THEN** it also exposes `autoCompleteWindow(search, offset, size)`
- **AND** the window items use the same output type and authorized Causeway autocomplete semantic

#### Scenario: Action parameter advertises autocomplete
- **WHEN** a rich object or service action parameter exposes autocomplete with preceding dependent arguments
- **THEN** its window field declares the same preceding arguments plus search, offset, and size
- **AND** evaluates those arguments through the same parameter negotiation model

#### Scenario: Existing client submits legacy autocomplete
- **WHEN** a client executes a previously valid `autoComplete(search)` document
- **THEN** its operation shape and semantic list result remain unchanged
- **AND** the client is not required to select window metadata

### Requirement: Bounded window request contract
Autocomplete windows SHALL accept zero-based offset and positive requested size and SHALL enforce configured default and maximum sizes before invoking domain autocomplete.
Invalid requests MUST fail with bounded errors that omit search text, pending arguments, object identities, and result values.

#### Scenario: Window arguments are omitted
- **WHEN** a client omits offset or size
- **THEN** offset defaults to zero and size defaults to the configured autocomplete window size
- **AND** the result reports the effective requested values

#### Scenario: Window request is valid
- **WHEN** offset is non-negative and size is within the configured maximum
- **THEN** the server returns at most the requested number of items
- **AND** no response exceeds the configured maximum

#### Scenario: Window request is invalid
- **WHEN** offset is negative, size is non-positive, or size exceeds the configured maximum
- **THEN** GraphQL returns a bounded autocomplete-window error before invoking the domain semantic
- **AND** does not echo protected or submitted values

### Requirement: Authoritative request-consistent window metadata
Each autocomplete window SHALL invoke the authorized Causeway autocomplete semantic once for that request, preserve its application encounter order, and report enough metadata to continue bounded offset paging.
The contract MUST NOT claim persistence query pushdown, a cursor, or a stable snapshot across requests.

#### Scenario: Result spans several windows
- **WHEN** the authoritative result contains more items than the requested size
- **THEN** the result reports items, offset, requested size, returned count, total count, configured maximum, previous and next availability, and `APPLICATION` ordering
- **AND** adjacent windows preserve the encounter order observed by their respective executions

#### Scenario: Offset reaches or exceeds total count
- **WHEN** a valid offset is at or beyond the materialized result size
- **THEN** the server returns an empty item list with accurate count and continuation metadata
- **AND** does not treat the empty window as an error

#### Scenario: Domain state changes between requests
- **WHEN** authoritative autocomplete order or membership changes between page requests
- **THEN** each response remains internally consistent for its own execution
- **AND** no cross-request snapshot or cursor stability is implied

### Requirement: Semantic identity and value-shape preservation
Window items SHALL use the same rich output mapping as existing autocomplete results and SHALL request or expose only fields valid for their advertised concrete types.

#### Scenario: Window returns domain references
- **WHEN** autocomplete items are object references
- **THEN** their advertised semantic identity, optional title, optional version, and polymorphic projection behavior remain available
- **AND** no bookmark or concurrency token is fabricated

#### Scenario: Window returns scalar or enum values
- **WHEN** an advertised autocomplete semantic returns a supported scalar or enum item type
- **THEN** the window preserves that item type and encounter order
- **AND** does not require object metadata
