## Context

Causeway's effective grid supplies default object composition.
`<cw-object>` consumes that grid and generates semantic property, action, collection, and header components.
Applications can override the generic page at the host router using ordinary HTML and Causeway components.

A designer could make that override path visual and model-aware.
Its palette can combine public custom-element declarations with targeted rich GraphQL knowledge of one logical type and its members.
However, direct manipulation of arbitrary HTML may not preserve context, member identity, accessibility, or deterministic source output.
The appropriate editing model and tooling need evidence.

## Goals / Non-Goals

**Goals:**

- Compare viable page-authoring foundations against Causeway semantic requirements.
- Decide whether the editable source is HTML, a semantic page model, or a constrained hybrid.
- Define deterministic portable output and router registration for all generic viewers.
- Prove logical-type-aware palette filtering and member binding.
- Define live preview, validation, diagnostics, round-trip, migration, and source-control behavior.
- Protect hidden, authorization-sensitive, password, and resource data during design.
- Produce a bounded implementation roadmap.

**Non-Goals:**

- Shipping a production page designer.
- Replacing Causeway grid or menu XML.
- Making `<cw-object>` select custom pages.
- Supporting arbitrary JavaScript execution inside the designer.
- Committing to GrapesJS before comparison.
- Implementing multi-user collaboration, hosted storage, or deployment workflows.

## Decisions

### Keep routing outside the object component

The designer creates custom page artifacts and registration metadata.
The generic HTMX, Vue, or Svelte router chooses that custom page for an exact logical type and otherwise uses the generic `<cw-object>` page.

The analysis does not introduce a framework-neutral page provider inside `<cw-object>`.
Generated pages must work beneath the established route-level object context.

### Treat the catalogue and schema as distinct inputs

The Custom Elements Manifest describes available element contracts, attributes, properties, events, slots, styling hooks, and context requirements.
Targeted rich GraphQL introspection describes the active logical type, member IDs, datatypes, interaction shapes, visibility contract, and supported operations.

The prototype combines them to present only semantically compatible palette entries and configuration choices.
It does not create a duplicate metamodel or fetch all application schema eagerly.

### Compare semantic model and direct HTML authoring

The analysis evaluates at least three approaches:

- direct DOM or HTML editing through a web-component-capable canvas such as GrapesJS;
- a purpose-built semantic page tree with deterministic HTML or ESM generation;
- a constrained hybrid where a semantic tree owns Causeway elements and controlled HTML regions.

Evaluation criteria include round-trip fidelity, deterministic diffs, nested contexts, slots, member identity, invalid-state prevention, accessibility, extension APIs, ecosystem health, bundle size, licensing, and testability.
No approach wins by feature count alone.

### Generate portable semantic pages

The target artifact uses ordinary HTML and public Causeway custom elements, with ESM only where a documented page factory or framework loader requires it.
It does not emit a replacement `.layout.xml` format and does not alter the server metamodel.

Generated output is deterministic, human-reviewable, source-control friendly, and free from transient preview state.
A companion registration artifact may map public logical type to the generated page for each host viewer without changing the page's semantic markup.

### Keep preview isolated and authorized

Deterministic fixture preview is the default.
An explicitly enabled live preview may use the current authenticated GraphQL context, but must honor hidden state, authorization, resource policy, diagnostics redaction, and bounded collection behavior.

Design documents, exported source, undo history, logs, and browser storage never capture hidden values, passwords, credentials, authorization rules, or resource content.
Arbitrary script execution and unsafe external resource loading are disabled.

### Prototype before implementation scope

The change may create disposable or development-only prototypes for tool evaluation.
Those prototypes do not ship in production packages and do not establish compatibility promises.
The final analysis selects, narrows, or rejects a production designer proposal and identifies prerequisite changes separately.

## Evaluation Criteria

| Area | Questions |
|---|---|
| Semantic safety | Can invalid member bindings and context placement be prevented before export? |
| Portability | Does output remain ordinary custom-element HTML usable by HTMX, Vue, and Svelte hosts? |
| Determinism | Can equivalent pages produce stable source and reviewable diffs? |
| Round trip | Can exported artifacts be reopened without losing unsupported but valid content? |
| Accessibility | Can authoring and generated pages be keyboard operable and structurally valid? |
| Extensibility | Can applications add custom elements, inspectors, and layout containers safely? |
| Security | Are hidden and sensitive values absent from model, preview history, diagnostics, and export? |
| Maintainability | Are tool dependencies active, licensable, testable, and replaceable? |

## Risks / Trade-offs

- [A generic canvas can reduce semantic components to pixels] → Weight semantic validation, context awareness, and deterministic output above free-form positioning.
- [A custom editor is expensive] → Prototype only the critical tree, palette, inspector, preview, and export path before estimating production scope.
- [Generated HTML can be overwritten manually] → Define ownership markers and round-trip behavior explicitly rather than silently dropping edits.
- [Live previews can leak data] → Default to fixtures and enforce authorization and redaction in explicitly enabled live mode.
- [Framework-specific artifacts can fragment] → Keep semantic HTML portable and isolate host registration adapters.

## Migration Plan

This change is analysis-only and requires no runtime migration.
Any later implementation proposal must define migration for generated page-model and registration versions.

## Open Questions

- Whether a semantic intermediate model or constrained hybrid can preserve arbitrary application HTML sufficiently.
- Whether GrapesJS supports the required custom-element metadata, nested contexts, deterministic serialization, and keyboard accessibility without extensive forks.
- Whether host registration should be generated separately for HTMX, Vue, and Svelte or expressed through one portable manifest consumed by adapters.
- Whether live preview belongs in the first production designer increment.
