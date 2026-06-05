## MODIFIED Requirements

### Requirement: Selector action invocation returns exactly one child object
The synthetic selector action SHALL access the collection from the supplied parent object.
The synthetic selector action SHALL filter collection elements using the supplied scalar parameter values.
The synthetic selector action SHALL be valid only when the supplied parent and scalar parameter values identify exactly one matching child object.
The synthetic selector action MUST prevent invocation with a clear no-match validation reason when no child matches.
The synthetic selector action MUST prevent invocation with a clear ambiguous-match validation reason when multiple children match.
The synthetic selector action SHALL return the single matching child object when exactly one child matches.
The synthetic selector action MUST fail clearly if invocation is attempted without prior successful validation and no child matches or multiple children match.

#### Scenario: Single child is valid and selected
- **GIVEN** a `Lease` has several `LeaseItem` children
- **AND** the selector action parameters identify exactly one `LeaseItem`
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation allows invocation
- **WHEN** the synthetic selector action is invoked
- **THEN** the action returns that `LeaseItem`

#### Scenario: No child match prevents invocation
- **GIVEN** a `Lease` has no `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation rejects the action with a clear no-match reason
- **AND** the action is not invoked

#### Scenario: Multiple child matches prevent invocation
- **GIVEN** a `Lease` has more than one `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action parameters are validated
- **THEN** validation rejects the action with a clear ambiguous-match reason
- **AND** the action is not invoked

#### Scenario: Direct invocation still fails for no child match
- **GIVEN** a caller bypasses validation
- **AND** a `Lease` has no `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked directly
- **THEN** the invocation fails with a clear no-match error

#### Scenario: Direct invocation still fails for multiple child matches
- **GIVEN** a caller bypasses validation
- **AND** a `Lease` has more than one `LeaseItem` matching the supplied scalar values
- **WHEN** the synthetic selector action is invoked directly
- **THEN** the invocation fails with a clear ambiguous-match error
