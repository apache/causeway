## ADDED Requirements

### Requirement: Opaque bookmark route regression coverage
The pinned Reference Application suite SHALL exercise the authoritative long memento identifier returned for `demo.CompositeValuesPage` through the generic HTMX canonical route.
It MUST preserve the exact identifier, distinguish corrected routing from object-read failures, and retain unrelated bounded invalid-route behavior.

#### Scenario: Composite-values result is routed
- **WHEN** `demo.CompositeValueTypeMenu.compositeValueTypes` returns the `demo.CompositeValuesPage` identity
- **THEN** its exact opaque identifier produces a canonical HTMX object route within the supported encoded bound
- **AND** the route reaches ready or documented partial object state instead of `invalid-route`

#### Scenario: Composite-values route reconstructs identity
- **WHEN** the server renders the canonical composite-values route
- **THEN** the route object context contains the same logical type and byte-for-byte identifier returned by GraphQL
- **AND** representative composite value content remains visible through the authoritative object read

#### Scenario: Composite-values history is traversed
- **WHEN** navigation replaces the active object route with the composite-values route and the browser traverses back and forward
- **THEN** each history entry restores its exact canonical logical identity and route state
- **AND** stale work from the replaced route cannot overwrite the restored context

#### Scenario: Malformed route remains bounded
- **WHEN** the same qualification run requests malformed escapes, separators, controls, non-canonical encoding, or a segment beyond the supported encoded bound
- **THEN** the viewer retains its non-disclosing `invalid-route` presentation
- **AND** successful long opaque routing does not weaken route validation
