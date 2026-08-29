## ADDED Requirements

### Requirement: Approved Grid column controls
The private Grid adapter SHALL map only reviewed Causeway presentation options to pinned Vaadin Grid properties.
It SHALL keep toolkit controls disabled unless the owning `<cw-collection>` explicitly opts in.

#### Scenario: Resizable columns are requested
- **WHEN** the adapter receives a presentation with column resizing enabled
- **THEN** every generated `vaadin-grid-column` has `resizable` enabled
- **AND** the setting is removed when a later presentation disables resizing

#### Scenario: Column reordering is requested
- **WHEN** the adapter receives a presentation with column reordering enabled
- **THEN** the generated `vaadin-grid` has `columnReorderingAllowed` enabled
- **AND** raw reorder events and toolkit column state remain private implementation details

#### Scenario: Adapter presentation is replaced
- **WHEN** responsive mode, policy, route generation, collection state, or attributes replace the current adapter presentation
- **THEN** the current Causeway options are applied exactly once to the current Grid and columns
- **AND** stale toolkit controls cannot reapply a previous option set

### Requirement: Explicit paging selects bounded Grid mode
A valid Causeway paging override SHALL select bounded Grid presentation and SHALL retain Causeway-owned previous and next controls.
Vaadin Grid MUST NOT create an independent paging state machine.

#### Scenario: Paged collection has a safe total
- **WHEN** a qualified collection declares a valid page size and the window reports a stable total
- **THEN** the adapter receives only the current bounded window as items
- **AND** Causeway-owned paging controls navigate normalized server offsets

#### Scenario: Unpaged collection has a safe total
- **WHEN** a qualified collection has no page-size override and reports a stable total
- **THEN** established virtual Grid qualification remains available
- **AND** the existing bounded range broker remains authoritative

### Requirement: Unsupported sort and filter hints are not advertised
The Grid adapter SHALL NOT enable sorter or filter controls until the Causeway range provider can apply their criteria across the complete collection.

#### Scenario: Adapter creates columns under the current range contract
- **WHEN** generated columns target an offset-and-size-only range provider
- **THEN** no sorter or filter element is attached
- **AND** the data provider does not ignore visible user sorting or filtering intent
