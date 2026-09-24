## MODIFIED Requirements

### Requirement: Case-preserving Causeway trace export
Causeway SHALL export its semantic observation contextual names without Micrometer's lower-hyphen conversion.
The specialized observation handling SHALL apply only to Causeway's dedicated observation registry and SHALL preserve existing tracing lifecycle, parentage, tags, errors, and scope behavior.
The separate semantic trace-display-name mechanism SHALL restrict automatic-span renaming to the selected Java-agent-created foreground HTTP entry span; naming of other automatic Java-agent spans SHALL remain unchanged.

#### Scenario: Logical identifier contains uppercase characters
- **WHEN** a Causeway contextual name contains a logical identifier such as `isisExtSecMan.ApplicationUser`
- **THEN** the exported span name retains that exact identifier casing
- **AND** automatic Java-agent JDBC, outbound HTTP, and non-selected server span naming remains unchanged

#### Scenario: Foreground entry span receives a semantic trace name
- **WHEN** the dedicated semantic trace-display-name mechanism selects a bounded name for a Java-agent-created foreground HTTP entry span
- **THEN** Causeway updates that entry span to the selected case-preserving name
- **AND** this exception does not change naming behavior for other automatic Java-agent spans
