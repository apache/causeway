## ADDED Requirements

### Requirement: Reproducible semantic designer evaluation
The project SHALL maintain a reproducible comparison of viable page-authoring tools and models against the Causeway semantic component contract.

#### Scenario: Tooling decision is reviewed
- **WHEN** a reviewer follows the recorded evaluation procedure
- **THEN** tool versions, licences, prototype configuration, use cases, criteria, scores, and evidence are unambiguous
- **AND** no production designer dependency is implied by a disposable prototype

### Requirement: Semantic authoring-model comparison
The analysis SHALL compare direct HTML editing, a semantic intermediate page model, and a constrained hybrid.

#### Scenario: Authoring model is selected
- **WHEN** the analysis recommends a production model
- **THEN** it records round-trip, deterministic output, context, member binding, accessibility, extensibility, and security evidence
- **AND** explains why rejected models are insufficient

### Requirement: Catalogue and GraphQL palette evidence
The prototype SHALL demonstrate how machine-readable custom-element declarations and targeted rich GraphQL introspection combine without creating a duplicate metamodel.

#### Scenario: Logical type is selected
- **WHEN** the prototype opens a page for a representative logical type
- **THEN** its palette and inspector can distinguish compatible properties, actions, collections, object composition, HTML, and application elements
- **AND** do not eagerly retrieve the complete application schema

### Requirement: Portable generated-page contract
The analysis SHALL define and test a deterministic custom-page artifact composed from ordinary HTML and public Causeway custom elements.

#### Scenario: Page is exported
- **WHEN** a representative designed page is generated
- **THEN** the output is human-reviewable, source-control friendly, independent of designer runtime, and free from transient preview state
- **AND** it does not replace Causeway grid XML or mutate the server metamodel

#### Scenario: Page is registered with a viewer
- **WHEN** generated output is associated with a public logical type
- **THEN** generic HTMX, Vue, and Svelte router prototypes can select it before generic `<cw-object>` fallback
- **AND** `<cw-object>` remains unaware of that registration

### Requirement: Designer security analysis
The analysis and prototypes SHALL prevent hidden values, passwords, credentials, authorization rules, and sensitive resource content from entering models, history, storage, exports, logs, or diagnostics.

#### Scenario: Sensitive member exists
- **WHEN** a representative type contains hidden or sensitive behavior
- **THEN** the authoring experience exposes no prohibited value or rule
- **AND** generated source contains no sensitive snapshot

### Requirement: Accessible authoring and output evidence
The analysis SHALL evaluate keyboard, focus, announcement, structure, responsive behavior, and generated-page accessibility.

#### Scenario: Author uses the prototype by keyboard
- **WHEN** the representative palette, tree, inspector, and preview are operated without a pointer
- **THEN** all essential authoring actions remain available
- **AND** generated output retains the semantic component accessibility contract

### Requirement: Analysis-only scope
The analysis SHALL NOT add a production page designer, runtime layout language, hosted page store, or collaboration service.

#### Scenario: Analysis completes
- **WHEN** all evidence and roadmap tasks are complete
- **THEN** production packages remain unchanged
- **AND** any implementation work is represented by separately reviewable proposals
