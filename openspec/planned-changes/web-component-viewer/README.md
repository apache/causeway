# Web-component viewer planned changes

This directory contains complete follow-on changes and evidence-gated proposal-only drafts for the Causeway web-component viewer programme.
They are held outside `openspec/changes/` because the repository permits only one active OpenSpec change at a time.
The foundation, domain-component, rich GraphQL correctness, value-semantics, collection-windowing, application-entry, composite-object, menubar, generic HTMX, theming, Vaadin evaluation, and Vaadin reference-widget pilot changes are archived.
The Reference Application regression suite and input-value hardening changes are archived.
The action-dispatch correctness change is archived, completing the remaining Priority 0 correction identified by that broad capability inventory.
The versionless-identity and preparation correction is archived.
The union-projection correction is archived.
The opaque-route correction is archived.
The archived `add-paged-graphql-reference-autocomplete` change adds honest bounded server response windows, and the archived `expand-vaadin-semantic-editor-families` change qualifies the broader editor families before the default-policy flip in `vaadin-default-roadmap.md`.
The archived `make-vaadin-default-for-webcomponent-viewer` change completes the policy-focused Vaadin-default sequence while retaining explicit native rollback.
No Vaadin-default change remains active.
The older Vue, Svelte, metadata, diagnostics, performance-analysis, catalogue, and designer drafts remain queued behind that higher-priority qualification and default-adoption work.

Complete child directories contain `.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`, and delta specifications and can be promoted verbatim after review.
Proposal-only directories contain only `proposal.md`; they require current evidence, full artifact generation, and strict validation before promotion.
After the active change is archived, promote an applicable complete draft with:

```shell
git mv openspec/planned-changes/web-component-viewer/<name> openspec/changes/<name>
openspec validate <name> --strict
```

The OpenSpec CLI does not scan this planned-change directory, so drafts must be strictly validated after promotion and before implementation.
Review each draft against discoveries made by preceding changes and update stale assumptions before promotion.
Do not promote the proposal-only Vaadin-default follow-ons out of the order and gates recorded in `vaadin-default-roadmap.md`.
Matrix entry references point to `viewers/graphql/adoc/modules/ROOT/examples/referenceapp-analysis/coverage-matrix.yaml` and provide the scope gate for evidence-derived drafts.

## Evidence-backed promotion order

| Order | Draft | Priority | Depends on | Capability impact |
|---:|---|---|---|---|
| 1 | `add-read-only-domain-web-components` *(archived)* | Complete | Archived foundation | NEW `domain-web-components` |
| 2 | `add-domain-web-component-interactions` *(archived)* | Complete | Archived read-only components | MODIFIED `domain-web-components` |
| 3 | `analyze-rich-graphql-referenceapp-coverage` *(archived; analysis only)* | Complete | Archived foundation and components; pinned reference application | NEW `rich-graphql-referenceapp-analysis` |
| 4 | `fix-rich-graphql-object-interaction-correctness` *(archived)* | Complete | Completed analysis | NEW `rich-graphql-object-interaction-correctness` |
| 5 | `fix-rich-graphql-resource-link-safety` *(archived)* | Complete | Completed analysis | NEW `rich-graphql-resource-link-safety` |
| 6 | `add-rich-graphql-value-and-resource-semantics` *(archived)* | Complete | Object-interaction correctness and resource-link safety | NEW `rich-graphql-value-semantics` |
| 7 | `add-rich-graphql-collection-windowing` *(archived)* | Complete | Object-interaction correctness | NEW `rich-graphql-collection-windowing` |
| 8 | `add-rich-graphql-application-entry-points` *(archived)* | Complete | Object-interaction correctness and resource-link safety | NEW `rich-graphql-application-entry-points` |
| 9 | `add-composite-object-web-component` *(archived)* | Complete | Accepted value semantics, collection windows, object correctness, and structural resource safety | MODIFIED `domain-web-components` with `<causeway-object>` |
| 10 | `add-menubar-web-components` *(archived)* | Complete | Application entry points, service-action correctness, accepted value semantics, and structural resource safety | MODIFIED `domain-web-components` with menu bars |
| 11 | `add-generic-htmx-web-component-viewer` *(archived)* | Complete | Accepted P0 and P1 GraphQL work, composite object, and menu bars | NEW `generic-htmx-web-component-viewer` |
| 12 | `add-generic-vue-web-component-viewer` | P1 | Same semantic prerequisites and shared canonical routing contract | NEW `generic-vue-web-component-viewer` |
| 13 | `add-generic-svelte-web-component-viewer` | P1 | Same semantic prerequisites and shared canonical routing contract | NEW `generic-svelte-web-component-viewer` |
| 14 | `add-rich-graphql-member-metadata` *(archived)* | Complete | Completed analysis and proven standalone-component requirements | NEW narrow `rich-graphql-member-metadata` |
| 15 | `add-graphql-web-component-diagnostics` *(pending refinement)* | P2 | Archived foundation and component interactions; accepted redaction boundaries | NEW `graphql-web-component-diagnostics` |
| 16 | `analyze-rich-graphql-collection-query-pushdown` *(analysis only)* | P2 performance | Archived collection windowing | NEW `rich-graphql-collection-query-pushdown-analysis` |
| 17 | `analyze-rich-graphql-parallel-execution` *(analysis only)* | P2 performance | Correct interactions and representative rich operations | NEW `rich-graphql-parallel-execution-analysis` |
| 18 | `publish-web-component-catalogue-and-workbench` | P2 | Completed public component vocabulary | NEW `web-component-catalogue-and-workbench` |
| 19 | `analyze-semantic-page-designer` *(analysis only)* | Future gate | Generic HTMX, Vue, and Svelte viewers plus component catalogue | NEW `semantic-page-designer-analysis` |

