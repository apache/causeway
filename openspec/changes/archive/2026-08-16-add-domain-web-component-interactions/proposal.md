## Why

The read-only domain components expose Causeway objects faithfully but do not yet let users edit properties or invoke actions through the rich GraphQL semantics.
Adding a shared interaction layer will keep validation, choices, autocomplete, parameter negotiation, mutation, results, and refresh behavior consistent across generic and custom pages.

## What Changes

- Add property edit state, value input selection, choices, autocomplete, validation, cancellation, and save or cancel behavior to the semantic component library.
- Execute property updates through introspected GraphQL mutation capabilities and reconcile the owning object context after success.
- Add a standard action interaction controller that handles semantic action requests from existing action affordances.
- Build action prompts from introspected rich-schema parameter wrappers and support hidden, disabled, default, choices, autocomplete, and validation semantics.
- Re-evaluate dependent parameter semantics when preceding parameter values change.
- Invoke safe actions through query capabilities and mutating actions through available top-level GraphQL mutations without exposing that distinction to page composers.
- Normalize scalar, object, collection, and void action outcomes as semantic result and navigation events.
- Add request sequencing, cancellation, mutation serialization, partial-error mapping, and conservative active-projection refresh.
- Preserve application override points for custom editors, prompts, result presentation, and interaction orchestration.
- Extend the existing `sample-html` reference showcase as the executable interaction acceptance fixture while preserving its route, bookmark, read-only selectors, responsive theme, same-origin architecture, and Maven run profile.
- Add deterministic editable scalar and enum properties, validation and choice behavior, safe and mutating actions, parameter negotiation, representative result shapes, stable interaction diagnostics, and additive browser hooks to the sample.
- Document and execute manual browser verification for save, cancel, validation, choices, autocomplete, action prompts, outcomes, keyboard focus, responsive themes, GraphQL operation placement, and context refresh.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Adds property editing and complete rich-schema action interaction semantics to the read-only component vocabulary.

## Impact

- Extends the existing domain web components and object-context command API.
- Adds editor and action-parameter renderer contracts and standard prompt presentation.
- Expands `viewers/webcomponents/sample-html` and its deterministic JPA domain, GraphQL integration assertions, stable selectors, event diagnostics, and manual-verification documentation.
- Exercises rich GraphQL validation, choices, autocomplete, invocation, and mutation fields and may identify concrete grammar gaps for separate proposals.
- Keeps the web-component layer framework-neutral and uses the existing same-origin `/graphql` endpoint and `run-sample-html` Maven profile.
- Does not add generic page routing, HTMX composition, collection mutation, bulk actions, Playwright, or speculative GraphQL metadata fields.
