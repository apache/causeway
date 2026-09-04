## ADDED Requirements

### Requirement: Concise Petclinic preview composition

The HTMX Petclinic application SHALL keep each collection row's authoritative title and canonical object link in the row and SHALL omit a repeated `<cw-object-header>` from the expanded preview body.
Title-free preview composition MUST preserve the selected detail members, actions, nested collections, context hydration, accessible preview label, and lifecycle behavior.

#### Scenario: Runtime-type default preview expands

- **WHEN** an Owner, Pet, or Visit row expands an authored runtime-type preview resource
- **THEN** the live preview contains no `<cw-object-header>` and does not repeat the row title
- **AND** its declared detailed properties, actions, or nested collections remain available

#### Scenario: Inline Pet preview expands

- **WHEN** the PetOwner page expands its inline Pet preview
- **THEN** the live preview begins with useful Pet details rather than a repeated object header
- **AND** editable notes, the preview action, and nested visits remain available under the selected row context

#### Scenario: Maintainer inspects preview resources and examples

- **WHEN** a maintainer inspects Petclinic preview resources, inline declarations, or the foundation usage example
- **THEN** preview bodies demonstrate title-free composition
- **AND** page-level object headers remain present where they identify the current route object
