# Vaadin Semantic Editor Families Specification

## Purpose

Define qualified internal Vaadin free-core adapters for reversible basic, numeric, and local-temporal semantic editors while preserving Causeway-owned contracts, strict delivery policy, and native fallback.
## Requirements
### Requirement: Codec-qualified internal field adapters
The viewer SHALL use internal Vaadin free-core adapters by default only for semantic input families whose existing Causeway value codec is reversible and whose candidate control preserves the advertised value shape.
Application markup and events MUST remain Causeway-owned and MUST NOT require raw Vaadin APIs.

#### Scenario: Basic scalar family is eligible
- **WHEN** the resolved Vaadin policy is active and a semantic editor advertises text, multiline text, protected text, Boolean, enum, or bounded scalar choices with a reversible codec
- **THEN** the registry selects the internal basic adapter
- **AND** the application continues to observe the existing Causeway element and semantic interaction contract

#### Scenario: Numeric family is eligible
- **WHEN** the resolved Vaadin policy is active and a semantic editor advertises an exact or machine numeric codec
- **THEN** exact numbers retain lexical text handling while machine numbers may use numeric controls
- **AND** GraphQL receives the value produced by the existing codec without additional JavaScript number coercion

#### Scenario: Local temporal family is eligible
- **WHEN** the resolved Vaadin policy is active and the value is `LocalDate`, or a `LocalTime` or `LocalDateTime` representable at millisecond precision
- **THEN** the registry selects the corresponding internal picker
- **AND** local value and supported fractional precision survive the semantic pending-value and GraphQL path

#### Scenario: Shape is not qualified
- **WHEN** the value has local temporal precision beyond milliseconds, is offset-bearing, zoned, legacy temporal, resource, custom, reference, collection, or lacks a reversible codec
- **THEN** the field-family adapter is ineligible
- **AND** the existing native or explicit unsupported presentation remains authoritative

#### Scenario: Native policy is selected
- **WHEN** the resolved common toolkit policy is native
- **THEN** every basic, numeric, and local-temporal descriptor is ineligible for the Vaadin adapter
- **AND** no field closure is imported

### Requirement: Causeway-owned interaction semantics
Each field adapter SHALL map Causeway labels, descriptions, required and disabled state, validation, pending values, focus, keyboard clearing, cancellation, and semantic events into its internal control.
It MUST NOT establish a second interaction or validation state machine.

#### Scenario: User changes an editable property
- **WHEN** an internal control emits a value change
- **THEN** the owning Causeway property interaction parses, validates, and saves through its existing context
- **AND** success, failure, cancellation, focus restoration, and semantic events match native behavior

#### Scenario: User changes an action parameter
- **WHEN** an internal control changes a prepared object or service action parameter
- **THEN** the interaction controller updates that parameter through the existing argument negotiation path
- **AND** dependent parameter preparation and submission emit only advertised arguments

#### Scenario: Optional field exposes clearing
- **WHEN** a qualified optional non-protected field has a non-empty value and presents a visible clear affordance
- **THEN** keyboard users can reach that affordance through normal forward and reverse Tab navigation
- **AND** its accessible name identifies the semantic field being cleared
- **AND** keyboard or pointer activation clears through the existing Causeway pending-value path

#### Scenario: Clear control survives owner rerendering
- **WHEN** validation replaces a field adapter while its Causeway-owned clear suffix has focus
- **THEN** the replacement adapter retains the clear-focus request until its visible suffix is ready
- **AND** focus returns to that suffix without exposing toolkit lifecycle details to the application

#### Scenario: Keyboard user clears a field
- **WHEN** the keyboard user activates the Causeway-owned clear affordance
- **THEN** the now-empty clear affordance is removed from the tab sequence
- **AND** focus returns to the field without cancelling the interaction, submitting it, or exposing a second value state

#### Scenario: Validation rejects a value
- **WHEN** client codec or authoritative GraphQL validation rejects the pending value
- **THEN** the Causeway host owns the bounded error and invalid state
- **AND** the internal control cannot submit or display a successful-looking alternative value

