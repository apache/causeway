## ADDED Requirements

### Requirement: One observation per invoked action primer

When observation is active, the framework SHALL create one `causeway.priming.action` observation around each matching `ActionPrimer` callback that it invokes.
The framework SHALL NOT create an action-primer observation when no primer matches, and SHALL NOT create an aggregate observation around several matching callbacks.

#### Scenario: One action primer matches

- **WHEN** an ordinary or contributed domain action has one matching registered action primer
- **THEN** the framework invokes the primer exactly once within one `causeway.priming.action` observation
- **AND** the observation closes before the domain action method begins

#### Scenario: Several action primers match

- **WHEN** several action primers match one domain-facing action key
- **THEN** the framework invokes them in the existing deterministic order within separate sequential sibling observations
- **AND** it creates no additional aggregate priming observation

#### Scenario: No action primer matches

- **WHEN** an action has no matching registered action primer or is excluded from priming by existing association-access rules
- **THEN** the framework creates no action-primer observation
- **AND** action execution remains unchanged

### Requirement: One observation per invoked view primer

When observation is active, the framework SHALL create one `causeway.priming.view` observation around each matching `ViewPrimer` callback that it invokes.
The framework SHALL preserve the existing once-per-request page visit guard and SHALL NOT create an aggregate observation around several matching callbacks.

#### Scenario: One view primer matches

- **WHEN** an entity page begins a request lifecycle and one view primer matches its exact logical type
- **THEN** the framework invokes the primer exactly once within one `causeway.priming.view` observation
- **AND** the observation closes before Wicket page preparation begins

#### Scenario: Several view primers match

- **WHEN** several view primers match one exact logical type
- **THEN** the framework invokes them in the existing deterministic order within separate sequential sibling observations
- **AND** it creates no additional aggregate priming observation

#### Scenario: View primer is not invoked

- **WHEN** no view primer matches or the existing page visit guard excludes a repeated lifecycle visit
- **THEN** the framework creates no view-primer observation

### Requirement: Stable and domain-facing primer names

Action-primer observations SHALL use stable name `causeway.priming.action` and contextual name `prime action <logical-member-identifier>`.
View-primer observations SHALL use stable name `causeway.priming.view` and contextual name `prime view <logical-object-type>`.
Contextual names SHALL preserve declared casing, reduce the logical-type namespace before truncation when necessary, and remain within the existing deterministic 50-character limit.

#### Scenario: Ordinary action primer is observed

- **WHEN** a primer is invoked for `demo.Customer#updateName`
- **THEN** its contextual name is `prime action demo.Customer#updateName`
- **AND** its stable name remains `causeway.priming.action`

#### Scenario: Contributed action primer is observed

- **WHEN** a primer is invoked for a contributed action
- **THEN** its contextual name uses the domain-facing action identity
- **AND** it does not expose the mixin implementation identity

#### Scenario: View primer is observed

- **WHEN** a primer is invoked for logical type `demo.Customer`
- **THEN** its contextual name is `prime view demo.Customer`
- **AND** its stable name remains `causeway.priming.view`

#### Scenario: Primer name exceeds the bound

- **WHEN** a primer's full contextual name would exceed 50 characters
- **THEN** the framework applies the existing namespace fallback and deterministic truncation policy
- **AND** the complete canonical attributes remain available

### Requirement: Canonical bounded primer metadata

Each action-primer observation SHALL carry the complete domain-facing `causeway.object.type` and `causeway.action.id` attributes.
Each view-primer observation SHALL carry the complete domain-facing `causeway.object.type` attribute.
Primer observations MUST NOT attach implementation class names, generated lambda names, registration order, target identity, arguments, values, bookmarks, object titles, users, tenants, transaction identifiers, interaction identifiers, or generated component paths.

#### Scenario: Action-primer metadata is exported

- **WHEN** an action primer is observed
- **THEN** its canonical attributes identify the logical target type and complete domain-facing action
- **AND** they contain no action argument or target-instance data

#### Scenario: View-primer metadata is exported

- **WHEN** a view primer is observed
- **THEN** its canonical attribute identifies the complete logical object type
- **AND** it contains no bookmark, title, target-instance, user, tenant, or component-path data

#### Scenario: Repeated sibling primers are exported

- **WHEN** several primers match the same action or view key
- **THEN** their observations use the same semantic name and canonical attributes
- **AND** the framework does not distinguish them using implementation or sequence metadata

### Requirement: Natural primer parentage and Java-agent ownership

A primer observation SHALL inherit the naturally current action, interaction, HTTP, or other trace context.
Causeway SHALL NOT fabricate an action, page-preparation, or aggregate priming parent.
Automatic JDBC spans initiated synchronously by a primer SHALL remain Java-agent-owned descendants of that primer observation.

#### Scenario: Action primer performs JDBC work

- **WHEN** an action primer performs database work during an observed action invocation
- **THEN** the primer observation is a child of the current action-invocation observation
- **AND** Java-agent JDBC spans are descendants of the primer observation

#### Scenario: View primer performs JDBC work

- **WHEN** a view primer performs database work before entity-page preparation
- **THEN** the primer observation inherits the current request or interaction context
- **AND** it is a sibling of later page-preparation and page-render observations rather than their child
- **AND** Java-agent JDBC spans are descendants of the primer observation

#### Scenario: Primer executes without a Causeway semantic parent

- **WHEN** a primer is invoked with only an automatic or externally supplied current context
- **THEN** its observation inherits that context without fabricating a Causeway parent

### Requirement: Primer failure and cleanup semantics

The framework SHALL record an escaping primer failure on the observation for that callback, SHALL close its scope, and SHALL propagate the original failure unchanged.
Observation MUST NOT retry, suppress, wrap, or convert a primer failure and MUST NOT alter the existing fail-fast callback sequence.

#### Scenario: Action primer fails

- **WHEN** an observed action primer throws a runtime exception or error
- **THEN** its observation records the failure and closes
- **AND** the same failure propagates through action execution
- **AND** the action method and any later matching primer are not invoked

#### Scenario: View primer fails

- **WHEN** an observed view primer throws a runtime exception or error
- **THEN** its observation records the failure and closes
- **AND** the same failure propagates through the Wicket request
- **AND** page preparation and any later matching primer do not continue normally

### Requirement: Safe inactive behavior

When Causeway observation is inactive, primer matching and callback execution SHALL remain unchanged and no primer observation or metadata SHALL be exported.

#### Scenario: Observation profile is inactive

- **WHEN** one or more action or view primers match while the observation integration is no-op
- **THEN** each callback is invoked exactly once in the existing order
- **AND** no primer observation, scope, or metadata is exported
