## MODIFIED Requirements

### Requirement: Optional single-row Grid details presentation

The Vaadin collection Grid adapter SHALL support an optional toolkit-neutral row-details descriptor while leaving expansion authority, effective preview templates, row contexts, and domain behavior with the owning `<cw-collection>`.
The adapter MUST keep at most one details item open and MUST preserve native fallback equivalence.
Its preview disclosure MUST use the same appropriately sized decorative direction indicator and state-driven presentation as the native collection disclosure.

#### Scenario: Grid item has an effective preview

- **WHEN** the collection projects a Grid item with a current safe preview descriptor
- **THEN** the Grid renders a compact leading disclosure control with row-title-based accessible naming
- **AND** the disclosure contains the shared non-focusable, assistive-technology-hidden SVG indicator instead of a Grid-specific text glyph
- **AND** activation delegates the selected stable row key to the owning collection

#### Scenario: Grid item has no effective preview

- **WHEN** no inline or runtime-type default template is available for a projected item
- **THEN** the item exposes no interactive preview disclosure
- **AND** Grid column, sorting, paging, and selection semantics remain unchanged

#### Scenario: Grid row details open

- **WHEN** the collection accepts a disclosure for one current Grid item
- **THEN** the adapter maps the collection-owned details renderer to Vaadin row details immediately after that item
- **AND** `aria-expanded`, `aria-controls`, and the toolkit's opened-item state identify exactly one live details subtree
- **AND** the shared visible indicator points downward in the expanded state just as it does in native presentation
- **AND** the adapter does not interpret domain members, create object contexts, or clone preview declarations independently

#### Scenario: A second Grid row is expanded

- **WHEN** another current item is selected while one details row is open
- **THEN** the adapter retires the prior rendered details destination before displaying the newer collection-owned preview
- **AND** no more than one opened item remains

#### Scenario: Grid details close

- **WHEN** the owning collection collapses through disclosure, Escape, mutation refresh, criteria change, paging, responsive replacement, or lifecycle cleanup
- **THEN** the adapter clears its opened items and details renderer state
- **AND** retired row details cannot remain focusable or interactive

#### Scenario: Virtual range introduces preview-capable rows

- **WHEN** a current virtual range resolves rows and runtime-type preview availability
- **THEN** its projected items receive disclosure only after current preview resolution settles
- **AND** obsolete ranges, evicted items, or late definitions cannot open details or retain preview contexts

#### Scenario: Details focus is restored

- **WHEN** Escape collapses a live Grid row preview
- **THEN** focus returns to the connected disclosure for that stable row key
- **AND** the adapter scrolls or requests a content update only when required to restore the current control

#### Scenario: Grid adapter fails with a peek declaration

- **WHEN** module loading, definition, adapter setup, disclosure rendering, or row-details rendering fails
- **THEN** failure remains scoped to the Grid family and established native rollback renders equivalent single-row disclosure behavior
- **AND** authoritative rows, preview availability, expanded-state ownership, and mutation refresh policy remain with the collection

#### Scenario: Wide and narrow browser qualification

- **WHEN** preview-enabled collections are exercised under Vaadin Grid, responsive native rollback, bounded paging, and virtual presentation
- **THEN** details remain keyboard-operable, width-bounded, free of horizontal document overflow, and compatible with sorting, filtering, paging, and canonical object navigation
