## MODIFIED Requirements

### Requirement: Declarative accessible tab layout

The foundation SHALL register `<cw-tabgroup>` and `<cw-tab>` as framework-neutral accessible tab containers.
A tab group MUST accept only direct tab children, and a tab MUST accept only direct row children.
The selected tab panel SHALL use the regular contained viewer-panel surface rather than an uncontained grey page background.

#### Scenario: Tab group initializes

- **WHEN** a connected tab group contains named tabs
- **THEN** exactly one current tab has its corresponding panel visible on the regular white viewer-panel surface
- **AND** tab, tablist, tabpanel, accessible name, selection, control, and focus relationships are exposed correctly

#### Scenario: User operates tabs with keyboard or pointer

- **WHEN** the user clicks a tab or presses ArrowLeft, ArrowRight, Home, or End within the tablist
- **THEN** selection and focus move deterministically among current tabs with RTL-aware horizontal direction
- **AND** no domain request, route change, or host-framework state is invented

#### Scenario: Tab children change

- **WHEN** tabs are inserted, removed, renamed, disabled, or select an authored current tab
- **THEN** controls and panels resynchronize while retaining the current valid selection when possible
- **AND** the first available tab becomes current when the prior selection is unavailable

#### Scenario: Tab group or tab contains an unsupported child

- **WHEN** a direct child violates the tabgroup-to-tab or tab-to-row relationship
- **THEN** it is excluded from presentation and a bounded diagnostic is emitted
- **AND** valid tabs remain keyboard operable

#### Scenario: Tab panel is presented at narrow width

- **WHEN** the selected tab panel is rendered in a narrow container
- **THEN** its white contained surface and nested layout remain within the available inline size
- **AND** the tab group does not introduce horizontal document overflow

### Requirement: Authoritative metadata fieldset component

The foundation SHALL register `<cw-metadata>` as an automatic fieldset bound to the current object's effective fieldset id `metadata`.
It MUST render every ordered authoritative metadata property as an ordinary `<cw-property>` and every associated metadata action as an ordinary `<cw-action>` inside one accessible ellipsis menu attached to the Metadata fieldset heading.
It MUST NOT hard-code metadata member ids, infer membership from labels, expose raw layout resources, or render members outside the effective metadata fieldset.

#### Scenario: Effective metadata fieldset resolves

- **WHEN** the current object context supplies an authorized effective grid containing fieldset id `metadata`
- **THEN** `<cw-metadata>` renders its ordered property members in a semantic Metadata fieldset
- **AND** hidden, disabled, editing, validation, and value semantics remain authoritative to each property component

#### Scenario: Metadata fieldset has associated actions

- **WHEN** the effective metadata fieldset contains one or more associated actions
- **THEN** one compact ellipsis trigger in the Metadata heading opens a bounded menu containing those actions in authoritative order
- **AND** the trigger has an explicit accessible name, visible focus, and authoritative expanded state
- **AND** opening or invoking through the menu uses ordinary action accessibility, authorization, disabled reasons, prompts, invocation, results, and focus behavior

#### Scenario: Metadata action menu is dismissed

- **WHEN** an open metadata action menu loses its active interaction through Escape, outside activation, action completion, supersession, or disconnection
- **THEN** the menu closes predictably and obsolete menu work is retired
- **AND** focus remains valid for the current connected component generation

#### Scenario: Metadata fieldset has no actions

- **WHEN** the effective metadata fieldset contains no authorized associated actions
- **THEN** no ellipsis trigger or empty action menu is rendered
- **AND** metadata properties remain available

#### Scenario: Metadata authority is unavailable

- **WHEN** the object context, schema description, effective-grid reference, structural resource, parse result, or exact metadata fieldset is unavailable or invalid
- **THEN** the component fails closed with a bounded status or diagnostic and invents no member declarations
- **AND** sibling layout and semantic components remain usable

#### Scenario: Metadata generation is superseded

- **WHEN** object identity, effective layout, connection, or component generation changes during asynchronous metadata preparation
- **THEN** obsolete structural-resource, parse, or open-menu work is retired
- **AND** it cannot overwrite or append members into the newer metadata generation
