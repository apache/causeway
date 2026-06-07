## MODIFIED Requirements

### Requirement: Persistent replay mappings are represented by applib and persistence-specific types
The system SHALL define an abstract applib replay result mapping entity that exposes recorded and actual bookmark properties.
The system SHALL define an abstract applib repository for finding, listing, and creating replay result mappings.
The abstract applib repository SHALL support listing all replay result mappings.
The abstract applib repository SHALL support listing changed replay result mappings where the recorded bookmark and actual bookmark differ.
The abstract applib repository SHALL support finding a replay result mapping by recorded bookmark.
The abstract applib repository SHALL support finding replay result mappings by actual bookmark.
The JDO command log persistence module SHALL provide a concrete replay result mapping entity and repository.
The JPA command log persistence module SHALL provide a concrete replay result mapping entity and repository.

#### Scenario: JDO module provides persistent mapping types
- **WHEN** the command log JDO persistence module is active
- **THEN** the system provides a concrete JDO replay result mapping entity and repository implementing the applib contracts

#### Scenario: JPA module provides persistent mapping types
- **WHEN** the command log JPA persistence module is active
- **THEN** the system provides a concrete JPA replay result mapping entity and repository implementing the applib contracts

#### Scenario: Repository lists changed replay mappings
- **WHEN** persisted replay mappings include `demoInvoice:1` to `demoInvoice:1` and `demoInvoice:2` to `demoInvoice:3`
- **THEN** the repository changed-mappings finder returns the mapping from `demoInvoice:2` to `demoInvoice:3`
- **AND** the repository changed-mappings finder does not return the mapping from `demoInvoice:1` to `demoInvoice:1`

#### Scenario: Repository finds replay mappings by actual bookmark
- **WHEN** persisted replay mappings include recorded bookmark `demoInvoice:1` with actual bookmark `demoInvoice:9`
- **AND** persisted replay mappings include recorded bookmark `demoInvoice:2` with actual bookmark `demoInvoice:9`
- **THEN** the repository actual-bookmark finder for `demoInvoice:9` returns both persisted replay mappings

### Requirement: Persistent replay mappings are listable from the command log menu
The system SHALL provide a command log menu action that lists all persisted replay result mappings.
The system SHALL provide a command log menu action that lists changed persisted replay result mappings where the recorded bookmark and actual bookmark differ.
The system SHALL provide a command log menu action that finds a persisted replay result mapping by recorded bookmark.
The system SHALL provide a command log menu action that finds persisted replay result mappings by actual bookmark.
The system SHALL provide fallback layout metadata for the persisted replay result mapping entity.
The list actions SHALL return persisted mappings using the abstract applib repository.
The listed replay result mappings SHALL include identity replay results where the recorded and actual bookmarks are equal when the user asks for all mappings.
The changed-mappings action MUST NOT return identity replay result mappings where the recorded and actual bookmarks are equal.
The replay mapping menu actions MUST be hidden when no persistent replay result mapping repository is available.

#### Scenario: User lists persisted replay mappings
- **WHEN** a user invokes the command log menu action for all replay result mappings
- **THEN** the system returns the persisted replay result mappings
- **AND** each mapping displays its recorded and actual bookmarks

#### Scenario: User lists persisted identity replay mappings
- **WHEN** a persisted replay result mapping has recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`
- **AND** a user invokes the command log menu action for all replay result mappings
- **THEN** the system returns the persisted identity replay result mapping
- **AND** the mapping displays recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

#### Scenario: User lists changed replay mappings
- **WHEN** persisted replay mappings include `demoInvoice:1` to `demoInvoice:1` and `demoInvoice:2` to `demoInvoice:3`
- **AND** a user invokes the command log menu action for changed replay result mappings
- **THEN** the system returns the mapping from `demoInvoice:2` to `demoInvoice:3`
- **AND** the system does not return the mapping from `demoInvoice:1` to `demoInvoice:1`

#### Scenario: User finds replay mapping by recorded bookmark
- **WHEN** a persisted replay result mapping has recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`
- **AND** a user invokes the command log menu action to find by recorded bookmark `demoInvoice:1`
- **THEN** the system returns the persisted replay result mapping with recorded bookmark `demoInvoice:1`

#### Scenario: User finds replay mappings by actual bookmark
- **WHEN** persisted replay result mappings include recorded bookmark `demoInvoice:1` with actual bookmark `demoInvoice:9`
- **AND** persisted replay result mappings include recorded bookmark `demoInvoice:2` with actual bookmark `demoInvoice:9`
- **AND** a user invokes the command log menu action to find by actual bookmark `demoInvoice:9`
- **THEN** the system returns both persisted replay result mappings
