# Command Replay Mapping Specification

## Purpose

Define portable bookmark remapping, replay result observation, and deterministic in-memory mapping behavior for command replay.
## Requirements
### Requirement: Replay mapping SPI uses portable bookmarks
The system SHALL provide a `CommandReplayMappingListener` SPI with a common lookup operation for recorded target and reference-parameter bookmarks.
The lookup operation SHALL return an optional replacement bookmark and MUST NOT require the recorded bookmark to resolve to a live domain object.
The SPI SHALL provide a replay-result observation operation receiving the recorded result bookmark, actual replay result bookmark, and replayed command interaction id.
Both SPI operations SHALL have default no-op behavior so applications can implement lookup, observation, or both.

#### Scenario: Application supplies a target replacement
- **WHEN** replay mapping lookup receives recorded bookmark `demoCustomer:1`
- **AND** an application listener returns `demoCustomer:2`
- **THEN** replay can use `demoCustomer:2` without first resolving `demoCustomer:1`

#### Scenario: Listener implements result observation only
- **WHEN** an application listener overrides replay-result observation but not lookup
- **THEN** lookup returns no replacement
- **AND** the listener remains a valid `CommandReplayMappingListener`

### Requirement: Replay targets are remapped before execution
Before command replay or retry, the system SHALL ask replay mapping listeners for a replacement for each recorded target bookmark.
The system SHALL consult listeners in dependency-injection order and use the first non-empty replacement.
When a listener throws during lookup, the system SHALL log the failure and continue with remaining listeners.
When a replacement is found, replay SHALL execute against the replacement target bookmark.
When no replacement is found, replay SHALL retain the recorded target bookmark.

#### Scenario: Target bookmark is remapped
- **WHEN** replay is about to execute a command whose recorded target is `demoCustomer:1`
- **AND** mapping lookup returns `demoCustomer:2`
- **THEN** replay executes the command against target `demoCustomer:2`

#### Scenario: Target bookmark has no replacement
- **WHEN** replay is about to execute a command whose recorded target is `demoCustomer:1`
- **AND** every mapping listener returns no replacement
- **THEN** replay executes the command against target `demoCustomer:1`

#### Scenario: First listener failure does not prevent later lookup
- **WHEN** the first mapping listener throws while looking up `demoCustomer:1`
- **AND** a later mapping listener returns `demoCustomer:2`
- **THEN** replay logs the first failure
- **AND** replay executes the command against target `demoCustomer:2`

### Requirement: Replay reference action parameters are remapped before execution
Before command replay or retry, the system SHALL use the common lookup operation for each action parameter represented by schema value type `REFERENCE` with a populated reference OID.
When a replacement is found, replay SHALL execute with the replacement parameter reference.
When no replacement is found, replay SHALL retain the recorded parameter reference.
The system MUST leave non-reference action parameters and property values unchanged by reference remapping.
The lookup operation MUST NOT require parameter name or index metadata.

#### Scenario: Reference parameter is remapped
- **WHEN** action parameter `customer` records reference bookmark `demoCustomer:1`
- **AND** mapping lookup returns `demoCustomer:2`
- **THEN** replay executes with parameter `customer` referring to `demoCustomer:2`

#### Scenario: Reference parameter has no replacement
- **WHEN** action parameter `customer` records reference bookmark `demoCustomer:1`
- **AND** mapping lookup returns no replacement
- **THEN** replay executes with parameter `customer` referring to `demoCustomer:1`

#### Scenario: Scalar selector parameter is unchanged
- **WHEN** a replayed synthetic selector action has a non-reference string or boolean parameter
- **THEN** reference remapping does not ask listeners to replace that parameter
- **AND** replay retains the recorded scalar value

### Requirement: Replay input remapping preserves recorded command data
The system SHALL apply target and reference-parameter replacements to a structurally independent execution copy created from the imported command DTO.
The imported command log entry SHALL retain its recorded command DTO, target, member, result, exception, username, and original timestamp.
Started and completed lifecycle synchronization SHALL continue to update execution timing for replay-enabled entries without overwriting those recorded fields.
Each replay or retry SHALL create a new execution copy from the imported command DTO and MUST NOT reuse a previously remapped execution copy.
Replay failure MUST NOT leave replacements in the imported command DTO.

#### Scenario: Remapped target preserves imported DTO
- **WHEN** replay remaps recorded target `demoCustomer:1` to `demoCustomer:2`
- **THEN** the execution DTO contains target `demoCustomer:2`
- **AND** the imported command log entry still contains target `demoCustomer:1`

