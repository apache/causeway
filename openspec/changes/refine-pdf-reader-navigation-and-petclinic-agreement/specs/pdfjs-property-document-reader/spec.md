## ADDED Requirements

### Requirement: Contained PDF navigation and compact reader chrome

The PDF reader SHALL keep page-control scrolling within its owned page viewport and SHALL keep its toolbar available without repositioning the surrounding host document.
The reader toolbar SHALL contain the persistent authoritative resource link alongside page and zoom controls, and the reader SHALL NOT render a separate visible canvas-disclaimer sentence.
Removing that sentence MUST NOT cause the reader to claim that canvas pixels expose semantic PDF text.

#### Scenario: User advances pages at a wide viewport

- **WHEN** the user activates next-page or previous-page while the reader and its toolbar are visible in a wide host page
- **THEN** only the PDF viewport scrolls to the requested page placeholder
- **AND** the surrounding document position remains stable and the toolbar remains visible
- **AND** keyboard focus remains in the page controls, moving to the available opposite control when the activated control becomes disabled at a boundary

#### Scenario: User advances pages at a narrow viewport

- **WHEN** the user activates page navigation after responsive layout has narrowed the reader
- **THEN** the requested page remains reachable within the owned PDF viewport
- **AND** the host page does not acquire horizontal overflow or an unexpected document-level jump

#### Scenario: Reader presents its resource link

- **WHEN** an eligible auto or manual PDF reader is inactive, loading, ready, failed, password-rejected, or stopped by a safety limit
- **THEN** its toolbar contains the ordinary authorized Blob resource link and media type
- **AND** no duplicate resource link is rendered below the page viewport

#### Scenario: Canvas page is presented without a visible disclaimer

- **WHEN** the reader presents page placeholders or rendered canvases
- **THEN** it omits the visible "Rendered pages are canvas images..." sentence
- **AND** the region, page, canvas, status, controls, and resource link retain concise accessible names without inventing PDF text content

#### Scenario: Link-only mode is used

- **WHEN** an eligible PDF property uses link-only mode
- **THEN** ordinary Blob resource-link presentation remains authoritative without a reader toolbar or inline viewport
- **AND** PDF.js remains unloaded as before
