## Context

The standard foundation Blob renderer currently recognizes an object containing `name`, `mimeType`, and `bytes`, then presents its authorized bytes URL as a resource link.
The rich GraphQL schema already supplies those fields for visible Blob properties and keeps content availability under the existing `FORBIDDEN`, `DIRECT`, or `ATTACHMENT` resource policy.
An `application/pdf` value therefore has enough authoritative public information for a browser document reader without schema, metamodel, or resource-controller changes.

The foundation is framework-neutral and is served as static ES modules from a Maven JAR rather than built from a root npm package.
Optional Vaadin integrations establish a precedent for pinned npm subprojects that generate reviewed assets and licenses into the foundation JAR.
HTMX applies a strict same-origin content-security policy, while Vue imports the same foundation modules from the application origin.

The requested reader displays every page but does not need search, text selection, printing, outlines, annotations, forms, attachments, or password handling.
That scope favors the PDF.js display API and a Causeway-owned continuous reader over embedding the complete Mozilla viewer application.

## Goals / Non-Goals

**Goals:**

- Automatically recognize an authoritative `application/pdf` Blob property with an available authorized bytes URL.
- Provide complete continuous access to every page with bounded loading, page navigation, and zoom controls.
- Support authored `pdf-render`, `pdf-initial-page`, and `pdf-zoom` presentation attributes.
- Preserve the existing Blob link as a persistent fallback and explicit link-only mode.
- Load pinned PDF.js code and its worker lazily from deterministic same-origin assets.
- Retire loading, worker, document, observer, and rendering work when the property is replaced or disconnected.
- Preserve application renderer precedence and identical foundation behavior beneath HTMX and Vue.
- Keep the existing GraphQL resource and authorization contracts authoritative.

**Non-Goals:**

- Embedding or adapting the complete PDF.js viewer application.
- Search, text selection, printing, document outlines, thumbnails, annotations, forms, attachments, or embedded PDF JavaScript.
- Inline reading of password-protected documents.
- Editing or uploading Blob properties beyond existing resource editing behavior.
- Cross-origin PDF resources, remote PDF.js modules, CDN dependencies, blob-backed workers, or data-URL workers.
- Adding `@PdfJsViewer`, metamodel facets, GraphQL presentation metadata, or effective-grid propagation.
- Making PDF.js configuration, worker paths, safety limits, or feature switches configurable per property.

## Decisions

### Use the PDF.js display API with a Causeway-owned continuous reader

The implementation will use the public PDF.js display API to load the authorized resource, obtain document and page proxies, and render page canvases.
After document metadata loads, the reader will create an ordered placeholder for every page so the complete document is represented and reachable.
An intersection observer will render visible and nearby pages progressively rather than allocating full-resolution canvases for the complete document immediately.
Previous-page and next-page controls will scroll to the corresponding placeholder, while ordinary scrolling will update the current-page status.

The complete PDF.js viewer was rejected because its search, print, outline, annotation, localization, and iframe application surface are outside scope and would substantially increase assets and integration complexity.
A first-page-only preview was rejected because the requested target is a document reader capable of viewing all pages.

### Keep asynchronous presentation behind the standard value-renderer boundary

A standard PDF renderer will qualify only a Blob-shaped value whose normalized authoritative MIME type is exactly `application/pdf` and whose `bytes` field contains a usable same-origin resource URL.
It will rank above the ordinary standard Blob renderer but below application registrations in `CausewayValueRendererRegistry`.
Filename extension, property title, member identity, Java type-name fragments, and authored labels will not qualify PDF presentation.

The renderer will emit a bounded reader scaffold identified as PDF presentation.
A dedicated foundation controller will mount against that scaffold after `<cw-property>` commits its primary markup and will own the PDF.js loading task, document proxy, render tasks, observers, canvases, controls, and cleanup.
The controller will not consume a Causeway object context or issue GraphQL operations; it receives only the already-authorized Blob value and normalized reader options.

A separately authorable public PDF custom element was rejected because this change extends `<cw-property>` rather than introducing another application composition boundary.
Embedding asynchronous PDF.js behavior directly inside the pure HTML value renderer was rejected because the renderer registry currently has synchronous deterministic precedence and output semantics.

### Define three authored loading modes

`pdf-render` accepts the exact lowercase values `auto`, `manual`, and `link` and defaults to `auto` when absent.

