# Rich GraphQL Reference-Application Analysis Specification

## Purpose

Define the reproducible evidence, classification, completion, and roadmap requirements for assessing rich GraphQL and web-component coverage against a pinned Apache Causeway reference application.

## Requirements

### Requirement: Pinned reference-application analysis
The project SHALL maintain a reproducible rich GraphQL coverage analysis against a pinned Apache Causeway reference-application revision.

#### Scenario: Analysis is reproduced
- **WHEN** a reviewer follows the recorded audit procedure
- **THEN** the repository, revision, application variant, build configuration, and representative source locations are unambiguous
- **AND** the normal product build does not require the external checkout

### Requirement: Evidence-backed coverage classification
Every audited representative feature SHALL include source evidence, current GraphQL evidence, an expected semantic contract, and a disposition.

#### Scenario: Suspected gap is classified
- **WHEN** source inspection suggests a missing capability
- **THEN** targeted schema or operation evidence confirms or rejects the gap
- **AND** the result is classified as supported, GraphQL work, web-component work, viewer policy, Wicket-specific, or explicitly unsupported

#### Scenario: Sensitive behavior is audited
- **WHEN** a feature contains hidden, password, authorization-sensitive, or resource data
- **THEN** the analysis verifies non-disclosure as part of the expected contract

### Requirement: Analysis-only scope
The analysis change SHALL NOT modify production GraphQL, web-component, viewer, or runtime behavior.

#### Scenario: Analysis completes
- **WHEN** all analysis tasks are complete
- **THEN** only durable analysis, evidence references, roadmap documentation, and pending planning artifacts have changed
- **AND** no shipped schema or component behavior has changed

### Requirement: Prioritized implementation roadmap
The analysis SHALL produce a prioritized roadmap of bounded follow-on changes and their dependencies.

#### Scenario: Confirmed gaps are scheduled
- **WHEN** the coverage matrix contains confirmed protocol or component gaps
- **THEN** each accepted gap maps to a bounded pending proposal or an explicit deferral
- **AND** prerequisites for composite object rendering, menu bars, and the generic HTMX viewer are identified

### Requirement: Reviewable completion criteria
The analysis SHALL be complete only when representative services, objects, members, values, collections, layouts, menu bars, home-page behavior, interactions, and sensitive cases have a recorded disposition.

#### Scenario: Roadmap review occurs
- **WHEN** reviewers assess the analysis for completion
- **THEN** they can trace every roadmap recommendation to coverage evidence
- **AND** identify any intentionally unresolved case without inferring product support