#### Scenario: Protected text is edited
- **WHEN** a protected semantic editor is prepared, changed, validated, cancelled, fails, or submits
- **THEN** its prior or pending value is absent from markup, semantic events, errors, diagnostics, operation summaries, and route evidence
- **AND** the internal control initializes empty with appropriate password-autocomplete behavior

### Requirement: Independent lazy family closures
Basic, numeric, and local-temporal Vaadin controls SHALL be delivered as independently lazy same-origin ESM closures with pinned deterministic policy metadata.
An unaffected route with no eligible read-only presentation or editor MUST request none of those closures.

#### Scenario: One family is first used by an editor
- **WHEN** an enabled eligible editor from one family connects before any read-only member from that family
- **THEN** only that family's closure is requested and upgraded
- **AND** checksum, gzip budget, entry points, licenses, telemetry opt-out, and exact style hashes match reviewed policy

#### Scenario: One family is first used by read-only presentation
- **WHEN** an enabled eligible read-only property from one family connects before any editor from that family
- **THEN** the same independently packaged family closure is requested and upgraded
- **AND** editor code does not require a second copy or closure for that family

#### Scenario: Other families are unused
- **WHEN** the active route uses no eligible read-only presentation or editor from another enabled family
- **THEN** the browser makes zero requests for that other closure
- **AND** route readiness does not wait for it

#### Scenario: Closure input drifts
- **WHEN** a direct or transitive version, integrity, license, vulnerability result, generated checksum, compressed size, entry point, telemetry behavior, or style hash differs from policy
- **THEN** verification fails with the changed closure identified
- **AND** packaging cannot silently publish the drifted asset

### Requirement: Family-scoped fallback and rollback
Each field family SHALL fail closed independently to the existing native semantic presentation or editor on unsupported shape, native component policy, module failure, definition failure, or policy rejection.
Fallback MUST preserve Causeway-owned authoritative and pending values and MUST require no GraphQL, route, persisted-data, or application-markup migration.

#### Scenario: Native component policy is configured
- **WHEN** the common component toolkit policy resolves to native
- **THEN** every qualified field family uses its established native read-only presentation and native editor
- **AND** no field-family asset is requested

#### Scenario: Family module fails for a read-only value
- **WHEN** an enabled field closure cannot load or define its required read-only control
- **THEN** only that family is disabled for the current document and the host rerenders through its authoritative native value renderer
- **AND** current value, description, errors, semantic events, and recoverable focus remain correct

#### Scenario: Family module fails for an editor
- **WHEN** an enabled field closure cannot load or define its required editor control
- **THEN** only that family is disabled for the current document and the host rerenders through its native semantic editor
- **AND** current pending value, required state, validation, semantic events, and recoverable focus are retained

#### Scenario: Reference and action families remain independent
- **WHEN** a field family fails while reference and action adapters remain eligible
- **THEN** reference and action loading and behavior remain unchanged
- **AND** the failure does not broaden or eagerly request another closure

#### Scenario: Deprecated subset compatibility is active
- **WHEN** the component and editor common properties are absent and old family configuration enables only a subset
- **THEN** only that normalized editor subset remains eligible
- **AND** read-only presentation and omitted editor families preserve native behavior during the compatibility period

### Requirement: Strict security accessibility and presentation qualification
Every adopted family SHALL pass enforcing strict-CSP, read-only and editor accessibility, keyboard, responsive, theme, reduced-motion, forced-colors, lifecycle, console, page-error, external-request, and overflow qualification.
The policy MUST retain `style-src-attr 'none'` and MUST NOT add blanket `unsafe-inline`.

#### Scenario: Read-only and editor qualification matrix runs
- **WHEN** controls connect, display, focus, enter and leave editing, validate, disable, reconnect, switch theme, and encounter representative errors
- **THEN** there are zero unexpected CSP, accessibility, console, page, external-request, duplicate-control, stale-state, or overflow failures
- **AND** accessible naming, description, keyboard, focus, authoritative values, and fallback behavior match the semantic contract

