## ADDED Requirements

### Requirement: Meaningful action-invocation display name
Causeway SHALL retain `causeway.action.invocation` as the stable observation name and SHALL assign each action-invocation observation a contextual display name derived from the action's stable logical type and logical member name.
The display name SHALL identify the invocation operation and prompted member without requiring an operator to inspect span attributes.

#### Scenario: Action identity is available
- **WHEN** Causeway invokes an action with observation active
- **THEN** the exported span display name identifies the operation as an invocation
- **AND** it contains a compact form of the action's logical member and declaring type
- **AND** the observation name remains `causeway.action.invocation`
- **AND** `causeway.action.id` retains the full canonical action identifier

### Requirement: Bounded contextual naming
Causeway SHALL derive semantic span contextual names only from bounded operation names and static metamodel identifiers.
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

#### Scenario: Contextual name exceeds tracing limit
- **WHEN** a generated contextual name exceeds the tracing integration's display-name limit
- **THEN** truncation does not remove or alter the full canonical identity attributes

### Requirement: Generic root-interaction naming
Causeway SHALL retain `causeway.root.interaction` as the contextual and stable observation name for the root interaction.
Semantic child spans SHALL describe action invocation, prompt rendering, and domain-object rendering rather than assigning one inferred purpose to the root.

#### Scenario: Request performs multiple semantic operations
- **WHEN** one request performs more than one semantic operation
- **THEN** the root span remains named `causeway.root.interaction`
- **AND** each instrumented semantic operation is represented by its applicable child span
