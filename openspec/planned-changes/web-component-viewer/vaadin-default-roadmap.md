# Vaadin-default follow-on roadmap

The architectural decision is that Vaadin free-core becomes the default internal widget toolkit for eligible semantic editors.
Causeway elements, events, GraphQL contexts, canonical routes, and application-owned ordinary HTML remain the stable public contracts.
Native semantic editors remain rollback and fail-safe implementations, and default selection does not imply eager asset delivery.

The repository permits one active OpenSpec change at a time.
The Reference Application suite and input-value hardening are archived.
The archived `fix-webcomponent-action-dispatch-correctness` change completed the remaining Priority 0 correctness tranche identified by the pinned regression corpus.
The next change should isolate versionless identity and preparation before separate union-projection and opaque-route corrections.
Proposal-only drafts must be refined against checked-in capability evidence before promotion.

| Order | Proposal | Purpose | Promotion gate |
|---:|---|---|---|
| 1 | `add-referenceapp-webcomponent-regression-suite` *(archived)* | Copy the pinned broad corpus, establish deterministic inventory, and expose real gaps. | Completed with reviewed classifications. |
| 2 | `harden-webcomponent-input-value-semantics` *(archived)* | Correct precision, temporal, nullable, protected, resource, and custom-value behavior independently of toolkit choice. | Completed against stable Reference Application fixtures. |
| 3 | `fix-webcomponent-action-dispatch-correctness` *(archived)* | Correct nested safe-query and flat mutation dispatch for object and service actions. | Completed with successful typed Reference Application interactions and unchanged public operations. |
| 4 | Focused identity, union-projection, and opaque-route changes | Correct versionless preparation, valid polymorphic metadata selection, and long composite bookmark handling. | Each retained gap has an executable focused contract and passing regression target. |
| 5 | `add-paged-graphql-reference-autocomplete` | Add honest server-side reference windows for large datasets. | Public GraphQL operation, identity, preparation, and ordering contracts are accepted. |
| 6 | `expand-vaadin-semantic-editor-families` | Qualify free-core adapters beyond reference controls with independent lazy closures and CSP gates. | Corrected codecs and Reference Application family journeys pass. |
| 7 | `make-vaadin-default-for-webcomponent-viewer` | Flip the supported selection policy to Vaadin-first and remove pilot status. | All prerequisite suites pass in default and explicit native modes. |

The final default change is intentionally small and policy-focused.
Correctness, GraphQL capability, and component-family expansion remain separate so failures do not become entangled with the default flip.

Each proposal-only directory currently contains `proposal.md` rather than a complete promotable change.
When its gate is satisfied, create or promote one active change, regenerate design, delta specifications, and tasks from current evidence, validate strictly, implement, archive, and only then promote the next item.
