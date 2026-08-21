# Follow-on proposal outline: add Bootstrap-derived Causeway theme pilot

## Suggested change name

`add-bootstrap-derived-web-component-theme-pilot`

## Why

The toolkit analysis found that a constrained Bootstrap-derived theme offers the strongest pragmatic balance of visual coverage, mature maintenance, Maven compatibility, and preservation of Causeway semantic components.
The score margin over the current theme and Open Props is small, so an opt-in evidence-driven pilot is warranted instead of immediate default-theme replacement.

## What Changes

- Add a pinned and reproducible selective Bootstrap Sass or scoped CSS build invoked by Maven.
- Generate one Causeway-owned pilot stylesheet without Bootstrap JavaScript or Popper.
- Preserve public `<causeway-*>` elements, semantic events, GraphQL contexts, HTMX lifecycle, and application-facing `--causeway-*` variables.
- Cover shell, menus, prompts, buttons, forms, validation, tabs, properties, collections, tables, loading, results, errors, and responsive navigation.
- Enable the theme only in the HTMX Petclinic and vanilla samples during the pilot.
- Add visual, accessibility, responsive, preference, packaging, size, and Playwright acceptance gates.
- Retain the current theme as the default and rollback path.

## Expected capability impact

### New capability

- `bootstrap-derived-web-component-theme`: Defines the opt-in generated theme, packaging, customization boundary, budgets, and acceptance behavior.

### Modified capability

- `generic-htmx-web-component-viewer`: Clarifies that internal theme generation may use Bootstrap design sources while public semantic components and documented customization remain toolkit-neutral.

## Design questions for proposal creation

- Use Bootstrap WebJar Sass sources or a repository-owned npm lock and Sass compiler.
- Decide whether any internal Bootstrap classes are acceptable or all rules must compile into Causeway selectors.
- Define the theme selection configuration and asset URL.
- Decide which existing `--causeway-*` variables require additions or compatibility aliases.
- Confirm browser floor and whether native Popover is a separate follow-on concern.
- Set screenshot baselines and Lighthouse policy for sample content.

## Acceptance summary

- Generated CSS is no more than 40 KB gzip.
- The pilot adds no Bootstrap JavaScript.
- Representative Lighthouse accessibility reaches 100 after contextual contrast fixes.
- Desktop and narrow layouts have no page-level overflow.
- Light, dark, reduced-motion, and forced-colors states remain operable.
- Existing foundation and Petclinic Playwright tests pass.
- Applications that do not opt in remain unchanged.
- Removing the pilot stylesheet restores the previous presentation without data or API migration.
