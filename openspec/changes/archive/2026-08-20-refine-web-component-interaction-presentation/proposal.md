## Why

The current web-component presentation leaves application-menu disclosures open after action selection, does not present disabled-property explanations as contextual tooltips, separates associated actions from their members, and ignores multiline property hints.
These gaps make the generic viewer less predictable and less faithful to Causeway's established Wicket presentation semantics.

## What Changes

- Close application-menu disclosures after action selection and when focus is intentionally dismissed outside the menu, while preserving accessible focus restoration.
- Preserve property descriptions as the default explanatory tooltip and expose disabled reasons through a separate keyboard-accessible tooltip indicator.
- Add consistent spacing between object actions.
- Preserve effective-grid nesting so actions associated with a property or collection render in a nearby semantic action group rather than in the top-level action strip.
- Interpret the effective-grid `multiLine` property hint and render a textarea editor with bounded rows.
- Mark Petclinic notes as multiline and place its property- and collection-associated actions beside the corresponding members.
- Extend foundation and headless Playwright acceptance coverage for the refined behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine menu dismissal, disabled-property explanations, effective-grid action association, action spacing, and multiline property editing.

## Impact

The change affects the framework-neutral foundation menu, property, editor, object-layout, and stylesheet modules, plus their Node tests.
The Petclinic layout, domain presentation metadata, and headless Playwright journeys provide executable acceptance coverage.
No GraphQL schema, public route shape, host-framework integration, or third-party dependency changes are required.
