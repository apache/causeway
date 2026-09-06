## MODIFIED Requirements

### Requirement: Declarative twelve-column row layout

The foundation SHALL register `<cw-row>` and `<cw-column>` as framework-neutral layout containers.
A row MUST accept only direct column children, and a column MUST accept direct fieldset, collection, metadata, or tabgroup children without taking ownership of their domain behavior.
A column SHALL stack multiple valid children in authored order with shared bounded layout spacing.

#### Scenario: Row contains columns

- **WHEN** a row contains valid columns whose normalized spans total no more than twelve
- **THEN** a wide presentation places them on a twelve-track grid according to authored order and span
- **AND** a narrow presentation stacks them in authored order without horizontal document overflow

#### Scenario: Column declares a valid span

- **WHEN** `<cw-column span="x">` declares an integer from 1 through 12
- **THEN** it occupies exactly that many wide-layout tracks
- **AND** the authored value remains inspectable as the column contract

#### Scenario: Column contains multiple layout children

- **WHEN** a column directly contains fieldsets, collections, metadata, or tabgroups
- **THEN** it presents them vertically in authored order with the shared layout gap and start alignment
- **AND** each child retains ownership of its semantic and domain behavior

#### Scenario: Column contains a tab group

- **WHEN** a column directly contains `<cw-tabgroup>`
- **THEN** the tab group remains a valid connected child with its existing tab, panel, focus, mutation, and responsive behavior
- **AND** the column does not translate or reconstruct the tab structure

#### Scenario: Column span is absent or invalid

- **WHEN** span is absent
- **THEN** the column uses twelve tracks
- **WHEN** span is not an integer from 1 through 12
- **THEN** the column safely uses twelve tracks and emits a bounded diagnostic

#### Scenario: Row or column contains an unsupported child

- **WHEN** a row or column has a direct child outside its allowed element set
- **THEN** that child is excluded from layout presentation and a bounded diagnostic is emitted
- **AND** valid siblings remain connected and usable
