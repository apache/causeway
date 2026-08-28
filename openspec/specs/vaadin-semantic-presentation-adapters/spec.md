# vaadin-semantic-presentation-adapters Specification

## Purpose
TBD - created by archiving change extend-vaadin-to-domain-member-presentation. Update Purpose after archive.
## Requirements
### Requirement: Internal presentation adapter boundary
The viewer SHALL use qualified Vaadin free-core controls only behind Causeway-owned semantic property and action components.
Application markup, selectors, events, GraphQL contexts, routes, and renderer extension points MUST NOT require raw Vaadin elements, events, renderer callbacks, item models, data providers, theme internals, or Shadow DOM.

#### Scenario: Application renders an eligible property
- **WHEN** a custom page contains an eligible `<cw-property>` under the resolved Vaadin component policy
- **THEN** the property MAY use an internal Vaadin read-only field
- **AND** the application continues to consume the same Causeway element, state, classes, and semantic events

#### Scenario: Application registers a specific renderer
- **WHEN** an application renderer wins documented precedence for a value that would otherwise qualify for a Vaadin field
- **THEN** the application renderer remains authoritative
- **AND** toolkit selection does not replace it with a standard field adapter

### Requirement: Qualified read-only field families
The presentation registry SHALL select an internal Vaadin read-only field only when the standard value renderer, semantic descriptor, output shape, and existing Causeway codec satisfy a reviewed family contract.
The displayed value MUST retain the authoritative lexical and semantic meaning without JavaScript numeric, temporal, locale, or Boolean approximation.

#### Scenario: Text family is eligible
- **WHEN** a visible standard string or semantic multiline string has a supported descriptor
- **THEN** the basic read-only field adapter displays the exact value
- **AND** multiline rows, logical-start alignment, wrapping, and responsive behavior preserve the established property contract

#### Scenario: Boolean is eligible
- **WHEN** a visible Boolean has a qualified Vaadin Checkbox presentation
- **THEN** the adapter displays the authoritative checked state using genuine read-only semantics
- **AND** it does not substitute disabled state or expose a value-changing affordance

#### Scenario: Enum or bounded choice is eligible
- **WHEN** a visible enum or bounded scalar choice has a candidate control with genuine read-only semantics
- **THEN** the basic read-only adapter displays the authoritative selected value
- **AND** it presents no enabled picker or value-changing affordance

#### Scenario: Numeric family is eligible
- **WHEN** a visible exact or machine numeric value has a qualified numeric presentation
- **THEN** exact values remain lexical strings and machine values preserve the established formatted meaning
- **AND** no value passes through an additional lossy JavaScript number conversion

#### Scenario: Local temporal family is eligible
- **WHEN** a visible value is `LocalDate`, or `LocalTime` or `LocalDateTime` representable at millisecond precision
- **THEN** the qualified read-only picker displays the same local lexical value
- **AND** browser locale or time zone cannot shift it

#### Scenario: Shape is not qualified
- **WHEN** a value is protected, null, reference, resource, LOB, offset-bearing, zoned, legacy temporal, custom, collection, unsupported, or otherwise unqualified
- **THEN** its established native or application renderer remains authoritative
- **AND** the adapter does not approximate it with a disabled or generic text control

### Requirement: Causeway-owned read-only field semantics
A read-only field adapter SHALL preserve Causeway ownership of visible labels, descriptions, disabled reasons, null and hidden state, errors, responsive layout, styling hooks, and accessible relationships.
The internal Vaadin control MUST be read-only rather than disabled and MUST NOT render a duplicate visible label.

#### Scenario: Described property is rendered
- **WHEN** an eligible property has a visible label and non-redundant description
- **THEN** the Causeway label and description remain the visible explanatory presentation
- **AND** the internal control receives the same accessible name and description without duplicating them

#### Scenario: Disabled property has a reason
- **WHEN** GraphQL disables an eligible visible property with a bounded reason
- **THEN** its value remains readable through a read-only control and no edit affordance is present
- **AND** the Causeway label retains pointer and keyboard access to the disabled-reason tooltip

#### Scenario: Property becomes hidden or erroneous
- **WHEN** a property becomes hidden, partially erroneous, terminally erroneous, or obsolete
- **THEN** the adapter cannot retain a successful-looking stale field
- **AND** the established hidden or bounded Causeway error presentation becomes authoritative

### Requirement: Coherent view and edit family selection
An eligible property SHALL use one semantic family decision across view and edit states while retaining separate Causeway-owned read-only and editor adapters.
A family failure MAY fall back independently, but no adapter transition may change the authoritative value, pending value, validation, event, or GraphQL contract.

#### Scenario: User begins editing
- **WHEN** an eligible read-only property enters editing
- **THEN** the corresponding qualified editor family receives the existing pending value and codec
- **AND** the transition preserves focus intent, label, description, requiredness, and semantic interaction state

#### Scenario: User cancels editing
- **WHEN** an eligible edited property is cancelled
- **THEN** the authoritative read-only value is restored through the current qualified view adapter or its native fallback
- **AND** focus returns according to the established property interaction contract

