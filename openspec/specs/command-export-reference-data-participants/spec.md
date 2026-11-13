# Command Export Reference-Data Participants Specification

## Purpose

Define the public bookmark-classification SPI for stable replay reference data, its composition semantics, and its R1 boundary before export reachability integration.

## Requirements

### Requirement: Applications classify replay reference data through a bookmark SPI

The commandlog extension SHALL provide a public `CommandReplayReferenceDataService` SPI that classifies an entity bookmark as stable replay reference data. The SPI contract SHALL require accepted bookmarks to identify entities whose logical type and identifier are expected to remain stable and to exist in every replay environment. Classification MUST NOT require the bookmark to have appeared in a command sequence.

#### Scenario: Application classifier accepts a bookmark

- **WHEN** an application-provided classifier returns `true` for a bookmark
- **THEN** the classification result identifies that bookmark as replay reference data

#### Scenario: Application classifier rejects a bookmark

- **WHEN** an application-provided classifier returns `false` for a bookmark
- **THEN** that classifier does not identify the bookmark as replay reference data

### Requirement: Reference-data classifiers compose using OR semantics

The commandlog extension SHALL provide a reusable classifier composition operation over zero or more `CommandReplayReferenceDataService` implementations. A non-null bookmark SHALL be classified as reference data when at least one non-null implementation returns `true`. It MUST return `false` for a null bookmark, a null or empty service list, or a list in which no implementation accepts the bookmark.

#### Scenario: Later classifier accepts a bookmark

- **WHEN** the default classifier and the first application classifier reject a bookmark but a later application classifier accepts it
- **THEN** the composed classification returns `true`

#### Scenario: No classifier accepts a bookmark

- **WHEN** every registered classifier rejects a bookmark
- **THEN** the composed classification returns `false`

#### Scenario: No classifiers are available

- **WHEN** classification is requested with no registered classifiers
- **THEN** the composed classification returns `false`

#### Scenario: Bookmark is null

- **WHEN** composed classification is requested for a null bookmark
- **THEN** the composed classification returns `false` without invoking a classifier

### Requirement: R1 classification does not change export reachability

R1 SHALL expose and register reference-data classification independently of command export reachability. R1 MUST NOT make a command exportable, add known participants, or change manager collections solely because a bookmark is classified as reference data; those integrations belong to R2 and later slices.

#### Scenario: Classifier is registered before R2

- **WHEN** R1 classifies a bookmark as reference data
- **THEN** the current command exportability and manager collection behavior remain unchanged
