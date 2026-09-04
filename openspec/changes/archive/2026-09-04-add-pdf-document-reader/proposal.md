## Why

`<cw-property>` currently presents a Blob, including an `application/pdf` Blob, as a resource link even when the authorized GraphQL value already provides the MIME type and a secured same-origin bytes URL.
Applications need an integrated read-only PDF document reader that can display every page without replacing authoritative property, resource, authorization, or download semantics.

## What Changes

- Add a Mozilla PDF.js-backed document reader for `<cw-property>` values whose authoritative Blob MIME type is `application/pdf` and whose authorized `bytes` URL is available.
- Render the complete document as an inline, progressively loaded sequence of pages while keeping a visible ordinary resource link as fallback.
- Add the authored-only `pdf-render="auto|manual|link"` attribute, defaulting to `auto`.
- In `auto` mode, initialize the document reader automatically when the property becomes ready and render pages progressively as needed.
- In `manual` mode, retain an accessible **Preview document** activation control and initialize the same complete reader only after activation.
- In `link` mode, retain the existing Blob resource-link presentation and do not load PDF.js or fetch PDF bytes for rendering.
- Define a small bounded authored-only PDF presentation vocabulary for initial page and zoom behavior during design, without projecting those attributes from effective grids in this change.
- Package a pinned Apache-2.0 PDF.js distribution and worker as deterministic same-origin foundation assets that are loaded only when an eligible PDF reader is initialized.
- Preserve application value-renderer precedence, existing GraphQL resource policy, authorized download behavior, property labels and descriptions, edit eligibility, lifecycle generation safety, and native/Vaadin host parity.
- Fail safely to bounded status plus the existing resource link when PDF.js, its worker, the PDF resource, or document rendering is unavailable.
- Reject password-protected PDFs as unsupported for inline reading while retaining the authorized resource link.
- Exclude search, text selection, print, outline, annotations, forms, attachments, and embedded PDF JavaScript from the reader introduced by this change.

## Capabilities

### New Capabilities

- `pdfjs-property-document-reader`: Defines deterministic PDF.js packaging, complete progressive page rendering, controls, lifecycle, security, accessibility, failure handling, and resource-link fallback.

### Modified Capabilities

- `domain-web-components`: Extends `<cw-property>` Blob presentation and authored presentation attributes for authoritative `application/pdf` values.
- `generic-htmx-web-component-viewer`: Allows the packaged same-origin PDF.js module and worker under the viewer's strict content-security policy without allowing remote viewer dependencies.

## Impact

The change affects the framework-neutral value-renderer registry, `<cw-property>` presentation state, a new PDF reader presentation element or controller, foundation styles and static assets, deterministic npm generation and license verification, HTMX CSP, shared browser tests, and representative HTMX and Vue sample acceptance coverage.
It reuses the existing Blob `name`, `mimeType`, and secured `bytes` URL and therefore does not require a GraphQL schema or resource-controller change.
The exact PDF MIME type remains authoritative; filenames and labels do not trigger PDF rendering.
Applications can use `pdf-render="link"` to retain the existing Blob-only presentation.

## Future Direction (Out of Scope)

A later proposal may introduce an application annotation such as `@PdfJsViewer` and corresponding metamodel facets so domain-authored PDF presentation defaults can flow through GraphQL metadata into generated and explicitly authored `<cw-property>` elements.
That future work must define annotation placement, inheritance, precedence against authored HTML, metadata exposure, and authorization-neutral facet projection.
This change does not add the annotation, facets, GraphQL fields, effective-grid propagation, or any metadata-to-attribute mapping.
