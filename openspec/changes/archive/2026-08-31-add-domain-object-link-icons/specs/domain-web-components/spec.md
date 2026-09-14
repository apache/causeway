## ADDED Requirements

### Requirement: Icon-bearing semantic object navigation
Every rendered domain-object navigation link SHALL use `<cw-object-link>` with the authoritative logical identity and title, and SHALL present the object's authoritative icon when the selected rich metadata supplies a non-empty icon URL.
The icon MUST be decorative, MUST NOT replace the title as the accessible link name, and MUST NOT participate in navigation identity or event payload semantics.

#### Scenario: Current object heading renders
- **WHEN** object header metadata supplies identity, title, and icon
- **THEN** the displayed title is an enabled semantic object link targeting that same identity
- **AND** the link presents the icon before the title within the single object heading
- **AND** the existing secondary identity remains available without being duplicated in the heading link presentation

#### Scenario: Object-valued property renders
- **WHEN** a readable property returns object metadata containing identity, title, and icon
- **THEN** its semantic object link presents the icon and title
- **AND** activation publishes the established navigation request for that returned object

#### Scenario: Collection row renders
- **WHEN** an object-valued collection row returns identity, title, and icon metadata
- **THEN** every list or grid identity link for that row presents the icon and title
- **AND** declared member columns and hydrated row-context behavior remain unchanged

#### Scenario: Breadcrumb ancestor renders
- **WHEN** a breadcrumb ancestor supplies an icon URL with its navigable identity and title
- **THEN** its semantic object link presents the icon and title
- **AND** breadcrumb ordering and current-object presentation remain unchanged

#### Scenario: Icon metadata is unavailable
- **WHEN** the described schema omits icon metadata, the selected icon is null or empty, or the icon resource fails to load
- **THEN** the component renders a usable title-bearing semantic object link without a broken-image affordance
- **AND** identity, keyboard activation, focus presentation, and navigation event behavior remain unchanged

#### Scenario: Link is announced accessibly
- **WHEN** assistive technology encounters an icon-bearing object link
- **THEN** the object title remains the link's accessible text
- **AND** the decorative icon contributes no duplicate or filename-derived announcement

### Requirement: Targeted object icon selection
Semantic object reads SHALL request icon metadata only when the introspected rich metadata type advertises the field and only for object values that are rendered as navigation links.

#### Scenario: Current schema advertises icon metadata
- **WHEN** header, object-valued property, concrete collection-row, or supported polymorphic result selection is planned
- **THEN** the targeted GraphQL selection includes the corresponding `_meta.icon` field
- **AND** does not broaden the operation to unrelated members

#### Scenario: Older or restricted schema omits icon metadata
- **WHEN** the described metadata type has no `icon` field
- **THEN** the client omits `icon` from the executable selection
- **AND** completes the semantic read using the available identity and title fields