#### Scenario: Candidate requires unapproved policy
- **WHEN** a control requires blanket inline style permission, external assets, telemetry, Pro code, Flow state, or another excluded capability
- **THEN** that family fails qualification and remains native
- **AND** no broader policy is enabled to make the candidate appear successful

### Requirement: Accessible local-date calendar trigger
Each editable qualified Vaadin date picker SHALL expose its calendar trigger as a field-labelled button in the normal sequential keyboard focus order.
Keyboard use of the trigger MUST preserve Causeway-owned pending value, validation, commit, cancellation, and focus semantics.

#### Scenario: Keyboard user reaches the calendar trigger
- **WHEN** focus is in an editable qualified date input and the user presses Tab
- **THEN** focus moves to the associated calendar trigger
- **AND** the trigger's accessible name identifies the calendar and semantic field

#### Scenario: Keyboard user opens the calendar
- **WHEN** the focused calendar trigger receives Enter or Space
- **THEN** the existing Vaadin calendar overlay opens for date selection
- **AND** no property commit, action-parameter commit, submission, or cancellation occurs merely because focus moved to or activated the trigger

#### Scenario: Date control is not operable
- **WHEN** a qualified date control is read-only or disabled
- **THEN** it does not expose an operable calendar trigger in the Tab sequence
- **AND** its existing read-only or disabled semantic state remains authoritative

#### Scenario: Pinned toolkit structure drifts
- **WHEN** the pinned Vaadin date picker no longer exposes the qualified internal calendar affordance
- **THEN** browser qualification fails with the missing keyboard contract identified
- **AND** applications are not required to address Vaadin shadow-DOM structure directly

### Requirement: Minute-resolution local-time entry
Each editable qualified Vaadin `LocalTime` or `LocalDateTime` control SHALL present time without seconds or fractional seconds and SHALL use the toolkit-supported 15-minute dropdown interval.
The adapter MUST use the public step contract, MUST NOT fabricate private dropdown items, and MUST NOT mutate authoritative state merely by entering edit mode.

#### Scenario: User edits a local time
- **WHEN** a property or action parameter opens an editable qualified `LocalTime` control
- **THEN** the visible entry omits seconds and fractional seconds
- **AND** the picker offers quarter-hour choices through Vaadin's supported overlay

#### Scenario: User edits a local date-time
- **WHEN** a property or action parameter opens an editable qualified `LocalDateTime` control
- **THEN** its integrated time field uses the same 15-minute dropdown interval
- **AND** the local date, local time, and absence of browser timezone conversion remain authoritative

#### Scenario: User cancels a precise existing value
- **WHEN** an authoritative value contains accepted seconds, fractional seconds, or a minute outside a quarter-hour choice and the user cancels without committing a changed picker value
- **THEN** the authoritative value remains unchanged
- **AND** no rounded or truncated value is submitted

#### Scenario: Value is read-only or uses native fallback
- **WHEN** the value is presented read-only or the Vaadin family is ineligible or disabled
- **THEN** its established precision and native semantic behavior remain unchanged

### Requirement: Accessible local-time clock trigger
Each editable qualified Vaadin time picker SHALL expose its clock trigger as a semantic-field-labelled button in the normal sequential keyboard focus order.
This contract SHALL apply to standalone time pickers and the time child of date-time pickers.
Keyboard or pointer use of the trigger MUST preserve Causeway-owned pending value, validation, commit, cancellation, and focus semantics.

#### Scenario: Keyboard user reaches the clock trigger
- **WHEN** focus is in an editable qualified time input and the user presses Tab
- **THEN** focus moves to the associated clock trigger
- **AND** the trigger's accessible name identifies the time picker and semantic field

#### Scenario: Keyboard user opens the time picker
- **WHEN** the focused clock trigger receives Enter or Space
- **THEN** the existing Vaadin time overlay opens with quarter-hour choices formatted to minutes
- **AND** no property commit, action-parameter commit, submission, or cancellation occurs merely because focus moved to or activated the trigger

