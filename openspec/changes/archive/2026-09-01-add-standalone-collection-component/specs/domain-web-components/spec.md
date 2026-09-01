## ADDED Requirements

### Requirement: Standalone action-result collection component
The component library SHALL provide `<cw-standalone-collection>` to present one normalized collection-valued action invocation result without requiring an object context or collection member.
The component MUST remain presentation-only and MUST NOT invoke an action, load a collection member, issue a GraphQL request, hydrate rows, or fabricate values absent from the supplied result.

#### Scenario: Collection result is assigned
- **WHEN** a host assigns `{kind: "collection", value: [...]}` through the component's `result` JavaScript property
- **THEN** the component snapshots the array container and renders the supplied finite result
- **AND** the structured result is not reflected into an HTML attribute or serialized into light DOM

#### Scenario: Result is replaced
- **WHEN** a later normalized collection result is assigned
- **THEN** the component advances its generation and replaces the prior rows atomically
- **AND** obsolete Grid, focus, and row presentation state cannot replace the newer result

#### Scenario: Result contains domain objects
- **WHEN** a returned row contains authoritative `_meta.logicalTypeName`, `_meta.id`, title, and optional icon
- **THEN** the row renders through `<cw-object-link>` using exactly that advertised identity and presentation metadata
- **AND** activating the link publishes the established semantic navigation request

#### Scenario: Result contains scalar values
- **WHEN** returned rows are scalar values supported by the standard value-renderer registry
- **THEN** each value is rendered through standard semantic value presentation
- **AND** no object identity or navigation behavior is invented

#### Scenario: Result is empty
- **WHEN** the normalized collection value is an empty array
- **THEN** the component renders an accessible `No items` state
- **AND** it does not render a pager, range loader, or fabricated total

#### Scenario: Assigned value is not a normalized collection result
- **WHEN** the result is null, malformed, non-collection, or has a non-array value
- **THEN** the component exposes a bounded idle or unsupported state without throwing an uncaught error
- **AND** no prior rows remain presented as the current result

### Requirement: Standalone collection presentation contract
`<cw-standalone-collection>` SHALL provide accessible collection heading and row presentation using public Causeway attributes, direct-child column declarations, semantic events, styles, and native fallback.
Presentation declarations MUST NOT change the supplied result, request absent fields, or imply collection-member capabilities.

#### Scenario: Heading presentation is authored
- **WHEN** `named`, `described-as`, or `description-as` is configured
- **THEN** the component renders the effective heading and accessible description using established collection presentation semantics
- **AND** redundant description text is omitted consistently

#### Scenario: Columns are declared before upgrade
- **WHEN** direct-child `<cw-collection-column>` elements exist before custom-element registration
- **THEN** their declaration order, labels, test identifiers, and node identity are preserved after upgrade and result rendering
- **AND** no rerender duplicates or discards the declarations

#### Scenario: Declared values are present
- **WHEN** object rows already contain wrappers for declared column members
- **THEN** native table cells use the established property-wrapper and value-renderer semantics
- **AND** hidden, disabled, null, error, and supported scalar values remain bounded to the supplied payload

#### Scenario: Declared value is absent
- **WHEN** a declared column value was not selected into a returned row
- **THEN** the cell exposes an explicit unavailable presentation
- **AND** the component does not issue a follow-up read or infer the value

#### Scenario: Member-only controls are authored
- **WHEN** paging, sorting, filtering, activation, or an identifier is placed on the standalone component
- **THEN** those unsupported member semantics do not create controls or GraphQL behavior
- **AND** the finite supplied result remains the only row authority

#### Scenario: Collection state changes
- **WHEN** the result becomes ready, empty, unsupported, or is replaced
- **THEN** the component publishes the established collection-state semantic event with bounded state detail
- **AND** the event bubbles and composes for framework-neutral observation

#### Scenario: Native presentation is used
- **WHEN** Grid qualification is unavailable, disabled, narrow, or failed
- **THEN** object rows render as a semantic list or declared columns render as an accessible native table
- **AND** keyboard navigation, responsive containment, empty state, and error presentation remain operable
