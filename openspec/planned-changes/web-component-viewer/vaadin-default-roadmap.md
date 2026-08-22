# Vaadin-default follow-on roadmap

The architectural decision is that Vaadin free-core becomes the default internal widget toolkit for eligible semantic editors.
Causeway elements, events, GraphQL contexts, canonical routes, and application-owned ordinary HTML remain the stable public contracts.
Native semantic editors remain rollback and fail-safe implementations, and default selection does not imply eager asset delivery.

The repository permits one active OpenSpec change at a time.
The following proposal-only drafts are deliberately sequenced behind `add-referenceapp-webcomponent-regression-suite` and must be refined against its checked-in capability inventory before promotion.

| Order | Proposal | Purpose | Promotion gate |
|---:|---|---|---|
| 1 | `add-referenceapp-webcomponent-regression-suite` | Copy the pinned broad corpus, establish deterministic inventory, and expose real gaps. | Active change completes with reviewed classifications. |
| 2 | `harden-webcomponent-input-value-semantics` | Correct precision, temporal, nullable, protected, resource, and custom-value behavior independently of toolkit choice. | Reference Application identifies concrete affected shapes and stable fixtures. |
| 3 | `add-paged-graphql-reference-autocomplete` | Add honest server-side reference windows for large datasets. | Public GraphQL operation and ordering contract are accepted. |
| 4 | `expand-vaadin-semantic-editor-families` | Qualify free-core adapters beyond reference controls with independent lazy closures and CSP gates. | Corrected codecs and Reference Application family journeys pass. |
| 5 | `make-vaadin-default-for-webcomponent-viewer` | Flip the supported selection policy to Vaadin-first and remove pilot status. | All prerequisite suites pass in default and explicit native modes. |

The final default change is intentionally small and policy-focused.
Correctness, GraphQL capability, and component-family expansion remain separate so failures do not become entangled with the default flip.

Each proposal-only directory currently contains `proposal.md` rather than a complete promotable change.
When its gate is satisfied, create or promote one active change, regenerate design, delta specifications, and tasks from current evidence, validate strictly, implement, archive, and only then promote the next item.