- `auto` mounts the reader and begins document loading as soon as the ready property presentation is committed.
- `manual` initially presents the Blob metadata, persistent resource link, and an accessible **Preview document** control without importing PDF.js or fetching PDF bytes for rendering.
- `link` uses the ordinary Blob presentation and does not import PDF.js, create a worker, or fetch PDF bytes for rendering.

Activating **Preview document** in manual mode mounts the same reader used by auto mode and moves focus to its reader heading or status landmark when initialization succeeds.
Changing `pdf-render` while connected will dispose any active reader before applying the new mode.
An absent, blank, mixed-case, or unsupported token will resolve to `auto` and publish a bounded presentation diagnostic.

### Define bounded initial page and zoom attributes

`pdf-initial-page` accepts a canonical positive safe integer from 1 through 100000 and defaults to 1.
After PDF metadata is available, a value beyond the actual page count will be clamped to the final page and reported through a bounded presentation diagnostic.
The reader will scroll to the resolved initial page after placeholders are established.

`pdf-zoom` accepts `page-width`, `page-fit`, `actual-size`, or a canonical integer percentage from `25%` through `400%` and defaults to `page-width`.
Zoom controls operate between 25% and 400% in bounded steps and update the current reader without modifying the authored attribute.
`page-width` responds to available inline size, `page-fit` fits both dimensions within the configured reader viewport, and `actual-size` uses PDF.js scale 1.
Resize-driven rendering will be debounced, and device-pixel scaling will be capped to prevent unbounded canvas allocation.

Invalid page or zoom values will use their defaults and emit a bounded diagnostic.
The attributes are meaningful only when the standard PDF renderer wins.
This change will not add them to effective-grid parsing, generated property presentation metadata, or GraphQL selection.
Applications control reader block size and responsive placement through documented `--causeway-pdf-*` CSS custom properties in their existing stylesheets rather than arbitrary inline-style attributes.

### Preserve the resource link in every mode and failure state

Auto and manual readers will retain a visible link using the authoritative Blob name and bytes URL.
The server remains authoritative for response content type, content disposition, authorization, cache policy, and filename.
A missing bytes URL, forbidden content policy, invalid URL, unavailable adapter, worker failure, HTTP failure, malformed PDF, password challenge, document limit, or render failure will produce bounded accessible status and leave the ordinary Blob presentation available.
No error presentation will include resource bytes, URLs containing sensitive query material, PDF content, worker internals, or stack traces.

### Package and load PDF.js like an audited foundation adapter

A dedicated foundation PDF.js asset subproject will contain a pinned `pdfjs-dist` dependency, lockfile, deterministic build script, verification script, generated browser assets, and generated license material.
Only the reviewed display module, module worker, and any required same-origin CMap, standard-font, or decoding assets will be copied or bundled into the foundation JAR.
The npm dependency tree and generated asset hashes will be verified during Maven validation, while regeneration will remain an explicit profile.
No runtime CDN or npm installation will be required by applications or Vue applications.

The foundation controller will dynamically import the packaged display module only for auto mode or after manual activation.
`GlobalWorkerOptions.workerSrc` will reference the packaged same-origin module worker.
The implementation will select a PDF.js release compatible with the repository's supported Node build toolchain, pin it exactly, and record its Apache-2.0 license and notices.

### Keep the reader feature set deliberately closed

The reader will render page canvases and expose Causeway-owned page and zoom controls only.
It will not mount PDF.js text, annotation, XFA, scripting, attachment, print, find, outline, or thumbnail layers.
It will not request or execute embedded JavaScript.
Password callbacks will not display a credential prompt; they will cancel loading and present the unsupported-password fallback.
WASM use will remain disabled unless the implementation demonstrates compatibility with the existing strict CSP without adding unsafe script capabilities.
Finite document-byte, page-count, image-pixel, canvas-dimension, concurrent-render, and nearby-page limits will be host-owned constants or foundation configuration, never authored per-property overrides.
Crossing a safety limit will stop inline rendering while preserving the resource link.

### Make lifecycle generation authoritative

