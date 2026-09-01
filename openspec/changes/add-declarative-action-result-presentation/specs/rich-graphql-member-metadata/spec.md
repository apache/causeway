## ADDED Requirements

### Requirement: Authoritative action result element logical type metadata
The shared rich member metadata object SHALL expose nullable `resultElementLogicalTypeName` for an action whose declared result is a collection with a canonical domain element logical type.
The value MUST be static, additive, authorization-safe, and derived without invoking domain behavior or inferring identity from generated GraphQL names.

#### Scenario: Action returns a domain-object collection
- **WHEN** metadata is requested for an action declared to return a collection of one canonical domain element type
- **THEN** `resultElementLogicalTypeName` returns that element specification's canonical logical type name
- **AND** the value is available before action invocation and for an empty result

#### Scenario: Action result is not a supported domain collection
- **WHEN** an action returns void, a scalar, one object, an unsupported aggregate, or a collection without one safely advertised canonical domain element type
- **THEN** `resultElementLogicalTypeName` returns null
- **AND** no value is inferred from action names, Java class names, generated GraphQL names, or runtime rows

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** `resultElementLogicalTypeName` returns null
- **AND** existing names, descriptions, icons, confirmation, prompt style, and editor constraints remain unchanged

#### Scenario: Result type behavior is imperative
- **WHEN** determining a presentation type would require invoking domain code or evaluating an action result
- **THEN** metadata returns null instead
- **AND** metadata resolution remains side-effect-free

#### Scenario: Existing client omits result element metadata
- **WHEN** an existing GraphQL document does not select `resultElementLogicalTypeName`
- **THEN** its operation and response shape remain unchanged
- **AND** action invocation semantics are unaffected
