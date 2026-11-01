## ADDED Requirements

### Requirement: Field-aligned property-associated actions
The component library SHALL place actions authored directly beneath a property in visual association with that property's field rather than its label while preserving semantic action behavior and light-DOM ownership.

#### Scenario: Associated action renders at wide width
- **WHEN** a property with a left-positioned label renders one or more associated actions at a wide layout width
- **THEN** each associated action begins at the logical start of the property's field column
- **AND** the label column remains free of action controls

#### Scenario: Associated action renders at narrow width
- **WHEN** responsive presentation stacks the property label and field
- **THEN** associated actions begin at the same logical start as the stacked field
- **AND** controls do not overlap, clip, or introduce horizontal page overflow

#### Scenario: Associated action is activated
- **WHEN** a user activates a field-aligned associated action
- **THEN** the established semantic action request, toolkit selection, focus, disabled state, tooltip, and invocation behavior remain unchanged
