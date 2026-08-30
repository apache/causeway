## ADDED Requirements

### Requirement: Static action Font Awesome metadata
The shared rich member metadata object SHALL expose nullable `cssClassFa` and `cssClassFaPosition` fields for applicable static action Font Awesome facets.
It MUST NOT execute imperative icon logic or expose arbitrary action layout internals.

#### Scenario: Static action icon facet exists
- **WHEN** an action has an accepted static Font Awesome facet
- **THEN** `cssClassFa` returns its canonical bounded quick notation
- **AND** `cssClassFaPosition` returns the canonical `LEFT` or `RIGHT` token

#### Scenario: Static action icon facet is absent
- **WHEN** an action has no static Font Awesome facet
- **THEN** both icon metadata fields return null
- **AND** no default icon or position is fabricated

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** action icon metadata fields return null
- **AND** existing names, descriptions, and editor constraints remain unchanged

#### Scenario: Icon behavior is imperative
- **WHEN** icon behavior would require invoking domain code
- **THEN** static icon metadata returns null
- **AND** metadata resolution remains side-effect-free
