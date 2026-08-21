## ADDED Requirements

### Requirement: Reproducible theming strategy evaluation
The analysis SHALL compare Bootstrap CSS and utilities, Web Awesome components, and Open Props with native browser primitives using pinned versions, common fixtures, and one published evaluation method.
Spectrum Web Components MAY be added as a fallback benchmark only when the primary set cannot provide sufficient evidence.
Material Web and Shoelace MUST remain comparison-only or rejected candidates unless current maintenance evidence changes before evaluation begins.

#### Scenario: Candidate evaluation begins
- **WHEN** a maintainer runs the documented analysis workflow
- **THEN** each primary candidate uses recorded versions, local assets, provenance, license information, and identical representative fixture content
- **AND** the current Causeway implementation is measured as the baseline

#### Scenario: Candidate set changes
- **WHEN** maintenance, licensing, packaging, or technical evidence invalidates a primary candidate
- **THEN** the analysis records the exclusion reason and replacement rationale
- **AND** does not silently compare a materially different candidate set

### Requirement: Representative viewer prototypes
The analysis SHALL prototype the stable application shell, application menus, action prompts, buttons, forms, validation, tabs, properties, collections, tables, loading states, result states, and responsive navigation for every evaluated strategy.
The prototypes MUST include representative long labels, disabled controls, errors, overflow, and narrow layouts rather than only ideal states.

#### Scenario: Desktop prototype is assessed
- **WHEN** a candidate renders the representative fixture at the documented desktop viewport
- **THEN** the evidence covers visual hierarchy, menus, prompts, forms, tabs, collections, tables, focus treatment, and error states
- **AND** records any missing or custom-built behavior

#### Scenario: Narrow prototype is assessed
- **WHEN** the same candidate renders at the documented narrow viewport
- **THEN** navigation, prompts, property layouts, and tables remain operable without page-level horizontal overflow
- **AND** disclosure and focus behavior are captured for keyboard and pointer interaction

#### Scenario: Candidate leads the fixture evaluation
- **WHEN** one or more candidates remain viable after fixture evaluation
- **THEN** each leading strategy receives a bounded integration check against the running Petclinic HTMX viewer
- **AND** fixture-only assumptions and production integration gaps are recorded

### Requirement: Stable Causeway contract boundary
The analysis SHALL prefer integrations that retain public `<causeway-*>` elements, Causeway semantic events, domain behavior, and documented `--causeway-*` customization variables.
Toolkit tags, classes, events, tokens, and global state MUST remain internal unless the final recommendation explicitly identifies a required later specification change.

#### Scenario: Toolkit component is used internally
- **WHEN** a prototype uses toolkit-owned markup or behavior
- **THEN** the evidence shows how Causeway attributes, properties, events, focus, disabled state, validation, and theme tokens map across the adapter boundary
- **AND** application code does not need to address the toolkit directly

#### Scenario: Candidate requires public contract leakage
- **WHEN** a viable integration requires applications to depend on toolkit-specific APIs
- **THEN** the candidate receives an explicit lock-in and migration assessment
- **AND** adoption is deferred to a separate proposal with complete specification deltas

### Requirement: Deterministic asset and supply-chain assessment
Every candidate SHALL demonstrate a credible path to pinned, offline, Maven-driven production packaging without runtime CDN access.
The assessment MUST record direct and transitive dependencies, licenses, notices, asset provenance, update mechanics, and any required npm or bundler step.

#### Scenario: Browser-ready distribution exists
- **WHEN** a candidate supplies browser-ready CSS or ES modules
- **THEN** the analysis demonstrates how those assets can be pinned and packaged into Maven resources
- **AND** distinguishes prototype convenience from the supported production distribution

#### Scenario: Build tooling is required
- **WHEN** selective packaging, compilation, Sass, or bundling is required for a candidate
- **THEN** the analysis provides a reproducible build outline invoked through Maven
- **AND** records generated outputs, lockfiles, cache behavior, and release implications

#### Scenario: Supply-chain gate fails
- **WHEN** licensing, provenance, security, commercial restrictions, or offline packaging cannot satisfy project policy
- **THEN** the candidate is rejected regardless of its numerical score
- **AND** the blocking evidence is retained in the decision record

### Requirement: Measurable quality evidence
The analysis SHALL collect comparable evidence for accessibility, keyboard and focus behavior, responsive presentation, light and dark themes, reduced motion, forced colors, visual consistency, bundle size, request count, startup cost, and rendering performance.
Evidence MUST be generated from documented commands or procedures and retained with the change.

#### Scenario: Accessibility evaluation runs
- **WHEN** a prototype is evaluated
- **THEN** automated accessibility results and manual keyboard journeys cover menus, dialogs, validation, tabs, and responsive navigation
- **AND** focus loss, hidden focused controls, inaccessible names, and contrast failures are recorded as candidate defects

#### Scenario: Visual evaluation runs
- **WHEN** desktop and narrow screenshots are captured in required color and motion preferences
- **THEN** every candidate and the current baseline use the same content, viewport, and state definitions
- **AND** subjective observations are separated from test failures

#### Scenario: Performance evaluation runs
- **WHEN** candidate assets and representative pages are measured
- **THEN** compressed CSS and JavaScript size, request count, loading cost, and relevant rendering timings are recorded
- **AND** full-bundle and selective-import costs are not conflated

### Requirement: Evidence-backed adoption decision
The analysis SHALL publish a weighted decision matrix, hard-gate results, architectural decision record, migration estimate, and explicit recommendation to adopt, adopt a constrained subset, or retain the current implementation.
A production toolkit MUST NOT be added by this analysis change.

#### Scenario: Decision matrix is completed
- **WHEN** all viable candidates have comparable evidence
- **THEN** scores use the published weights for accessibility, compatibility, maintenance, packaging, theming, and performance
- **AND** every score links to evidence or a documented limitation

#### Scenario: Adoption is recommended
- **WHEN** a candidate or constrained subset passes every hard gate and demonstrates sufficient value
- **THEN** the analysis defines a staged migration and rollback outline
- **AND** identifies the separate implementation proposal and specification changes that are required

#### Scenario: Retention is recommended
- **WHEN** no candidate provides sufficient net benefit
- **THEN** the analysis records why the current approach remains preferable
- **AND** identifies focused internal theme or native-platform improvements supported by the evidence
