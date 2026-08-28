## MODIFIED Requirements

### Requirement: Stable Causeway contract boundary
The analysis SHALL prefer integrations that retain public `<cw-*>` elements, Causeway semantic events, domain behavior, and documented `--causeway-*` customization variables.
Toolkit tags, classes, events, tokens, and global state MUST remain internal unless the final recommendation explicitly identifies a required later specification change.

#### Scenario: Toolkit component is used internally
- **WHEN** a prototype uses toolkit-owned markup or behavior
- **THEN** the evidence shows how Causeway attributes, properties, events, focus, disabled state, validation, and theme tokens map across the adapter boundary
- **AND** application code does not need to address the toolkit directly

#### Scenario: Candidate requires public contract leakage
- **WHEN** a viable integration requires applications to depend on toolkit-specific APIs
- **THEN** the candidate receives an explicit lock-in and migration assessment
- **AND** adoption is deferred to a separate proposal with complete specification deltas
