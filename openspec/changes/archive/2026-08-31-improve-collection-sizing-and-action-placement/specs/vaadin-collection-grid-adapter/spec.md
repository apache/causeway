## MODIFIED Requirements

### Requirement: Explicit paging selects bounded Grid mode
A valid Causeway paging override SHALL select bounded Grid presentation and SHALL retain Causeway-owned previous and next controls.
Vaadin Grid MUST NOT create an independent paging state machine.

#### Scenario: Paged collection has a safe total
- **WHEN** a qualified collection declares a valid page size and the window reports a stable total
- **THEN** the adapter receives only the current bounded window as items
- **AND** Causeway-owned paging controls navigate normalized server offsets

#### Scenario: Bounded Grid fits its current page
- **WHEN** the adapter renders a bounded page as Grid
- **THEN** Grid height fits the current bounded rows without retaining its default empty viewport
- **AND** paging controls remain outside Grid and metadata-driven

#### Scenario: Complete virtual window is small
- **WHEN** an unpaged virtual Grid has a valid authoritative total no greater than its currently projected row count
- **THEN** Grid height fits those rows without retaining an empty scrolling viewport
- **AND** the collection retains its virtual range contract and can requalify after refresh

#### Scenario: Virtual collection exceeds its projected rows
- **WHEN** an unpaged virtual Grid has an authoritative total greater than its currently projected row count
- **THEN** Grid retains its bounded scrolling viewport and range provider
- **AND** it does not expand to the complete collection or infer completeness from sparse rows

#### Scenario: Unpaged collection has a safe total
- **WHEN** a qualified collection has no page-size override and reports a stable total
- **THEN** established virtual Grid qualification remains available
- **AND** the existing bounded range broker remains authoritative
