## MODIFIED Requirements

### Requirement: Convention-registered private HTML pages
The HTMX viewer SHALL discover trusted `.html` page resources from one documented private classpath root and SHALL register each resource by the exact public logical type represented by its filename.
It SHALL support configured `CACHED` and `RELOAD` resource-page modes, SHALL default to `CACHED`, and MUST keep the registered page set immutable between application-context startups in both modes.

#### Scenario: Application packages an exact logical-type page
- **WHEN** an application packages `META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html`
- **THEN** viewer startup registers the literal resource for exact logical type `petclinic.PetOwner`
- **AND** no application Java bean, annotation, manifest, template controller, or client-side page fetch is required

#### Scenario: Page is supplied by a dependency jar
- **WHEN** one application module or dependency jar contributes a qualifying page beneath the private root
- **THEN** classpath discovery includes that page in the same immutable registry
- **AND** the resource is not exposed by the ordinary static-resource handler

#### Scenario: Cached page content is loaded
- **WHEN** a qualifying resource is decoded during startup in the default `CACHED` mode
- **THEN** it is read as bounded non-empty UTF-8 literal HTML and retained immutably for subsequent renders
- **AND** no expression, route value, object identifier, metamodel value, persistence value, or GraphQL result is interpolated into its text

#### Scenario: Reload page content is initially loaded
- **WHEN** a qualifying resource is decoded during startup in configured `RELOAD` mode
- **THEN** the same bounded non-empty UTF-8 and NUL-content validation runs before registration completes
- **AND** the definition retains authority for its exact logical type without retaining startup content as a stale fallback

#### Scenario: Existing reload page is edited
- **WHEN** the content of an already-registered page resource changes on the running classpath in `RELOAD` mode
- **AND** a subsequent route render selects that page
- **THEN** the viewer opens and validates the resource again and renders its current literal HTML
- **AND** no Spring application-context restart, classpath rescan, public page fetch, or Java fragment-factory invocation is required

#### Scenario: Reloaded content is defective
- **WHEN** an already-registered page in `RELOAD` mode becomes unreadable, oversized, empty, malformed UTF-8, or contains forbidden NUL content
- **THEN** the affected render fails with a bounded safe diagnostic
- **AND** the viewer does not serve cached stale content, expose an absolute resource path, or silently use generic object layout fallback

#### Scenario: Page registration changes while running
- **WHEN** a page resource is added, deleted, renamed, or changed to claim another logical type after viewer startup
- **THEN** the immutable registry does not add, remove, or rename that registration in either mode
- **AND** an application-context restart is required to re-run bounded discovery and conflict validation

#### Scenario: Invalid resource-page mode is configured
- **WHEN** application configuration supplies a resource-page mode other than `CACHED` or `RELOAD`
- **THEN** viewer startup fails through bounded configuration binding
- **AND** it does not infer behavior from the classpath launch mechanism

#### Scenario: Page registration is defective
- **WHEN** a discovered page has an invalid logical-type filename, exceeds a documented bound, is empty, contains malformed UTF-8 or forbidden NUL content, cannot be read, or conflicts with another definition
- **THEN** viewer startup fails with a bounded safe diagnostic
- **AND** the viewer does not silently use generic layout fallback for that defective registration

#### Scenario: Registry discovery is bounded
- **WHEN** classpath discovery reaches the documented finite page-count ceiling
- **THEN** startup rejects additional registrations deterministically
- **AND** does not allocate an unbounded page registry
