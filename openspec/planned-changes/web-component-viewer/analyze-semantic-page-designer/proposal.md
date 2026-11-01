## Why

The semantic Causeway component palette creates an opportunity for a model-driven page designer that understands logical types, members, object contexts, actions, and collections rather than only pixels.
The architecture discussion identified GrapesJS as a possible open-source foundation, but did not establish that direct HTML editing, a generic canvas, or any current tool can preserve Causeway semantics safely and deterministically.
An analysis and prototype should decide the authoring model, generated artifact, router integration, and security boundary before an implementation proposal is drafted.

## What Changes

- Evaluate GrapesJS, a purpose-built semantic editor, and at least one other viable web-component authoring approach.
- Define how the web-component catalogue and targeted rich GraphQL introspection combine into a logical-type-aware palette.
- Compare direct HTML editing with a semantic intermediate page model and deterministic HTML generation.
- Prototype property panels, member binding, layout containers, nested object contexts, collections, actions, live preview, and diagnostics.
- Define a portable custom-page artifact using Causeway custom elements and ordinary HTML rather than a new Causeway layout XML dialect.
- Define how generated pages register with the router-level custom-page mechanisms of the generic HTMX, Vue, and Svelte viewers.
- Evaluate round-trip editing, source control, migration, extensibility, accessibility, security, sensitive data, and build integration.
- Produce an evidence-backed implementation roadmap without changing production component or viewer behavior.

## Capabilities

### New Capabilities

- `semantic-page-designer-analysis`: Defines the reproducible tool evaluation, prototype, artifact contract, and roadmap for a Causeway-aware semantic page designer.

### Modified Capabilities

None.

## Impact

- Adds analysis documents, disposable prototypes, decision matrices, example generated artifacts, and a proposed implementation boundary.
- Depends on completed generic HTMX, Vue, and Svelte router contracts and the published component catalogue and workbench.
- Is explicitly lower priority than the three generic viewer implementations.
- Does not add a production page designer, new runtime layout language, metamodel mutation, or external reference-application dependency.
