## ADDED Requirements

### Requirement: Declarative unreferenced member component vocabulary

The foundation SHALL register `<cw-unreferenced-properties>`, `<cw-unreferenced-collections>`, and `<cw-unreferenced-actions>` as framework-neutral automatic-composition elements.
Each element MUST consume only the nearest object context, accept no authored member children, and identify members only by authoritative semantic kind and ID.

#### Scenario: Catch-all elements register

- **WHEN** the foundation package is registered in a browser
- **THEN** all three unreferenced member element names resolve to their public constructors
- **AND** their public names, attributes, classes, state hooks, and events do not depend on HTMX, Vue, or a presentation toolkit

#### Scenario: Catch-all contains an authored child

- **WHEN** an unreferenced member element contains an authored member or unsupported direct child
- **THEN** the authored child is excluded from catch-all presentation and a bounded layout diagnostic is emitted
- **AND** generated ownership cannot be forged by an application attribute

### Requirement: Context-scoped unreferenced member allocation

One allocation coordinator per object-context boundary SHALL derive the current authorized member inventory from the context's object description and subtract valid explicit claims bound to that same boundary.
It MUST allocate each remaining `(kind, id)` at most once in authoritative member-map order and MUST NOT use labels, DOM text, CSS classes, route paths, or visibility as identity.

#### Scenario: Explicit member is declared before or after a catch-all

- **WHEN** a valid ordinary property, collection, or action is bound to the same context before or after the corresponding catch-all
- **THEN** that semantic kind and ID is treated as explicitly claimed regardless of document order
- **AND** the catch-all does not retain a duplicate generated renderer after bounded synchronization

#### Scenario: Referenced member is in a hidden panel

- **WHEN** an ordinary member renderer remains bound inside a currently hidden tab or disclosure panel
- **THEN** it remains an explicit claim
- **AND** changing presentation visibility does not move the member into or out of catch-all composition

#### Scenario: Reference is stale or wrong-kind

- **WHEN** a bound requirement names a member absent from the authoritative inventory or requests it using the wrong semantic kind
- **THEN** it does not consume a valid unreferenced member
- **AND** existing local member errors remain independent of allocation

#### Scenario: Nested object context contains a matching member ID

- **WHEN** a nested object context binds a member with the same kind and ID as a member in the enclosing context
- **THEN** each reference participates only in its own nearest boundary's allocation
- **AND** row previews and nested result objects cannot consume enclosing members

#### Scenario: Inert declaration is present

- **WHEN** a preview or action-result declaration has not connected an ordinary member renderer to the current object context
- **THEN** it does not count as an explicit claim
- **AND** a live renderer participates only through its actual nearest context

#### Scenario: Catch-all-generated member connects

- **WHEN** an unreferenced component connects its owned ordinary member descendants
- **THEN** those descendants are excluded from the explicit-claim set used by every catch-all in that boundary
- **AND** their requirement registration cannot cause allocation oscillation

### Requirement: Deterministic unreferenced destination ownership

For each object-context boundary and member kind, the first valid catch-all in document order SHALL be the sole effective allocation destination.
Later destinations of the same kind MUST remain empty and emit at most one bounded duplicate-destination diagnostic per generation.

#### Scenario: One destination exists

- **WHEN** one catch-all for a member kind is connected to an object context
- **THEN** it receives every remaining authorized member of that kind in authoritative order

#### Scenario: Multiple destinations exist

- **WHEN** two or more catch-alls for the same kind are connected to one object context
- **THEN** only the first valid destination in document order receives members
- **AND** every later destination fails closed without duplicating controls

#### Scenario: Effective destination is moved or removed

- **WHEN** document order changes or the effective catch-all disconnects
- **THEN** the coordinator deterministically reallocates the kind to the new first valid destination
- **AND** obsolete generated children and registrations are retired before replacement output becomes presentable

### Requirement: Unreferenced property fieldset composition

`<cw-unreferenced-properties>` SHALL render remaining properties as ordinary `<cw-property>` elements within one owned `<cw-fieldset>` whose name defaults to **Other**.
Generated properties MUST remain read-only unless the catch-all carries the authored boolean `editable` attribute, after which each property's authoritative capability still controls whether editing is available.

#### Scenario: Remaining properties resolve

- **WHEN** authorized unclaimed properties remain for the current context
- **THEN** the component renders them in authoritative order inside one semantic Other fieldset
- **AND** each generated property retains ordinary loading, hidden, disabled, value, validation, editing, and reconciliation behavior

#### Scenario: Application names and enables the fieldset

- **WHEN** the application supplies `name="Additional details"` and `editable`
- **THEN** the generated fieldset uses **Additional details** as its accessible heading and generated properties opt into ordinary edit affordances
- **AND** neither attribute changes property identity or domain capability

### Requirement: Unreferenced collection tab composition

`<cw-unreferenced-collections>` SHALL render remaining collections as ordinary `<cw-collection>` elements within one owned accessible `<cw-tabgroup>` whose name defaults to **Other collections**.
It MUST create one tab per collection in authoritative order, including when exactly one collection remains.

