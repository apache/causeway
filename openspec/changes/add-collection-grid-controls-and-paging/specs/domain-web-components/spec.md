## ADDED Requirements

### Requirement: Declarative collection paging
`<cw-collection>` SHALL accept an optional `paged` attribute containing a bounded positive integer page size.
A valid override SHALL apply consistently to initial loading, previous and next navigation, refresh, and current-generation reloads.

#### Scenario: Valid page size is declared
- **WHEN** an active collection declares `paged="10"`
- **THEN** its first collection-window request starts at offset zero with size ten
- **AND** previous and next requests preserve the authoritative normalized page size

#### Scenario: Page size is absent
- **WHEN** a collection does not declare `paged`
- **THEN** established default collection loading and Grid qualification remain unchanged
- **AND** no paging controls are introduced solely by this change

#### Scenario: Page size is invalid
- **WHEN** `paged` is blank, malformed, zero, negative, or above the accepted public bound
- **THEN** the component treats it as absent
- **AND** does not issue an unbounded or invalid request from that attribute

#### Scenario: Page size changes reactively
- **WHEN** a connected collection's effective `paged` value changes
- **THEN** current loading and range generations are retired before loading offset zero with the new effective size
- **AND** stale rows, focus, errors, or paging controls cannot replace the new state

### Requirement: Declarative collection Grid controls
`<cw-collection>` SHALL accept optional `resizable-columns` and `reorderable-columns` attributes for qualified Grid presentation.
The attributes MUST NOT expose toolkit elements, events, or item objects and MUST NOT alter native fallback semantics.

#### Scenario: Resizing is enabled
- **WHEN** a qualified Grid collection declares `resizable-columns`
- **THEN** each accepted generated Grid column is resizable
- **AND** Causeway cell rendering and column selection remain authoritative

#### Scenario: Reordering is enabled
- **WHEN** a qualified Grid collection declares `reorderable-columns`
- **THEN** the current Grid permits pointer and keyboard-supported column reordering provided by the toolkit
- **AND** declarative order is restored after adapter replacement, native fallback, refresh, or navigation

#### Scenario: Controls are absent
- **WHEN** neither Grid-control attribute is declared
- **THEN** resizing and reordering remain disabled
- **AND** existing collection presentation is unchanged

#### Scenario: Native presentation is active
- **WHEN** responsive, policy, capability, or failure qualification selects native collection presentation
- **THEN** Grid-control attributes do not create toolkit controls or alter the native table or list
- **AND** later safe Grid qualification may apply the current attributes once

### Requirement: Collection sorting and filtering remain collection-wide concerns
The component SHALL NOT expose sorting or filtering behavior that applies only to a loaded window while appearing to cover the complete collection.

#### Scenario: Current server contract lacks sort and filter inputs
- **WHEN** the collection-window operation accepts offset and size but no ordering or filtering criteria
- **THEN** the component does not enable Vaadin sorting or filtering controls
- **AND** deterministic server ordering remains authoritative across windows
