## MODIFIED Requirements

### Requirement: Optional semantic reference widget implementation
The semantic editor registry SHALL use the qualified internal Vaadin implementation by default for eligible single-reference and multi-reference inputs without changing the public Causeway element, context, identity, validation, or event contracts.
The registry MUST retain the native editor for explicit native policy, unsupported descriptors, failed qualification gates, load failure, and rollback.

#### Scenario: Eligible single-reference editor uses the default
- **WHEN** an introspected single-reference property or action parameter is eligible and the resolved toolkit policy is Vaadin
- **THEN** the registry renders the internal control beneath the existing semantic Causeway component
- **AND** labels, required state, disabled reason, current value, choices, validation, and semantic changes remain Causeway-owned

#### Scenario: Eligible multi-reference editor uses the default
- **WHEN** an introspected member exposes supported multi-reference pending-value and validation semantics and the resolved toolkit policy is Vaadin
- **THEN** the registry renders internal tokenized reference selection with stable identities and deterministic ordering
- **AND** application markup and listeners do not require toolkit-specific tags or events

#### Scenario: Reference descriptor is unsupported
- **WHEN** a reference input lacks stable identity, authoritative list semantics, bounded choices, or another required adapter capability
- **THEN** the registry selects the native editor or presents the existing semantic unsupported state
- **AND** does not expose a partially functional raw toolkit control

#### Scenario: Native policy is explicit
- **WHEN** the resolved toolkit policy is native
- **THEN** the registry selects the native reference editor without importing Vaadin
- **AND** the public semantic contract remains identical

### Requirement: Toolkit-backed semantic scalar editors
Semantic property and action-parameter components SHALL use qualified internal toolkit adapters by default for existing scalar, bounded-choice, numeric, and local-temporal editor contracts.
Selection MUST remain introspection-driven and codec-driven and MUST preserve the same pending values, validation, GraphQL variables, and semantic events as native editors.

#### Scenario: Internal adapter is selected
- **WHEN** the resolved Vaadin policy is active and the advertised input type and codec satisfy a qualified family
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
- **WHEN** the resolved common toolkit policy is native
- **THEN** the same semantic component renders its established native editor
- **AND** public value, validation, event, focus, and submission contracts remain compatible
