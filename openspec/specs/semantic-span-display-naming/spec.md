# semantic-span-display-naming Specification

## Purpose
Define bounded, case-preserving contextual names and canonical metadata for Causeway semantic observations.
## Requirements
### Requirement: Meaningful action-invocation display name
Causeway SHALL retain `causeway.action.invocation` as the stable observation name and SHALL assign each action-invocation observation the contextual display name `act <logical-member-identifier>`.
The logical member identifier SHALL be the domain-facing identity of the invoked action, including for actions implemented by mixins.

#### Scenario: Declared action identity is available
- **WHEN** Causeway invokes a declared action with observation active
- **THEN** the exported span display name begins with `act ` and contains the action's logical type name and member id separated by `#`
- **AND** the observation name remains `causeway.action.invocation`
- **AND** `causeway.action.id` retains the full canonical action identifier

#### Scenario: Mixed-in action is invoked
- **WHEN** Causeway invokes a mixed-in action whose implementation identity is `ApplicationUser_updateEmailAddress#act`
- **THEN** the contextual name uses the domain-facing logical member identifier such as `isisExtSecMan.ApplicationUser#updateEmailAddress`
- **AND** neither the contextual name nor `causeway.action.id` uses the implementation member `act`

### Requirement: Bounded contextual naming
Causeway SHALL derive semantic span contextual names only from bounded operation names and static metamodel or layout identifiers.
Causeway SHALL preserve the declared casing of those identifiers.
Causeway MUST NOT include domain-object instance identifiers, object titles, bookmarks, values, arguments, localized labels, user identities, or tenancy identifiers in contextual names.

#### Scenario: Invocation has instance-specific context
- **WHEN** an observed action is invoked on a particular domain-object instance with argument values
- **THEN** its contextual name contains no target-instance identity, object title, bookmark, or argument value
- **AND** two invocations of the same logical action use the same contextual name

### Requirement: Mixed-in association access identity
Causeway SHALL recognize when an action invocation is the implementation mechanism for a mixed-in property or collection and SHALL represent it using the domain-facing association identity.
A mixed-in property access SHALL use stable observation name `causeway.property.access`, contextual name `prop <logical-member-identifier>`, and canonical attribute `causeway.property.id`.
A mixed-in collection access SHALL use stable observation name `causeway.collection.access`, contextual name `coll <logical-member-identifier>`, and canonical attribute `causeway.collection.id`.

#### Scenario: Mixed-in property getter is evaluated
- **WHEN** Causeway evaluates a mixed-in property implemented by `Property_salesAreaNonFoodTotal#prop`
- **THEN** the exported contextual name identifies the domain-facing association, such as `prop Property#salesAreaNonFoodTotal`
- **AND** the stable observation name is `causeway.property.access`
- **AND** `causeway.property.id` contains the complete domain-facing property identifier
- **AND** the span does not expose the implementation action as `causeway.action.id`

#### Scenario: Mixed-in collection getter is evaluated
- **WHEN** Causeway evaluates a mixed-in collection implemented by a `coll` action
- **THEN** the exported contextual name begins with `coll ` and contains the domain-facing collection identifier
- **AND** the stable observation name is `causeway.collection.access`
- **AND** `causeway.collection.id` contains the complete domain-facing collection identifier

#### Scenario: Association cannot be recovered
- **WHEN** an invocation uses the mixed-in association facet but its domain-facing association cannot be resolved safely
- **THEN** Causeway falls back to ordinary action-invocation instrumentation
- **AND** the invocation remains observable

### Requirement: Canonical identity remains authoritative
Causeway SHALL retain full canonical logical identifiers as span attributes when it uses compact or truncated contextual display names.
Operators SHALL be able to use those attributes to disambiguate equal display names and to identify spans whose contextual names were truncated by the tracing integration.

#### Scenario: Compact names collide
- **WHEN** two logical actions have the same compact type and member display name but different canonical logical identities
- **THEN** their contextual names MAY be equal
- **AND** their `causeway.action.id` attributes distinguish the actions

#### Scenario: Full logical member name exceeds tracing limit
- **WHEN** an `act`, `prop`, or `coll` contextual name using the full logical member identifier exceeds 50 characters
- **THEN** Causeway retries the display name using the logical type name without its namespace
- **AND** the full canonical identity attribute remains unchanged

#### Scenario: Namespace-free logical member name still exceeds tracing limit
- **WHEN** the namespace-free action display name still exceeds 50 characters
- **THEN** Causeway truncates the contextual name to 50 characters
- **AND** truncation does not remove or alter the full canonical identity attribute

### Requirement: Case-preserving Causeway trace export
Causeway SHALL export its semantic observation contextual names without Micrometer's lower-hyphen conversion.
The specialized observation handling SHALL apply only to Causeway's dedicated observation registry and SHALL preserve existing tracing lifecycle, parentage, tags, errors, and scope behavior.
The separate semantic trace-display-name mechanism SHALL restrict automatic-span renaming to the selected Java-agent-created foreground HTTP entry span; naming of other automatic Java-agent spans SHALL remain unchanged.

#### Scenario: Logical identifier contains uppercase characters
- **WHEN** a Causeway contextual name contains a logical identifier such as `isisExtSecMan.ApplicationUser`
- **THEN** the exported span name retains that exact identifier casing
- **AND** automatic Java-agent JDBC, outbound HTTP, and non-selected server span naming remains unchanged

#### Scenario: Foreground entry span receives a semantic trace name
- **WHEN** the dedicated semantic trace-display-name mechanism selects a bounded name for a Java-agent-created foreground HTTP entry span
- **THEN** Causeway updates that entry span to the selected case-preserving name
- **AND** this exception does not change naming behavior for other automatic Java-agent spans

### Requirement: Generic root-interaction naming
Causeway SHALL retain `causeway.root.interaction` as the contextual and stable observation name for the root interaction.
Semantic child spans SHALL describe action invocation, association access, page preparation, prompt rendering, and domain-object rendering rather than assigning one inferred purpose to the root.

#### Scenario: Request performs multiple semantic operations
- **WHEN** one request performs more than one semantic operation
- **THEN** the root span remains named `causeway.root.interaction`
- **AND** each instrumented semantic operation is represented by its applicable child span

