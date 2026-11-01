## ADDED Requirements

### Requirement: HTMX unreferenced member composition

Application-authored HTMX page resources SHALL be permitted to place `<cw-unreferenced-properties>`, `<cw-unreferenced-collections>`, and `<cw-unreferenced-actions>` beneath valid columns in an existing object-context boundary.
They SHALL also be permitted to place `<cw-unreferenced-properties>` directly among a tabgroup's ordinary tabs for conditional non-empty Other-tab presentation.
The HTMX host MUST preserve these elements and MUST NOT calculate member inventories, inspect explicit claims, allocate destinations, generate member controls, or duplicate their interaction behavior.

#### Scenario: Exact page declares catch-all elements

- **WHEN** a valid exact HTMX page resource contains the three unreferenced member elements beneath its route object context
- **THEN** strict template validation preserves their names and bounded public attributes
- **AND** the existing context and interaction controller serve all generated ordinary member descendants

#### Scenario: Exact page declares a conditional property tab

- **WHEN** a valid exact HTMX page places `<cw-unreferenced-properties>` directly between ordinary tabs
- **THEN** strict template validation preserves that authored order
- **AND** foundation alone adds or removes the Other tab according to the catch-all allocation state

#### Scenario: Remaining members render in HTMX

- **WHEN** the current authorized object has unclaimed properties, collections, or actions
- **THEN** foundation presents the Other fieldset, collection tabs, and horizontal action group in authoritative order
- **AND** HTMX performs no fragment request, member reconstruction, or route replacement for allocation

#### Scenario: HTMX references change dynamically

- **WHEN** parser-late content, metadata resolution, fragment lifecycle, or application code connects or removes an ordinary member renderer in the same boundary
- **THEN** foundation updates catch-all allocation without duplicating the member
- **AND** the stable application shell, route focus, history, context, and interaction ownership remain unchanged

#### Scenario: HTMX boundary is replaced

- **WHEN** HTMX navigation retires the current route object context
- **THEN** unreferenced allocation, generated descendants, pending producers, and context requirements from that boundary are retired
- **AND** they cannot update the replacement route

#### Scenario: HTMX host has no remaining members

- **WHEN** all members of a catch-all kind are explicitly claimed
- **THEN** the catch-all remains inspectably empty without an empty visible container
- **AND** HTMX does not supply placeholder content
