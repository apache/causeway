## ADDED Requirements

### Requirement: Qualified PDF Blob property presentation

The standard `<cw-property>` value-renderer policy SHALL qualify an authoritative Blob-shaped value for PDF document reading only when its normalized MIME type is exactly `application/pdf` and its `bytes` field supplies an acceptable authorized same-origin resource URL.
The standard PDF renderer MUST rank above the standard Blob renderer and below application-registered renderers.

#### Scenario: Authorized PDF Blob is visible

- **WHEN** a visible property returns Blob metadata with MIME type `application/pdf` and an acceptable bytes URL and no application renderer claims it
- **THEN** `<cw-property>` applies its effective PDF render mode
- **AND** retains the authoritative Blob name, media type, and resource link

#### Scenario: Filename resembles a PDF without PDF MIME type

- **WHEN** a Blob name ends in `.pdf` but its authoritative MIME type is absent or differs from `application/pdf`
- **THEN** the standard PDF renderer does not claim the value
- **AND** the ordinary Blob renderer remains authoritative

#### Scenario: PDF content is unavailable

- **WHEN** an `application/pdf` Blob has no bytes URL because resource content is forbidden or unavailable
- **THEN** PDF.js is not loaded and no inline reader is manufactured
- **AND** the ordinary bounded Blob metadata presentation remains visible

#### Scenario: Application renderer claims the PDF value

- **WHEN** an application-registered value renderer with higher precedence supports the PDF property
- **THEN** its output remains authoritative
- **AND** PDF-specific authored attributes do not cause the standard reader to mount beside or replace it

#### Scenario: PDF property is editable

- **WHEN** existing property authority allows editing of a PDF Blob
- **THEN** PDF reading does not remove or reinterpret the established edit affordance and interaction lifecycle
- **AND** successful edit refresh replaces and retires the prior reader through ordinary property generation semantics

### Requirement: Authored PDF property presentation attributes

`<cw-property>` SHALL support authored `pdf-render`, `pdf-initial-page`, and `pdf-zoom` attributes as bounded presentation input for the standard PDF renderer.
These attributes MUST NOT alter property identity, GraphQL selection, authorization, value-renderer precedence, resource policy, editability, validation, invocation, or navigation.

#### Scenario: PDF render mode is absent

- **WHEN** an eligible PDF property omits `pdf-render`
- **THEN** the effective mode is `auto`
- **AND** the reader begins initialization after the ready property presentation commits

#### Scenario: Automatic PDF rendering is authored

- **WHEN** an eligible PDF property declares `pdf-render="auto"`
- **THEN** it initializes the complete reader automatically
- **AND** retains the ordinary resource link throughout loading and reading

#### Scenario: Manual PDF rendering is authored

- **WHEN** an eligible PDF property declares `pdf-render="manual"`
- **THEN** it presents an accessible **Preview document** control without importing PDF.js or fetching PDF bytes for rendering
- **AND** activating that control initializes the complete reader

#### Scenario: Link-only PDF rendering is authored

- **WHEN** an eligible PDF property declares `pdf-render="link"`
- **THEN** it uses the ordinary Blob resource-link presentation
- **AND** does not import PDF.js, create a worker, or fetch PDF bytes for rendering

#### Scenario: Initial page is authored

- **WHEN** `pdf-initial-page` is a canonical positive safe integer from 1 through 100000
- **THEN** the reader uses it as the requested initial page and resolves it against the authoritative page count
- **AND** the attribute does not suppress creation of any other page placeholder

#### Scenario: Initial zoom is authored

- **WHEN** `pdf-zoom` is `page-width`, `page-fit`, `actual-size`, or a canonical integer percentage from 25% through 400%
- **THEN** the reader uses it as its initial zoom
- **AND** later user zoom controls do not rewrite the authored attribute

#### Scenario: PDF attribute is invalid

- **WHEN** a PDF render, initial-page, or zoom attribute is blank, malformed, mixed-case, out of range, or unsupported
- **THEN** the corresponding `auto`, page 1, or `page-width` default applies
- **AND** the property emits a bounded presentation diagnostic without exposing Blob content

#### Scenario: PDF attribute changes while connected

- **WHEN** a supported PDF attribute changes on a connected ready property
- **THEN** the current reader generation is retired before the new effective presentation is applied
- **AND** late work from the earlier generation cannot mutate the replacement

#### Scenario: PDF attributes are used on another datatype

- **WHEN** authored PDF attributes appear on a null, scalar, reference, non-PDF Blob, Clob, collection, unsupported value, or application-rendered value
- **THEN** they are inert
- **AND** the established renderer and member semantics remain authoritative

#### Scenario: Effective grid generates a PDF property

- **WHEN** `<cw-object>` generates `<cw-property>` from an effective Causeway grid in this change
- **THEN** the grid does not synthesize PDF-specific attributes
- **AND** the generated property uses foundation PDF defaults when its authoritative value qualifies

### Requirement: PDF property reader composition

An active standard PDF reader SHALL remain inside the property's primary field presentation and SHALL coexist with its resolved label, description, disabled reason, associated actions, edit affordance, and resource link.
PDF reader controls MUST remain presentation-only and MUST NOT publish domain action, property-update, navigation, or result commands.

#### Scenario: Label and description are presented

- **WHEN** an eligible PDF property has authoritative or authored name and description presentation
- **THEN** the reader region remains labelled and described by the same property semantics
- **AND** PDF controls do not replace or duplicate the member heading

#### Scenario: Property is disabled

- **WHEN** an eligible PDF property is disabled by authoritative member state
- **THEN** read-only document viewing and its resource link remain available while the disabled reason retains ordinary presentation
- **AND** no edit or domain mutation becomes available

#### Scenario: Associated action is declared

- **WHEN** a PDF property has a direct associated action
- **THEN** member composition renders the reader as primary presentation followed by the associated action
- **AND** reader controls are not interpreted as associated domain actions
