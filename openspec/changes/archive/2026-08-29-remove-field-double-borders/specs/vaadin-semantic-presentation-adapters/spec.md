## ADDED Requirements

### Requirement: Single toolkit field boundary
A toolkit-backed read-only field SHALL present one coherent toolkit-owned control boundary and MUST NOT inherit native-control border, padding, sizing, focus, or resize chrome on its slotted internal form element.
The internal control MUST retain genuine read-only semantics, accessible relationships, responsive sizing, theme variables, visible focus, and forced-colors behavior.

#### Scenario: Read-only text field is rendered
- **WHEN** an eligible string property uses the Vaadin read-only field adapter
- **THEN** the field presents one visible bordered rectangle around its value
- **AND** no separately bordered native input appears inside that rectangle

#### Scenario: Read-only multiline, choice, numeric, or temporal field is rendered
- **WHEN** another qualified field family creates a native form element in its reviewed internal input slot
- **THEN** global native-control rules do not add a duplicate nested boundary or padding
- **AND** the toolkit retains ownership of that field's read-only presentation

#### Scenario: Native control is rendered outside a toolkit field
- **WHEN** a Causeway fallback, editor, prompt, shell, or application control is not a toolkit-owned slotted internal input
- **THEN** the established native theme border, sizing, padding, and focus rules continue to apply

#### Scenario: Field receives keyboard focus or forced-colors presentation
- **WHEN** the toolkit-backed field exposes its supported focus or forced-colors state
- **THEN** its toolkit-owned visible indicator remains present and distinguishable
- **AND** the duplicate native-control chrome does not reappear
