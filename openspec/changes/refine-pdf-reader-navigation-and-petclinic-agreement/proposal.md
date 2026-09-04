## Why

The PDF reader's page controls currently use document-level scrolling at wide viewport sizes, which can move the reader toolbar out of view and make repeated navigation awkward.
The reader and Petclinic example also need a more compact, realistic presentation: the persistent download link belongs with the controls, the visible canvas disclaimer is unnecessary, and the sample should represent a pet-owner clinic agreement in a side column without a redundant property label.

## What Changes

- Keep previous-page and next-page navigation inside the PDF reader viewport so the host page and toolbar remain in place at wide and narrow responsive sizes.
- Move the persistent authorized PDF download link into the reader toolbar while preserving it during inactive, loading, ready, failure, password-rejection, and safety-limit states.
- Remove the visible "Rendered pages are canvas images..." note without claiming that canvas pixels provide semantic document text.
- Replace the synthetic reader-demo PDF with a deterministic, realistic multipage pet-owner agreement fixture.
- Present the automatic owner agreement in the Petclinic page's secondary side column in both HTMX and Vue.
- Suppress the agreement property's own label with `label-position="NONE"` so the reader uses the full card width while the surrounding Agreement section supplies its heading.
- Retain manual and link-only examples and the existing PDF authorization, renderer-precedence, security, safety, and lifecycle contracts.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `pdfjs-property-document-reader`: Constrain page-control scrolling to the reader viewport, place the persistent resource link in the toolbar, and remove the visible canvas disclaimer while retaining honest accessibility semantics.
- `generic-htmx-web-component-viewer`: Refine the HTMX Petclinic owner-agreement fixture, side-column composition, label suppression, and wide/narrow browser acceptance coverage.
- `generic-vue-web-component-viewer`: Keep the Vue Petclinic owner-agreement example and responsive composition equivalent to HTMX.

## Impact

The change affects the shared PDF reader controller, renderer markup, foundation styling and tests, the shared Petclinic PDF fixture and owner property metadata, HTMX and Vue PetOwner pages, application styling, browser acceptance tests, and related documentation.
It adds no dependency, authentication behavior, GraphQL policy, route format, or public custom element.