#### Scenario: Remapped parameter preserves imported DTO
- **WHEN** replay remaps reference parameter `customer` from `demoCustomer:1` to `demoCustomer:2`
- **THEN** the execution DTO contains parameter reference `demoCustomer:2`
- **AND** the imported command log entry still contains parameter reference `demoCustomer:1`

#### Scenario: Retry starts from recorded identities
- **GIVEN** an earlier replay remapped target or parameter bookmark `demoCustomer:1` to `demoCustomer:2`
- **WHEN** the command is retried
- **THEN** mapping lookup is evaluated again using recorded bookmark `demoCustomer:1`

#### Scenario: Lifecycle synchronization preserves imported facts
- **WHEN** normal command publishing callbacks synchronize a replay-enabled command log entry during execution
- **THEN** execution timing is updated
- **AND** imported command DTO, target, member, result, exception, username, and original timestamp remain unchanged

### Requirement: Successful replay observes result mappings atomically
After replay execution produces an actual bookmark, the system SHALL notify every replay mapping listener when the imported command log entry has a recorded result bookmark.
The notification SHALL include recorded bookmark, actual bookmark, and command interaction id.
The system SHALL notify identity mappings where recorded and actual bookmarks are equal.
Notification SHALL occur inside the same transaction as replayed command execution.
A notification exception SHALL fail replay and roll back the replay transaction.

#### Scenario: Different result bookmark is observed
- **WHEN** an imported command records result `demoInvoice:1`
- **AND** successful replay produces actual result `demoInvoice:2`
- **THEN** every mapping listener is notified with `demoInvoice:1`, `demoInvoice:2`, and the command interaction id

#### Scenario: Identity result bookmark is observed
- **WHEN** an imported command records result `demoInvoice:1`
- **AND** successful replay produces actual result `demoInvoice:1`
- **THEN** every mapping listener is notified with the identity mapping

#### Scenario: Listener rejects a result mapping
- **WHEN** replayed domain work succeeds
- **AND** a mapping listener throws while observing its result
- **THEN** replay fails
- **AND** the replay transaction rolls back

### Requirement: Unavailable replay results are not observed
The system MUST NOT notify replay mapping listeners unless replay execution succeeds and both recorded and actual result bookmarks are available.
Replay input lookup MAY still occur when result observation data is unavailable.

#### Scenario: Replay fails
- **WHEN** command execution fails for an imported entry with a recorded result bookmark
- **THEN** no listener receives a replay-result observation

#### Scenario: Recorded result is absent
- **WHEN** command execution succeeds but the imported entry has no recorded result bookmark
- **THEN** no listener receives a replay-result observation

#### Scenario: Actual result is absent
- **WHEN** command execution succeeds without an actual result bookmark
- **THEN** no listener receives a replay-result observation

### Requirement: In-memory listener records deterministic mappings
The system SHALL provide an in-memory `CommandReplayMappingListener` that stores the first actual bookmark observed for each recorded result bookmark.
The listener SHALL store identity mappings and the originating command interaction id when available.
Repeating the same recorded-to-actual mapping SHALL be idempotent and SHALL retain the original interaction id.
A conflicting actual bookmark MUST NOT replace the stored bookmark or original interaction id.
The default conflict policy SHALL throw an exception.
The `LOG_AND_CONTINUE` conflict policy SHALL log the conflict without throwing.

#### Scenario: Different mapping is recorded
- **WHEN** the in-memory listener observes recorded result `demoInvoice:1` and actual result `demoInvoice:2`
- **THEN** it stores `demoInvoice:1` to `demoInvoice:2`

#### Scenario: Identity mapping is recorded
- **WHEN** the in-memory listener observes recorded and actual result `demoInvoice:1`
- **THEN** it stores `demoInvoice:1` to `demoInvoice:1`

#### Scenario: Repeated mapping is idempotent
- **GIVEN** the listener stored `demoInvoice:1` to `demoInvoice:2` with interaction id `11111111-1111-1111-1111-111111111111`
- **WHEN** it observes the same mapping with a different interaction id
- **THEN** the stored bookmark and original interaction id remain unchanged
- **AND** no exception is thrown

#### Scenario: Conflict is rejected by default
- **GIVEN** the listener stored `demoInvoice:1` to `demoInvoice:2`
- **WHEN** it observes `demoInvoice:1` mapping to `demoInvoice:3`
- **THEN** it throws an exception
- **AND** it retains `demoInvoice:2`

