## ADDED Requirements

### Requirement: Vue Petclinic owner agreement parity

The executable Vue Petclinic application SHALL present the same deterministic pet-owner clinic agreement, secondary-column relationship, suppressed property label, compact reader chrome, and responsive behavior as the authoritative HTMX PetOwner page.
Vue wrapper markup MUST NOT duplicate or override foundation PDF navigation or resource-link behavior.

#### Scenario: Vue owner agreement is presented at a wide viewport

- **WHEN** the deterministic PetOwner route is rendered by Vue at the documented wide viewport
- **THEN** the Agreement card follows Pets and Visits in the secondary side column
- **AND** the property label is suppressed while the Agreement heading and full-width foundation reader remain available
- **AND** the toolbar contains the persistent resource link without a visible canvas-disclaimer sentence

#### Scenario: Vue owner agreement is presented at a narrow viewport

- **WHEN** the Vue PetOwner route crosses the established narrow-layout breakpoint
- **THEN** the Agreement card stacks in the same semantic order as HTMX
- **AND** its toolbar, resource link, controls, and viewport remain operable without horizontal page overflow

#### Scenario: Vue user navigates agreement pages

- **WHEN** browser automation activates next and previous controls in the Vue-hosted agreement reader
- **THEN** navigation scrolls only the foundation-owned PDF viewport and leaves the host document position stable
- **AND** focus, page status, progressive rendering, and resource authorization remain equivalent to HTMX
