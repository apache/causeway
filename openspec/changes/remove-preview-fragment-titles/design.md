## Context

Petclinic collection rows already render each object's authoritative title as the row's canonical link.
Several authored preview templates also include `<cw-object-header>`, causing the expanded details area to repeat the same title and identity link.
The duplication exists in HTMX runtime-type resources, an HTMX inline Pet preview, and Vue inline Owner, Pet, and Visit previews.
The foundation correctly treats preview contents as application-authored composition and should not suppress arbitrary authored components.

## Goals / Non-Goals

**Goals:**

- Make Petclinic previews concise by omitting repeated object titles.
- Keep HTMX default/inline and Vue inline previews presentation-equivalent.
- Preserve all selected fields, actions, nested collections, labels, context hydration, and lifecycle behavior.
- Assert that live previews contain no `<cw-object-header>` while remaining useful.

**Non-Goals:**

- Removing page-level `<cw-object-header>` components.
- Changing `<cw-preview>`, `<cw-object-header>`, collection rows, canonical object links, or host resolver behavior.
- Preventing applications from deliberately authoring an object header in other previews.
- Inferring title elements from arbitrary markup or stripping supplied HTML at runtime.

## Decisions

### Change authored composition rather than foundation rendering

Remove explicit `<cw-object-header>` declarations from the sample's preview templates.
The row remains the authoritative title and navigation surface, while preview bodies begin with the selected detailed properties or actions.

Runtime suppression was rejected because `<cw-preview>` must preserve safe application-authored composition and cannot assume that every object header is redundant in every host layout.

### Cover every Petclinic preview source

Update all three HTMX runtime-type preview resources, the HTMX inline Pet preview, both Vue Home previews, and both Vue PetOwner previews.
Update the foundation usage example to demonstrate the preferred concise composition.

Changing only the preview visible in the supplied page was rejected because it would leave framework and inline/default inconsistencies.

### Verify absence and retained content

Source-level integration tests will verify default preview resources have no object header.
Browser tests will verify live Owner, Pet, and Visit previews omit `<cw-object-header>` while representative properties, actions, or nested collections still render.

Text-only screenshot assertions were rejected because the same title remains correctly visible in the owning row and would not identify its source.

## Risks / Trade-offs

- [Preview context might appear less identifiable in isolation] → The live preview is structurally attached to its titled row, and its section keeps a type-specific accessible label.
- [Tests could confuse page and preview headers] → Scope selectors to `cw-preview[data-causeway-preview-live]`.
- [Vue source and packaged assets could drift] → Rebuild the Vue frontend through its established Maven lifecycle and verify packaged parity.
