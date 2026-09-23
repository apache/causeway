## ADDED Requirements

### Requirement: Meaningful action-invocation display name
Causeway SHALL retain `causeway.action.invocation` as the stable observation name and SHALL assign each action-invocation observation the contextual display name `invoke <logical-member-identifier>`.
The logical member identifier SHALL be the domain-facing identity of the invoked action, including for actions implemented by mixins.

#### Scenario: Declared action identity is available
- **WHEN** Causeway invokes a declared action with observation active
- **THEN** the exported span display name begins with `invoke ` and contains the action's logical type name and member id separated by `#`
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

### Requirement: Canonical identity remains authoritative
Causeway SHALL retain full canonical logical identifiers as span attributes when it uses compact or truncated contextual display names.
Operators SHALL be able to use those attributes to disambiguate equal display names and to identify spans whose contextual names were truncated by the tracing integration.

#### Scenario: Compact names collide
- **WHEN** two logical actions have the same compact type and member display name but different canonical logical identities
- **THEN** their contextual names MAY be equal
- **AND** their `causeway.action.id` attributes distinguish the actions

#### Scenario: Full logical member name exceeds tracing limit
- **WHEN** `invoke <logical-member-identifier>` exceeds 50 characters
- **THEN** Causeway retries the display name using the logical type name without its namespace
- **AND** the full canonical identity attribute remains unchanged

#### Scenario: Namespace-free logical member name still exceeds tracing limit
- **WHEN** the namespace-free action display name still exceeds 50 characters
- **THEN** Causeway truncates the contextual name to 50 characters
- **AND** truncation does not remove or alter the full canonical identity attribute

### Requirement: Case-preserving Causeway trace export
Causeway SHALL export its semantic observation contextual names without Micrometer's lower-hyphen conversion.
The specialized handling SHALL apply only to Causeway's dedicated observation registry and SHALL preserve existing tracing lifecycle, parentage, tags, errors, and scope behavior.

#### Scenario: Logical identifier contains uppercase characters
- **WHEN** a Causeway contextual name contains a logical identifier such as `isisExtSecMan.ApplicationUser`
- **THEN** the exported span name retains that exact identifier casing
- **AND** automatic Java-agent HTTP and JDBC span naming remains unchanged

### Requirement: Generic root-interaction naming
Causeway SHALL retain `causeway.root.interaction` as the contextual and stable observation name for the root interaction.
Semantic child spans SHALL describe action invocation, prompt rendering, and domain-object rendering rather than assigning one inferred purpose to the root.

#### Scenario: Request performs multiple semantic operations
- **WHEN** one request performs more than one semantic operation
- **THEN** the root span remains named `causeway.root.interaction`
- **AND** each instrumented semantic operation is represented by its applicable child span
