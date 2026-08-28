## MODIFIED Requirements

### Requirement: Shared GraphQL client provider
The `<cw-graphql-client>` element SHALL provide endpoint, execution, cancellation, schema-name, and schema-cache services to descendant Causeway contexts.

#### Scenario: Several object contexts share a client
- **WHEN** several object contexts are descendants of one GraphQL client provider
- **THEN** they share the provider's executor and schema-description cache
- **AND** retain independent object snapshots and active read projections

### Requirement: Semantic object context
The `<cw-object-context>` element SHALL represent one domain object using its logical type name and identifier and SHALL expose a semantic context API to descendant components.

#### Scenario: Descendant requests context
- **WHEN** a descendant dispatches the standard bubbling and composed context-request event
- **THEN** the nearest object context supplies its semantic context API

#### Scenario: Nested object contexts
- **WHEN** a component is nested beneath more than one object context
- **THEN** its context request resolves to the nearest object context

#### Scenario: Missing object identity
- **WHEN** an object context lacks a logical type name or required object identifier
- **THEN** it enters a diagnostic error state without issuing an object query

### Requirement: Minimal context-validation components
The foundation SHALL provide a minimal object-header component and scalar read-only property component that consume the semantic object context.

#### Scenario: Object header rendering
- **WHEN** `<cw-object-header>` is connected beneath a ready object context
- **THEN** it renders the object title and semantic identity obtained through the context

#### Scenario: Visible scalar property
- **WHEN** `<cw-property>` identifies a visible scalar property
- **THEN** it renders the current value supplied by the context

#### Scenario: Hidden property
- **WHEN** the rich schema reports that a requested property is hidden for the current object and user
- **THEN** the property component does not render the property value
