## ADDED Requirements

### Requirement: Recorded command target participants are available for record review
A replayable command participant with role `TARGET` SHALL expose its target object when the owning command's replay state is `UNDEFINED` and the recorded target bookmark resolves locally.
A replayable command participant with role `TARGET` SHALL expose its target object when the owning command's replay state is `EXPORTED` and the recorded target bookmark resolves locally.
A replayable command participant with role `TARGET` MUST remain optional and SHALL expose no target object when the applicable bookmark cannot be resolved locally.
This target availability MUST NOT change the command replay state.

#### Scenario: Undefined command target is available
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its target participant has recorded bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the target object for `demoCustomer:1` is returned

#### Scenario: Exported command target is available
- **GIVEN** a replayable command has replay state `EXPORTED`
- **AND** its target participant has recorded bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the target object for `demoCustomer:1` is returned

#### Scenario: Unresolvable recorded target remains absent
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its target participant has recorded bookmark `demoCustomer:1`
- **AND** bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the participant target property
- **THEN** no target object is returned

### Requirement: Domain service target participants are available in every replay state
A replayable command participant with role `TARGET` SHALL expose its target object when the recorded target bookmark identifies a domain service and resolves locally.
This rule SHALL apply regardless of the owning command's replay state.
This rule MUST NOT enable replay or export by itself.

#### Scenario: Domain service target is available for pending replay
- **GIVEN** a replayable command has replay state `PENDING`
- **AND** its target participant has a recorded bookmark for a domain service
- **AND** that domain service bookmark resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the domain service target object is returned

#### Scenario: Domain service target is available for failed replay
- **GIVEN** a replayable command has replay state `FAILED`
- **AND** its target participant has a recorded bookmark for a domain service
- **AND** that domain service bookmark resolves locally
- **WHEN** the system reads the participant target property
- **THEN** the domain service target object is returned

### Requirement: Recorded command reference arguments are available for record review
A replayable command participant with role `PARAMETER` SHALL expose its argument object when the owning command's replay state is `UNDEFINED` and the recorded reference-parameter bookmark resolves locally.
A replayable command participant with role `PARAMETER` SHALL expose its argument object when the owning command's replay state is `EXPORTED` and the recorded reference-parameter bookmark resolves locally.
A replayable command participant with role `PARAMETER` MUST remain optional and SHALL expose no argument object when the applicable bookmark cannot be resolved locally.
This argument availability MUST NOT change the command replay state.

#### Scenario: Undefined command reference argument is available
- **GIVEN** a replayable command has replay state `UNDEFINED`
- **AND** its parameter participant is named `customer`
- **AND** the participant recorded bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant argument property
- **THEN** the argument object for `demoCustomer:1` is returned

#### Scenario: Exported command reference argument is available
- **GIVEN** a replayable command has replay state `EXPORTED`
- **AND** its parameter participant is named `customer`
- **AND** the participant recorded bookmark `demoCustomer:1` resolves locally
- **WHEN** the system reads the participant argument property
- **THEN** the argument object for `demoCustomer:1` is returned

#### Scenario: Unresolvable recorded argument remains absent
- **GIVEN** a replayable command has replay state `EXPORTED`
- **AND** its parameter participant is named `customer`
- **AND** the participant recorded bookmark `demoCustomer:1` does not resolve locally
- **WHEN** the system reads the participant argument property
- **THEN** no argument object is returned
