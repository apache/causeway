## ADDED Requirements

### Requirement: Default collection-result presentation resources
The generic HTMX viewer SHALL discover bounded default standalone collection presentations from `META-INF/causeway/webcomponents/collections/*.html` and SHALL key each accepted resource by its canonical logical-type filename.
A default resource MUST be resolved inertly before invocation and MUST NOT execute arbitrary fragment markup or acquire result ownership.

#### Scenario: Valid default resource is discovered
- **WHEN** one valid UTF-8 resource named `petclinic.PetOwner.html` contains exactly one supported standalone collection root and bounded direct-child collection columns
- **THEN** the registry exposes that presentation for canonical logical type `petclinic.PetOwner`
- **AND** cached or reload behavior follows configured resource-page policy with collection-specific diagnostics

#### Scenario: Matching result type is prepared
- **WHEN** an action advertises collection element logical type `petclinic.PetOwner` and has no valid inline override
- **THEN** the viewer resolves and normalizes the matching default before invocation
- **AND** its valid columns can shape the original authoritative result selection

#### Scenario: No default resource exists
- **WHEN** no resource is registered for the advertised element logical type
- **THEN** the action proceeds with generic standalone result presentation
- **AND** an absent presentation does not become an invocation error

#### Scenario: Resource is malformed or duplicated
- **WHEN** a resource is oversized, invalid UTF-8, invalidly named, duplicated, has an unsupported root, or exceeds bounded discovery limits
- **THEN** startup or reload resolution reports a deterministic collection-presentation diagnostic according to established resource policy
- **AND** arbitrary classpath paths, scripts, event handlers, and unsupported elements are not exposed as live result content

#### Scenario: First lookup is cached
- **WHEN** repeated actions use the same unchanged result element logical type
- **THEN** accepted normalized presentation or confirmed absence is reused according to current cache policy
- **AND** redundant same-origin lookups do not delay every invocation

### Requirement: HTMX action-result outlet resolution
The generic HTMX viewer SHALL prefer one unique `<cw-action-results>` in the active route page for successful non-navigating action-result presentation and SHALL retain its stable shell result region as fallback.
Result routing MUST remain host-owned and generation-safe.

#### Scenario: Active page has one outlet
- **WHEN** an action interaction begins while exactly one connected action-result outlet belongs to the active route page
- **THEN** the viewer snapshots that outlet and route generation as the preferred destination
- **AND** a later successful scalar, void-status, or collection presentation is mounted there only while the destination remains current

#### Scenario: Active page has no outlet
- **WHEN** no applicable page outlet exists
- **THEN** successful non-navigating results use the stable shell result region
- **AND** existing applications require no markup migration

#### Scenario: Active page has duplicate outlets
- **WHEN** more than one equally applicable outlet exists in the active route page
- **THEN** the viewer exposes a bounded ambiguity diagnostic and uses the stable shell fallback
- **AND** DOM order is not used to choose an arbitrary owner

#### Scenario: Captured outlet disconnects
- **WHEN** route replacement disconnects the captured outlet before an asynchronous result completes
- **THEN** stale work cannot mount into that outlet or a different page's outlet
- **AND** the still-current host policy may use the stable shell fallback without transferring stale node state

#### Scenario: Application claims the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims a result
- **THEN** neither page outlet nor shell fallback is modified by default policy
- **AND** the application receives the authoritative semantic result detail and additive resolved-presentation snapshot

#### Scenario: Object result is returned
- **WHEN** a successful result advertises one navigable object identity
- **THEN** established canonical object navigation remains authoritative
- **AND** the object is not mounted into an action-result outlet

#### Scenario: Void refresh preserves status
- **WHEN** void-result policy refreshes the active route while preserving current result status
- **THEN** the still-current status is rehomed to the unique equivalent outlet in the refreshed page or stable shell fallback
- **AND** obsolete outlet or route generations cannot reclaim it

## MODIFIED Requirements

### Requirement: Semantic standalone collection action outcomes
The generic HTMX viewer SHALL present collection-valued action outcomes through `<cw-standalone-collection>` in the resolved action-result outlet or stable shell fallback.
The viewer MUST retain policy ownership while delegating collection row markup, links, value rendering, responsive presentation, and optional Grid qualification to the semantic component.

#### Scenario: Action returns a collection
- **WHEN** the standard interaction controller publishes a normalized collection-valued action result with an immutable resolved presentation snapshot
- **THEN** the viewer creates one live `<cw-standalone-collection>`, applies that snapshot, and assigns the normalized result through its property
- **AND** the authored declaration node is neither moved nor reused as the live result node

#### Scenario: Collection contains selected domain values
- **WHEN** returned rows advertise navigable object metadata and authoritative wrappers selected for resolved columns
- **THEN** the standalone component presents semantic object links and declared cells in the resolved result destination
- **AND** established HTMX navigation policy handles link activation through the normal navigation event

#### Scenario: Collection result is announced
- **WHEN** standalone result presentation becomes ready or empty
- **THEN** the shell announces the effective action heading and authoritative finite result count through its established live-region policy
- **AND** the interaction controller's duplicate local result presentation is dismissed as before

#### Scenario: Application handles the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims the collection result
- **THEN** the default live standalone component is not created
- **AND** the application receives unchanged normalized result data plus additive presentation context

#### Scenario: Action returns another result kind
- **WHEN** an action returns an object, scalar, or void result
- **THEN** existing object navigation, scalar presentation, void refresh, missing-object recovery, and result preservation remain unchanged apart from resolved outlet placement
- **AND** no standalone collection is created

#### Scenario: Later result replaces the collection
- **WHEN** another action outcome is handled by default policy
- **THEN** the resolved result destination replaces the prior live presentation according to established result lifecycle
- **AND** prior rows, outlet ownership, and toolkit state cannot remain interactive or overwrite the newer result

### Requirement: Standalone collection viewer qualification
The Petclinic browser acceptance application SHALL exercise default and action-specific collection-valued outcomes through semantic action-result outlets under default Vaadin and explicit native component-toolkit policies.
Unexpected GraphQL, fragment, CSP, accessibility, console, page, external-request, stale-state, focus, overlay, route, or overflow failures MUST fail qualification.

#### Scenario: Petclinic action uses a type default
- **WHEN** browser acceptance invokes a deterministic collection-valued action with a registered element-type presentation and no inline override
- **THEN** the active page outlet contains one ready standalone collection with authoritative object count, selected columns, icons, and links
- **AND** following a result link uses the canonical route lifecycle

#### Scenario: Petclinic action uses an inline override
- **WHEN** another authored action returning the same element type declares its own direct-child standalone collection
- **THEN** the result uses exactly the inline heading and columns rather than the type default
- **AND** the original GraphQL invocation contains the bounded authoritative fields needed by that presentation

#### Scenario: Native toolkit policy runs
- **WHEN** the same default and inline collection-result journeys run with `component-toolkit=native`
- **THEN** each outlet renders equivalent semantic rows without requesting a Vaadin Grid asset
- **AND** no application-specific raw result-list markup or follow-up row request is required

#### Scenario: Result lifecycle remains accessible
- **WHEN** a result appears, is replaced, survives a permitted refresh, falls back, or its link receives keyboard focus
- **THEN** region naming, heading, count, row navigation, announcements, and focus remain understandable and operable
- **AND** the stable shell, current route, and application override retain their established ownership
