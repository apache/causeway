## ADDED Requirements

### Requirement: Semantic collection headings
The `<cw-collection>` component SHALL render one semantic collection heading from an explicit HTML name, canonical member metadata, or a safe member-id fallback.
It SHALL render a distinct available description directly below that heading in subdued, smaller text and associate both texts with every collection presentation state.

#### Scenario: HTML supplies both overrides
- **WHEN** a collection declares non-blank `named` and `described-as` attributes
- **THEN** those values are the rendered collection name and description
- **AND** they override canonical friendly-name and description metadata

#### Scenario: Canonical metadata supplies heading text
- **WHEN** no applicable HTML override exists and the collection response supplies canonical `metadata.friendlyName` or `metadata.description`
- **THEN** each available value is used independently
- **AND** a missing value falls back to a humanized member id for the name or no description for the description

#### Scenario: Legacy label remains present
- **WHEN** a collection declares the existing `label` attribute without `named`
- **THEN** the label remains the effective name for backward compatibility
- **AND** `named` takes precedence when both are non-blank

#### Scenario: Description duplicates resolved name
- **WHEN** the candidate description equals the resolved name after trimming and case-insensitive comparison
- **THEN** no duplicate description is rendered or referenced

#### Scenario: Heading attributes change
- **WHEN** `named`, `described-as`, `label`, or the member id changes while the component is connected
- **THEN** the current collection shell rerenders with the newly resolved accessible heading
- **AND** collection data is not independently reloaded solely for the text change

### Requirement: Quiet read-only collection presentation
Collection wrappers SHALL remain read-only and authorization-aware without rendering their disabled or unmodifiable boolean or reason as visible explanatory text, a label, or a tooltip.

#### Scenario: Mixed-in collection is unmodifiable
- **WHEN** a readable mixed-in collection reports a disabled reason such as “Cannot edit a mixed-in collection”
- **THEN** its rows, heading, description, paging, and applicable associated actions remain available
- **AND** the collection-level reason is not rendered as text or tooltip

#### Scenario: Member or associated action is disabled
- **WHEN** an individual row member or associated action has its own disabled state and reason
- **THEN** that control retains its established semantic state and explanation
- **AND** collection-level suppression does not broaden authorization or enable mutation
