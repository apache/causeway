# application-priming-hooks Specification

## Purpose

TBD - created by archiving change add-application-priming-hooks.
Update Purpose after archive.

## Requirements

### Requirement: Applications can register action primers by class and local action name
The framework SHALL provide an application SPI for registering an action primer using a domain class and one or more local action logical names.
The framework SHALL derive each complete domain-facing logical member identifier from the supplied class and local action name, and the primer SHALL NOT be required to inspect or parse that identifier.

#### Scenario: Register one action primer
- **WHEN** an application registers a primer for domain class `Invoice` and local action name `approve`
- **THEN** the framework associates the primer with the metamodel action named `approve` on the exact logical type represented by `Invoice`

#### Scenario: Register one primer for several actions
- **WHEN** an application registers one primer for domain class `Invoice` and local action names `approve` and `recalculate`
- **THEN** the framework associates that primer independently with both resolved domain-facing action identities

#### Scenario: Register a contributed action
- **WHEN** an application registers a primer using a domain target class and the local name of an action contributed to that class by a mixin
- **THEN** the framework associates the primer with the action's domain-facing mixee identity rather than the mixin implementation type

### Requirement: Applications can register entity-view primers by class
The framework SHALL provide an application SPI for registering a view primer using only the domain class of a Wicket entity page's root object.
The framework SHALL derive the registration key from the exact logical type assigned to that class by the metamodel.

#### Scenario: Register a view primer
- **WHEN** an application registers a view primer for domain class `Invoice`
- **THEN** the framework associates the primer with the exact logical type represented by `Invoice` without requiring a logical type name from the application

### Requirement: Priming registrations are validated and frozen at startup
The framework SHALL collect priming registrations after metamodel loading, validate them against the completed metamodel, and freeze the resulting action and view lookup registries before serving application requests.
Invalid registration SHALL fail application startup with a diagnostic that identifies the offending registration.

#### Scenario: Registered class has no exact logical type
- **WHEN** a registrar supplies a class that has no exact metamodel specification and logical type
- **THEN** application startup fails instead of searching its superclasses, interfaces, or subclasses

#### Scenario: Local action name cannot be resolved
- **WHEN** a registrar supplies a local action name that does not resolve on the exact metamodel specification for the supplied class
- **THEN** application startup fails and identifies the class and local action name

#### Scenario: Registration is attempted after startup registration closes
- **WHEN** application code attempts to add a registration after the registry has been frozen
- **THEN** the framework rejects the mutation and leaves the active lookup registry unchanged

#### Scenario: Related logical type is rendered
- **WHEN** a view primer is registered for a superclass or other domain class with its own logical type and an object of a different subtype logical type is rendered
- **THEN** the framework does not select the primer by Java assignability or hierarchy traversal

### Requirement: Matching action primers run before action methods
For each ordinary domain action execution, the framework SHALL perform an exact lookup using the action's domain-facing logical member identifier and SHALL synchronously invoke every matching action primer before invoking the domain method.
The primer SHALL run within the action's current interaction, transaction, thread, and ORM persistence context.

#### Scenario: Matching ordinary action is invoked
- **WHEN** an ordinary domain action has a matching registered primer and passes action validation
- **THEN** the framework invokes the primer after any executing-phase argument adjustment and before invoking the domain action method

#### Scenario: Contributed action is invoked
- **WHEN** a contributed action has a matching registered primer
- **THEN** the primer receives the mixed-in domain object as its target rather than the transient mixin implementation instance

#### Scenario: Nested ordinary action is invoked
- **WHEN** wrapper-mediated or nested execution invokes an ordinary domain action with a matching registration
- **THEN** the framework invokes that action's matching primer as part of the nested execution

#### Scenario: Pass-through or synthetic association access occurs
- **WHEN** the framework performs pass-through invocation or synthetic mixed-in property or collection access
- **THEN** it does not introduce a separate action-priming callback

#### Scenario: Action has no registered primer
- **WHEN** an ordinary domain action has no exact action-primer registration
- **THEN** the framework invokes the action normally without executing an application priming callback

### Requirement: Action primers receive immutable positional arguments
An action primer SHALL receive the effective action arguments as an immutable positional value that preserves the action's arity.
The argument API SHALL provide the argument count, untyped indexed access, type-checked indexed access, and an unmodifiable list view.

#### Scenario: Primer reads a typed argument
- **WHEN** a primer requests an argument at a valid index using a compatible required type
- **THEN** the argument API returns that argument without requiring the primer to parse action metadata

#### Scenario: Action parameter is an array
- **WHEN** an action has one parameter whose value is an array
- **THEN** the argument API represents that array as one positional argument rather than flattening its elements

#### Scenario: Primer requests an incompatible argument type
- **WHEN** a primer requests a positional argument using an incompatible required type
- **THEN** the argument API fails with an error identifying the index and expected type

#### Scenario: Primer attempts to modify the list view
- **WHEN** a primer attempts to mutate the action arguments through their list view
- **THEN** the argument API rejects the mutation

### Requirement: Matching view primers run before Wicket entity-page preparation
For each relevant Wicket entity-page rendering request, the framework SHALL perform an exact lookup using the root object's logical type and SHALL synchronously invoke every matching view primer after object visibility authorization but before page preparation traverses the root object's title, layout, properties, or collections.
The primer SHALL run within the rendering request's current thread and ORM persistence context.

#### Scenario: Initial entity-page rendering
- **WHEN** Wicket initially renders an authorized entity page whose exact root logical type has a registered view primer
- **THEN** the framework invokes the primer before preparing the expensive page content

#### Scenario: Later rendering request
- **WHEN** the same stateful entity page participates in a later full or Ajax rendering request with a matching registration
- **THEN** the framework invokes the primer for the current request so it can populate that request's ORM persistence context

#### Scenario: Wicket lifecycle revisits page preparation
- **WHEN** Wicket visits the same entity page preparation path more than once during one rendering request
- **THEN** the framework invokes each matching view primer at most once for that entity page during that request

#### Scenario: Root logical type has no registered primer
- **WHEN** Wicket renders an entity page whose exact root logical type has no view-primer registration
- **THEN** the framework prepares and renders the page normally without executing an application priming callback

### Requirement: Multiple matching primers all execute
The registry SHALL permit multiple action or view primers for the same exact key and the framework SHALL invoke each matching callback deterministically.
Primer implementations MUST be independently idempotent and MUST NOT depend on relative callback order.

#### Scenario: Several primers match one key
- **WHEN** two independently registered primers match one action or view lookup key
- **THEN** the framework invokes both primers exactly once for that priming opportunity

### Requirement: Primer execution is synchronous and failure-transparent
Action and view primers MUST be synchronous, read-only application callbacks intended to load likely-needed objects into the active ORM persistence context.
The framework SHALL propagate any callback exception through the enclosing action execution or Wicket request and SHALL NOT retry, suppress, or convert the failure into an optional optimization result.

#### Scenario: Action primer fails
- **WHEN** a matching action primer throws an exception
- **THEN** the action method is not invoked and the exception propagates through the action execution

#### Scenario: View primer fails
- **WHEN** a matching view primer throws an exception
- **THEN** page preparation does not continue normally and the exception propagates through the Wicket request
