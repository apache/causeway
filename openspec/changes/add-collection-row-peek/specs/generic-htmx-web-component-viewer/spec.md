## ADDED Requirements

### Requirement: Default collection row preview resources
The generic HTMX viewer SHALL discover bounded runtime-type preview resources from `META-INF/causeway/webcomponents/previews/<logical-type-name>.html` and SHALL expose safe definitions to empty collection peek declarations through a host resolver.
Preview resources MUST remain presentation-only and MUST NOT select row identity, alter collection projection, bypass member metadata, or invoke domain behavior independently.

#### Scenario: One valid preview resource exists
- **WHEN** exactly one bounded UTF-8 resource with one supported `<cw-peek>` root is discovered for a valid logical type
- **THEN** the viewer registers an immutable inert definition for that exact logical type
- **AND** an empty collection peek can clone its content for an eligible row of that runtime type

#### Scenario: Preview resource is absent
- **WHEN** no registered preview exists for a row's runtime logical type
- **THEN** lookup resolves as absent without failing collection data
- **AND** the collection renders no preview disclosure for that row

#### Scenario: Inline preview content is authored
- **WHEN** a collection's direct `<cw-peek>` has meaningful inline content
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
- **WHEN** an empty peek resolves a valid runtime logical type through the private preview endpoint
- **THEN** the response is privately non-cacheable at the HTTP boundary and distinguished as a Causeway preview resource
- **AND** cached viewer mode reuses the validated inert template while cloning fresh live content per expansion
- **AND** reload mode re-resolves according to established resource-page policy

#### Scenario: Preview lookup fails at runtime
- **WHEN** a lookup returns an unexpected status, malformed body, or unsupported document
- **THEN** the affected row has no expander and the viewer publishes a bounded presentation diagnostic
- **AND** the containing collection remains usable and does not disclose resource bodies or row values

### Requirement: Collection row preview qualification
The Petclinic browser acceptance application SHALL demonstrate inline and runtime-type default collection peeks under default Vaadin and explicit native component-toolkit policies.
Unexpected context, projection, request, disclosure, focus, Escape, refresh, virtualization, overflow, console, page, or external-network failures MUST fail qualification.

#### Scenario: Inline preview is demonstrated
- **WHEN** a Petclinic collection with non-empty inline peek content expands an eligible row
- **THEN** declared properties, actions, collections, layout, row identity, keyboard disclosure, and canonical object links are verified
- **AND** no type-default preview request is made

#### Scenario: Default preview is demonstrated
- **WHEN** a Petclinic collection has an empty peek and a matching resource exists under `previews/`
- **THEN** the runtime-type default renders inside the selected hydrated row context
- **AND** a row whose runtime type has no default exposes no expander

#### Scenario: Single-row and Escape behavior is qualified
- **WHEN** users expand successive rows and press Escape from preview content
- **THEN** only one details subtree remains, prior contexts are retired, focus returns correctly, and no stale preview reopens

#### Scenario: Preview action refreshes the collection
- **WHEN** a deterministic Petclinic action succeeds inside an expanded preview
- **THEN** normal action-result policy remains authoritative
- **AND** the parent collection reloads current authoritative rows and remains collapsed

#### Scenario: Preview property update refreshes the collection
- **WHEN** a deterministic editable property update succeeds inside an expanded preview
- **THEN** the parent collection applies the same authoritative reload and collapsed result used for an action

#### Scenario: Collection lifecycle collapses the preview
- **WHEN** sorting, filtering, paging, reload, responsive Grid replacement, virtual range supersession, or route replacement occurs with a preview open
- **THEN** expansion is not preserved and late preview work cannot alter the current collection
