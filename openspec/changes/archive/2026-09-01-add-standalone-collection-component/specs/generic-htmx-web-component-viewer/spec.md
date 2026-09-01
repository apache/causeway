## ADDED Requirements

### Requirement: Semantic standalone collection action outcomes
The generic HTMX viewer SHALL present collection-valued action outcomes through `<cw-standalone-collection>` in the stable shell result region.
The viewer MUST retain policy ownership while delegating collection row markup, links, value rendering, responsive presentation, and optional Grid qualification to the semantic component.

#### Scenario: Action returns a collection
- **WHEN** the standard interaction controller publishes a normalized collection-valued action result
- **THEN** the viewer creates one `<cw-standalone-collection>` with an action-derived bounded heading and assigns the normalized result through its property
- **AND** viewer-owned ad-hoc collection list markup is not created

#### Scenario: Collection contains domain objects
- **WHEN** returned rows advertise navigable object metadata
- **THEN** the standalone component presents their semantic object links in the stable result region
- **AND** established HTMX navigation policy handles link activation through the normal navigation event

#### Scenario: Collection result is announced
- **WHEN** standalone result presentation becomes ready or empty
- **THEN** the shell announces the action heading and authoritative finite result count through its established live-region policy
- **AND** the interaction controller's duplicate local result presentation is dismissed as before

#### Scenario: Application handles the result
- **WHEN** configured `causewayHtmxPolicy.handleResult` claims the collection result
- **THEN** the default standalone component is not created
- **AND** the application receives the unchanged semantic result detail

#### Scenario: Action returns another result kind
- **WHEN** an action returns an object, scalar, or void result
- **THEN** existing object navigation, scalar presentation, void refresh, missing-object recovery, and result preservation remain unchanged
- **AND** no standalone collection is created

#### Scenario: Later result replaces the collection
- **WHEN** another action outcome is handled by default policy
- **THEN** the stable result region replaces the prior standalone component according to established result lifecycle
- **AND** prior rows and toolkit state cannot remain interactive or overwrite the newer result

### Requirement: Standalone collection viewer qualification
The Petclinic browser acceptance application SHALL exercise collection-valued action outcomes through the new semantic component under default Vaadin and explicit native component-toolkit policies.
Unexpected GraphQL, CSP, accessibility, console, page, external-request, stale-state, focus, overlay, or overflow failures MUST fail qualification.

#### Scenario: Petclinic action returns objects
- **WHEN** browser acceptance invokes a deterministic collection-valued Petclinic action
- **THEN** the stable shell result contains one ready `<cw-standalone-collection>` with the authoritative object count and links
- **AND** following a result link uses the canonical route lifecycle

#### Scenario: Native toolkit policy runs
- **WHEN** the same collection-result journey runs with `component-toolkit=native`
- **THEN** the component renders equivalent semantic rows without requesting a Vaadin Grid asset
- **AND** no application-specific raw result-list markup is required

#### Scenario: Result lifecycle remains accessible
- **WHEN** a collection result appears, is replaced, or its link receives keyboard focus
- **THEN** heading, count, row navigation, announcements, and focus remain understandable and operable
- **AND** the stable shell and current route retain their established ownership
