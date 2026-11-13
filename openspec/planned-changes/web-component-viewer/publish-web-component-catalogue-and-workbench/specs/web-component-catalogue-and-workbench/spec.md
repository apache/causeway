## ADDED Requirements

### Requirement: Machine-readable public component catalogue
The web-component package SHALL publish a deterministic machine-readable manifest for every public Causeway custom element.

#### Scenario: Consumer reads the manifest
- **WHEN** a developer tool loads the packaged catalogue
- **THEN** it can discover public tag names, modules, attributes, properties, events, slots, CSS custom properties, light-DOM hooks, and context requirements
- **AND** declarations correspond to supported runtime exports

#### Scenario: Public contract changes
- **WHEN** an element contract changes between releases
- **THEN** generated catalogue output changes deterministically
- **AND** compatibility follows documented package versioning

### Requirement: Interactive web-component workbench
The project SHALL provide a development workbench that renders public custom elements directly from the packaged ESM API without requiring per-element framework wrappers.

#### Scenario: Developer explores a component
- **WHEN** the developer selects a public element and semantic state
- **THEN** the workbench renders that state with documented controls and context
- **AND** exposes relevant events and light-DOM behavior

### Requirement: Representative semantic state coverage
Every public element SHALL have representative stories for each applicable lifecycle, value, interaction, result, empty, unsupported, partial-error, and terminal-error equivalence class.

#### Scenario: Interactive state is exercised
- **WHEN** a story demonstrates editing, action prompting, menu disclosure, collection activation, or another interaction
- **THEN** pointer and keyboard behavior can be exercised
- **AND** semantic events and resulting state remain observable

### Requirement: Deterministic and real-endpoint separation
Default workbench builds SHALL use deterministic synthetic fixtures, while real GraphQL integration SHALL require explicit local enablement.

#### Scenario: Static workbench is published
- **WHEN** workbench assets are built for publication
- **THEN** they contain no live endpoint credentials or mutable production data
- **AND** all network behavior is synthetic unless explicitly documented otherwise

#### Scenario: Developer enables integration mode
- **WHEN** a developer explicitly targets the local sample endpoint
- **THEN** selected stories use the established real GraphQL contract
- **AND** the workbench identifies that mode clearly

### Requirement: Workbench accessibility and responsive coverage
The workbench SHALL verify meaningful component states across keyboard operation, focus behavior, responsive widths, themes, zoom, and reduced-motion settings.

#### Scenario: Accessibility checks run
- **WHEN** automated or interactive accessibility verification executes
- **THEN** it evaluates ready and interaction states rather than only empty hosts
- **AND** reports failures against the component contract

### Requirement: Catalogue non-disclosure
Catalogue artifacts and stories SHALL NOT expose hidden domain values, credentials, authorization rules, or sensitive resource content.

#### Scenario: Sensitive fixture is represented
- **WHEN** a story needs hidden, password, authorization, or resource behavior
- **THEN** it uses synthetic redacted data
- **AND** exported metadata and diagnostics remain non-disclosing

### Requirement: No production explorer dependency
Production semantic component modules SHALL NOT import the workbench or its explorer dependencies.

#### Scenario: Application installs runtime components
- **WHEN** an application consumes the normal ESM package
- **THEN** workbench runtime code is absent from its production dependency path
