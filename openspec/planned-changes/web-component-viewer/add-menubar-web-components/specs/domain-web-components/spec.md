## ADDED Requirements

### Requirement: Causeway menu-bar component vocabulary
The component library SHALL provide `<causeway-menubars>`, `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>` as framework-neutral semantic application-menu components.

#### Scenario: Composite menu bars connect
- **WHEN** `<causeway-menubars>` connects beneath a configured GraphQL client
- **THEN** it obtains and securely parses the effective application-entry menu resource once
- **AND** composes or coordinates present primary, secondary, and tertiary bar components in semantic order

#### Scenario: One bar is used independently
- **WHEN** an application uses a primary, secondary, or tertiary bar without the composite
- **THEN** that component can obtain the same authorized resource and render only its semantic bar

### Requirement: Declarative and generated bar composition
The composite SHALL preserve declaratively supplied semantic bar children and generate only required missing bar roles.

#### Scenario: Children exist before custom-element upgrade
- **WHEN** declarative primary or tertiary children are parsed before registration
- **THEN** the composite captures and reuses them after upgrade
- **AND** does not generate duplicate bars for those roles

#### Scenario: Effective bar is absent
- **WHEN** the effective menu resource contains no visible entries for a bar
- **THEN** the composite does not expose an empty interactive landmark for that bar

### Requirement: Secure effective menu structure rendering
Each bar SHALL securely parse the documented menu-resource subset and preserve its effective ordered menus, sections, service-action references, labels, descriptions, icons, and supported hints.

#### Scenario: Menu resource is parsed
- **WHEN** a bar receives an authorized menu resource
- **THEN** parsing disables external entities, executable markup, and cross-origin expansion
- **AND** unsupported content produces a bounded diagnostic rather than executable DOM

#### Scenario: Bar contains multiple menus and sections
- **WHEN** a bar is rendered
- **THEN** menus, sections, and visible service actions follow effective Causeway order
- **AND** optional presentation metadata is available through documented light-DOM hooks

#### Scenario: Menu reference is invalid
- **WHEN** one entry references an unavailable service action
- **THEN** the bar records a bounded local diagnostic
- **AND** retains unrelated valid entries

### Requirement: Service-action interaction reuse
Menu entries SHALL reuse established semantic parameter, editor, choices, autocomplete, validation, invocation, cancellation, stale-response, and result behavior for rich service actions.

#### Scenario: Service action requires parameters
- **WHEN** a user activates a parameterized menu action
- **THEN** the standard accessible interaction presentation negotiates and validates parameters
- **AND** invokes the existing rich service-action operation with typed values

#### Scenario: Service action returns a result
- **WHEN** invocation returns scalar, object, collection, or void semantics
- **THEN** the menu component publishes the established semantic result event
- **AND** does not impose navigation or result presentation

### Requirement: Dynamic menu visibility and availability
Menu components SHALL honor current hidden and disabled service-action state without exposing authorization rules.

#### Scenario: Action is hidden
- **WHEN** a service action is hidden for the current context
- **THEN** no visible menu entry or sensitive metadata for that action is rendered

#### Scenario: Action is disabled
- **WHEN** a service action is visible but disabled
- **THEN** its entry is non-invokable
- **AND** its established disabled reason is presented accessibly where available

### Requirement: Accessible menu disclosure behavior
Menu bars SHALL use labelled landmarks and keyboard-operable native controls with documented disclosure, traversal, closing, and focus-restoration behavior.

#### Scenario: User opens and closes a menu by keyboard
- **WHEN** the user operates a menu button with Enter or Space and later presses Escape
- **THEN** the menu opens and closes without hover dependency
- **AND** focus returns to the originating menu button

#### Scenario: User traverses controls
- **WHEN** the user uses Tab, Shift+Tab, Home, End, or documented arrow keys
- **THEN** focus follows the documented menu order without becoming trapped

### Requirement: Responsive semantic menu bars
Menu bars SHALL adapt to wide and narrow layouts without changing semantic bar, menu, section, action order, or event contracts.

#### Scenario: Narrow layout is active
- **WHEN** available width requires collapsed presentation
- **THEN** bars and menus use accessible disclosure regions
- **AND** every visible action remains keyboard operable with visible focus

### Requirement: Host-controlled menu policy
Menu components SHALL leave routes, browser history, authentication chrome, automatic home-page invocation, and action-result navigation to the host.

#### Scenario: Host receives an object result
- **WHEN** a service action publishes a semantic object result
- **THEN** the host may navigate or render it according to policy
- **AND** the menu component does not assume HTMX or a canonical route
