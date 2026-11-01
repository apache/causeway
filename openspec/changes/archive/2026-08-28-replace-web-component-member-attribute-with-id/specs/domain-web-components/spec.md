## ADDED Requirements

### Requirement: Native domain member identifier
`<cw-property>`, `<cw-action>`, `<cw-collection>`, and `<cw-collection-column>` SHALL identify their represented domain member through the standard HTML `id` attribute and its native reflected `id` property.
They MUST NOT read, observe, reflect, or alias a `member` attribute or custom `member` element property.
Internal GraphQL descriptors, context requirements, layout plans, semantic payloads, and collection-column configuration MAY continue to use `member` as domain terminology.

#### Scenario: Authored component identifies a domain member
- **WHEN** authored HTML contains `<cw-property id="firstName">`
- **THEN** the property resolves and renders the `firstName` domain property
- **AND** its native `id` property equals `firstName`

#### Scenario: Generated component identifies a domain member
- **WHEN** a fallback or effective-grid layout generates a property, action, or collection component
- **THEN** the generated host's `id` equals its represented Causeway member identifier
- **AND** no `member` attribute is emitted

#### Scenario: Declarative collection column identifies a projected member
- **WHEN** a collection directly contains `<cw-collection-column id="name">`
- **THEN** the collection projects the `name` field using its established internal column configuration
- **AND** the column host exposes `name` through its native `id` property

#### Scenario: Identifier changes while connected
- **WHEN** application code changes the native `id` of a connected property, action, or collection
- **THEN** the component reconnects its context requirement for the new domain member
- **AND** stale state for the former identifier cannot become current

#### Scenario: Former member attribute is present
- **WHEN** a member-bearing component has `member="firstName"` but no `id`
- **THEN** it does not resolve `firstName` through that obsolete attribute
- **AND** no custom `member` element property supplies a compatibility alias

## MODIFIED Requirements

### Requirement: Declarative member-associated action composition
The domain components SHALL treat each direct `<cw-action>` child of `<cw-property>` or `<cw-collection>` as an ordered presentation association with that owner member.

#### Scenario: Property declares an associated action
- **WHEN** authored HTML places `<cw-action id="updateName">` directly beneath `<cw-property id="name">`
- **THEN** the property renders its primary presentation followed by the `updateName` action in one member composition
- **AND** no adjacent association attribute, wrapper, grid resource, or Java renderer is required

#### Scenario: Collection declares associated actions and columns
- **WHEN** a collection directly contains collection-column and action declarations
- **THEN** column declarations contribute only to row projection and table presentation
- **AND** action declarations contribute only to the associated-action presentation in their source order

#### Scenario: Declaration is not a direct child
- **WHEN** an action is nested inside an arbitrary descendant wrapper rather than directly beneath the property or collection
- **THEN** the owner does not claim it as an associated-action declaration
- **AND** does not infer association from naming, proximity, or descendant scanning

#### Scenario: Parser completes children after owner connection
- **WHEN** the HTML parser or application appends a direct action declaration after the owner custom element has connected
- **THEN** the owner recognizes that direct declaration deterministically
- **AND** does not duplicate or reorder existing declarations
