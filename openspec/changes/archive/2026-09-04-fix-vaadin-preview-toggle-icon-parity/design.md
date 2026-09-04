## Context

`<cw-collection>` renders native preview disclosures as HTML strings, while `CausewayCollectionGridElement` creates equivalent Vaadin Grid cell controls through DOM APIs.
The clearer preview-icon change updated only the native string renderer, leaving Grid's `button.textContent = expanded ? '▾' : '▸'` path unchanged.
Pets qualifies for Grid at the shown width while Visits uses native fallback, exposing the divergence on the same PetOwner page.

## Goals / Non-Goals

**Goals:**

- Render the same larger SVG chevron in native and Vaadin Grid preview disclosures.
- Keep direction driven by each button's existing `aria-expanded` state and shared CSS.
- Prevent future geometry drift by defining the decorative SVG markup and DOM construction together.
- Verify parity in foundation adapter tests and the mixed-presentation Petclinic page.

**Non-Goals:**

- Changing Grid qualification, responsive fallback, preview eligibility, or row-details ownership.
- Changing button dimensions, preview-column width, accessible naming, focus, or lifecycle behavior.
- Adding a third-party icon library or host-specific markup.

## Decisions

### Introduce a small shared preview-icon utility

A foundation-local module will own the SVG namespace, class, view box, path geometry, string markup, and DOM construction helper.
The native renderer will interpolate the shared string, while the Grid renderer will append the shared DOM node using `createElementNS` with the test DOM's safe `createElement` fallback.
Both buttons will continue to own `aria-expanded`; existing shared CSS will rotate the common icon.

An alternative was to duplicate equivalent SVG construction in `grid-widget.mjs`.
That would fix the immediate symptom but retain the same two-source drift that caused this regression.

### Test the adapter seam directly

The Grid widget test will assert that its preview button contains the same hidden, non-focusable SVG class, view box, and path as native markup instead of text glyphs.
Browser coverage will compare useful icon dimensions and transforms in a Grid-qualified Pets collection and a native Visits collection on the same wide page.

An alternative was to rely only on the existing native preview test.
That test cannot execute the independent Grid cell renderer and therefore did not detect the regression.

## Risks / Trade-offs

- [A shared utility adds a module for small markup] → Keep it dependency-free and limited to one stable decorative geometry plus two representations required by current renderers.
- [Test DOM lacks namespace creation] → Use standards-based `createElementNS` in browsers with a narrow fallback only for compatible non-browser test documents.
- [Grid cell recycling could retain stale icon state] → Continue clearing each cell root before appending a newly state-bound button and icon.
