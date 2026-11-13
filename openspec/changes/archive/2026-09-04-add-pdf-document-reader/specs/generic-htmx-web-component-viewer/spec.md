## ADDED Requirements

### Requirement: Strict same-origin PDF worker policy

The generic HTMX viewer SHALL permit the foundation's packaged PDF.js display module, support assets, authorized PDF resource request, and module worker under its strict same-origin content-security policy.
It MUST declare `worker-src 'self'` explicitly and MUST NOT add a remote origin, `blob:`, `data:`, `unsafe-inline`, `unsafe-eval`, or an unbounded connection source for PDF rendering.

#### Scenario: HTMX page initializes a PDF reader

- **WHEN** an eligible PDF property initializes beneath a full HTMX page
- **THEN** the packaged display module, module worker, support assets, and PDF bytes are requested from the application origin
- **AND** the page's content-security policy permits those requests without weakening unrelated script, style, object, frame, or connection directives

#### Scenario: HTMX page does not initialize a PDF reader

- **WHEN** the page has no eligible auto reader, a manual reader remains inactive, or every PDF property uses link mode
- **THEN** the browser requests no PDF.js module, worker, support asset, or PDF bytes for inline rendering
- **AND** the same content-security policy remains in force

#### Scenario: PDF worker attempts a disallowed source

- **WHEN** PDF presentation attempts to use a remote, blob, data, or otherwise non-same-origin worker
- **THEN** the viewer policy does not permit that worker
- **AND** the property falls back locally without relaxing the page policy

#### Scenario: PDF reader runs under a context path

- **WHEN** the HTMX application is mounted beneath a servlet context or configured viewer base path
- **THEN** foundation PDF.js assets and the authoritative Blob URL resolve against their established application paths
- **AND** no root-path assumption, external CDN, or duplicate host controller is introduced

### Requirement: HTMX PDF reader acceptance coverage

The executable HTMX reference application SHALL demonstrate a deterministic multipage PDF property through authored auto, manual, and link modes under native and Vaadin presentation policies.
The existing GraphQL resource controller SHALL remain authoritative for content type, content disposition, authorization, caching, filename, and bytes.

#### Scenario: Automatic reader is exercised

- **WHEN** headless browser coverage opens the authored auto PDF property
- **THEN** it verifies complete page count, first and final page reachability, progressive rendering, page status, zoom, fallback link, and absence of browser or worker errors
- **AND** no explicit activation is required

#### Scenario: Manual reader is exercised

- **WHEN** headless browser coverage opens the authored manual PDF property
- **THEN** it verifies that PDF.js and PDF bytes are not requested before **Preview document** is activated
- **AND** activation initializes the same complete reader and preserves keyboard focus semantics

#### Scenario: Link reader is exercised

- **WHEN** headless browser coverage opens the authored link-only PDF property
- **THEN** it verifies ordinary Blob presentation without PDF.js module, worker, support-asset, or inline-byte requests
- **AND** the authoritative resource link remains usable

#### Scenario: Secured reader is exercised

- **WHEN** the secured HTMX browser journey opens the PDF property before and after authentication or session expiry
- **THEN** anonymous resource access remains protected and authenticated inline reading uses the existing session
- **AND** failure or expiry does not disclose PDF bytes, protected URLs, or stale rendered pages
