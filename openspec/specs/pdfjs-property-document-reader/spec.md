# PDF.js Property Document Reader Specification

## Purpose

Define the deterministic, secure, bounded, accessible, and framework-neutral PDF.js adapter used by qualified Causeway Blob properties.

## Requirements

### Requirement: Deterministic same-origin PDF.js adapter

The foundation SHALL package an exactly pinned Apache-2.0 PDF.js display module, module worker, required support assets, and license material as deterministic same-origin browser resources.
The reader MUST load those resources lazily without a CDN, runtime package installation, remote module, blob worker, or data-URL worker.

#### Scenario: Application contains no active PDF reader

- **WHEN** a page contains no eligible auto reader and no manual reader has been activated
- **THEN** the browser does not import PDF.js or create its worker
- **AND** ordinary properties, Blob links, HTMX, and Vue behavior remain unchanged

#### Scenario: PDF reader initializes

- **WHEN** an eligible auto reader initializes or an eligible manual reader is activated
- **THEN** the foundation imports its pinned same-origin PDF.js display module and configures the pinned same-origin module worker
- **AND** no remote origin or application-specific bundler is required

#### Scenario: Maintainer regenerates PDF.js assets

- **WHEN** the documented regeneration profile runs from the locked dependency tree
- **THEN** it reproduces the committed browser assets and license inventory
- **AND** ordinary Maven packaging verifies those committed assets without running npm or contacting a package registry

### Requirement: Complete progressive PDF document reader

The reader SHALL expose every page of an eligible PDF in authoritative document order within one continuous property-owned reader.
It MUST establish bounded placeholders for the complete page range and render visible and nearby pages progressively rather than allocating every full-resolution canvas at once.

#### Scenario: Multipage document loads

- **WHEN** PDF.js reports a valid document containing multiple pages
- **THEN** the reader exposes an ordered reachable placeholder for every page from 1 through the authoritative page count
- **AND** visible and nearby placeholders receive their corresponding rendered page canvases
- **AND** scrolling can reach the final page without replacing the current Causeway route

#### Scenario: Current page changes by scrolling

- **WHEN** the user scrolls a different page into the reader's current-page position
- **THEN** the bounded current-page status updates to that page number and total page count
- **AND** no domain interaction, GraphQL mutation, or browser-history entry is produced

#### Scenario: User navigates by page controls

- **WHEN** the user activates previous-page or next-page navigation
- **THEN** the reader scrolls the corresponding page placeholder into view and schedules that page for rendering
- **AND** controls are disabled at the first and final page boundaries

#### Scenario: Reader becomes visible at a different size

- **WHEN** responsive layout changes the available reader size
- **THEN** width-dependent page rendering is recalculated after a bounded debounce
- **AND** obsolete render tasks are cancelled before replacement canvases commit

### Requirement: Bounded page and zoom operation

The reader SHALL support initial page selection and bounded zoom without changing the PDF resource, domain state, authored attributes, or canonical route.
Rendered canvas dimensions and device-pixel scaling MUST remain within foundation-owned safety limits.

#### Scenario: Initial page is valid

- **WHEN** the normalized initial page is within the loaded document's page range
- **THEN** the reader establishes all page placeholders and scrolls the selected page into view
- **AND** the current-page status identifies that page

#### Scenario: Initial page exceeds the document

- **WHEN** the normalized initial page exceeds the authoritative page count
- **THEN** the reader clamps it to the final page and publishes a bounded diagnostic
- **AND** every page remains reachable

#### Scenario: Page-width zoom is selected

- **WHEN** effective zoom is `page-width`
- **THEN** each rendered page fits the reader's available inline size while preserving its PDF aspect ratio
- **AND** responsive width changes schedule bounded replacement rendering

#### Scenario: Page-fit or actual-size zoom is selected

- **WHEN** effective zoom is `page-fit` or `actual-size`
- **THEN** the reader respectively fits each page within its configured viewport or uses PDF.js scale 1
- **AND** page order and resource identity remain unchanged

#### Scenario: Percentage zoom is selected

- **WHEN** effective zoom is a canonical percentage from 25% through 400%
- **THEN** the reader renders at that bounded scale
- **AND** zoom controls cannot move beyond those limits

#### Scenario: User changes zoom

- **WHEN** the user activates zoom in, zoom out, or reset
- **THEN** the current reader scale changes in bounded steps and visible pages are rerendered generation-safely
- **AND** the authored `pdf-zoom` attribute is not rewritten

### Requirement: Generation-safe PDF reader lifecycle

The reader SHALL own and deterministically retire its module-loading continuation, PDF.js loading task, document proxy, page and render tasks, observers, canvases, controls, and focus restoration state.
No asynchronous work belonging to an obsolete reader generation may mutate a current property presentation.

