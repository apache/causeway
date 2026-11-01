## ADDED Requirements

### Requirement: Declarative fieldset layout component

The foundation SHALL register `<cw-fieldset>` as a framework-neutral semantic group for explicitly authored property components.
A fieldset MUST accept only direct `<cw-property>` element children, preserve their authored order and ordinary context behavior, and derive its visible group name from `name` with a bounded fallback.

#### Scenario: Application authors a fieldset

- **WHEN** `<cw-fieldset name="Identity">` contains ordered `<cw-property>` children
- **THEN** it renders one visibly named semantic group in the authored order
- **AND** each property independently consumes canonical metadata, value, editing, validation, and authorization from the surrounding object context

#### Scenario: Fieldset contains an unsupported child

- **WHEN** a direct element child is not `<cw-property>`
- **THEN** the unsupported child is excluded from presentation and a bounded layout-component diagnostic identifies the invalid relationship
- **AND** valid property siblings remain usable

### Requirement: Declarative twelve-column row layout

The foundation SHALL register `<cw-row>` and `<cw-column>` as framework-neutral layout containers.
A row MUST accept only direct column children, and a column MUST accept direct fieldset, collection, or metadata children without taking ownership of their domain behavior.

#### Scenario: Row contains columns

- **WHEN** a row contains valid columns whose normalized spans total no more than twelve
- **THEN** a wide presentation places them on a twelve-track grid according to authored order and span
- **AND** a narrow presentation stacks them in authored order without horizontal document overflow

#### Scenario: Column declares a valid span

- **WHEN** `<cw-column span="x">` declares an integer from 1 through 12
- **THEN** it occupies exactly that many wide-layout tracks
- **AND** the authored value remains inspectable as the column contract

#### Scenario: Column span is absent or invalid

- **WHEN** span is absent
- **THEN** the column uses twelve tracks
- **WHEN** span is not an integer from 1 through 12
- **THEN** the column safely uses twelve tracks and emits a bounded diagnostic

#### Scenario: Row or column contains an unsupported child

- **WHEN** a row or column has a direct child outside its allowed element set
- **THEN** that child is excluded from layout presentation and a bounded diagnostic is emitted
- **AND** valid siblings remain connected and usable

### Requirement: Declarative accessible tab layout

The foundation SHALL register `<cw-tabgroup>` and `<cw-tab>` as framework-neutral accessible tab containers.
A tab group MUST accept only direct tab children, and a tab MUST accept only direct row children.

#### Scenario: Tab group initializes

- **WHEN** a connected tab group contains named tabs
- **THEN** exactly one current tab has its corresponding panel visible
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

### Requirement: Authoritative metadata fieldset component

The foundation SHALL register `<cw-metadata>` as an automatic fieldset bound to the current object's effective fieldset id `metadata`.
It MUST render every ordered authoritative metadata property as an ordinary `<cw-property>` and every associated metadata action as an ordinary `<cw-action>` inside one accessible **Actions** panel dropdown.
It MUST NOT hard-code metadata member ids, infer membership from labels, expose raw layout resources, or render members outside the effective metadata fieldset.

#### Scenario: Effective metadata fieldset resolves

- **WHEN** the current object context supplies an authorized effective grid containing fieldset id `metadata`
- **THEN** `<cw-metadata>` renders its ordered property members in a semantic Metadata fieldset
- **AND** hidden, disabled, editing, validation, and value semantics remain authoritative to each property component

#### Scenario: Metadata fieldset has associated actions

- **WHEN** the effective metadata fieldset contains one or more associated actions
- **THEN** one closed-by-default **Actions** panel dropdown contains those actions in authoritative order
- **AND** opening or invoking through the panel uses ordinary action accessibility, authorization, prompts, invocation, results, and focus behavior

#### Scenario: Metadata fieldset has no actions

- **WHEN** the effective metadata fieldset contains no authorized associated actions
- **THEN** no empty Actions dropdown is rendered
- **AND** metadata properties remain available

#### Scenario: Metadata authority is unavailable

- **WHEN** the object context, schema description, effective-grid reference, structural resource, parse result, or exact metadata fieldset is unavailable or invalid
- **THEN** the component fails closed with a bounded status or diagnostic and invents no member declarations
- **AND** sibling layout and semantic components remain usable

#### Scenario: Metadata generation is superseded

- **WHEN** object identity, effective layout, connection, or component generation changes during asynchronous metadata preparation
- **THEN** obsolete structural-resource or parse work is retired
- **AND** it cannot overwrite or append members into the newer metadata generation

### Requirement: Layout component diagnostics and host neutrality

Declarative layout components SHALL publish one bounded composed `causeway-layout-component-diagnostic` event family for invalid nesting, attributes, and unavailable metadata composition.
HTMX, Vue, and other hosts MUST receive equivalent behavior without owning layout validation, tab state, metadata selection, or action grouping.

#### Scenario: Host uses layout components

- **WHEN** equivalent declarations are connected beneath an application-owned object context in different hosts
- **THEN** foundation owns structure, responsive spans, tabs, diagnostics, metadata loading, and semantic member composition
- **AND** the host continues to own only its established shell, context, interaction controller, routing, and policy boundaries
