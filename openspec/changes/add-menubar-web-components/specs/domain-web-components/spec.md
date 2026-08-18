## ADDED Requirements

### Requirement: Causeway menu-bar component vocabulary
The component library SHALL provide `<causeway-menubars>`, `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>` as framework-neutral semantic application-menu components.

#### Scenario: Composite menu bars connect
- **WHEN** `<causeway-menubars>` connects beneath a configured GraphQL client
- **THEN** it discovers the optional application menu capability through targeted introspection
- **AND** coordinates present primary, secondary, and tertiary bar components in semantic order

#### Scenario: One bar is used independently
- **WHEN** an application uses a primary, secondary, or tertiary bar without the composite
- **THEN** that component can obtain the same authorized effective resource and render only its semantic bar
- **AND** does not require a public application-context element

### Requirement: Generation-scoped application-menu coordination
The composite SHALL own one bounded application-menu generation shared by its bars without caching effective content across users, client instances, or explicit refresh generations.

#### Scenario: Composite loads its current generation
- **WHEN** application metadata exposes a safe effective menu resource descriptor
- **THEN** the coordinator reads that metadata once, fetches the opaque resource once with same-origin no-store semantics, and shares one immutable plan with child bars

#### Scenario: Application menu capability is unavailable
- **WHEN** targeted introspection shows that `application` or `menuBars` is absent
- **THEN** the component reports a local bounded unsupported state
- **AND** does not issue an invalid GraphQL operation or invent a client-side menu hierarchy

#### Scenario: Application refreshes menu state
- **WHEN** the application calls `refresh()`
- **THEN** a new generation re-evaluates application metadata, resource content, and current service-action state
- **AND** superseded metadata, resource, and action-state responses cannot replace the newer generation

### Requirement: Declarative and generated bar composition
The composite SHALL preserve declaratively supplied semantic bar children and generate only missing effective non-empty bar roles.

#### Scenario: Children exist before custom-element upgrade
- **WHEN** declarative primary or tertiary children are parsed before registration
- **THEN** the composite captures and reuses them after upgrade
- **AND** does not generate duplicate bars for those roles

#### Scenario: Effective bar is absent
- **WHEN** the effective menu resource contains no visible entries for a bar
- **THEN** the composite does not generate a missing child for that role
- **AND** a declarative child for that role exposes no empty interactive landmark

### Requirement: Secure effective menu structure rendering
Each bar SHALL use shared bounded structural-resource and XML safety rules to preserve the effective ordered menus, sections, service-action references, labels, descriptions, icon hints, and supported presentation data.

#### Scenario: Menu resource is parsed
- **WHEN** a bar receives an authorized menu resource in the documented Causeway namespace
- **THEN** parsing rejects document types, entities, executable markup, cross-origin expansion, malformed nesting, and configured size or complexity limit violations
- **AND** arbitrary response markup is never inserted into the component DOM

#### Scenario: Bar contains multiple menus and sections
- **WHEN** a bar is rendered
- **THEN** menus, sections, and visible service actions follow effective Causeway document order
- **AND** optional presentation metadata is exposed only as text-safe documented light-DOM hooks

#### Scenario: Menu content is partially unsupported
- **WHEN** one local node or action reference is unknown, malformed, stale, or wrong-kind
- **THEN** the bar records a bounded redacted local diagnostic
- **AND** retains unrelated recognized menus, sections, and actions

### Requirement: Coordinated service-action state
Menu components SHALL resolve current hidden and disabled state through established rich service-action wrappers while coordinating reads by logical service type.

#### Scenario: Current menu action state is loaded
- **WHEN** one generation contains actions from one or more logical service types
- **THEN** targeted schema descriptions are cached by the GraphQL client
- **AND** hidden and disabled state reads are grouped by logical service type rather than issued once per action

#### Scenario: Action is hidden
- **WHEN** a canonical service-action wrapper reports hidden for the current interaction
- **THEN** no visible menu entry, label, description, icon hint, or authorization metadata for that action is rendered

