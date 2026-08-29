## ADDED Requirements

### Requirement: Semantic object breadcrumbs
The component library SHALL provide a framework-neutral `<cw-breadcrumbs>` custom element that consumes the nearest object context's semantic breadcrumbs requirement and renders the navigable ancestor chain followed by the current object.

#### Scenario: Object has multiple ancestors
- **WHEN** `<cw-breadcrumbs>` receives root-to-parent entries and current-object metadata
- **THEN** it renders each ancestor as a semantic `<cw-object-link>` in the supplied order
- **AND** renders the escaped current title as the final non-link item marked `aria-current="page"`

#### Scenario: Object has no ancestors
- **WHEN** the breadcrumb list is empty and current-object metadata is available
- **THEN** the component renders a stable breadcrumb landmark containing only the current item

#### Scenario: Ancestor entry is malformed
- **WHEN** an entry lacks a logical type name, identifier, or title
- **THEN** the component omits that entry defensively
- **AND** does not construct a partial navigation target

### Requirement: Accessible breadcrumb presentation
`<cw-breadcrumbs>` SHALL render keyboard-operable, escaped, responsive light-DOM markup with a `nav` landmark labelled `Breadcrumb`, an ordered list, documented semantic classes, and ordinary link focus behavior.

#### Scenario: Assistive technology inspects breadcrumbs
- **WHEN** breadcrumb state is ready
- **THEN** the navigation landmark and list communicate hierarchy order
- **AND** exactly one final current item exposes `aria-current="page"`

#### Scenario: Breadcrumb title is long or contains markup-like text
- **WHEN** an ancestor or current title contains long text or HTML-significant characters
- **THEN** the title is escaped as text and wraps without causing horizontal page overflow

### Requirement: Existing semantic navigation ownership
Ancestor activation SHALL use the existing bubbling and composed `causeway-navigation-request` contract produced by `<cw-object-link>` and SHALL NOT impose a URL, router, HTMX, or application callback API.

#### Scenario: User activates an ancestor
- **WHEN** the user follows an ancestor breadcrumb by pointer or keyboard
- **THEN** one semantic navigation event identifies that ancestor's logical type, id, and title
- **AND** the host remains responsible for route handling

### Requirement: Local breadcrumb states
`<cw-breadcrumbs>` SHALL render loading, unsupported, partial-error, and terminal-error states locally without suppressing successful sibling components.

#### Scenario: Breadcrumb traversal is loading
- **WHEN** the semantic requirement is awaiting schema or object data
- **THEN** the component exposes a bounded busy status within its own landmark

#### Scenario: Breadcrumb traversal fails
- **WHEN** the requirement reports unsupported, partial-error, or terminal-error state
- **THEN** the component renders a bounded accessible local diagnostic
- **AND** does not render stale ancestor links

### Requirement: Public breadcrumb component contract
The component registry, ECMAScript exports, semantic element-name constants, stylesheet surfaces, usage documentation, fixtures, and automation hooks SHALL include `<cw-breadcrumbs>` without changing existing component contracts.

#### Scenario: Application uses plain HTML
- **WHEN** an application imports the standard foundation entry module and writes `<cw-breadcrumbs>` beneath an object context
- **THEN** the element is registered and operates without a frontend framework or direct GraphQL document construction