The two P0 changes correct successful-looking or unsafe established contracts and precede additive capabilities.
The P1 GraphQL changes are independent bounded capabilities after their stated prerequisites, but the single-active-change rule requires serial promotion.
The three generic viewers are higher priority than the catalogue workbench and page-designer analysis.
HTMX is the first reference router implementation, while Vue and Svelte remain sibling production viewers rather than samples or wrappers.
The narrow member-metadata, diagnostics, collection-query-pushdown analysis, and parallel-execution analysis are useful but do not block the generic viewer routers unless implementation evidence reveals a new hard dependency.
The two performance analyses must preserve the existing semantic contracts and produce separate implementation proposals rather than changing production behavior directly.
No production semantic page-designer proposal will be drafted until its analysis selects an authoring model and artifact contract.

## Shared generic-viewer routing contract

The three generic viewers preserve one architectural boundary:

```text
canonical bookmark or application entry
                 |
                 v
       host framework router
       +---------------------+
       |                     |
exact logical-type page   no registration
       |                     |
       v                     v
framework custom page   generic route page
                             |
                             v
                    <causeway-object>
```

- Routing and exact-logical-type page selection belong to the host viewer.
- `<causeway-object>` remains a pure effective-grid or fallback object renderer and never discovers custom pages.
- Custom and generic pages render beneath one route-level object context.
- `<causeway-menubars>` remains in a stable shell outside changing object pages.
- Semantic object navigation, home entries, and interaction results flow into replaceable viewer policy.
- HTMX uses server routes and HTML fragments.
- Vue uses Vue Router and registered Vue components or async components.
- Svelte uses SvelteKit routes and registered Svelte components or lazy loaders.
- Canonical bookmark meaning and custom-before-generic precedence remain compatible across viewers.
- Framework-specific lifecycle, history, hydration, and rendering remain internal to each viewer.

No framework-neutral page provider is introduced inside the semantic component library.

## Minimum generic-viewer prerequisite set

Before any generic viewer can be promoted, the programme must have:

- correct public object identity, polymorphic output, action argument conversion, and authoritative property mutation;
- valid and policy-separated same-origin structural and value resource links;
- accepted reversible value semantics for the reference-derived input set used by default pages;
- bounded collection windows for generic collection pages;
- application entry points for the effective menu resource and configured home-page object;
- completed `<causeway-object>` and `<causeway-menubars>` semantic components;
- stable semantic navigation and result events from the archived component interactions.

The generic viewers do not require the later designer, catalogue workbench, or a page-provider abstraction.

## Page-authoring sequence

The catalogue and designer work are intentionally later:

```text
generic HTMX, Vue, and Svelte viewers
                 |
                 v
 component catalogue and workbench
                 |
                 v
 semantic page-designer analysis
                 |
                 v
 separately reviewed implementation proposals, if justified
```

The catalogue publishes machine-readable element contracts and an interactive developer workbench.
The designer analysis evaluates GrapesJS, a purpose-built semantic tree, and another viable approach before selecting direct HTML, an intermediate model, or a constrained hybrid.
Any generated custom page uses ordinary HTML and public Causeway elements and registers at the host router.
It does not replace Causeway grid XML or make `<causeway-object>` aware of custom pages.

## Programme constraints

- The rich GraphQL schema and standard GraphQL introspection are the application protocol.
- Generated rich-schema naming rules are accepted public client grammar rather than accidental implementation details.
- Components expose semantic Causeway APIs and do not expose GraphQL document construction to page composers.
- HTMX, Vue, and Svelte remain host viewer technologies; the component library remains framework-neutral.
- The programme does not add duplicate member-list, datatype-catalogue, grid, or menu metadata APIs.
- Effective grid and menu resources remain canonical structural sources.
- Rich-schema extensions are proposed only when executable evidence and a concrete semantic client requirement demonstrate missing behavior.
- Unsupported input values never silently promise reversible generic-string behavior.
- Passwords, hidden values, authorization rules, and sensitive resource content remain outside metadata, diagnostics, errors, fallback serialization, workbench fixtures, and designer artifacts.