#### Scenario: Conflict can be logged and ignored
- **GIVEN** conflict policy is `LOG_AND_CONTINUE`
- **AND** the listener stored `demoInvoice:1` to `demoInvoice:2`
- **WHEN** it observes `demoInvoice:1` mapping to `demoInvoice:3`
- **THEN** it logs the conflict without throwing
- **AND** it retains `demoInvoice:2`

### Requirement: In-memory mappings remap later replay inputs
The in-memory listener SHALL return the stored actual bookmark when lookup receives a recorded bookmark with a mapping.
When no mapping exists, the listener SHALL return no replacement.
Mapping storage SHALL be scoped to the listener instance and SHALL NOT be required to survive application restart, application nodes, or bean recreation.

#### Scenario: Later target uses earlier result mapping
- **GIVEN** the listener stored `demoInvoice:1` to `demoInvoice:2`
- **WHEN** a later replay target lookup receives `demoInvoice:1`
- **THEN** lookup returns `demoInvoice:2`

#### Scenario: Unmapped bookmark is retained
- **WHEN** lookup receives `demoInvoice:9` with no stored mapping
- **THEN** the listener returns no replacement

#### Scenario: New listener has fresh state
- **GIVEN** one in-memory listener stored a replay mapping
- **WHEN** a new in-memory listener instance is created
- **THEN** the new instance is not required to contain that mapping

### Requirement: Built-in listener configuration is conditional
The system SHALL provide `causeway.extensions.command-log.replay-result-mapping.storage-strategy` with values `IN_MEMORY` and `PERSISTENT`, defaulting to `IN_MEMORY`.
The system SHALL provide `causeway.extensions.command-log.replay-result-mapping.on-conflict-policy` with values `THROW_EXCEPTION` and `LOG_AND_CONTINUE`, defaulting to `THROW_EXCEPTION`.
When storage strategy is `IN_MEMORY` and no application-defined `CommandReplayMappingListener` bean exists, commandlog autoconfiguration SHALL provide the configured in-memory listener.
When any application-defined listener bean exists, the in-memory default MUST back off.
When storage strategy is `PERSISTENT`, this change MUST NOT create the in-memory listener.

#### Scenario: In-memory listener is the default
- **WHEN** no storage strategy and no custom listener are configured
- **THEN** commandlog autoconfiguration provides one in-memory listener using `THROW_EXCEPTION`

#### Scenario: Custom listener replaces the default
- **WHEN** the application defines a `CommandReplayMappingListener` bean
- **THEN** commandlog autoconfiguration does not provide the in-memory listener

#### Scenario: Persistent selection suppresses in-memory listener
- **WHEN** storage strategy is `PERSISTENT`
- **THEN** commandlog autoconfiguration does not provide the in-memory listener
- **AND** this change does not provide persistent mapping storage

### Requirement: Optional export remapping preserves recorded envelopes
When export remapping is requested, the system SHALL create a structurally independent `CommandExportDto` copy and apply the existing ordered replay-mapping lookup to every command target, populated reference-valued action parameter, and optional result bookmark. The first non-empty listener replacement SHALL be used for each identity; lookup failure SHALL be logged and later listeners SHALL still be consulted. Unmapped identities SHALL remain recorded values. Export remapping MUST NOT mutate the recorded command DTO, command-log result, replay state, or source export envelope. When export remapping is not requested, export SHALL emit recorded identities.

#### Scenario: Export remaps inputs and result together
- **GIVEN** mapping lookup replaces recorded bookmark `demo.Invoice:1` with actual bookmark `demo.Invoice:2`
- **WHEN** an export envelope uses `demo.Invoice:1` as a target, reference parameter, or result and remapping is requested
- **THEN** the exported copy uses `demo.Invoice:2` for each mapped occurrence
- **AND** the recorded envelope and command-log entry retain `demo.Invoice:1`

#### Scenario: Unmapped export identity is retained
- **WHEN** no listener supplies a replacement for a recorded export identity
- **THEN** the exported copy retains that recorded identity

#### Scenario: Listener failure permits later export mapping
- **WHEN** the first listener throws during export lookup
- **AND** a later listener supplies a replacement
- **THEN** the failure is logged
- **AND** the exported copy uses the later replacement

#### Scenario: Export remapping can be disabled
- **WHEN** sequence export is invoked without result remapping
- **THEN** targets, reference parameters, and result metadata use their recorded identities
