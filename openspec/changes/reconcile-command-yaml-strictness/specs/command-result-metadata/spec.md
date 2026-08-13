## ADDED Requirements

### Requirement: Every replay import entry point uses the strict decoder

The system SHALL decode YAML at every replay import entry point — the unified manager importer and the retained legacy replay-manager importer — using the strict replay-import decoder. The strict decoder accepts the wrapped `CommandExportDto` multi-document form and the plain `CommandDto` multi-document form, rejects a YAML list root, and surfaces a failure when the input cannot be decoded. A malformed replay YAML upload MUST therefore be reported as a failure and MUST NOT be silently treated as an empty import at any replay import entry point. The general-purpose plain-`CommandDto` YAML API used by non-replay callers is unaffected by this requirement.

#### Scenario: Legacy importer rejects malformed YAML

- **GIVEN** a malformed YAML file uploaded through the legacy replay-manager import action
- **WHEN** the import is performed
- **THEN** the import fails with an error
- **AND** no commands are imported
- **AND** the failure is reported rather than a silent successful empty import

#### Scenario: Legacy importer accepts a canonical result-bearing stream

- **GIVEN** a valid wrapped `CommandExportDto` multi-document stream uploaded through the legacy importer
- **WHEN** the import is performed
- **THEN** each embedded command is persisted for replay
- **AND** any present result bookmark is retained

#### Scenario: Legacy importer accepts a legacy multi-document stream

- **GIVEN** a valid plain `CommandDto` multi-document stream uploaded through the legacy importer
- **WHEN** the import is performed
- **THEN** each command is persisted for replay in document order

#### Scenario: Non-replay YAML API is unchanged

- **WHEN** a non-replay caller uses the general-purpose plain-`CommandDto` YAML API
- **THEN** that API retains its previously accepted list and multi-document behavior
