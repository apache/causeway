## ADDED Requirements

### Requirement: Petclinic owner agreement PDF presentation

The executable HTMX Petclinic application SHALL demonstrate the automatic PDF reader with a deterministic, realistic multipage agreement between a sample pet owner and the clinic.
The agreement SHALL be presented in the PetOwner secondary column with its redundant property label suppressed while the surrounding section supplies an accessible Agreement heading.

#### Scenario: Owner agreement fixture is queried

- **WHEN** the deterministic PetOwner fixture supplies its PDF property
- **THEN** the authoritative Blob has a stable owner-agreement filename, exact `application/pdf` media type, and repeatable multipage bytes
- **AND** its content identifies the sample owner and relevant pets and contains plausible clinic-agreement sections rather than reader-test instructions

#### Scenario: Agreement is presented at a wide viewport

- **WHEN** the HTMX PetOwner page renders the automatic agreement at the documented wide viewport
- **THEN** its Agreement card occupies the secondary side column after the owner collections instead of spanning the full page below both columns
- **AND** `label-position="NONE"` suppresses the property label so the PDF reader uses the card's available width
- **AND** the Agreement section heading remains the accessible visible heading

#### Scenario: Agreement is presented at a narrow viewport

- **WHEN** the HTMX PetOwner page crosses its established narrow-layout breakpoint
- **THEN** the details and secondary columns stack in their established semantic order
- **AND** the Agreement card, toolbar link, controls, and PDF viewport remain visible and operable without horizontal page overflow

#### Scenario: HTMX user navigates agreement pages

- **WHEN** headless browser coverage repeatedly activates next and previous on the agreement at wide and narrow viewport sizes
- **THEN** requested PDF pages render within the reader viewport while the outer page position remains stable
- **AND** the toolbar stays visible, focus remains within the available page controls, and no browser, worker, resource, or GraphQL failure occurs

#### Scenario: HTMX user changes and restores agreement zoom

- **WHEN** browser automation selects percentage, page-height, page-fit, and page-width zoom choices in the HTMX-hosted agreement reader
- **THEN** the shared foundation selector reports each choice and preserves the current page
- **AND** restoring page width requires no HTMX-specific behavior and causes no host-page movement or horizontal overflow
