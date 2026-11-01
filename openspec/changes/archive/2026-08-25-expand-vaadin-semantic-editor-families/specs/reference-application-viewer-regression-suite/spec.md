## ADDED Requirements

### Requirement: Vaadin semantic field-family regression coverage
The pinned Reference Application suite SHALL provide deterministic editable property and action-parameter targets for every adopted Vaadin field family and representative unqualified values.
It MUST execute the same authoritative outcomes in explicitly enabled candidate and explicit native modes.

#### Scenario: Basic families are exercised
- **WHEN** qualification edits text, multiline, protected, nullable and required Boolean, enum, and bounded scalar-choice targets
- **THEN** candidate and native modes preserve requiredness, nullability, choices, validation, redaction, submission, and semantic events
- **AND** protected values appear in none of the captured markup, errors, diagnostics, or route evidence

#### Scenario: Numeric families are exercised
- **WHEN** qualification edits boundary machine numbers and exact `Long`, `BigInteger`, and `BigDecimal` lexicals
- **THEN** candidate and native GraphQL variables preserve the established codec result
- **AND** no exact value passes through lossy JavaScript numeric coercion

#### Scenario: Local temporal families are exercised
- **WHEN** qualification edits local date, local time, and local date-time values including supported fractional precision
- **THEN** candidate and native modes preserve the authoritative lexical value
- **AND** browser time zone or locale does not shift the submitted local value

#### Scenario: Unqualified values remain visible
- **WHEN** the corpus advertises offset-bearing, zoned, resource, custom, or other unqualified inputs
- **THEN** inventory and browser evidence retain their reviewed native, graceful-unsupported, or not-exercised classification
- **AND** no candidate adapter turns them into a successful-looking fallback

#### Scenario: Family route isolation is measured
- **WHEN** qualification visits routes containing no eligible member for a packaged family
- **THEN** those routes request zero assets for that family
- **AND** CSP, accessibility, console, page, external-request, and overflow checks remain clean

### Requirement: Field adapter lifecycle regression coverage
The Reference Application browser suite SHALL exercise candidate upgrade and replacement around real HTMX route, property, and action prompt lifecycles.

#### Scenario: Route changes during upgrade
- **WHEN** an eligible field begins asynchronous upgrade and HTMX replaces the route
- **THEN** disconnected work cannot restore the old control, focus, pending value, error, or route state
- **AND** the current route remains authoritative

#### Scenario: Family load failure is injected
- **WHEN** a qualification family module fails to load
- **THEN** its active editor rerenders to the matching native semantic editor with pending state preserved
- **AND** other family and reference closures remain independently usable
