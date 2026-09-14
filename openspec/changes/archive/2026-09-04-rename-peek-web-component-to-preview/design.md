## Context

Collection row previews are declared with `<cw-peek>`, but HTMX resolves their default fragments from the established `previews/<logical-type-name>.html` resource convention and the UI consistently describes the behavior as a preview.
The custom-element name, JavaScript class and file names, selectors, tests, documentation, sample markup, and generated Vue assets therefore expose two terms for one concept.
The rename crosses the foundation, HTMX host, sample applications, and public specifications.

## Goals / Non-Goals

**Goals:**

- Make `<cw-preview>` the only registered and supported custom element for collection-row previews.
- Use preview terminology consistently in source filenames, JavaScript symbols, CSS selectors, diagnostics, tests, documentation, and sample markup.
- Preserve existing preview resource paths, HTTP contracts, hydration, lifecycle, accessibility, security, and navigation behavior.
- Regenerate committed Vue package and sample assets deterministically after source markup changes.

**Non-Goals:**

- Retaining `<cw-peek>` as a compatibility alias.
- Renaming the established `META-INF/causeway/webcomponents/previews/...` resource directory, preview HTTP endpoint, or user-facing preview terminology.
- Changing preview composition, data selection, disclosure behavior, or object-context ownership.
- Rewriting historical archived OpenSpec changes.

## Decisions

### Replace rather than alias the custom element

The component registration SHALL move from `cw-peek` to `cw-preview`, and authored `cw-peek` markup SHALL no longer opt a collection into row previews.
An alias was rejected because it would preserve the terminology mismatch indefinitely, complicate duplicate-declaration detection, and make public API documentation ambiguous.
The proposal marks this as a breaking change and documents the direct markup migration.

### Rename implementation symbols that identify the component

`peek-element.mjs`, its exported class, constants, CSS selectors, test file, and component-facing helper names SHALL use preview terminology.
Generic verbs unrelated to this component need not be mechanically rewritten, but no supported source, sample, test, or current documentation SHALL advertise `cw-peek`.

### Preserve host preview-resource contracts

The HTMX classpath convention and private preview endpoint already use the desired terminology and SHALL remain unchanged.
Their parser and client-side root validation SHALL accept exactly one supported `cw-preview` root instead of `cw-peek`, retaining all existing safety bounds and fail-closed behavior.

### Update authored and generated consumers together

HTMX templates and Vue single-file components SHALL migrate in source.
The Vue viewer package and Petclinic production assets SHALL then be regenerated with existing deterministic build profiles so committed artifacts match their source.

## Risks / Trade-offs

- **Risk:** Existing application markup using `<cw-peek>` stops enabling previews. → **Mitigation:** Treat the rename as explicitly breaking and document the one-token migration to `<cw-preview>`.
- **Risk:** A stale selector or registration leaves native or Vaadin-backed previews partially functional. → **Mitigation:** Search non-generated sources, update focused component and host tests, and run both sample acceptance suites.
- **Risk:** Generated bundles retain the old tag. → **Mitigation:** Regenerate package and sample assets and assert that supported source and generated artifacts contain no `cw-peek` references.
- **Risk:** Broad replacement alters historical records or unrelated resource conventions. → **Mitigation:** Limit edits to current code, tests, docs, samples, and main/delta specs while preserving `previews/...` paths.

## Migration Plan

1. Replace authored `<cw-peek>` elements with `<cw-preview>`.
2. Update direct imports of the renamed foundation module or class.
3. Rebuild Vue and application assets.
4. Deploy the rebuilt viewer and migrated markup together.
5. Roll back by reverting the viewer and authored markup as one unit.

## Open Questions

None.
