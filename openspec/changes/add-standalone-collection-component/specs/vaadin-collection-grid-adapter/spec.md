## ADDED Requirements

### Requirement: Finite standalone collection Grid qualification
The internal Vaadin Grid adapter SHALL accept a qualified finite projection from `<cw-standalone-collection>` without requiring an object-member range broker.
The supplied action-result array MUST remain the complete row authority and Grid presentation MUST NOT introduce GraphQL paging, sorting, filtering, hydration, or mutation.

#### Scenario: Standalone result qualifies for Grid
- **WHEN** Grid policy is enabled, the component is wide, rows have valid identities, declared columns and renderers are supported, and the result generation is current
- **THEN** the component supplies the projected rows, authoritative array length, accessible heading references, and a static range provider to the existing Grid adapter
- **AND** the Grid renders without a collection-member load or range request

#### Scenario: Grid requests a finite range
- **WHEN** the adapter requests an offset and size within the action-result snapshot
- **THEN** the component returns the corresponding slice of already-projected rows
- **AND** no network request, row hydration, cache broker, or inferred total is introduced

#### Scenario: Small complete result renders
- **WHEN** the authoritative result count proves every supplied row is represented
- **THEN** existing small-virtual-Grid height behavior fits the projected standalone rows
- **AND** empty filler rows and unnecessary tall whitespace are avoided

#### Scenario: Standalone result is replaced
- **WHEN** a newer result generation replaces a Grid-presented result
- **THEN** obsolete static range requests fail as superseded and cannot replace current rows
- **AND** the new generation independently requalifies its presentation

#### Scenario: Standalone result does not qualify
- **WHEN** policy is native, the component is narrow, identity or columns are unsupported, renderers fail, or the Grid family fails
- **THEN** the standalone component uses its accessible native list or table
- **AND** authoritative rows, order, links, values, count, and lifecycle remain unchanged

#### Scenario: Unsupported Grid features are requested
- **WHEN** standalone presentation is inspected for paging, sorting, filtering, column persistence, or server range behavior
- **THEN** those member-only controls and callbacks are absent
- **AND** optional resizable and reorderable column presentation remains non-authoritative
