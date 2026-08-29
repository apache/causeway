## ADDED Requirements

### Requirement: Targeted collection heading metadata
A semantic collection requirement SHALL select supported canonical `friendlyName` and `description` fields from the collection wrapper's existing metadata object alongside collection visibility and usability state.

#### Scenario: Collection metadata fields are supported
- **WHEN** targeted introspection finds `metadata.friendlyName` or `metadata.description` on a requested collection wrapper
- **THEN** the object-context selection includes only the supported heading metadata fields
- **AND** accepted response metadata is delivered with the current collection requirement state

#### Scenario: Collection metadata is unavailable or partially erroneous
- **WHEN** the wrapper lacks either heading field or GraphQL reports a nullable field-local error
- **THEN** the remaining supported collection state stays usable
- **AND** the component can apply explicit HTML overrides or safe fallbacks without a complete-schema read

#### Scenario: Collection heading attribute changes
- **WHEN** an HTML-only name or description override changes after metadata has loaded
- **THEN** the component reuses the current requirement state
- **AND** no extra GraphQL request is issued solely for presentation text
