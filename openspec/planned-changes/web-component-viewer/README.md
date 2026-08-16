# Web-component viewer planned changes

These are fully drafted follow-on changes for the Causeway web-component viewer programme.
They are held outside `openspec/changes/` because the repository permits only one active OpenSpec change at a time.
The foundation change `establish-graphql-web-component-foundation`, read-only change `add-read-only-domain-web-components`, and interaction change `add-domain-web-component-interactions` are archived.
No web-component viewer change is currently active.
The next queued draft is the analysis-only `analyze-rich-graphql-referenceapp-coverage` change.
That analysis is the roadmap gate: it must confirm, narrow, reorder, merge, split, or reject the provisional GraphQL and component implementation drafts before any of them is promoted.
The `add-generic-htmx-web-component-viewer` draft remains deferred until the composite object, menu-bar, and required GraphQL coverage work is complete.

Each child directory is a complete change folder containing `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`, and its delta specifications.
After the active change is implemented, synced, and archived, promote the next draft verbatim:

```shell
git mv openspec/planned-changes/web-component-viewer/<name> openspec/changes/<name>
openspec validate <name> --strict
```

The OpenSpec CLI does not scan this planned-change directory, so drafts must be strictly validated after promotion and before implementation.
Review each draft against discoveries made by preceding changes and update stale assumptions before promotion.
In particular, the analysis-only roadmap is expected to revise the provisional follow-on drafts rather than treating their current scope as predetermined.

## Promotion order

| Order | Draft | Depends on | Capability impact |
|---:|---|---|---|
| 1 | `add-read-only-domain-web-components` *(archived)* | Archived `establish-graphql-web-component-foundation` | NEW `domain-web-components` |
| 2 | `add-domain-web-component-interactions` *(archived)* | Archived read-only component change | MODIFIED `domain-web-components` |
| 3 | `analyze-rich-graphql-referenceapp-coverage` *(next; analysis only)* | Archived foundation and component changes; preliminary reference-app audit | NEW `rich-graphql-referenceapp-analysis` |
| 4 | `add-rich-graphql-value-and-resource-semantics` *(provisional)* | Completed reference-app analysis | NEW `rich-graphql-value-semantics` |
| 5 | `add-rich-graphql-member-metadata` *(provisional)* | Completed reference-app analysis | NEW `rich-graphql-member-metadata` |
| 6 | `add-rich-graphql-collection-windowing` *(provisional)* | Completed reference-app analysis | NEW `rich-graphql-collection-windowing` |
| 7 | `add-rich-graphql-application-entry-points` *(provisional)* | Completed reference-app analysis and required member metadata | NEW `rich-graphql-application-entry-points` |
| 8 | `add-composite-object-web-component` *(provisional)* | Analysis plus required metadata and collection capabilities | MODIFIED `domain-web-components` with `<causeway-object>` |
| 9 | `add-menubar-web-components` *(provisional)* | GraphQL application entry points and required member metadata | MODIFIED `domain-web-components` with menu bars |
| 10 | `add-graphql-web-component-diagnostics` *(pending refinement)* | Archived foundation and component changes | NEW `graphql-web-component-diagnostics` |
| 11 | `add-generic-htmx-web-component-viewer` *(deferred)* | Composite object, menu bars, and required GraphQL coverage | NEW `generic-web-component-viewer` |

## Programme constraints

- The rich GraphQL schema and standard GraphQL introspection are the application protocol.
- Generated rich-schema naming rules are accepted public client grammar rather than treated as accidental implementation details.
- Components expose semantic Causeway APIs and do not expose GraphQL document construction to page composers.
- HTMX is used only by the generic reference viewer; the component library remains framework-neutral.
- The programme does not add a duplicate GraphQL member-list metadata API.
- Rich-schema extensions are proposed only when a concrete component requirement demonstrates missing semantics.
