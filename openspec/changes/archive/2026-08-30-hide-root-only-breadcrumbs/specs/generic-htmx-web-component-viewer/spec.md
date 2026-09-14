## MODIFIED Requirements

### Requirement: Petclinic navigable breadcrumbs demonstration
The Petclinic HTMX sample SHALL demonstrate `<cw-breadcrumbs>` through standard navigable-parent annotations and HTML-authored page composition without application-specific breadcrumb rendering or route code.
It SHALL omit breadcrumb presentation for root objects that have no navigable parents.

#### Scenario: Pet owner page is rendered
- **WHEN** the Pet Owner custom page renders for Mary Smith
- **THEN** no breadcrumb landmark or current-only breadcrumb item is rendered
- **AND** the owner header continues to identify Mary Smith

#### Scenario: Pet page is rendered
- **WHEN** Mary Smith's pet Basil is rendered
- **THEN** the breadcrumb landmark contains a Mary Smith ancestor link followed by Basil as the current item
- **AND** the hierarchy is derived from `Pet.petOwner` marked `Navigable.PARENT`

#### Scenario: Visit page is rendered
- **WHEN** Basil's scheduled visit is rendered
- **THEN** the breadcrumb landmark contains Mary Smith and Basil ancestor links in that order followed by the visit as the current item
- **AND** the hierarchy is derived through `Visit.pet` and then `Pet.petOwner`

#### Scenario: User follows a breadcrumb
- **WHEN** a user activates the Mary Smith or Basil ancestor link from a descendant route
- **THEN** the established HTMX semantic navigation bridge installs the corresponding canonical object route
- **AND** no breadcrumb-specific route handler or URL construction is required

### Requirement: Petclinic breadcrumb regression coverage
Petclinic integration and browser acceptance SHALL verify navigable annotations, rich GraphQL breadcrumb metadata, root-only omission, descendant component rendering, accessibility, responsive presentation, and canonical navigation while retaining existing custom-page behavior.

#### Scenario: Rich GraphQL hierarchy is queried
- **WHEN** integration coverage reads metadata for owner, pet, and visit fixtures
- **THEN** it observes deterministic zero-, one-, and two-ancestor breadcrumb chains

#### Scenario: Browser exercises root and descendant hierarchy
- **WHEN** browser automation opens owner, pet, and visit routes at desktop and mobile widths
- **THEN** the owner route exposes no breadcrumb landmark
- **AND** descendant breadcrumb order, current state, focusable links, no-overflow presentation, and navigation are correct
- **AND** no unexpected console, page, resource, or GraphQL failure is observed