#### Scenario: Remaining collections resolve

- **WHEN** authorized unclaimed collections remain for the current context
- **THEN** each collection receives one tab labelled from its authoritative friendly name
- **AND** the tab contains the required row and twelve-span column around one ordinary collection component

#### Scenario: User operates generated collection tabs

- **WHEN** the user changes generated collection tabs by pointer or keyboard
- **THEN** ordinary tab selection, focus, RTL, mutation, responsive, collection action, preview, range, sorting, and filtering behavior remains foundation-owned
- **AND** collection data is not fetched or reconstructed by the allocation coordinator

### Requirement: Unreferenced action group composition

`<cw-unreferenced-actions>` SHALL render remaining actions as ordinary `<cw-action>` elements in one wrapping horizontal group whose accessible name defaults to **Other actions**.
It MUST preserve authoritative action order and ordinary interaction ownership.

#### Scenario: Remaining actions resolve

- **WHEN** authorized unclaimed actions remain for the current context
- **THEN** the component renders one ordinary action control per remaining action in a labelled wrapping group
- **AND** authorization, hidden and disabled reasons, prompts, parameters, confirmation, invocation, results, navigation, and focus remain ordinary action behavior

#### Scenario: Application names the action group

- **WHEN** the application supplies a bounded `name`
- **THEN** the group uses that value as its accessible name without changing action labels or identities

### Requirement: Unreferenced composition lifecycle and states

Every unreferenced member component SHALL expose `data-causeway-unreferenced-state` as `loading`, `ready`, `empty`, or `error` and SHALL retire generation-scoped work on supersession or disconnection.
Loading, empty, and error states MUST invent no visible fieldset, tabgroup, tab, heading, collection, property, action, or action group.

#### Scenario: No members remain

- **WHEN** every authorized member of the component's kind is explicitly claimed or the inventory contains none
- **THEN** the component enters `empty`
- **AND** it renders no empty presentation container

#### Scenario: Composite claim producer is pending

- **WHEN** `<cw-metadata>`, `<cw-object>`, or another same-context composite has reserved claims for an affected member kind but has not settled them
- **THEN** catch-all allocation for that kind remains non-presentational in `loading`
- **AND** unaffected member kinds may settle independently

#### Scenario: Composite claims resolve

- **WHEN** a pending composite publishes its exact claims or settles empty or error
- **THEN** allocation excludes the resolved claims and presents remaining members once
- **AND** no stable duplicate is exposed between the composite and catch-all

#### Scenario: Context or inventory is unavailable

- **WHEN** no context is available or authoritative object description fails
- **THEN** the component enters `error`, emits a bounded redacted diagnostic, and renders no inferred members
- **AND** sibling semantic components remain usable

#### Scenario: Generation is superseded or disconnected

- **WHEN** identity, context generation, claim sources, connection, or allocation ownership changes during asynchronous work
- **THEN** obsolete work, generated descendants, subscriptions, and claims are retired
- **AND** they cannot mutate or issue semantic reads for the replacement generation

## MODIFIED Requirements

### Requirement: Deterministic member allocation

Every introspected member claimed by automatic XML or declarative composition SHALL be placed at most once according to exact semantic references, unreferenced destinations, and fallback policy.
XML-plan and object-context allocation MUST preserve the same explicit-claim precedence, authoritative order, wrong-kind rejection, and allocate-once semantics.

#### Scenario: Explicit member is also eligible as unreferenced

- **WHEN** a member has already been claimed by a valid explicit layout or bound component reference
- **THEN** an XML unreferenced marker or declarative catch-all does not place it again

#### Scenario: Member is intentionally omitted

- **WHEN** a usable explicit grid or application-authored page neither references a member nor provides the corresponding unreferenced destination
- **THEN** automatic composition does not invent placement for that member

#### Scenario: Layout reference is stale or wrong-kind

- **WHEN** a layout or bound semantic requirement names a missing member or uses a member in an incompatible role
- **THEN** the component records its existing bounded error or diagnostic
- **AND** does not create a broken or duplicate semantic child

### Requirement: Declarative twelve-column row layout

The foundation SHALL register `<cw-row>` and `<cw-column>` as framework-neutral layout containers.
A row MUST accept only direct column children, and a column MUST accept direct fieldset, collection, metadata, tabgroup, unreferenced-properties, unreferenced-collections, or unreferenced-actions children without taking ownership of their domain behavior.
A column SHALL stack multiple valid children in authored order with shared bounded layout spacing.

#### Scenario: Row contains columns

- **WHEN** a row contains valid columns whose normalized spans total no more than twelve
- **THEN** a wide presentation places them on a twelve-track grid according to authored order and span
- **AND** a narrow presentation stacks them in authored order without horizontal document overflow

#### Scenario: Column declares a valid span

- **WHEN** `<cw-column span="x">` declares an integer from 1 through 12
- **THEN** it occupies exactly that many wide-layout tracks
- **AND** the authored value remains inspectable as the column contract

