## ADDED Requirements

### Requirement: Petclinic collection heading demonstrations
The Petclinic HTMX application SHALL selectively demonstrate canonical collection descriptions, HTML-authored collection names, and HTML-authored collection description overrides without requiring application-specific component code.

#### Scenario: Domain facet supplies a description
- **WHEN** a Petclinic collection annotated with `@CollectionLayout(describedAs)` is rendered without an HTML description override
- **THEN** its canonical description appears below the effective collection name
- **AND** collections without a description remain free of placeholder prose

#### Scenario: HTML supplies collection heading overrides
- **WHEN** a Petclinic resource page declares `named` or `described-as` on selected `<cw-collection>` elements
- **THEN** those explicit values appear for those collections only
- **AND** the same component continues using canonical metadata or fallbacks for other collections

#### Scenario: Petclinic owner visits are rendered
- **WHEN** the mixed-in owner visits collection loads
- **THEN** its selected name and description are visible and accessible
- **AND** “Cannot edit a mixed-in collection” or an equivalent collection-level tooltip is absent

### Requirement: Petclinic collection heading regression coverage
Petclinic integration and browser acceptance SHALL verify canonical and HTML-authored collection heading combinations, quiet read-only collection presentation, and unaffected collection navigation and actions.

#### Scenario: Petclinic heading combinations are exercised
- **WHEN** integration and browser tests inspect selected home and owner collections
- **THEN** names and descriptions follow documented precedence and selective application
- **AND** existing rows, links, paging, responsive behavior, add/remove/book actions, and error monitoring remain valid
