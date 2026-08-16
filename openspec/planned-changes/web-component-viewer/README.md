# Web-component viewer planned changes

These are fully drafted follow-on changes for the Causeway web-component viewer programme.
They are held outside `openspec/changes/` because the repository permits only one active OpenSpec change at a time.
The foundation change `establish-graphql-web-component-foundation`, read-only change `add-read-only-domain-web-components`, and interaction change `add-domain-web-component-interactions` are archived.
No web-component viewer change is currently active.
The next queued draft is `expand-rich-graphql-referenceapp-coverage` so protocol gaps found from the reference application are resolved before further generic composition.
Component refinement, including the pending `add-graphql-web-component-diagnostics` draft, follows that protocol work.
The `add-generic-htmx-web-component-viewer` draft is deferred until the GraphQL and component coverage work is complete.

Each child directory is a complete change folder containing `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`, and its delta specifications.
After the active change is implemented, synced, and archived, promote the next draft verbatim:

```shell
git mv openspec/planned-changes/web-component-viewer/<name> openspec/changes/<name>
openspec validate <name> --strict
```

The OpenSpec CLI does not scan this planned-change directory, so drafts must be strictly validated after promotion and before implementation.
Review each draft against discoveries made by preceding changes and update stale assumptions before promotion.

## Promotion order

| Order | Draft | Depends on | Capability impact |
|---:|---|---|---|
| 1 | `add-read-only-domain-web-components` *(archived)* | Archived `establish-graphql-web-component-foundation` | NEW `domain-web-components` |
| 2 | `add-domain-web-component-interactions` *(archived)* | Archived read-only component change | MODIFIED `domain-web-components` |
| 3 | `expand-rich-graphql-referenceapp-coverage` *(next)* | Archived foundation and component changes; reference-app audit | NEW `rich-graphql-reference-coverage` |
| 4 | `add-graphql-web-component-diagnostics` *(pending refinement)* | Archived foundation and component changes | NEW `graphql-web-component-diagnostics` |
| 5 | `add-generic-htmx-web-component-viewer` *(deferred)* | GraphQL reference coverage and component refinement | NEW `generic-web-component-viewer` |

## Programme constraints

- The rich GraphQL schema and standard GraphQL introspection are the application protocol.
- Generated rich-schema naming rules are accepted public client grammar rather than treated as accidental implementation details.
- Components expose semantic Causeway APIs and do not expose GraphQL document construction to page composers.
- HTMX is used only by the generic reference viewer; the component library remains framework-neutral.
- The programme does not add a duplicate GraphQL member-list metadata API.
- Rich-schema extensions are proposed only when a concrete component requirement demonstrates missing semantics.
