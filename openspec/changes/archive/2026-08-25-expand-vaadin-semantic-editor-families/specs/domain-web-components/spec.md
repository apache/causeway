## ADDED Requirements

### Requirement: Toolkit-backed semantic scalar editors
Semantic property and action-parameter components SHALL permit qualified internal toolkit adapters for existing scalar, bounded-choice, numeric, and local-temporal editor contracts.
Selection MUST remain introspection-driven and codec-driven and MUST preserve the same pending values, validation, GraphQL variables, and semantic events as native editors.

#### Scenario: Internal adapter is selected
- **WHEN** an explicit viewer policy enables the semantic family and the advertised input type and codec satisfy its qualification rules
- **THEN** the editor registry renders a Causeway-owned adapter element
- **AND** no application component, event listener, or GraphQL context depends on a toolkit-specific tag or event

#### Scenario: Property input rerenders during validation
- **WHEN** a toolkit-backed text input causes debounced validation and its host rerenders
- **THEN** the pending value and recoverable text selection or focus remain Causeway-owned
- **AND** stale upgrade work from the replaced control cannot alter current state

#### Scenario: Action parameter changes dependencies
- **WHEN** a toolkit-backed parameter changes and later parameters depend on it
- **THEN** the existing interaction controller invalidates and prepares affected parameters
- **AND** toolkit state cannot preserve an obsolete dependent value

#### Scenario: Native editor is explicitly requested
- **WHEN** field-family policy is empty or excludes the member's family
- **THEN** the same semantic component renders its established native editor
- **AND** public value, validation, event, focus, and submission contracts remain compatible

### Requirement: Protected toolkit adapter boundary
A toolkit-backed protected editor SHALL use the existing sensitive codec and SHALL keep both prior and pending protected values outside observable presentation and diagnostics.

#### Scenario: Protected editor is prepared
- **WHEN** a protected property or action parameter uses an internal field adapter
- **THEN** the control receives no prior value and markup contains no protected value
- **AND** semantic interaction state exposes no value

#### Scenario: Protected operation fails
- **WHEN** parsing, validation, module loading, or GraphQL submission fails for protected input
- **THEN** the bounded error contains no protected value or serialized operation variables
- **AND** native fallback also initializes without the value