#### Scenario: Column contains multiple layout children

- **WHEN** a column directly contains fieldsets, collections, metadata, tabgroups, or unreferenced member elements
- **THEN** it presents them vertically in authored order with the shared layout gap and start alignment
- **AND** each child retains ownership of its semantic and domain behavior

#### Scenario: Column contains a tab group

- **WHEN** a column directly contains `<cw-tabgroup>`
- **THEN** the tab group remains a valid connected child with its existing tab, panel, focus, mutation, and responsive behavior
- **AND** the column does not translate or reconstruct the tab structure

#### Scenario: Column contains an unreferenced destination

- **WHEN** a column directly contains one of the three unreferenced member elements
- **THEN** it remains a valid connected child while the boundary coordinator owns allocation and the generated ordinary components own member behavior
- **AND** the column does not inspect or reconstruct generated members

#### Scenario: Column span is absent or invalid

- **WHEN** span is absent
- **THEN** the column uses twelve tracks
- **WHEN** span is not an integer from 1 through 12
- **THEN** the column safely uses twelve tracks and emits a bounded diagnostic

#### Scenario: Row or column contains an unsupported child

- **WHEN** a row or column has a direct child outside its allowed element set
- **THEN** that child is excluded from layout presentation and a bounded diagnostic is emitted
- **AND** valid siblings remain connected and usable

### Requirement: Authoritative metadata fieldset component

The foundation SHALL register `<cw-metadata>` as an automatic fieldset bound to the current object's effective fieldset id `metadata`.
It MUST render every ordered authoritative metadata property as an ordinary `<cw-property>` and every associated metadata action as an ordinary `<cw-action>` inside one accessible ellipsis menu attached to the Metadata fieldset heading.
It MUST reserve property and action claims with the current object-context allocation coordinator before asynchronous discovery and settle those claims on every ready, empty, error, superseded, and disconnected path.
It MUST NOT hard-code metadata member ids, infer membership from labels, expose raw layout resources, or render members outside the effective metadata fieldset.

#### Scenario: Effective metadata fieldset resolves

- **WHEN** the current object context supplies an authorized effective grid containing fieldset id `metadata`
- **THEN** `<cw-metadata>` publishes its exact property and action claims and renders its ordered property members in a semantic Metadata fieldset
- **AND** hidden, disabled, editing, validation, and value semantics remain authoritative to each property component

#### Scenario: Metadata fieldset has associated actions

- **WHEN** the effective metadata fieldset contains one or more associated actions
- **THEN** one compact ellipsis trigger in the Metadata heading opens a bounded menu containing those actions in authoritative order
- **AND** the trigger has an explicit accessible name, visible focus, and authoritative expanded state
- **AND** opening or invoking through the menu uses ordinary action accessibility, authorization, disabled reasons, prompts, invocation, results, and focus behavior

#### Scenario: Metadata claims are pending

- **WHEN** metadata is connected but its authoritative effective fieldset has not settled
- **THEN** it reserves property and action allocation without guessing IDs
- **AND** corresponding catch-all components remain non-presentational rather than temporarily duplicating metadata members

#### Scenario: Metadata action menu is dismissed

- **WHEN** an open metadata action menu loses its active interaction through Escape, outside activation, action completion, supersession, or disconnection
- **THEN** the menu closes predictably and obsolete menu work is retired
- **AND** focus remains valid for the current connected component generation

#### Scenario: Metadata fieldset has no actions

- **WHEN** the effective metadata fieldset contains no authorized associated actions
- **THEN** no ellipsis trigger or empty action menu is rendered
- **AND** metadata properties remain available

#### Scenario: Metadata authority is unavailable

- **WHEN** the object context, schema description, effective-grid reference, structural resource, parse result, or exact metadata fieldset is unavailable or invalid
- **THEN** the component settles its reservation empty, fails closed with a bounded status or diagnostic, and invents no member declarations
- **AND** sibling layout and semantic components remain usable

#### Scenario: Metadata generation is superseded

- **WHEN** object identity, effective layout, connection, or component generation changes during asynchronous metadata preparation
- **THEN** obsolete structural-resource, parse, open-menu, and claim-source work is retired
- **AND** it cannot overwrite, append, or reserve members in the newer generation

### Requirement: Layout component diagnostics and host neutrality

Declarative layout and unreferenced member components SHALL publish one bounded composed `causeway-layout-component-diagnostic` event family for invalid nesting, attributes, duplicate destinations, unavailable allocation, and unavailable metadata composition.
HTMX, Vue, and other hosts MUST receive equivalent behavior without owning layout validation, tabs, metadata selection, unreferenced allocation, generated member ordering, or action grouping.

#### Scenario: Host uses layout and unreferenced components

- **WHEN** equivalent declarations are connected beneath an application-owned object context in different hosts
- **THEN** foundation owns structure, responsive spans, tabs, diagnostics, metadata loading, unreferenced allocation, and semantic member composition
- **AND** the host continues to own only its established shell, context, interaction controller, routing, and policy boundaries
