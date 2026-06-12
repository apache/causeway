## ADDED Requirements

### Requirement: Selector actions are disabled for empty collections
The synthetic selector action SHALL be disabled for a current parent object when the associated parented collection has no elements.
The synthetic selector action SHALL remain visible in the metamodel when disabled because the associated collection is empty.
The synthetic selector action SHALL be enabled for a current parent object when the associated parented collection has at least one element.
The synthetic selector action SHALL keep existing validation behavior for non-empty collections, including no-match and ambiguous-match validation after filter parameters are supplied.

#### Scenario: Empty collection disables selector action
- **GIVEN** an object has an eligible parented collection with no elements
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework evaluates usability for the synthetic selector action on that object
- **THEN** the selector action is disabled with a clear reason

#### Scenario: Non-empty collection keeps selector action enabled
- **GIVEN** an object has an eligible parented collection with at least one element
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework evaluates usability for the synthetic selector action on that object
- **THEN** the selector action is enabled

#### Scenario: Non-empty collection still validates filter matches
- **GIVEN** an object has an eligible parented collection with at least one element
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework validates synthetic selector action parameters that match no collection element
- **THEN** validation rejects the action with a clear no-match reason
