## ADDED Requirements

### Requirement: Host-owned menu-action label projection

A semantic menu host SHALL be able to supply a presentation-only label mapper for exact service-action identities before native or toolkit menu projection.
The mapper MUST preserve authoritative role, menu, section, order, identity, visibility, usability, description, icon, prompt, result, and activation semantics, and mapped text MUST use the existing bounded text-safe projection rules.
A mapper failure or invalid result MUST fail closed for the affected customization without allowing arbitrary markup or changing the action invoked.

#### Scenario: Host relabels one exact action

- **WHEN** a host label mapper recognizes one exact service logical type and action id and returns a replacement label
- **THEN** native and toolkit menu presentations show the bounded replacement in the action's authoritative menu location
- **AND** activation publishes the unchanged service logical type and action id exactly once

#### Scenario: Other menu semantics remain authoritative

- **WHEN** the mapper does not recognize an action
- **THEN** the resource-projected label and all other action and hierarchy semantics remain unchanged
- **AND** the host cannot use label mapping to reveal a hidden action, enable a disabled action, reorder hierarchy, or manufacture a menu entry

#### Scenario: Label customization is defective

- **WHEN** a mapper throws, returns an unsupported value, or supplies unsafe or overlong text
- **THEN** the affected customization is rejected or normalized through bounded text-only rules
- **AND** no executable markup, changed identity, or unintended GraphQL operation is introduced
