## MODIFIED Requirements

### Requirement: Default collection row preview resources
The generic HTMX viewer SHALL discover bounded runtime-type preview resources from `META-INF/causeway/webcomponents/previews/<logical-type-name>.html` and SHALL expose safe definitions to empty collection preview declarations through a host resolver.
Preview resources MUST remain presentation-only and MUST NOT select row identity, alter collection projection, bypass member metadata, or invoke domain behavior independently.

#### Scenario: One valid preview resource exists
- **WHEN** exactly one bounded UTF-8 resource with one supported `<cw-preview>` root is discovered for a valid logical type
- **THEN** the viewer registers an immutable inert definition for that exact logical type
- **AND** an empty collection preview can clone its content for an eligible row of that runtime type

#### Scenario: Preview resource is absent
- **WHEN** no registered preview exists for a row's runtime logical type
- **THEN** lookup resolves as absent without failing collection data
- **AND** the collection renders no preview disclosure for that row

#### Scenario: Inline preview content is authored
- **WHEN** a collection's direct `<cw-preview>` has meaningful inline content
- **THEN** the viewer performs no default preview lookup for that declaration
- **AND** a registered runtime-type default cannot merge with or override the inline template

#### Scenario: Preview resource is unsafe or malformed
- **WHEN** a resource exceeds configured bounds, contains invalid UTF-8, has an invalid filename or root, advertises row identity, or contains executable, embedding, event-handler, or unsupported markup
- **THEN** the viewer rejects it with a stable bounded diagnostic
- **AND** no rejected markup enters a live preview or collection data path

#### Scenario: Duplicate preview resources are discovered
- **WHEN** more than one classpath resource claims the same logical type
- **THEN** registry construction fails deterministically with bounded safe source identifiers
- **AND** classpath order does not select an arbitrary definition

#### Scenario: Client resolves a preview
- **WHEN** an empty preview resolves a valid runtime logical type through the private preview endpoint
- **THEN** the response is privately non-cacheable at the HTTP boundary and distinguished as a Causeway preview resource
- **AND** cached viewer mode reuses the validated inert template while cloning fresh live content per expansion
- **AND** reload mode re-resolves according to established resource-page policy

#### Scenario: Preview lookup fails at runtime
- **WHEN** a lookup returns an unexpected status, malformed body, or unsupported document
- **THEN** the affected row has no expander and the viewer publishes a bounded presentation diagnostic
- **AND** the containing collection remains usable and does not disclose resource bodies or row values
