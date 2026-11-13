# application-priming-hooks Specification

## Purpose

Provide validated application registration and synchronous action/entity-view preloading hooks without changing authorization, invocation identity or persistence context ownership.

## Requirements

### Requirement: Application registration API

Causeway SHALL expose action/view primer callbacks and a registrar SPI allowing registration by exact domain class and one or more local action logical names, or by exact domain class for entity viewing.
Applications SHALL NOT need to parse logical identifiers.
Contributed actions SHALL use the domain-facing mixee identity.

#### Scenario: Single and multiple actions

- **WHEN** a registrar supplies a domain class and one or several local action names
- **THEN** each resolved exact action key has the supplied callback registered independently

#### Scenario: Contributed action and entity view

- **WHEN** a registrar supplies a mixee class and contributed action name, or registers an entity-view callback for a class
- **THEN** the registry derives the exact domain-facing action or logical-type key from the metamodel

### Requirement: Validated immutable startup registry

Registrations SHALL be collected after metamodel loading and frozen before application requests.
Invalid registrations SHALL fail startup with offending registrar/class/action diagnostics.
Lookup SHALL use exact logical-type/action keys without Java hierarchy matching.
The registry SHALL reject mutation outside its registration window, including after failed initialization, and SHALL NOT publish partially validated registrations.

#### Scenario: Unknown exact class or action

- **WHEN** a registration names an unloaded/nonexact class, malformed local name or unresolved action
- **THEN** startup fails with a diagnostic identifying that registration

#### Scenario: Frozen or failed registry

- **WHEN** a registrar retains the registry and attempts mutation after successful or failed initialization
- **THEN** mutation is rejected without changing active lookup state

#### Scenario: Related but distinct type

- **WHEN** an object of a distinct subtype logical type is dispatched
- **THEN** a registration for another logical type is not selected by assignability

### Requirement: Ordinary action priming boundary

Every matching ordinary action primer SHALL run synchronously after validation and executing-event argument adjustment but before the domain method/cache lookup, within the existing interaction, transaction, thread and persistence context.
Contributed actions SHALL supply the mixed-in domain object, not the transient mixin instance, while preserving physical invocation identity.

#### Scenario: Adjusted arguments and contributed target

- **WHEN** an authorized ordinary or contributed action executes with adjusted effective arguments
- **THEN** matching primers receive those effective positional values and the domain target before invocation

#### Scenario: Nested ordinary execution

- **WHEN** wrapper-mediated or nested ordinary execution invokes a registered action
- **THEN** that invocation runs its matching primers before its own method

#### Scenario: Excluded and unmatched work

- **WHEN** execution is pass-through, a synthetic mixed-in property/collection accessor, or has no exact registration
- **THEN** no action priming callback is introduced and existing execution behavior is retained

### Requirement: Immutable positional arguments

`ActionArguments` SHALL expose count, indexed untyped/typed access and an unmodifiable shallow snapshot of effective arguments.
It SHALL preserve nulls and array-valued parameters as individual positions and reject incompatible typed access with index/type diagnostics.

#### Scenario: Defensive snapshot and array parameter

- **WHEN** the original argument list is mutated or one parameter contains an array
- **THEN** the snapshot retains original arity and positional references without flattening or list mutation

#### Scenario: Typed null or incompatible value

- **WHEN** a primer reads a typed null or requests an incompatible required type
- **THEN** null is retained or the request fails with the index and expected type respectively

### Requirement: Authorized request-local entity-view priming

Wicket SHALL invoke exact root-type view primers after visibility authorization and before expensive title/layout/property/collection preparation on initial and subsequent relevant full/Ajax requests.
Callbacks SHALL use the rendering request's current thread and persistence context.
A page SHALL be admitted at most once per request without retaining callback state in serialized page fields.

#### Scenario: Authorized initial page and lifecycle re-entry

- **WHEN** an authorized domain-object page is prepared and revisited within one request
- **THEN** its matching primers run before expensive traversal and at most once for that page

#### Scenario: Later full or Ajax request

- **WHEN** the stateful page participates in a later full or Ajax request
- **THEN** fresh request admission allows priming again after existing refresh and authorization

#### Scenario: Unauthorized or unmatched root

- **WHEN** the root is unauthorized or has no exact registered callback
- **THEN** no application primer runs for that unauthorized/unmatched root
- **AND** authorization failure or normal unmatched rendering follows the existing behavior

### Requirement: Multiple deterministic callbacks and transparent failures

All callbacks registered for one exact key SHALL execute in deterministic registration order, but applications MUST NOT rely on relative order and MUST make callbacks independently idempotent and safe for concurrent invocation.
Callbacks SHALL be synchronous and intended for read-only preloading.
Failures SHALL propagate unchanged through the enclosing action/Wicket boundary without framework retry or suppression.

#### Scenario: Multiple registrations

- **WHEN** several callbacks match an admitted action or view opportunity
- **THEN** each callback runs once in deterministic order unless a preceding callback fails

#### Scenario: Callback fails

- **WHEN** a primer throws
- **THEN** normal action/page preparation stops and that failure propagates through the existing boundary
- **AND** Wicket lifecycle re-entry does not retry that page's callback in the same request
