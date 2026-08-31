## ADDED Requirements

### Requirement: Canonical self-linked object heading
Every generic or application-authored HTMX object route that renders the standard object header SHALL expose the displayed current-object title as a semantic link to the route's same canonical identity.
The viewer SHALL handle that link through its established semantic navigation policy rather than embedding a second route grammar in component markup.

#### Scenario: User activates the current title
- **WHEN** a user activates the object title on a canonical object route
- **THEN** the standard navigation event identifies the same logical type and opaque identifier as the current route
- **AND** viewer routing retains the canonical encoded object route and ordinary history behavior

#### Scenario: Custom page uses the standard header
- **WHEN** an application-owned resource page composes `<cw-object-header>` beneath the route object context
- **THEN** it receives the same self-linked title and available icon presentation as the generic fallback page
- **AND** requires no application-specific URL, image, or event-handler markup

### Requirement: Petclinic icon-bearing object navigation demonstration
Petclinic SHALL demonstrate authoritative domain icons on standard object navigation links without reproducing icon URLs in its private HTML pages or application stylesheet.

#### Scenario: User views Petclinic object navigation
- **WHEN** an owner page renders its current heading, a pet reference or collection row, and navigable breadcrumbs with available icon metadata
- **THEN** each standard object link presents the corresponding domain icon and title
- **AND** activating each link continues through canonical HTMX object routing

#### Scenario: Automated browser verification
- **WHEN** Petclinic browser coverage inspects representative current-object, property-reference, collection-row, and breadcrumb links
- **THEN** it observes decorative icon images and the expected semantic titles
- **AND** verifies that current-title self-navigation and referenced-object navigation reach the expected canonical routes without browser console errors
