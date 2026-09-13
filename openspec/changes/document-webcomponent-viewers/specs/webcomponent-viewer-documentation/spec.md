## ADDED Requirements

### Requirement: Discoverable Antora viewer guides

The documentation site SHALL provide a web-component viewer component with an overview, shared foundation guidance, and clearly labelled HTMX and Vue guide entry points.
The component SHALL follow existing viewer descriptor, navigation, licensing, and AsciiDoc conventions and be registered in applicable playbooks without introducing references to absent content in historical versions.

#### Scenario: Discover either viewer from the documentation site

- **WHEN** a developer follows the site's viewer navigation
- **THEN** the developer can reach the web-component overview and both viewer guides
- **AND** each guide provides working navigation to shared concepts and relevant GraphQL documentation.

### Requirement: Shared architecture and component guidance

The shared documentation SHALL distinguish host routing, shells, and lifecycle from component-owned domain presentation and GraphQL interactions.
It SHALL describe rich GraphQL prerequisites, client and object contexts, menus, layouts, property display and editing, actions, collection loading and previews, resource/value presentation including PDF, theming, accessibility, and toolkit selection with supported limitations.

#### Scenario: Understand shared responsibilities before customising a viewer

- **WHEN** a developer reads the shared architecture and component pages
- **THEN** the developer can identify which responsibilities belong to the HTMX or Vue host, the shared components, and the GraphQL backend
- **AND** can find prerequisite configuration and documented extension points without relying on internal implementation APIs.

### Requirement: HTMX adoption and customisation guide

The HTMX guide SHALL document dependencies and bootstrap enablement, configuration and defaults, canonical routing, shells, generic and custom pages, resource handling, and a source-grounded Petclinic walkthrough.

#### Scenario: Enable and customise the HTMX viewer

- **WHEN** a developer follows the HTMX getting-started and customisation guidance
- **THEN** the documentation provides the required dependencies, imports, configuration, startup commands, and expected application entry point
- **AND** illustrates supported shell or page customisation while preserving shared component ownership of domain interactions.

### Requirement: Vue adoption and customisation guide

The Vue guide SHALL document frontend and backend prerequisites, package and peer dependencies, custom-element compilation, router/plugin setup, application-owned shells and assets, custom route pages, public integration points, lifecycle, and a source-grounded Petclinic walkthrough.

#### Scenario: Compose an application-owned Vue host

- **WHEN** a developer follows the Vue getting-started and customisation guidance
- **THEN** the documentation provides the required frontend setup, backend assets, startup commands, and expected application entry point
- **AND** demonstrates supported host composition without duplicating GraphQL or domain state in Vue.

### Requirement: Optional security and operational boundaries

Each viewer guide SHALL describe its optional local SecMan integration and distinguish it from the generic viewer.
The guides SHALL explain relevant session, CSRF, resource-access, and server authorization responsibilities and label sample credentials as development-only.
The documentation SHALL provide troubleshooting for common setup, schema, asset, routing, and authentication problems and avoid unsupported feature-parity claims.

#### Scenario: Choose and diagnose a secured viewer setup

- **WHEN** a developer reads a viewer's security and troubleshooting guidance
- **THEN** the developer can identify the additional integration modules and configuration required by the documented secured sample
- **AND** can distinguish browser-session failures from GraphQL configuration or authorization failures without being advised to disable security protections.

### Requirement: Source-grounded examples and maintainable entry points

Published configuration names, defaults, public APIs, resource paths, dependencies, and walkthrough commands SHALL be verified against the current implementation and reference applications.
The Antora guides SHALL be the canonical application-developer guidance while relevant READMEs retain contributor verification instructions and link to the guides.
Existing GraphQL reference material SHALL be cross-linked rather than unnecessarily duplicated.

#### Scenario: Verify a published setup example

- **WHEN** a documentation example is reviewed for publication
- **THEN** its configuration and API usage match current source or sample code
- **AND** execution checks or any limitations preventing execution are recorded accurately.

### Requirement: Rendered-site verification

The documentation change SHALL be validated using `preview.sh` to generate and serve the Antora site when tooling is available.
Verification SHALL cover guide discovery, representative page rendering, code blocks, cross-references, includes, and assets, and SHALL distinguish pre-existing warnings from new defects.
Any blocked checks SHALL be reported explicitly.

#### Scenario: Verify the new documentation in the preview site

- **WHEN** the updated site is generated and inspected through the preview workflow
- **THEN** the overview, shared guidance, both viewer getting-started paths, and security pages render and are reachable
- **AND** the change introduces no unresolved cross-references, missing includes, or missing assets.

#### Scenario: Preview verification is blocked by the environment

- **WHEN** required tools, generated prerequisites, or remote assets prevent preview verification
- **THEN** the blocker and unverified checks are recorded rather than reported as passing.
