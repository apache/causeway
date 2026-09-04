## 1. Contained Reader Navigation

- [x] 1.1 Replace page-placeholder `scrollIntoView()` navigation with geometry-based scrolling on the owned PDF viewport.
- [x] 1.2 Keep focus within the available previous and next controls and avoid changing the surrounding document scroll position.
- [x] 1.3 Keep observer-driven current-page detection and nearby progressive rendering coherent after programmatic viewport scrolling.
- [x] 1.4 Add foundation unit coverage for forward, backward, first-page, final-page, and initial-page contained scrolling.
- [x] 1.5 Add foundation coverage proving page navigation does not call placeholder-level `scrollIntoView()` or mutate an outer scrolling ancestor.

## 2. Compact Reader Chrome

- [x] 2.1 Move the existing authorized Blob resource link and media type into the auto and manual reader toolbar.
- [x] 2.2 Add responsive toolbar styling that lets the resource link use remaining space and wrap without horizontal overflow.
- [x] 2.3 Remove the visible canvas-disclaimer paragraph and its unused styling hook.
- [x] 2.4 Preserve concise accessible names for the reader region, status, controls, page placeholders, canvases, and resource link without claiming semantic PDF text.
- [x] 2.5 Verify inactive, loading, ready, failure, password-rejection, safety-limit, and link-only markup retains exactly the intended resource-link presentation.
- [x] 2.6 Update foundation markup, style, accessibility, and renderer tests for the revised toolbar and removed note.

## 3. Realistic Petclinic Agreement Fixture

- [x] 3.1 Replace reader-demo PDF prose with a deterministic multipage clinic agreement containing owner identity, relevant pet names, plausible agreement terms, and signature sections.
- [x] 3.2 Give generated agreements stable owner-specific filenames, exact `application/pdf` media type, fixed metadata, deterministic ordering, and repeatable bytes.
- [x] 3.3 Use the realistic owner-agreement content for the automatic, manual, and link-only Petclinic demonstrations through each object's authoritative owner relationship.
- [x] 3.4 Rename sample property presentation away from reader-test terminology while retaining authored auto, manual, and link mode coverage.
- [x] 3.5 Add domain tests for agreement content, page count, filename, MIME type, owner/pet personalization, and byte-for-byte repeatability.

## 4. HTMX and Vue Side-Column Composition

- [x] 4.1 Move the HTMX PetOwner Agreement card into the secondary column after Pets and Visits.
- [x] 4.2 Move the Vue PetOwner Agreement card to the equivalent secondary-column position.
- [x] 4.3 Apply `LabelPosition.NONE` in property metadata and `label-position="NONE"` in explicit host markup so the card heading replaces the redundant property label.
- [x] 4.4 Keep the agreement reader full-width within its card and preserve established wide and narrow Petclinic column ordering.
- [x] 4.5 Update HTMX and Vue semantic-composition tests for the Agreement heading, property identity, side-column ownership, and suppressed label.
- [x] 4.6 Regenerate and verify the committed Vue production bundle after the source-page changes.

## 5. Browser Acceptance and Documentation

- [x] 5.1 Extend HTMX headless coverage to navigate repeatedly in both directions at wide and narrow viewports while asserting stable outer scroll position, visible toolbar, retained focus, and correct current page.
- [x] 5.2 Extend Vue headless coverage with equivalent agreement placement, responsive, toolbar-link, no-disclaimer, and contained-navigation assertions.
- [x] 5.3 Retain native and Vaadin presentation-policy coverage for the refined reader and verify no horizontal page overflow.
- [x] 5.4 Retain secured HTMX and Vue resource-link, authentication, session-expiry, and reader-retirement coverage with the renamed agreement fixture.
- [x] 5.5 Update foundation and host documentation to describe contained navigation, toolbar download placement, accessibility semantics, and the realistic owner-agreement example.

## 6. Verification

- [x] 6.1 Run the full foundation JavaScript test suite.
- [x] 6.2 Run affected Java integration tests and rebuild the Vue frontend.
- [x] 6.3 Run ordinary and secured HTMX and Vue Playwright suites, including native and Vaadin PDF presentation coverage.
- [x] 6.4 Run affected-module Maven packaging, RAT, license, and PDF.js verification.
- [x] 6.5 Run IDE formatting and inspections, strict OpenSpec validation, and whitespace checks.

## 7. Selectable Zoom Modes

- [x] 7.1 Replace the read-only zoom status with an accessible native selector containing page width, page height, page fit, and bounded percentage choices while retaining zoom-out and zoom-in buttons.
- [x] 7.2 Extend the foundation controller to apply selected fit or percentage modes, preserve the current page, and keep the selector synchronized after incremental zooming.
- [x] 7.3 Calculate page-height fitting from the owned viewport and PDF page geometry without moving a host scrolling ancestor or bypassing rendering limits.
- [x] 7.4 Add foundation unit, renderer, style, and accessibility coverage for selector options, synchronization, restoration, bounds, page preservation, and page-height scale.
- [x] 7.5 Extend HTMX and Vue browser coverage for percentage, page-height, page-fit, and restored page-width choices at wide and narrow sizes without host movement or horizontal overflow.
- [x] 7.6 Update reader documentation and rerun affected foundation, host, packaging, OpenSpec, IDE, and whitespace verification.
