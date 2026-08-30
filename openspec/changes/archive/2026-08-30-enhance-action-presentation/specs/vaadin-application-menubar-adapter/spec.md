## ADDED Requirements

### Requirement: Equivalent service-action tooltip composition
Native and Vaadin-backed application-menu action items SHALL present the same bounded canonical description and disabled reason tooltip sections.
A disabled reason MUST NOT replace an available description.

#### Scenario: Enabled described menu action renders
- **WHEN** a current visible service action has a canonical description and is enabled
- **THEN** its native or Vaadin-backed menu item exposes the description tooltip
- **AND** activation continues through the existing Causeway service-action path

#### Scenario: Disabled described menu action renders
- **WHEN** a current visible service action has both a canonical description and a disabled reason
- **THEN** its menu item exposes both as separate tooltip sections
- **AND** the item remains non-invoking and accessibly explained

#### Scenario: Menu presentation changes
- **WHEN** responsive, policy, recovery, or generation state replaces native or Vaadin-backed presentation
- **THEN** the current immutable composed tooltip is applied exactly once
- **AND** stale descriptions or disabled reasons cannot reappear

### Requirement: Positioned Font Awesome service-action icons
Native and Vaadin-backed application-menu action items SHALL preserve accepted static Font Awesome classes and canonical left or right position from the effective menu model.
Icons MUST remain decorative and MUST NOT change semantic action identity or label text.

#### Scenario: Menu action has a left icon
- **WHEN** the effective menu action supplies accepted icon classes and `LEFT` position
- **THEN** native and Vaadin-backed items render the decorative icon before the text
- **AND** toolkit projection retains only bounded immutable icon metadata

#### Scenario: Menu action has a right icon
- **WHEN** the effective menu action supplies accepted icon classes and `RIGHT` position
- **THEN** native and Vaadin-backed items render the decorative icon after the text
- **AND** keyboard order and activation remain unchanged

#### Scenario: Menu icon hint is malformed
- **WHEN** effective menu icon metadata contains unsupported or oversized tokens
- **THEN** the item renders without an icon
- **AND** bounded diagnostics disclose no action values or implementation details
