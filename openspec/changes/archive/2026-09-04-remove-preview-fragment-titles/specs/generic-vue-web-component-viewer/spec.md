## ADDED Requirements

### Requirement: Concise Vue Petclinic preview parity

The Vue Petclinic application SHALL match the HTMX reference by retaining each collection row's authoritative title and canonical object link while omitting a repeated `<cw-object-header>` from expanded preview content.
Its title-free inline previews MUST preserve the same useful details, actions, nested collections, hydrated context, accessible labels, and lifecycle semantics as the HTMX composition.

#### Scenario: Vue Home preview expands

- **WHEN** an Owner or Visit row preview expands on the Vue Home page
- **THEN** the live preview contains no `<cw-object-header>` and does not repeat the row title
- **AND** the selected Owner or Visit details remain available

#### Scenario: Vue PetOwner preview expands

- **WHEN** a Pet or Visit row preview expands on the Vue PetOwner page
- **THEN** the live preview contains no repeated object header
- **AND** its selected properties, actions, or nested collection remain available under the authoritative row context

#### Scenario: Vue route page renders

- **WHEN** an exact Vue object route renders outside a collection preview
- **THEN** its page-level `<cw-object-header>` remains present
- **AND** removing preview titles does not weaken route identity or application navigation
