## ADDED Requirements

### Requirement: Distinct customizable tooltip presentation
Component-owned action, member, collection, parameter, and disabled-state tooltips SHALL use a shared high-contrast explanatory surface that is visually distinct from adjacent action controls.
The default treatment MUST use a light neutral background, dark text, a visible boundary, and stable `--causeway-*` customization variables without changing tooltip semantics or activation.

#### Scenario: Tooltip appears beside a filled action
- **WHEN** pointer or keyboard interaction reveals a tooltip adjacent to a filled action control
- **THEN** the tooltip uses the light neutral explanatory surface and dark text
- **AND** its border or elevation distinguishes it from both the page and the action control

#### Scenario: Different component tooltip families render
- **WHEN** an action description, member description, collection description, action-parameter description, or disabled reason is presented as a tooltip
- **THEN** each tooltip consumes the same background, text, border, and shadow presentation tokens
- **AND** existing content, sections, positioning, responsive bounds, pointer access, keyboard access, and accessible associations remain unchanged

#### Scenario: Application customizes tooltip presentation
- **WHEN** an application overrides the documented tooltip variables
- **THEN** component-owned tooltip background, text, border, and shadow presentation use those values
- **AND** no component markup or semantic event customization is required

#### Scenario: Optional theme is absent
- **WHEN** an application installs only the structural component styles
- **THEN** equivalent high-contrast light tooltip fallback values remain effective
- **AND** tooltip content remains readable and visually bounded