#### Scenario: Save reconciles authoritatively
- **WHEN** an eligible edit saves successfully
- **THEN** the view adapter displays only the value returned by authoritative context refresh
- **AND** stale pre-save view or editor work cannot replace it

### Requirement: Qualified action-button adapter
A visible ordinary `<cw-action>` SHALL use an internal Vaadin Button under the resolved Vaadin component policy only after the button closure and action semantics pass qualification.
The adapter SHALL delegate activation to the existing Causeway action-request path and MUST NOT invoke GraphQL, navigation, or result handling directly.

#### Scenario: Enabled action is activated
- **WHEN** a keyboard or pointer user activates a qualified action button
- **THEN** `<cw-action>` emits its established semantic request exactly once
- **AND** the existing interaction controller owns parameter preparation, validation, invocation, result handling, and focus restoration

#### Scenario: Disabled action is rendered
- **WHEN** GraphQL disables an action with a bounded reason
- **THEN** the internal button cannot activate
- **AND** its accessible name, disabled state, description, and reason remain available through Causeway-owned presentation

#### Scenario: Hidden action is rendered
- **WHEN** GraphQL hides an action
- **THEN** no native or Vaadin action control remains visible, focusable, or actionable

#### Scenario: Excluded button remains native
- **WHEN** the control is a property edit, save, cancel, clear, action-prompt, shell, or another control outside this qualification
- **THEN** its established native button remains authoritative
- **AND** ordinary action qualification does not silently broaden to it

### Requirement: Independently lazy presentation closures
Read-only field families and ordinary action buttons SHALL use independently lazy same-origin ESM closures with pinned deterministic policy metadata.
A closure MUST load only after an eligible connected presentation or editor requests its family.

#### Scenario: Read-only family is first used
- **WHEN** the first eligible read-only property from one family connects
- **THEN** only that family's closure is requested and upgraded
- **AND** unrelated family and action closures remain unrequested

#### Scenario: Action is first used
- **WHEN** the first visible qualified ordinary action connects on a route with no eligible field
- **THEN** only the action-button closure is requested
- **AND** no field closure is downloaded as a transitive convenience

#### Scenario: Presentation disconnects during loading
- **WHEN** HTMX or another host disconnects an eligible presentation before its closure finishes
- **THEN** late load or definition work cannot attach a control, listener, focus, error, or state to the obsolete route

### Requirement: Presentation-scoped fallback and native rollback
Each read-only family and the action-button family SHALL fail closed independently to its established native Causeway presentation on unsupported shape, native component policy, module failure, definition failure, policy rejection, or stale lifecycle.
Fallback MUST require no GraphQL, route, persisted-data, or application-markup migration.

#### Scenario: Native component policy is active
- **WHEN** the resolved component policy is native
- **THEN** every property value and ordinary action uses its established native presentation
- **AND** no Vaadin reference, field, or action closure is requested

#### Scenario: Read-only family fails
- **WHEN** one field-family closure cannot load or define its required read-only control
- **THEN** that family is disabled for the current document and affected values rerender through their authoritative native renderer
- **AND** current value, errors, descriptions, focus intent, other families, and actions remain unchanged

#### Scenario: Action closure fails
- **WHEN** the action-button closure cannot load or define Vaadin Button
- **THEN** ordinary actions rerender as their established native buttons
- **AND** no duplicate activation listener, request, control, or stale focus target remains

### Requirement: Deterministic secure presentation packaging
Every adopted presentation closure SHALL have pinned direct and transitive inputs, deterministic generated bytes, checksums, legal metadata, vulnerability review, telemetry exclusion, compressed budgets, and exact CSP style hashes.
The policy MUST retain same-origin delivery, `style-src-attr 'none'`, no blanket `unsafe-inline`, and no unapproved external source.

#### Scenario: Generated closure is verified
- **WHEN** packaging or release verification runs
- **THEN** entry points, dependencies, checksums, licenses, vulnerability results, gzip size, telemetry behavior, and style hashes match reviewed policy
- **AND** any drift fails with the affected closure identified

#### Scenario: Candidate requires excluded capability
- **WHEN** a field or button requires Flow state, Binder, Pro code, telemetry, CDN content, blanket inline style, or another unapproved capability
- **THEN** the candidate fails qualification and remains native
- **AND** policy is not broadened to make it pass

### Requirement: Presentation accessibility and lifecycle qualification
Every adopted read-only field and action button SHALL pass keyboard, accessible-name, description, disabled-reason, visible-focus, responsive, reduced-motion, forced-colors, theme, reconnection, failure, console, page-error, external-request, overlay, and overflow qualification in default and native modes.

#### Scenario: Qualification matrix runs
- **WHEN** representative properties and actions load, disable, hide, error, edit, cancel, save, invoke, reconnect, change route, switch theme, and encounter injected module failure
- **THEN** semantic outcomes match native behavior and there are no unexpected accessibility, CSP, console, page, external-request, stale-state, duplicate-control, overlay, focus, or overflow failures

#### Scenario: Native comparison runs
- **WHEN** the same representative journeys run with explicit native component policy
- **THEN** GraphQL outcomes, semantic events, route identities, values, validation, redaction, and reviewed classifications match
- **AND** no Vaadin asset or style hash is present