Each reader mount receives a monotonically increasing property-presentation generation.
Every module import, loading-task continuation, document promise, page promise, observer callback, resize callback, and render-task completion must verify that its generation remains current before mutating the DOM.
Rerender, source replacement, mode change, property identifier change, context generation change, route replacement, and disconnection will cancel render tasks, disconnect observers, destroy the loading task or document proxy, clear canvases, and release references.
A late completion from an obsolete reader cannot restore pages, status, focus, or controls.

### Provide accessible controls without claiming accessible PDF text

The reader will expose a labelled region containing document name, loading or failure status, current page and total page count, previous and next controls, zoom controls, and the persistent resource link.
Each page placeholder and rendered canvas will have a bounded **Page N of M** accessible label.
Controls will be keyboard operable, disabled authoritatively at their boundaries, and retain visible focus under native and Vaadin presentation policies.
Loading and failure transitions will use polite status semantics unless an immediate failure requires an alert.

Because text and structure layers are excluded, the canvas rendering itself is not a semantic text alternative for assistive technology.
Documentation and presentation will retain the resource link so users can open the document in an external reader with the accessibility capabilities they require.

### Extend HTMX CSP narrowly

HTMX full-page responses will declare `worker-src 'self'` explicitly for the packaged PDF.js module worker.
The policy will not add remote origins, `blob:`, `data:`, `unsafe-eval`, or `unsafe-inline` for PDF rendering.
The existing `script-src 'self'`, `connect-src 'self'`, object prohibition, frame restrictions, and style policy remain authoritative.
Vue hosts will consume the same same-origin foundation assets and must not need a Vue-specific PDF implementation.

### Demonstrate the reader with one deterministic shared sample PDF

The shared Petclinic domain will expose one deterministic `application/pdf` Blob property backed by a small multipage fixture with clear page identifiers.
Authored HTMX and Vue pages will use that property to exercise auto, manual, and link behavior without copying the PDF reader into either host.
The fixture will carry documented provenance and compatible licensing or be deterministically generated from repository-owned content.
Adding the sample property must not make PDF.js, HTMX, or Vue a dependency of the shared domain model.

### Defer annotation-driven defaults with an explicit precedence direction

A future `@PdfJsViewer` annotation may contribute metamodel facets and GraphQL presentation metadata.
If introduced later, explicit authored `<cw-property>` attributes are expected to override annotation-derived values, which in turn would override foundation defaults.
No annotation class, facet factory, metadata field, selection, effective-grid mapping, compatibility placeholder, or dormant reflection logic will be added now.

## Risks / Trade-offs

- **PDF.js increases the foundation artifact size and security-maintenance surface.** → Copy only required pinned assets, verify them deterministically, retain license material, and document dependency update responsibility.
- **A malicious or unusually large PDF can consume browser CPU or memory.** → Parse in the module worker, render only visible and nearby pages, cap pixel density and safety dimensions, bound document characteristics, and destroy superseded work.
- **Canvas pages do not expose document text to assistive technology.** → State this limitation, provide accessible reader controls and page labels, and retain the resource link for an external accessible reader.
- **Some PDFs require fonts, CMaps, WASM decoders, password entry, or unsupported features.** → Package reviewed same-origin support assets where compatible and fall back safely when the deliberately closed reader cannot render the document.
- **Attachment disposition or session expiry can make a valid URL fail during PDF.js loading.** → Treat HTTP and parsing failures as reader-local errors without changing server policy and retain the ordinary link.
- **Responsive rerendering can race page rendering.** → Debounce resize work, cancel page render tasks, and require generation checks before every DOM commit.
- **Manual mode might accidentally preload PDF.js or PDF bytes.** → Test module import and resource fetch counts and require both to remain zero until activation.
- **Future annotation facets could conflict with authored HTML.** → Keep annotation support absent now and document the intended authored-over-facet-over-default precedence for the later proposal.

## Migration Plan

1. Add and verify the pinned PDF.js adapter assets without activating them for non-PDF values.
2. Add the standard PDF qualification and reader controller behind application renderer precedence.
3. Add authored attributes and document their defaults and fallback behavior.
4. Add the deterministic shared multipage fixture and browser acceptance coverage under HTMX and Vue.
5. Enable the explicit same-origin worker CSP directive for HTMX.
6. Release the foundation and hosts together so the reader module and worker paths are present when `<cw-property>` starts qualifying PDFs.
7. Roll back by selecting `pdf-render="link"` in authored pages or reverting the foundation and host assets together.

## Open Questions

None.
