## Context

The shared theme defines `--causeway-content-width: 82rem` and `--causeway-shell-width: 96rem` as sensible defaults for generic applications.
The HTMX shell consumes those variables while retaining responsive inline gutters.
Petclinic and the Reference Application already load application-owned stylesheets after the shared styles, so they can opt into a wider presentation without changing reusable components.

## Goals / Non-Goals

**Goals:**

- Let both applications use the available wide viewport for shell and route content.
- Preserve the HTMX shell's existing desktop and narrow-screen gutters.
- Keep the behavior application-local and covered by browser tests.

**Non-Goals:**

- Change shared foundation or HTMX viewer defaults.
- Force every consuming application to use full width.
- Remove responsive gutters, collection containment, or overflow safeguards.
- Change the vanilla HTML sample or Wicket viewer.

## Decisions

### Override public width variables in each application stylesheet

Each stylesheet will set `--causeway-content-width` and `--causeway-shell-width` to `100%` on `:root`.
The existing shell expressions continue to apply their viewport-relative gutters, while the variable no longer imposes a fixed desktop ceiling.

Changing `theme.css` or `causeway-htmx.css` is rejected because the user requested application-specific behavior and other applications may prefer bounded reading width.
Adding application-specific classes to shared shell markup is rejected because the documented variables already provide the correct customization seam.
Using `100vw` is rejected because viewport units can include scrollbar width; `100%` composes with the shell's containing block and existing overflow protections.

### Assert effective geometry in each browser suite

At a wide viewport, browser tests will compare `.causeway-shell-main` width with the document viewport and require the retained gutter-sized difference rather than a fixed-width ceiling.
Existing narrow viewport and no-horizontal-overflow checks continue to verify responsive safety.

## Risks / Trade-offs

- [Risk] Very wide content can reduce readability for prose-heavy pages. → Limit the override to the two data-dense demonstration and regression applications.
- [Risk] Wide collections or grids expose latent overflow. → Retain existing contained scrolling and browser overflow assertions.
- [Risk] Tests become sensitive to scrollbar geometry. → Assert a bounded width ratio and gutter range rather than pixel equality.

## Migration Plan

Deploy the two application stylesheet overrides with no shared artifact migration.
Rollback removes those four custom-property declarations.

## Open Questions

None.
