## MODIFIED Requirements

### Requirement: HTMX action-result outlet resolution
The generic HTMX viewer SHALL prefer one unique `<cw-action-results>` in the active route page for successful non-navigating action-result presentation and SHALL retain its stable shell result region as fallback.
Result routing MUST remain host-owned and generation-safe, while the resolved outlet's normalized presentation style controls only the visual surface.

#### Scenario: Active page has one outlet
- **WHEN** an action interaction begins while exactly one connected action-result outlet belongs to the active route page
- **THEN** the viewer snapshots that outlet and route generation as the preferred destination
- **AND** a later successful scalar, void-status, or collection presentation is mounted there only while the destination remains current

#### Scenario: Inline result is outside the viewport
- **WHEN** the current host mounts a successful non-navigating result outside the visible viewport into an `INLINE` outlet
- **THEN** the viewer scrolls the result outlet into view without moving keyboard focus
- **AND** reduced-motion preference disables animated scrolling

#### Scenario: Dialog or sidebar result is mounted
- **WHEN** the resolved outlet normalizes to `DIALOG` or `SIDEBAR`
- **THEN** the viewer supplies the originating action focus target and mounts the unchanged host-owned result presentation into that outlet
- **AND** opening the styled surface does not scroll the underlying route or alter result data

#### Scenario: Styled result is dismissed
- **WHEN** a dialog or sidebar dismiss control or supported Escape lifecycle requests dismissal
- **THEN** the viewer clears the current result through the established destination lifecycle
- **AND** focus returns to the eligible originating object-action or service-action control when it remains connected

#### Scenario: Active page has no outlet
- **WHEN** no applicable page outlet exists
- **THEN** successful non-navigating results use the stable shell result region and its normalized presentation style
- **AND** existing applications require no markup migration

#### Scenario: Active page has duplicate outlets
- **WHEN** more than one equally applicable outlet exists in the active route page
- **THEN** the viewer exposes a bounded ambiguity diagnostic and uses the stable shell fallback
- **AND** DOM order is not used to choose an arbitrary owner or presentation style

#### Scenario: Captured outlet disconnects
- **WHEN** route replacement disconnects the captured outlet before an asynchronous result completes
- **THEN** stale work cannot mount into that outlet, reopen its styled surface, or use a different page's outlet
- **AND** the still-current host policy may use the stable shell fallback without transferring stale node or focus state

#### Scenario: Application claims the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims a result
- **THEN** neither page outlet nor shell fallback is modified or opened by default policy
- **AND** the application receives the authoritative semantic result detail and additive resolved-presentation snapshot

#### Scenario: Object result is returned
- **WHEN** a successful result advertises one navigable object identity
- **THEN** established canonical object navigation remains authoritative
- **AND** no inline, dialog, or sidebar result surface is mounted

#### Scenario: Void refresh preserves status
- **WHEN** void-result policy refreshes the active route while preserving current result status
- **THEN** the still-current status is rehomed to the unique equivalent outlet in the refreshed page or stable shell fallback
- **AND** obsolete outlet, route, surface, or focus generations cannot reclaim it

## ADDED Requirements

### Requirement: Action-result presentation style qualification
The Petclinic browser acceptance application SHALL exercise `INLINE`, `DIALOG`, and `SIDEBAR` action-result outlets under default Vaadin and explicit native component-toolkit policies.
Unexpected invocation, routing, focus, Escape, backdrop, overflow, responsive, replacement, dismissal, announcement, console, page, or external-request failures MUST fail qualification.

#### Scenario: Inline result is qualified
- **WHEN** a deterministic Petclinic action returns a non-navigating result to an `INLINE` outlet
- **THEN** sticky-header-aware reveal, links, values, announcements, replacement, and dismissal remain operable
- **AND** long result content scrolls within a bounded area above a visible Dismiss control
- **AND** the result surface introduces no modal or sidebar behavior

#### Scenario: Dialog result is qualified
- **WHEN** a deterministic Petclinic action returns a result to a `DIALOG` outlet
- **THEN** labelled modal semantics, backdrop, initial focus, Tab containment, Escape, explicit dismissal, replacement, and origin focus restoration are verified
- **AND** bounded result scrolling keeps the Dismiss control visible below the content
- **AND** route content cannot remain interactively exposed through stale modal state

#### Scenario: Sidebar result is qualified
- **WHEN** a deterministic Petclinic action returns a result to a `SIDEBAR` outlet
- **THEN** right-side placement, non-modal page access, ordinary Tab order, Escape while focused within, explicit dismissal, replacement, and origin focus restoration are verified
- **AND** wide and narrow viewports retain bounded internal result scrolling above a visible Dismiss control with no horizontal document overflow

#### Scenario: Styled result respects established ownership
- **WHEN** duplicate, disconnected, superseded, application-claimed, object-valued, or void-refresh result paths are exercised
- **THEN** established outlet fallback, canonical navigation, application ownership, preservation, and stale-generation behavior remain authoritative
- **AND** presentation style cannot select a destination or reinterpret the result