#### Scenario: Action is disabled
- **WHEN** a service action is visible but disabled
- **THEN** its entry remains represented and counted as visible
- **AND** it is non-invokable and exposes its established disabled reason accessibly where available

#### Scenario: Group becomes empty
- **WHEN** current state removes every visible action from a section, menu, or bar
- **THEN** empty sections, menus, and bars collapse in that order without an empty interactive landmark

### Requirement: Service-action interaction reuse
Menu entries SHALL reuse established semantic parameter, editor, choices, autocomplete, validation, invocation, cancellation, stale-response, mutation-serialization, and typed-result behavior for rich service actions.

#### Scenario: Service action requires parameters
- **WHEN** a user activates a parameterized menu action
- **THEN** the standard accessible interaction presentation negotiates and validates parameters through a service-bound adapter
- **AND** invokes the advertised rich service-action operation with typed values and no manufactured object target

#### Scenario: Service action is mutating
- **WHEN** a mutating service action is submitted through one application-menu coordinator
- **THEN** it uses the existing top-level mutation field
- **AND** mutating submissions are serialized in submission order

#### Scenario: Service action returns a result
- **WHEN** invocation returns scalar, object, collection, or void semantics
- **THEN** the menu component publishes the established semantic result event and typed result
- **AND** additive target detail identifies the public service logical type without pretending it is an object bookmark

### Requirement: Accessible menu disclosure behavior
Menu bars SHALL use labelled navigation landmarks and keyboard-operable native disclosure and action controls with documented traversal, closing, sibling coordination, and focus restoration.

#### Scenario: User opens and closes a menu by keyboard
- **WHEN** the user operates a menu disclosure with Enter or Space and later presses Escape
- **THEN** the menu opens and closes without hover dependency
- **AND** focus returns to the originating disclosure button

#### Scenario: User traverses controls
- **WHEN** the user uses Tab, Shift+Tab, Home, End, or documented arrow keys
- **THEN** focus follows native document order or documented peer-disclosure order without becoming trapped
- **AND** opening one menu closes sibling menus in the same bar

#### Scenario: Assistive technology encounters a bar
- **WHEN** a non-empty bar is rendered
- **THEN** it exposes a labelled navigation landmark, native buttons, `aria-expanded`, and `aria-controls`
- **AND** it does not misuse ARIA application-menu roles for ordinary page navigation

### Requirement: Responsive semantic menu bars
Menu bars SHALL adapt to wide and narrow layouts without changing semantic bar, menu, section, action order, interaction state, or event contracts.

#### Scenario: Narrow layout is active
- **WHEN** available width crosses the documented narrow threshold
- **THEN** non-empty bars and menus use accessible disclosures in unchanged document order
- **AND** every visible action remains keyboard operable with visible focus and no horizontal page overflow

### Requirement: Observable and customizable menu composition
Generated light DOM SHALL expose stable bar, menu, section, action, state, lifecycle, diagnostic, data-attribute, CSS-variable, and styling hooks without exposing sensitive remote content.

#### Scenario: Application themes menu bars
- **WHEN** an application supplies documented CSS variables and selectors
- **THEN** it can style primary, secondary, tertiary, menu, section, and action regions without replacing semantic behavior

#### Scenario: Partial menu error occurs
- **WHEN** a capability, resource, reference, or service-state operation fails locally
- **THEN** bounded lifecycle and diagnostic events identify the safe failure scope
- **AND** diagnostics omit response bodies, credentials, authorization rules, submitted values, and remote exception text

### Requirement: Host-controlled menu policy
Menu components SHALL leave routes, browser history, authentication chrome, automatic home-page behavior, shell-closing behavior, and action-result navigation or presentation to the host.

#### Scenario: Host receives a service object result
- **WHEN** a service action publishes a semantic object result
- **THEN** the host may navigate, render it, close a shell menu, or do nothing according to policy
- **AND** the menu component does not assume HTMX, a canonical route, or an automatic home action
