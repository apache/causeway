## ADDED Requirements

### Requirement: Optional semantic reference widget implementation
The semantic editor registry SHALL support an optional internal widget implementation for eligible single-reference and multi-reference inputs without changing the public Causeway element, context, identity, validation, or event contracts.
The registry MUST retain the existing editor for disabled configuration, unsupported descriptors, failed qualification gates, and rollback.

#### Scenario: Eligible single-reference editor is enabled
- **WHEN** an introspected single-reference property or action parameter is eligible and the pilot is explicitly enabled
- **THEN** the registry may render the internal candidate control beneath the existing semantic Causeway component
- **AND** labels, required state, disabled reason, current value, choices, validation, and semantic changes remain Causeway-owned

#### Scenario: Eligible multi-reference editor is enabled
- **WHEN** an introspected member exposes supported multi-reference pending-value and validation semantics and the pilot is explicitly enabled
- **THEN** the registry may render internal tokenized reference selection with stable identities and deterministic ordering
- **AND** application markup and listeners do not require toolkit-specific tags or events

#### Scenario: Reference descriptor is unsupported
- **WHEN** a reference input lacks stable identity, authoritative list semantics, bounded choices, or another required pilot capability
- **THEN** the registry selects the existing supported editor or presents the existing semantic unsupported state
- **AND** does not expose a partially functional raw candidate control

### Requirement: Toolkit-neutral reference interaction lifecycle
Candidate-backed reference editors SHALL follow existing Causeway cancellation, route generation, connection, disconnection, focus, validation, and pending-value lifecycle behavior.
Toolkit overlays, listeners, callbacks, and caches MUST remain subordinate to the owning semantic component and disposable route context.

#### Scenario: Candidate-backed editor disconnects
- **WHEN** HTMX replacement, custom-fragment navigation, or ordinary DOM removal disconnects the editor
- **THEN** pending lookup work, toolkit listeners, and overlays are cancelled or removed
- **AND** late callbacks cannot mutate the current route or retain hidden focus

#### Scenario: Validation rejects a candidate value
- **WHEN** Causeway conversion or server validation rejects the selected reference value
- **THEN** the semantic component presents the authoritative error and reconciles the internal control to the accepted pending state
- **AND** the toolkit does not independently commit the rejected value
