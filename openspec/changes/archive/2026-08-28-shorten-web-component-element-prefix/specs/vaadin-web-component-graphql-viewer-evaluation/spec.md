## MODIFIED Requirements

### Requirement: Composable custom HTML page evaluation
The evaluation SHALL demonstrate the candidate widgets inside a router-selected custom object HTML fragment while preserving the generic viewer's exact-type resolution, canonical route identity, and one disposable route-level `<cw-object-context>`.
Application composition MUST remain possible with ordinary HTML and browser modules rather than requiring Vaadin Flow or Java Vaadin extension APIs.

#### Scenario: Custom object page is selected
- **WHEN** the HTMX router resolves a registered custom fragment for an exact public logical type
- **THEN** the fragment composes semantic Causeway elements and evaluated browser-side Vaadin controls beneath one route object context
- **AND** obtains all domain behavior through GraphQL and browser-side adapters

#### Scenario: Route fragment is replaced repeatedly
- **WHEN** navigation connects and disconnects candidate-backed custom and generic fragments across multiple route generations
- **THEN** listeners, overlays, pending requests, focus, and component state remain generation-scoped
- **AND** stale widgets cannot update the current page

#### Scenario: Application uses only semantic elements
- **WHEN** a custom page author chooses not to use raw Vaadin tags
- **THEN** standard Causeway object, property, action, reference, and collection behavior remains composable through stable `<cw-*>` elements
- **AND** the page does not need Vaadin event or data-provider knowledge
