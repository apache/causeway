## ADDED Requirements

### Requirement: Persistent replay mappings retain originating command interaction id
The persistent replay mapping listener SHALL store the replayed command interaction id when it creates a new replay result mapping and the command interaction id is available.
The persistent replay mapping listener MUST NOT update the stored command interaction id when a replay result notification matches an existing mapping.
The persistent replay mapping listener MUST NOT update the stored command interaction id when a replay result notification conflicts with an existing mapping and the configured conflict policy logs and continues.
The persisted replay result mapping entity SHALL expose the command interaction id as an optional property.
The JDO and JPA command log persistence modules SHALL provide storage for the command interaction id property.

#### Scenario: New persistent mapping records command interaction id
- **WHEN** command replay notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** the replayed command interaction id is `11111111-1111-1111-1111-111111111111`
- **THEN** the system persists a mapping with command interaction id `11111111-1111-1111-1111-111111111111`

#### Scenario: Existing idempotent mapping keeps original command interaction id
- **GIVEN** a persisted replay result mapping for recorded bookmark `demoInvoice:1` has actual bookmark `demoInvoice:2`
- **AND** the mapping has command interaction id `11111111-1111-1111-1111-111111111111`
- **WHEN** command replay later notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:2`
- **AND** the later replayed command interaction id is `22222222-2222-2222-2222-222222222222`
- **THEN** the persisted mapping keeps command interaction id `11111111-1111-1111-1111-111111111111`

#### Scenario: Existing conflicting mapping keeps original command interaction id when conflict is logged
- **GIVEN** the replay mapping conflict policy is configured to log and continue
- **AND** a persisted replay result mapping for recorded bookmark `demoInvoice:1` has actual bookmark `demoInvoice:2`
- **AND** the mapping has command interaction id `11111111-1111-1111-1111-111111111111`
- **WHEN** command replay later notifies the persistent listener that recorded result bookmark `demoInvoice:1` mapped to actual result bookmark `demoInvoice:3`
- **AND** the later replayed command interaction id is `22222222-2222-2222-2222-222222222222`
- **THEN** the persisted mapping keeps command interaction id `11111111-1111-1111-1111-111111111111`

### Requirement: Persistent replay mapping layout displays command interaction id
The system SHALL include the command interaction id property in the fallback layout metadata for persisted replay result mappings.
The system SHALL include the command interaction id in replay result mapping column order metadata when such metadata is provided.

#### Scenario: User views replay mapping command interaction id
- **WHEN** a persisted replay result mapping has command interaction id `11111111-1111-1111-1111-111111111111`
- **AND** a user views the replay result mapping
- **THEN** the mapping layout displays the command interaction id
