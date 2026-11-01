## ADDED Requirements

### Requirement: HTMX Petclinic declarative layout demonstration

The HTMX Petclinic application SHALL demonstrate application-authored `<cw-row>`, `<cw-column>`, `<cw-fieldset>`, `<cw-tabgroup>`, `<cw-tab>`, and `<cw-metadata>` composition beneath its existing object context and interaction controller.
HTMX templates MUST NOT duplicate foundation span, tab, metadata-discovery, or panel-dropdown behavior.

#### Scenario: Petclinic layout renders wide and narrow

- **WHEN** the demonstrated exact page is opened at documented wide and narrow viewports
- **THEN** fieldsets and collections use authored column spans at wide width and stack in semantic order at narrow width
- **AND** page navigation, member behavior, collection presentation, and document overflow remain correct

#### Scenario: User operates the HTMX tab group

- **WHEN** the user changes tabs by pointer and keyboard
- **THEN** foundation updates the accessible current panel without an HTMX request or route replacement
- **AND** the application shell and object context remain stable

#### Scenario: HTMX metadata panel resolves

- **WHEN** the demonstrated object's effective metadata fieldset is available
- **THEN** `<cw-metadata>` shows exactly its authoritative properties and an Actions dropdown only when associated actions exist
- **AND** HTMX does not provide hard-coded metadata member ids or action grouping
