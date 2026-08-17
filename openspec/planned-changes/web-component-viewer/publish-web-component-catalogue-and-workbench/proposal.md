## Why

Developers need a discoverable catalogue of the semantic Causeway custom-element vocabulary before they can confidently compose custom pages or build visual authoring tools.
The current documentation and vanilla sample prove behavior, but they do not provide a machine-readable component manifest or an interactive state-by-state workbench.
A catalogue and Storybook-style workbench should make the components inspectable without becoming a page designer or runtime viewer.

## What Changes

- Publish a machine-readable Custom Elements Manifest for every public Causeway web component.
- Add an interactive web-component workbench using Storybook or an equivalent selected tool.
- Catalogue attributes, properties, events, slots, light-DOM hooks, CSS custom properties, context requirements, lifecycle states, and accessibility behavior.
- Provide deterministic stories for loading, ready, hidden, disabled, validation, interaction, result, partial-error, terminal-error, empty, and unsupported states.
- Provide wide, narrow, light, dark, pointer, keyboard, and reduced-motion presentations.
- Support fixture-backed stories and a separately enabled real GraphQL sample environment.
- Export stable catalogue metadata that a later semantic page-designer analysis can evaluate as its palette source.

## Capabilities

### New Capabilities

- `web-component-catalogue-and-workbench`: Defines machine-readable component metadata and an interactive developer workbench for the Causeway semantic custom-element library.

### Modified Capabilities

None.

## Impact

- Adds development tooling, generated catalogue artifacts, stories, fixtures, accessibility checks, and documentation.
- Depends on the completed public component vocabulary, including `<causeway-object>` and menu-bar components.
- Remains lower priority than the generic HTMX, Vue, and Svelte viewers.
- Adds no production viewer dependency and does not define visual page authoring, routing, or GraphQL protocol changes.
