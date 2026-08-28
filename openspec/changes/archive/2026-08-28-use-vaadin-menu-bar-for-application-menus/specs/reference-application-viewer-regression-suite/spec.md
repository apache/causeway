## ADDED Requirements

### Requirement: Vaadin Menu Bar application-navigation regression coverage
The pinned Reference Application SHALL provide deterministic targets for primary, secondary, tertiary, nested, labeled-section, disabled, hidden, parameterized, overflow, refresh, and action-result Menu Bar behavior.
The same targets MUST remain executable under default Vaadin and explicit native component policies.

#### Scenario: Representative hierarchy renders
- **WHEN** browser acceptance opens the authenticated Reference Application shell
- **THEN** primary, secondary, and tertiary tiers preserve menu, section, action, label, description, icon, order, visibility, usability, and disabled-reason semantics
- **AND** default mode may use qualified Menu Bar while native mode uses no Vaadin menu control or asset

#### Scenario: Enabled service action executes
- **WHEN** keyboard and pointer journeys activate representative parameterless and parameterized service actions
- **THEN** established preparation, validation, invocation, cancellation, result, refresh, and navigation outcomes occur exactly once
- **AND** raw Vaadin item or event contracts are not required by the test

#### Scenario: Disabled and hidden actions are covered
- **WHEN** authorization or preparation hides an action or marks it unusable with a reason
- **THEN** hidden actions remain absent and disabled actions remain inoperable and described in both modes
- **AND** overflow does not reveal unauthorized or duplicate actions

#### Scenario: Nested and labeled sections are covered
- **WHEN** representative menus contain nested groupings and labeled or unlabeled sections
- **THEN** qualified and native presentations preserve source order, grouping, names, descriptions, and leaf activation
- **AND** grouping nodes cannot invoke service actions

### Requirement: Menu Bar lifecycle delivery and accessibility regression coverage
The regression suite SHALL prove authenticated stable-shell delivery, family isolation, refresh, stale-result rejection, responsive behavior, focus continuity, failure recovery, security, accessibility, and complete native rollback.
It MUST fail on unexpected requests, hashes, external resources, stale items, duplicate controls, or semantic divergence.

#### Scenario: Stable-shell delivery is measured
- **WHEN** login, authenticated menu, empty-menu, unrelated route, and repeated route journeys run
- **THEN** Menu Bar requests are absent from authentication chrome, deduplicated after first qualified use, and independent of member-local closures
- **AND** explicit native mode has zero Menu Bar requests and hashes

#### Scenario: Refresh supersedes preparation and projection
- **WHEN** menu metadata, resource, or service-action state refresh overlaps adapter import, item projection, or action preparation
- **THEN** only current generation items, disabled reasons, actions, focus, and results remain active
- **AND** stale item selection cannot invoke or navigate

#### Scenario: Route or shell changes during upgrade
- **WHEN** logout, shell replacement, route replacement, disconnect, policy change, or width change occurs during Menu Bar work
- **THEN** stale controls, listeners, items, focus, and events cannot reappear
- **AND** current native or qualified shell state remains authoritative

#### Scenario: Menu Bar family failure is injected
- **WHEN** failure occurs before connection, during import, during definition, after connection, during projection, or during event translation
- **THEN** every current tier remains usable natively and a connected retry can recover from current state
- **AND** reference, field, action, Grid, GraphQL, route, and authentication families remain unaffected

#### Scenario: Responsive keyboard matrix runs
- **WHEN** wide, narrow, overflowed, zoomed, light, dark, reduced-motion, and forced-colors journeys operate menus with Tab, arrows, Enter, Space, Escape, and pointer input
- **THEN** every visible authorized action remains reachable in order with visible focus, correct names, descriptions, disabled state, dismissal, focus return, and zero page overflow
- **AND** unexpected axe, CSP, console, page, external-request, duplicate-control, stale-item, clipping, overlay, focus, or order failures fail the suite

#### Scenario: Capability inventory remains reviewed
- **WHEN** clean and incremental inventory and retained-target generation run after Menu Bar coverage is added
- **THEN** output remains byte-identical unless a reviewed additive capability change intentionally updates the baseline
- **AND** zero viewer defects and every qualified or excluded target remain discoverable
