# Web-component viewer planned changes

These are fully drafted follow-on changes for the Causeway web-component viewer programme.
They are held outside `openspec/changes/` because the repository permits only one active OpenSpec change at a time.
The foundation change `establish-graphql-web-component-foundation`, read-only change `add-read-only-domain-web-components`, and interaction change `add-domain-web-component-interactions` are archived.
The analysis-only `analyze-rich-graphql-referenceapp-coverage` change is active and has produced executable evidence, a machine-readable matrix, a narrative roadmap, two new P0 correctness proposals, and narrower P1 or P2 follow-on scopes.
No implementation draft may be promoted until that analysis is validated, completed, synced, and archived.
The `add-generic-htmx-web-component-viewer` draft remains deferred until the accepted GraphQL, composite-object, and menu-bar work is complete.

Each child directory is a complete change folder containing `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`, and its delta specifications.
After the active change is implemented, synced, and archived, promote the next draft verbatim:

```shell
git mv openspec/planned-changes/web-component-viewer/<name> openspec/changes/<name>
openspec validate <name> --strict
```

The OpenSpec CLI does not scan this planned-change directory, so drafts must be strictly validated after promotion and before implementation.
Review each draft against discoveries made by preceding changes and update stale assumptions before promotion.
Matrix entry references point to `viewers/graphql/adoc/modules/ROOT/examples/referenceapp-analysis/coverage-matrix.yaml` and provide the scope gate for each draft.

## Evidence-backed promotion order

| Order | Draft | Priority | Depends on | Capability impact |
|---:|---|---|---|---|
| 1 | `add-read-only-domain-web-components` *(archived)* | Complete | Archived foundation | NEW `domain-web-components` |
| 2 | `add-domain-web-component-interactions` *(archived)* | Complete | Archived read-only components | MODIFIED `domain-web-components` |
| 3 | `analyze-rich-graphql-referenceapp-coverage` *(active; analysis only)* | Gate | Archived foundation and components; pinned reference application | NEW `rich-graphql-referenceapp-analysis` |
| 4 | `fix-rich-graphql-object-interaction-correctness` | P0 | Completed analysis | NEW `rich-graphql-object-interaction-correctness` |
| 5 | `fix-rich-graphql-resource-link-safety` | P0 | Completed analysis | NEW `rich-graphql-resource-link-safety` |
| 6 | `add-rich-graphql-value-and-resource-semantics` | P1 | Object-interaction correctness and resource-link safety | NEW `rich-graphql-value-semantics` |
| 7 | `add-rich-graphql-collection-windowing` | P1 | Object-interaction correctness | NEW `rich-graphql-collection-windowing` |
| 8 | `add-rich-graphql-application-entry-points` | P1 | Object-interaction correctness and resource-link safety | NEW `rich-graphql-application-entry-points` |
| 9 | `add-composite-object-web-component` | P1 | Accepted value semantics, collection windows, object correctness, and structural resource safety | MODIFIED `domain-web-components` with `<causeway-object>` |
| 10 | `add-menubar-web-components` | P1 | Application entry points, service-action correctness, accepted value semantics, and structural resource safety | MODIFIED `domain-web-components` with menu bars |
| 11 | `add-rich-graphql-member-metadata` | P2 | Completed analysis and proven standalone-component requirements | NEW narrow `rich-graphql-member-metadata` |
| 12 | `add-graphql-web-component-diagnostics` *(pending refinement)* | P2 | Archived foundation and component interactions; accepted redaction boundaries | NEW `graphql-web-component-diagnostics` |
| 13 | `add-generic-htmx-web-component-viewer` *(deferred)* | Final | Accepted P0 and P1 GraphQL work, composite object, and menu bars | NEW `generic-web-component-viewer` |

The two P0 changes correct successful-looking or unsafe established contracts and should precede additive capabilities.
The P1 GraphQL changes are independent bounded capabilities after their stated prerequisites, but the single-active-change rule requires serial promotion.
The narrow member-metadata change is not a prerequisite for effective-grid object composition or effective-resource menu composition because those resources remain the canonical structural sources.
Diagnostics may be refined earlier if needed for implementation troubleshooting, but the generic viewer remains last.

## Minimum generic-viewer prerequisite set

Before `add-generic-htmx-web-component-viewer` can be promoted, the programme must have:

- correct public object identity, polymorphic output, action argument conversion, and authoritative property mutation;
- valid and policy-separated same-origin structural and value resource links;
- accepted reversible value semantics for the reference-derived input set used by the default viewer;
- bounded collection windows for generic collection pages;
- application entry points for the effective menu resource and configured home-page object or action;
- completed `<causeway-object>` and `<causeway-menubars>` semantic components;
- stable semantic navigation and result events from the archived component interactions.

Narrow wrapper metadata and diagnostics are desirable but are not hard prerequisites unless implementation of the preceding components demonstrates a new required field or observability gate.

## Programme constraints

- The rich GraphQL schema and standard GraphQL introspection are the application protocol.
- Generated rich-schema naming rules are accepted public client grammar rather than treated as accidental implementation details.
- Components expose semantic Causeway APIs and do not expose GraphQL document construction to page composers.
- HTMX is used only by the generic reference viewer; the component library remains framework-neutral.
- The programme does not add duplicate member-list, datatype-catalogue, grid, or menu metadata APIs.
- Effective grid and menu resources remain the canonical structural sources.
- Rich-schema extensions are proposed only when executable evidence and a concrete semantic client requirement demonstrate missing behavior.
- Unsupported input values never silently promise reversible generic-string behavior.
- Passwords, hidden values, authorization rules, and sensitive resource content remain outside metadata, diagnostics, errors, and fallback serialization.
