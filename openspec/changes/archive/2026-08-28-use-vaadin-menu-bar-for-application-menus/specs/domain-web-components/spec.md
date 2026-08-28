## ADDED Requirements

### Requirement: Toolkit-backed qualified application menus
The semantic application-menu components SHALL permit an internal Menu Bar presentation only when the complete tier preserves established hierarchy, order, state, interaction, responsive, and accessibility contracts.
The public element vocabulary, semantic events, menu state, action state, and host policy MUST remain Causeway-owned.

#### Scenario: Qualified semantic tier renders
- **WHEN** a connected non-empty primary, secondary, or tertiary tier is representable under the Vaadin component policy
- **THEN** that public tier may host one internal Menu Bar without changing application markup or selectors
- **AND** menus, sections, actions, labels, descriptions, icon hints, order, visibility, usability, and disabled reasons remain equivalent

#### Scenario: Service action is selected
- **WHEN** an enabled current action item is activated by keyboard or pointer
- **THEN** the existing Causeway interaction-controller path executes its service logical type and action ID exactly once
- **AND** established validation, cancellation, result, navigation, refresh, and semantic-event policy remains authoritative

#### Scenario: Tier is empty or unqualified
- **WHEN** a tier is empty, disconnected, hidden, unsupported, stale, responsively disqualified, failed, or governed by native policy
- **THEN** it remains hidden or uses the complete established native presentation
- **AND** no approximate or mixed toolkit hierarchy is presented

#### Scenario: Three tiers qualify
- **WHEN** primary, secondary, and tertiary bars are all non-empty and qualified
- **THEN** each retains one independently identified internal control in original semantic order
- **AND** the tiers are not merged into one application-facing or internal menu tree

### Requirement: Toolkit-neutral menu adapter lifecycle
Application-menu generation, preparation, refresh, activation, result, focus, and responsive state SHALL remain valid independently of native or toolkit presentation.
Superseded presentation work MUST NOT mutate current menu state or invoke a stale action.

#### Scenario: Menu generation changes
- **WHEN** refresh, authorization, preparation, metadata, or resource loading produces a newer accepted generation
- **THEN** current connected tiers project only that generation into native or toolkit presentation
- **AND** stale items, events, focus targets, definitions, and render completions are ignored

#### Scenario: Presentation mode changes
- **WHEN** policy, width, hierarchy, family health, connection, or visibility changes presentation mode
- **THEN** the tier rebuilds from current immutable Causeway state without another semantic request
- **AND** generated controls, listeners, observers, items, and transient focus state are cleaned exactly once

#### Scenario: Disabled action remains explained
- **WHEN** current preparation marks a visible service action unusable with a bounded reason
- **THEN** both native and qualified presentation expose equivalent disabled and described semantics
- **AND** neither presentation can invoke it

#### Scenario: Family failure occurs
- **WHEN** Menu Bar loading, definition, projection, rendering, event translation, or CSP fails
- **THEN** current menu tiers remain usable through native presentation
- **AND** references, fields, actions, collections, GraphQL, routing, authentication, and menu state remain unaffected

### Requirement: Semantic menu focus continuity
Application menus SHALL track focus and dismissal by current tier and semantic menu, section, or action identity rather than toolkit DOM identity.
Focus restoration MUST remain safe when overflow, refresh, responsive switching, action completion, or fallback recreates controls.

#### Scenario: Expanded menu is dismissed
- **WHEN** the user presses Escape in a native or toolkit nested or overflow menu
- **THEN** the transient menu closes and focus returns to its current semantic origin
- **AND** other tiers remain independently scoped

#### Scenario: Focused action disappears
- **WHEN** refresh removes, hides, disables, or relocates the focused action
- **THEN** focus moves to a safe current tier or shell target
- **AND** no stale, hidden, disconnected, or toolkit-internal node regains focus
