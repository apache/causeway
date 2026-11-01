## MODIFIED Requirements

### Requirement: Semantic object breadcrumbs
The component library SHALL provide a framework-neutral `<cw-breadcrumbs>` custom element that consumes the nearest object context's semantic breadcrumbs requirement.
When valid navigable ancestors exist, it SHALL render the ancestor chain followed by the current object.
When no valid navigable ancestors exist in ready state, it SHALL omit breadcrumb presentation.

#### Scenario: Object has multiple ancestors
- **WHEN** `<cw-breadcrumbs>` receives root-to-parent entries and current-object metadata
- **THEN** it renders each ancestor as a semantic `<cw-object-link>` in the supplied order
- **AND** renders the escaped current title as the final non-link item marked `aria-current="page"`

#### Scenario: Object has no ancestors
- **WHEN** the breadcrumb list is empty and current-object metadata is available
- **THEN** the component host is hidden and contains no breadcrumb landmark or current-only item

#### Scenario: Ancestor entry is malformed
- **WHEN** an entry lacks a logical type name, identifier, or title
- **THEN** the component omits that entry defensively
- **AND** does not construct a partial navigation target
- **AND** hides ready-state breadcrumb presentation when no valid ancestor remains

### Requirement: Accessible breadcrumb presentation
When one or more valid ancestors exist, `<cw-breadcrumbs>` SHALL render keyboard-operable, escaped, responsive light-DOM markup with a `nav` landmark labelled `Breadcrumb`, an ordered list, documented semantic classes, and ordinary link focus behavior.
A ready root-only state MUST NOT expose an empty or current-only breadcrumb landmark to the accessibility tree.

#### Scenario: Assistive technology inspects descendant breadcrumbs
- **WHEN** breadcrumb state is ready with at least one valid ancestor
- **THEN** the navigation landmark and list communicate hierarchy order
- **AND** exactly one final current item exposes `aria-current="page"`

#### Scenario: Assistive technology inspects a root object
- **WHEN** breadcrumb state is ready with no valid ancestors
- **THEN** no breadcrumb navigation landmark or current item is exposed

#### Scenario: Breadcrumb title is long or contains markup-like text
- **WHEN** an ancestor or current title contains long text or HTML-significant characters
- **THEN** the title is escaped as text and wraps without causing horizontal page overflow
