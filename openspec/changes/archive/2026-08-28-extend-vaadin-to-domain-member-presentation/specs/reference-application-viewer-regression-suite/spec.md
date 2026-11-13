## ADDED Requirements

### Requirement: Vaadin read-only field and action regression coverage
The pinned Reference Application suite SHALL provide deterministic read-only property and ordinary action targets for every adopted Vaadin presentation family and representative excluded values and controls.
It MUST execute the same authoritative outcomes in default component configuration and explicit native component rollback modes.

#### Scenario: Read-only field families are exercised
- **WHEN** default and native qualification display text, multiline, Boolean, enum, bounded-choice, exact and machine numeric, local date, local time, and local date-time targets
- **THEN** both modes preserve authoritative values, labels, descriptions, disabled reasons, nullability, alignment, wrapping, local semantics, and accessible relationships
- **AND** application renderer precedence and supported fractional precision remain unchanged

#### Scenario: Excluded values remain authoritative
- **WHEN** the corpus displays protected, null, reference, resource, LOB, offset-bearing, zoned, legacy temporal, custom, collection, unsupported, or otherwise excluded values
- **THEN** their reviewed native, application-renderer, or explicit-unsupported presentations remain visible
- **AND** no default adapter turns them into an approximate successful-looking field

#### Scenario: Ordinary action states are exercised
- **WHEN** default and native qualification renders and activates visible enabled, visible disabled, hidden, object, service, parameterless, and parameterized ordinary actions
- **THEN** both modes preserve labels, descriptions, disabled reasons, visibility, exact single request publication, parameter preparation, validation, invocation, result handling, and focus restoration
- **AND** property editor, action-prompt, menu, and shell buttons remain on their reviewed native contracts

#### Scenario: View and edit transitions are compared
- **WHEN** an eligible property enters edit, validates, cancels, saves, fails, and reconciles authoritatively in default and native modes
- **THEN** family selection does not change pending or authoritative values, GraphQL variables, validation, semantic events, or focus intent
- **AND** the final read-only presentation contains no stale editor or duplicate control

### Requirement: Presentation adapter lifecycle and delivery regression coverage
The Reference Application browser suite SHALL exercise read-only field and action adapter loading, replacement, failure, routing, theming, responsiveness, and policy precedence against real HTMX lifecycles.

#### Scenario: Route changes during read-only upgrade
- **WHEN** an eligible default read-only field or action begins asynchronous upgrade and HTMX replaces the route
- **THEN** disconnected work cannot restore the old control, listener, focus, value, error, or route state
- **AND** the current route remains authoritative

#### Scenario: Read-only family failure is injected
- **WHEN** one default field-family module fails while displaying an eligible value
- **THEN** that value rerenders through its matching authoritative native renderer
- **AND** editors, references, actions, other families, values, descriptions, errors, and recoverable focus remain correct

#### Scenario: Action-button failure is injected
- **WHEN** the default action-button module fails before or after an ordinary action connects
- **THEN** ordinary actions rerender as their established native buttons and remain singly operable
- **AND** no duplicate listener, request, control, stale focus target, or toolkit error escapes the bounded failure state

#### Scenario: Route asset isolation is measured
- **WHEN** default qualification visits routes containing distinct combinations of eligible read-only families, editors, references, ordinary actions, and unaffected content
- **THEN** each route requests only the closures selected by connected eligible components
- **AND** unused closures, external requests, unexpected CSP violations, and route-readiness dependencies are absent

#### Scenario: Component policy precedence is exercised
- **WHEN** browser profiles cover explicit component policy, deprecated editor policy, deprecated pilot subsets, conflicting properties, and the no-property default
- **THEN** adapter selection, shell diagnostics, CSP hashes, and requested closures match documented precedence
- **AND** explicit `component-toolkit=native` produces no Vaadin request or hash

#### Scenario: Presentation accessibility matrix runs
- **WHEN** default and native controls are exercised by keyboard at wide and narrow viewports with theme switching, reduced motion, forced colors, disabled reasons, validation, route replacement, and representative errors
- **THEN** there are no unexpected accessibility, console, page, external-request, duplicate-control, overlay, focus, clipping, or overflow failures
- **AND** accessible names, descriptions, state, order, and authoritative outcomes remain equivalent
