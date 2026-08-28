## ADDED Requirements

### Requirement: Toolkit-backed read-only scalar presentation
A `<cw-property>` whose selected standard renderer and semantic value shape are qualified SHALL use the resolved internal component toolkit for read-only presentation without changing the public property, value-renderer, accessibility, or semantic-event contract.
Application renderer precedence and established non-field renderers MUST remain authoritative.

#### Scenario: Qualified standard scalar is visible
- **WHEN** the resolved component policy is Vaadin and a visible standard text, Boolean, enum, bounded-choice, numeric, or qualified local-temporal value satisfies its read-only family contract
- **THEN** the property renders that value through the internal qualified read-only field adapter
- **AND** its Causeway label, description, disabled reason, errors, alignment, responsive layout, classes, and events remain authoritative

#### Scenario: Application renderer wins precedence
- **WHEN** an application-specific renderer is selected for an otherwise eligible scalar
- **THEN** the application renderer remains visible
- **AND** toolkit selection does not replace or wrap it with a standard Vaadin field

#### Scenario: Value is not field-qualified
- **WHEN** the selected value is protected, null, reference, resource, LOB, offset-bearing, zoned, legacy temporal, custom, collection, unsupported, or otherwise unqualified
- **THEN** its established native or application renderer remains authoritative
- **AND** the property does not display an approximate disabled or generic field

#### Scenario: Native component policy is selected
- **WHEN** the resolved component policy is native
- **THEN** the property uses its established native value renderer and interaction controls
- **AND** application markup, GraphQL state, and semantic events remain unchanged

### Requirement: Toolkit-backed ordinary action affordance
A visible ordinary `<cw-action>` SHALL use the qualified internal action control selected by the resolved component toolkit while retaining Causeway ownership of action identity, visibility, usability, descriptions, request publication, interaction, results, and focus policy.

#### Scenario: Vaadin action is enabled
- **WHEN** the resolved component policy is Vaadin and a visible enabled ordinary action renders
- **THEN** an internal qualified Vaadin Button presents the action
- **AND** keyboard or pointer activation publishes the established Causeway action request exactly once

#### Scenario: Vaadin action is disabled
- **WHEN** a visible ordinary action is disabled with a bounded reason
- **THEN** the internal action control cannot activate
- **AND** Causeway-owned accessible presentation exposes its name, description, disabled state, and reason

#### Scenario: Vaadin action is hidden
- **WHEN** an ordinary action becomes hidden while an adapter is loading or connected
- **THEN** no native or Vaadin action control remains visible, focusable, or actionable
- **AND** late adapter work cannot restore it

#### Scenario: Non-ordinary control is rendered
- **WHEN** the control is a property edit, save, cancel, clear, action-prompt, shell, or another affordance outside ordinary `<cw-action>` qualification
- **THEN** its established native control remains authoritative
- **AND** the ordinary action adapter does not broaden its scope
