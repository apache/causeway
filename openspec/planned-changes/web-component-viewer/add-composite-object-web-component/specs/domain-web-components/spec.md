## ADDED Requirements

### Requirement: High-level semantic object component
The component library SHALL provide `<causeway-object>` as a framework-neutral high-level projection that composes a complete object from established semantic child components.

#### Scenario: Object component connects
- **WHEN** `<causeway-object>` connects beneath an authoritative object context
- **THEN** it discovers the logical type's members through the context's targeted schema description
- **AND** composes the object's supported layout without constructing GraphQL documents itself

#### Scenario: Object context is absent
- **WHEN** the component has no usable object context
- **THEN** it presents a local actionable configuration error
- **AND** does not create an unrelated second state owner

### Requirement: Causeway grid interpretation
`<causeway-object>` SHALL interpret a documented safe subset of the effective Causeway grid resource for semantic object composition.

#### Scenario: Supported grid is available
- **WHEN** object metadata references an accessible grid containing supported rows, columns, tabs, field sets, domain-object placement, and member references
- **THEN** the component maps them into responsive semantic layout regions

#### Scenario: Grid contains unsupported content
- **WHEN** one layout region contains an unknown node or unsupported instruction
- **THEN** the component publishes a bounded diagnostic
- **AND** preserves unrelated recognized regions while applying local fallback where possible

#### Scenario: Grid resource is unsafe
- **WHEN** layout XML attempts external entity resolution or executable content
- **THEN** the parser rejects that content
- **AND** no external entity or executable markup is processed

### Requirement: Canonical fallback object layout
The component SHALL provide deterministic fallback composition modeled on Causeway's `GridFallbackLayout.xml` when no usable effective grid is available.

#### Scenario: No usable grid exists
- **WHEN** the grid is absent, forbidden, unreachable, or wholly malformed
- **THEN** the component renders header and actions followed by conventional property field-set and collection regions
- **AND** uses framework-neutral CSS Grid rather than requiring Bootstrap

#### Scenario: Conventional region is empty
- **WHEN** no visible member belongs to a fallback field set or collection region
- **THEN** the empty region collapses without leaving an unlabelled interactive container

### Requirement: Decomposition into established components
The object component SHALL generate existing semantic header, property, action, and collection elements rather than duplicate their runtime behavior.

#### Scenario: Layout references a property
- **WHEN** a valid property reference is placed
- **THEN** the generated `<causeway-property>` uses the same shared object context, editing, validation, mutation, loading, and error contracts as an authored property element

#### Scenario: Layout references an action or collection
- **WHEN** a valid action or collection reference is placed
- **THEN** the generated established component retains its standard hidden, disabled, interaction, result, lazy-read, and error semantics

### Requirement: Deterministic member allocation
Every introspected member claimed by automatic composition SHALL be placed at most once according to explicit references, unreferenced markers, and fallback policy.

#### Scenario: Explicit member is also eligible as unreferenced
- **WHEN** a member has already been claimed by an explicit layout reference
- **THEN** an unreferenced marker does not place it again

#### Scenario: Layout reference is stale or wrong-kind
- **WHEN** the layout names a missing member or uses a member in an incompatible role
- **THEN** the component records a diagnostic
- **AND** does not create a broken or duplicate semantic child

### Requirement: Accessible responsive object composition
Generated object layout SHALL provide accessible groups and tabs and SHALL adapt to narrow viewports without changing semantic member behavior.

#### Scenario: User operates a tab group by keyboard
- **WHEN** focus is within generated tabs
- **THEN** documented arrow, Home, End, Enter, and Space behavior updates the selected tab and associated panel accessibly

#### Scenario: Viewport becomes narrow
- **WHEN** the available width crosses the documented narrow-layout threshold
- **THEN** columns stack in meaningful document order
- **AND** controls retain visible focus and keyboard operation

### Requirement: Observable and customizable composition
Generated light DOM SHALL expose documented stable styling, region, lifecycle, and diagnostic hooks while preserving explicit low-level composition as an alternative.

#### Scenario: Application themes generated layout
- **WHEN** an application supplies supported CSS variables and selectors
- **THEN** it can style regions and established child components without replacing object semantics

#### Scenario: Application needs custom structure
- **WHEN** automatic layout is not suitable
- **THEN** the application can continue composing established lower-level components directly beneath the same object context
