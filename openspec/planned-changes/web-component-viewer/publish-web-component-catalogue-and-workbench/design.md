## Context

The component library exposes semantic custom elements in light DOM and is intended for vanilla HTML, HTMX, Vue, Svelte, React, and other hosts.
Its public contract spans element names, attributes, properties, native custom events, slots, context requirements, lifecycle states, semantic result events, styling hooks, and accessibility behavior.

A visual page designer needs a reliable palette definition, but that is not the first purpose of this change.
The immediate purpose is a developer catalogue and workbench that detects drift between implementation, documentation, examples, and declared metadata.

## Goals / Non-Goals

**Goals:**

- Publish machine-readable public custom-element contracts.
- Provide interactive isolated demonstrations of every supported semantic state.
- Reuse deterministic fixtures and optionally connect selected stories to the real sample endpoint.
- Exercise accessibility, themes, responsive behavior, and keyboard interactions.
- Make catalogue output suitable for later page-designer evaluation.

**Non-Goals:**

- Building a drag-and-drop page designer.
- Defining application routes or custom-page resolution.
- Making Storybook or another explorer a production runtime dependency.
- Replacing the vanilla HTML acceptance sample.
- Exposing hidden domain values or production credentials in stories.

## Decisions

### Generate a Custom Elements Manifest

The build produces a versioned machine-readable manifest for public element definitions.
Each declaration identifies tag name, public attributes and properties, events, slots, CSS custom properties, documented light-DOM hooks, context requirements, and source module.

Semantic Causeway concepts such as member ID, logical type, bookmark, result kind, and lifecycle state are documented as extensions or linked contract descriptions without inventing frontend widget behavior.
Generated output is checked for deterministic ordering and documentation drift.

### Use a web-component-native workbench

The selected explorer must render standards-based custom elements directly rather than require React wrappers around every component.
Storybook's web-component support is the initial candidate, but implementation confirms current support and alternatives before locking dependencies.

Stories import the packaged public ESM entry and register elements through the same supported path as applications.
Workbench-only adapters provide fixture GraphQL executors, object contexts, viewport controls, theme controls, and diagnostic capture.

### Model semantic states, not screenshots alone

Each component has stories for applicable lifecycle, authorization-safe hidden or disabled behavior, values, interaction states, validation, result shapes, partial failures, terminal failures, empty content, unsupported shapes, and stale responses.
Interactive play tests verify pointer and keyboard operation rather than relying only on static snapshots.

### Keep deterministic and real-endpoint modes separate

Default workbench builds use deterministic fixture executors and contain no network credentials.
A separately enabled local mode can target the same-origin sample GraphQL endpoint for selected integration stories.
Static published workbench assets never embed live endpoint secrets or mutable application data.

### Treat catalogue output as a future palette input

The manifest describes what a component can accept, but it does not decide which components are valid for a logical type or member.
The semantic page-designer analysis will combine catalogue declarations with targeted rich GraphQL introspection and object-context requirements.

### Preserve accessibility and theming contracts

Workbench controls exercise narrow and wide viewports, light and dark color schemes, reduced motion, zoom, keyboard-only operation, focus visibility, and screen-reader semantics.
Accessibility checks run against meaningful ready and interaction states, not only empty element shells.

## Risks / Trade-offs

- [Generated metadata can drift from runtime behavior] → Add contract tests comparing manifest declarations with element registration and documented exports.
- [Workbench fixtures can become a second domain model] → Reuse established deterministic fixtures and keep fixture adapters minimal.
- [Storybook can bias the project toward React] → Require direct web-component rendering and no wrappers as the catalogue contract.
- [Published stories can leak sensitive data] → Use synthetic fixtures, redaction checks, and opt-in local real-endpoint mode.
- [Large state matrices can become expensive] → Define representative equivalence classes while retaining every public component.

## Migration Plan

The catalogue and workbench are additive development assets.
Runtime packages do not import their dependencies.
Applications may consume the manifest or workbench documentation without changing component usage.

## Open Questions

- Whether Storybook remains the best current web-component-native explorer after a small dependency and accessibility spike.
- Whether semantic manifest extensions should use a project namespace or separate companion file.
- Which stories should be published publicly versus kept as repository-only integration fixtures.
