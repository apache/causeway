## ADDED Requirements

### Requirement: Bounded pager authoritative total presentation
The collection-owned bounded pager SHALL append a valid authoritative total count to its current range for both native-table and bounded-Grid presentation.
Unavailable or invalid totals MUST remain undisclosed rather than being derived from page offsets, returned rows, requested size, or navigation flags.

#### Scenario: Bounded Grid has an authoritative total
- **WHEN** bounded Grid presentation renders a non-empty normalized window with a non-negative safe-integer total
- **THEN** the live pager range includes `of <total>`
- **AND** responsive adapter changes do not alter that count

#### Scenario: Native bounded presentation has an authoritative total
- **WHEN** the same bounded window renders through native table presentation
- **THEN** it exposes the same range and total wording
- **AND** the displayed count remains toolkit-neutral

#### Scenario: Bounded window has no authoritative total
- **WHEN** total metadata is absent, null, negative, unsafe, or otherwise invalid
- **THEN** the pager omits the `of` suffix
- **AND** continues to use authoritative previous and next offsets without inventing collection size
