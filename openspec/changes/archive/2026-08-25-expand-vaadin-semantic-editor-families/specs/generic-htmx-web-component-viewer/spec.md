## ADDED Requirements

### Requirement: Explicit route-lazy field-family delivery
The HTMX shell SHALL expose an explicit bounded allow-list for qualified Vaadin field families and SHALL leave that allow-list empty by default during qualification.
It MUST serve each reviewed family closure from same-origin packaged resources only when an eligible internal adapter requests it.

#### Scenario: No field family is enabled
- **WHEN** the HTMX viewer uses default configuration
- **THEN** the shell advertises no Vaadin field-family policy
- **AND** scalar, numeric, and temporal interactions request zero field-family assets

#### Scenario: Selected families are enabled
- **WHEN** configuration enables basic and local-temporal but not numeric fields
- **THEN** the shell advertises only the normalized supported family names
- **AND** numeric members retain native editors and make zero numeric-closure requests

#### Scenario: Configured value is invalid
- **WHEN** the configured family list contains an unknown or malformed family name
- **THEN** viewer startup or policy rendering rejects it with a bounded configuration error
- **AND** the shell does not emit an ambiguous or broadened allow-list

### Requirement: Exact-hash field-family CSP
The HTMX response CSP SHALL add only the reviewed style hashes needed by explicitly enabled Vaadin reference and field closures.
It MUST retain `style-src-attr 'none'`, same-origin script and connection policy, and no `unsafe-inline` source.

#### Scenario: One field family is enabled
- **WHEN** an HTMX response enables one reviewed field family
- **THEN** `style-src` and `style-src-elem` include that family's exact accepted hash set
- **AND** omit hashes used only by disabled families where the generated policy distinguishes them

#### Scenario: Multiple families are enabled
- **WHEN** several reviewed families are enabled
- **THEN** the response contains the deterministic deduplicated union of their accepted hashes
- **AND** policy output remains stable across repeated renders

#### Scenario: Native rollback is configured
- **WHEN** reference and field Vaadin policies are all disabled
- **THEN** the response returns to the native strict policy without Vaadin hashes
- **AND** no route, GraphQL endpoint, application stylesheet, or canonical identity changes