#### Scenario: Property presentation is superseded

- **WHEN** the property value, member identity, context generation, renderer, PDF mode, or route is replaced
- **THEN** the active reader cancels render tasks, disconnects observers, destroys PDF.js loading and document state, and releases canvases
- **AND** replacement presentation is not overwritten by late PDF work

#### Scenario: Property disconnects

- **WHEN** a property containing an active or loading reader disconnects
- **THEN** all reader-owned asynchronous work and browser observers are retired
- **AND** no worker, document, canvas, or event listener remains owned by that property

#### Scenario: Resize supersedes page rendering

- **WHEN** a new size or zoom generation starts while one or more pages are rendering
- **THEN** obsolete render tasks are cancelled
- **AND** only canvases from the current generation can commit

### Requirement: Closed PDF feature and security boundary

The reader SHALL render page canvases without mounting PDF.js text, annotation, XFA, scripting, attachment, print, find, outline, thumbnail, or form layers.
It MUST NOT execute embedded PDF JavaScript or solicit a password.
Document size, page count, image pixels, canvas dimensions, concurrent rendering, nearby-page work, and device-pixel scaling MUST be bounded by host-owned policy rather than authored property attributes.

#### Scenario: PDF contains embedded interactive features

- **WHEN** a PDF advertises JavaScript, annotations, forms, attachments, XFA, or external actions
- **THEN** the reader does not mount or execute those features
- **AND** page canvas rendering and the resource link remain isolated from those features

#### Scenario: PDF requires a password

- **WHEN** PDF.js requests a document password
- **THEN** the reader cancels inline loading and presents a bounded unsupported-password status
- **AND** it does not render a password prompt or disclose password state
- **AND** the authorized resource link remains available

#### Scenario: PDF exceeds a reader safety limit

- **WHEN** document metadata or rendering would exceed a configured client safety bound
- **THEN** inline work stops with a bounded accessible status
- **AND** no authored attribute can raise that safety bound
- **AND** the authorized resource link remains available

#### Scenario: PDF resource URL is not acceptable

- **WHEN** the supplied bytes value is absent, malformed, or not an acceptable same-origin resource URL
- **THEN** the reader does not import or fetch PDF content
- **AND** ordinary bounded Blob metadata or link presentation remains authoritative

### Requirement: Accessible PDF reader controls and fallback

The reader SHALL expose a labelled region with document name, loading or failure status, current and total page count, previous and next controls, zoom controls, and the authoritative resource link.
Each page placeholder and canvas MUST expose a bounded page-number label, and every control MUST remain keyboard operable with visible focus.
The reader MUST NOT claim that canvas pixels provide a semantic text alternative for PDF content.

#### Scenario: Reader is loading

- **WHEN** module, worker, document, or initial page work is pending
- **THEN** the reader exposes a polite bounded loading status associated with the property
- **AND** the property label, description, disabled reason, and resource link remain available

#### Scenario: Page canvas is rendered

- **WHEN** a page render commits
- **THEN** its placeholder and canvas identify **Page N of M** without exposing PDF text as invented accessible content
- **AND** keyboard navigation can continue to reader controls and the resource link

#### Scenario: Reader fails

- **WHEN** module import, worker startup, authorized fetch, PDF parsing, or page rendering fails
- **THEN** the failure remains local to that property and is represented by bounded status without resource bytes, sensitive URL material, stack traces, or PDF content
- **AND** the ordinary authorized resource link remains usable

#### Scenario: Application requires an external accessible reader

- **WHEN** a user activates the persistent resource link
- **THEN** the existing resource-controller response and browser handling remain authoritative
- **AND** the inline reader does not alter content disposition, authorization, filename, or media type

### Requirement: Framework-neutral reader behavior

The PDF reader SHALL be owned by the shared foundation and SHALL behave equivalently beneath HTMX and Vue hosts and under native and Vaadin presentation policies.
Hosts and applications MUST NOT duplicate PDF parsing, page rendering, or reader lifecycle policy.

#### Scenario: Same PDF property is shown by HTMX and Vue

- **WHEN** equivalent authored property markup receives the same authorized PDF Blob value
- **THEN** both hosts expose the same render mode, page order, controls, fallback, diagnostics, and lifecycle semantics
- **AND** framework wrapper markup does not become presentation authority

#### Scenario: Presentation toolkit changes

- **WHEN** native or Vaadin presentation policy is selected
- **THEN** the reader retains the same document, page, zoom, safety, and resource-link semantics
- **AND** controls remain accessible and visually coherent without adopting toolkit-specific PDF behavior
