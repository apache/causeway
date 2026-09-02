## ADDED Requirements

### Requirement: Declarative action collection-result presentation
A `<cw-action>` SHALL accept at most one direct-child `<cw-standalone-collection>` as an inert presentation declaration for collection-valued action results.
The declaration MUST NOT become the live result node, invoke the action, alter parameters, bypass validation, or acquire object-member ownership.

#### Scenario: Action declares collection presentation
- **WHEN** an authored collection-valued action contains one direct-child standalone collection with supported attributes and columns
- **THEN** the action captures an immutable result-presentation snapshot before invocation
- **AND** the declaration and its column nodes remain hidden, connected, identity-stable, and reusable after completion

#### Scenario: Inline declaration overrides the type default
- **WHEN** both a valid direct-child declaration and a default presentation for the declared result element logical type are available
- **THEN** the complete inline declaration is selected for that invocation
- **AND** default and inline columns are not merged implicitly

#### Scenario: Inline declaration has no columns
- **WHEN** a valid direct-child declaration contains no collection columns
- **THEN** its authored standalone heading and description presentation remain effective
- **AND** rows use generic identity or scalar presentation without inheriting default columns

#### Scenario: Action declaration is rerendered
- **WHEN** action state, metadata, toolkit presentation, or surrounding composition rerenders the action
- **THEN** the same parser-authored standalone declaration and column nodes remain registered exactly once
- **AND** no idle result presentation becomes visible inside the action

#### Scenario: Declaration is duplicated or inapplicable
- **WHEN** an action has multiple direct-child standalone declarations or its authoritative result is not a collection
- **THEN** the invalid declaration cannot alter invocation or result selection
- **AND** a deterministic diagnostic and safe generic fallback remain available

### Requirement: Pre-invocation collection-result projection
The interaction lifecycle SHALL resolve the current collection-result presentation before invoking an action and SHALL include valid declared result columns in the original GraphQL action-result selection.
Canonical schema, authorization, action validation, and returned GraphQL values MUST remain authoritative.

#### Scenario: Declared object columns are supported
- **WHEN** the effective presentation declares members advertised on the authoritative concrete collection element type
- **THEN** invocation selects `_meta` identity and the bounded property-wrapper fields needed by those columns
- **AND** the completed result can render them without another GraphQL request

#### Scenario: Declared column is unavailable
- **WHEN** a column is unknown, malformed, unsupported, abstract-incompatible, or unavailable in the authoritative result schema
- **THEN** it is omitted from the invocation selection and receives bounded diagnostic or unavailable presentation
- **AND** invocation cannot infer, fabricate, or hydrate the missing value

#### Scenario: Result is empty
- **WHEN** an action returns an empty collection
- **THEN** presentation resolution and selection use the declared result element type rather than a returned row
- **AND** the live component renders its accessible empty state with the resolved heading presentation

#### Scenario: Presentation resolution is superseded
- **WHEN** a newer action, route, presentation, or interaction generation supersedes asynchronous fragment resolution
- **THEN** obsolete resolution cannot invoke the action or alter the current invocation selection
- **AND** pending parameter values and established cancellation behavior remain bounded to their current interaction

#### Scenario: Invocation completes
- **WHEN** a selected collection presentation shaped a successful invocation
- **THEN** the action-result semantic detail carries the same immutable presentation snapshot additively
- **AND** later fragment or declaration changes cannot reinterpret that completed result generation

### Requirement: Generic action-result outlet
The component library SHALL provide `<cw-action-results>` as a passive accessible outlet for host-owned action-result presentation.
The outlet MUST NOT globally subscribe to action results, invoke actions, choose application policy, navigate, resolve fragments, or independently construct domain presentation.

#### Scenario: Outlet is empty
- **WHEN** no current host-owned result presentation is mounted
- **THEN** `<cw-action-results>` contributes no misleading result content, focus target, or occupied layout space
- **AND** it remains available as a semantic placement boundary

#### Scenario: Host mounts a result
- **WHEN** the owning viewer places a current scalar, void-status, or standalone collection presentation into the outlet
- **THEN** the outlet exposes an accessible result-region relationship for that content
- **AND** result rendering, announcements, links, values, and lifecycle remain owned by their established host and semantic components

#### Scenario: Result is replaced
- **WHEN** the host accepts a newer successful result generation
- **THEN** prior result nodes and transient toolkit state are retired before the newer presentation becomes current
- **AND** stale nodes cannot remain interactive or overwrite the replacement

#### Scenario: Outlet disconnects
- **WHEN** route replacement or page lifecycle disconnects the outlet
- **THEN** its mounted presentation cannot receive later asynchronous state
- **AND** the viewer remains free to use its current deterministic fallback destination

## MODIFIED Requirements

### Requirement: Standalone collection presentation contract
`<cw-standalone-collection>` SHALL provide accessible collection heading and row presentation using public Causeway attributes, direct-child column declarations, semantic events, styles, and native fallback.
A live standalone component MUST NOT change its supplied result, issue a GraphQL request, infer absent values, or imply collection-member capabilities, while an owning interaction host MAY use an immutable pre-invocation declaration snapshot to select authoritative action-result fields.

#### Scenario: Heading presentation is authored
- **WHEN** `named`, `described-as`, or `description-as` is configured
- **THEN** the component renders the effective heading and accessible description using established collection presentation semantics
- **AND** redundant description text is omitted consistently

#### Scenario: Columns are declared before upgrade
- **WHEN** direct-child `<cw-collection-column>` elements exist before custom-element registration
- **THEN** their declaration order, labels, test identifiers, and node identity are preserved after upgrade and result rendering
- **AND** no rerender duplicates or discards the declarations

#### Scenario: Declared values are present
- **WHEN** object rows contain authoritative wrappers selected into the original action result for declared column members
- **THEN** native table cells use the established property-wrapper and value-renderer semantics
- **AND** hidden, disabled, null, error, and supported scalar values remain bounded to the supplied payload

#### Scenario: Declared value is absent
- **WHEN** a declared column value was not selected into a returned row
- **THEN** the cell exposes an explicit unavailable presentation
- **AND** the live component does not issue a follow-up read or infer the value

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