#### Scenario: Pointer user opens the time picker
- **WHEN** a pointer user activates the visible clock trigger
- **THEN** the existing Vaadin time overlay opens
- **AND** the trigger is not blocked by adapter styling or hidden accessibility state

#### Scenario: Composite date-time order
- **WHEN** a keyboard user traverses an editable qualified date-time picker
- **THEN** the date input, calendar trigger, time input, and clock trigger are reachable in meaningful order
- **AND** reverse Tab traversal returns through the same associated controls

#### Scenario: Time control is not operable
- **WHEN** a qualified time control is read-only or disabled
- **THEN** it does not expose an operable clock trigger in the Tab sequence
- **AND** its existing read-only or disabled semantic state remains authoritative

#### Scenario: Pinned toolkit structure drifts
- **WHEN** the pinned Vaadin time picker no longer exposes the qualified internal clock affordance
- **THEN** browser qualification fails with the missing keyboard and pointer contract identified
- **AND** applications are not required to address Vaadin shadow-DOM structure directly

### Requirement: Single-ring multiline toolkit focus
An editable qualified Vaadin multiline control SHALL display one clear focus indicator rather than combining the application host outline with the toolkit's internal input focus ring.

#### Scenario: Multiline property receives focus
- **WHEN** keyboard focus enters a qualified Vaadin multiline property editor
- **THEN** the toolkit focus ring remains visible
- **AND** no second application-level outline surrounds the complete toolkit host

#### Scenario: Multiline action parameter receives focus
- **WHEN** keyboard focus enters a qualified Vaadin multiline action parameter
- **THEN** it uses the same single-ring presentation as the property editor
- **AND** its clear affordance, resize behavior, validation, and accessible description remain unchanged

#### Scenario: Native textarea receives focus
- **WHEN** native toolkit fallback renders a multiline editor
- **THEN** the application focus-visible outline remains available
- **AND** focus indication is not suppressed by the Vaadin-specific rule

### Requirement: Visibly presented keyboard time overlay
Enter or Space activation of an editable qualified time-picker clock trigger SHALL synchronously present the current Vaadin time overlay while processing the trusted keyboard event.
The qualification contract MUST verify visible overlay presentation and MUST NOT treat the `opened` property alone as sufficient evidence.

#### Scenario: User opens time choices with Enter
- **WHEN** keyboard focus is on the current clock trigger and the user presses Enter
- **THEN** focus returns to the associated time input and the Vaadin overlay is visibly presented
- **AND** quarter-hour choices formatted to minutes are available without committing or cancelling the Causeway interaction

#### Scenario: User opens time choices with Space
- **WHEN** keyboard focus is on the current clock trigger and the user presses Space
- **THEN** the same visible overlay is presented synchronously
- **AND** page scrolling, action invocation, property save, and prompt submission do not occur

#### Scenario: Trigger generation is obsolete
- **WHEN** a trigger or picker has disconnected or been superseded before activation
- **THEN** it does not open an obsolete overlay or move focus
- **AND** the current editor generation remains authoritative

#### Scenario: Overlay state is not visibly presented
- **WHEN** keyboard activation changes picker state but the pinned Vaadin overlay is not `:popover-open` with visible geometry
- **THEN** browser qualification fails
- **AND** the adapter does not report keyboard time selection as operable

### Requirement: Toolkit-owned multiline boundary
Vaadin multiline editors SHALL exclude their slotted internal textarea from application-native textarea border, padding, sizing, and focus-outline selectors.
The toolkit SHALL remain responsible for the one visible editor boundary and focus ring.

#### Scenario: Vaadin multiline editor receives focus
- **WHEN** a qualified multiline property or action parameter receives keyboard focus
- **THEN** the Vaadin input container presents one visible boundary and focus ring
- **AND** its internal `slot="textarea"` element has no application-native border or outline

#### Scenario: Native multiline fallback receives focus
- **WHEN** toolkit policy or family fallback renders an unslotted native textarea
- **THEN** application-native border, padding, sizing, resizing, and focus indication remain available
- **AND** the Vaadin-specific exclusion does not suppress native usability
