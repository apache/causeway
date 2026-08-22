## ADDED Requirements

### Requirement: Opt-in route-lazy reference widget delivery
The generic HTMX viewer SHALL load the candidate reference-widget closure only when explicit configuration and an eligible semantic reference editor require it.
Generic routes, custom object fragments, menus, and shell behavior that do not use the pilot MUST remain independent of candidate readiness and requests.

#### Scenario: Route contains an enabled candidate reference editor
- **WHEN** route rendering encounters the first eligible explicitly enabled reference editor
- **THEN** the viewer resolves the same-origin packaged candidate entry lazily and upgrades the internal editor
- **AND** the route retains one disposable Causeway context and existing canonical navigation

#### Scenario: Route contains no candidate editor
- **WHEN** a generic or custom route uses existing editors or no reference input
- **THEN** the browser requests no Vaadin asset
- **AND** viewer readiness, menu behavior, custom-fragment composition, and route replacement remain unchanged

#### Scenario: Candidate loading fails
- **WHEN** the route-lazy asset cannot load, initialize, satisfy CSP, or pass supported-browser checks
- **THEN** the viewer uses the existing reference editor or presents a Causeway-owned recoverable failure according to configuration
- **AND** does not leave an unupgraded raw toolkit tag as ordinary domain UI

### Requirement: Production CSP compatibility for candidate widgets
The generic HTMX viewer SHALL preserve a documented security-reviewed Content Security Policy when the candidate pilot is enabled.
The viewer MUST test component connection, overlay operation, interaction, responsive layout, and route disposal with zero unexpected policy violations and MUST NOT require blanket inline-style permission.

#### Scenario: Production-like CSP journey runs
- **WHEN** Petclinic exercises single and multi-reference candidate states under the documented production-like policy
- **THEN** browser violation events, console output, requests, overlays, focus, overflow, and viewer readiness satisfy the accepted baseline
- **AND** the journey fails on any unclassified or newly introduced violation

#### Scenario: Application does not enable the pilot
- **WHEN** an application retains default viewer configuration
- **THEN** its CSP, browser assets, routes, semantic markup, and custom fragment contract remain unchanged
- **AND** no Vaadin dependency is required at browser runtime

### Requirement: Sample-scoped pilot qualification
Petclinic and the vanilla HTML sample SHALL exercise the candidate pilot as explicit qualification consumers before any wider default adoption.
Their browser evidence MUST cover semantic correctness, keyboard operation, accessibility, narrow and themed presentation, cancellation, repeated route replacement, external-request isolation, and rollback.

#### Scenario: Sample qualification passes
- **WHEN** the complete pilot acceptance suite runs headlessly
- **THEN** existing viewer tests and candidate-specific CSP, accessibility, lifecycle, package, bundle, and interaction assertions pass
- **AND** results distinguish adapter defects, toolkit defects, content exceptions, and unsupported GraphQL behavior

#### Scenario: Sample qualification fails
- **WHEN** a hard gate, budget, existing viewer regression, or unsupported production behavior is detected
- **THEN** the pilot remains disabled outside analysis or sample troubleshooting
- **AND** the existing editor remains the supported viewer behavior
