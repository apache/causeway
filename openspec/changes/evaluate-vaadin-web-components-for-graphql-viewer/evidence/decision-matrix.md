# Hard gates and widget-first decision matrix

## Hard gates

| Gate | Current viewer | Vaadin free-core evidence | Result |
|---|---|---|---|
| Approved free-core licensing | Pass | Direct Vaadin packages and all Vaadin runtime transitive packages declare Apache-2.0; no Pro package is present | Pass |
| No Flow runtime or state | Pass | Standalone and Petclinic journeys found no Flow client and used direct ES modules | Pass |
| Public GraphQL remains authoritative | Pass | Candidate adapters used GraphQL-shaped autocomplete, collection-window, validation, and invocation operations | Pass with recorded API gaps |
| Searchable single and multi-reference selection without another widget library | Current handcrafted autocomplete; Wicket uses Select2 | Keyboard and programmatic Vaadin selection passed with stable bookmark identity and stale-request suppression | Pass for current Wicket parity |
| Lazy collection behavior | Existing offset and size window | Grid consumed offset, size, total count, configured order, stable identity, and canonical navigation | Pass for read-only windows; fail for user sort and filter parity |
| Custom HTML page composition | Pass | Generic, semantic custom, and raw custom pages retained one route context and ordinary HTML composition | Pass |
| Pinned offline Maven packaging | Pass | Selective assets and fourteen direct licenses packaged in a 320,248-byte JAR without CDN access | Pass |
| Accessibility and responsive behavior | Pass with known baseline contrast work | Zero axe violations, successful keyboard journeys, zero overflow, and light, dark, reduced-motion, and forced-color evidence | Pass with retained manual-incomplete checks |
| Current production CSP | Pass | Four component-originated inline-style attempts were blocked by `style-src 'self'` | **Fail until a reviewed style CSP strategy exists** |
| External network isolation | Pass | Zero external requests and no usage-statistics submission | Pass |

Vaadin is not production-ready under the current CSP despite passing most functional gates.
Weighted scores therefore describe potential value after the CSP issue is resolved; they do not override the failed gate.

## Weighted scores

Scores range from 0 to 5.

| Strategy | Widget coverage 30% | GraphQL architecture and composability 25% | Accessibility 15% | Supply chain and packaging 15% | Performance 10% | Theming 5% | Weighted total |
|---|---:|---:|---:|---:|---:|---:|---:|
| Current Causeway web-component viewer | 2.5 | 5.0 | 3.5 | 5.0 | 4.5 | 3.5 | **3.90** |
| Constrained Vaadin reference-widget subset | 4.5 | 4.0 | 4.5 | 3.5 | 3.0 | 4.0 | **4.05** |
| Broad evaluated Vaadin suite | 4.7 | 3.5 | 4.5 | 3.5 | 2.0 | 4.0 | **3.89** |

## Score evidence

### Domain widget coverage and Wicket parity

The free core supplies Combo Box, Multi-Select Combo Box, Grid, date, time, date-time, upload, dialog, selection, text, boolean, tabs, and other business controls.
Single and multi-selection reproduced the important Wicket Select2 behavior: debounced remote filtering, stable identities, required and clearable states, deterministic selection reconciliation, keyboard operation, and Causeway-owned validation.
Vaadin's page-aware Combo Box API exceeds the current Wicket provider, although GraphQL cannot yet page autocomplete results.
Evidence: `wicket-select2-parity.md`, `graphql-contract-inventory.md`, and `results/browser-evidence.json`.

### GraphQL architecture and custom-page composability

No Flow runtime or state participated.
Generic, semantic custom, and raw custom pages used ordinary HTML and retained one route object context.
Causeway wrappers hid Vaadin data-provider and event protocols successfully.
The score is reduced because autocomplete paging and Grid sort and filter cannot map to current public GraphQL arguments.
Evidence: `graphql-gap-analysis.md`, `extension-tier-assessment.md`, and browser journey assertions.

### Accessibility

All six axe scenarios reported zero violations after correcting fixture use of Vaadin's documented checkbox label API.
Actual keyboard input selected single and multiple references, focused Grid internals, dismissed the action dialog, and restored focus.
Shadow-DOM ARIA and contrast checks remain manually incomplete, and strict-CSP blocking can interfere with production styling.
Evidence: `accessibility-and-visual-review.md` and retained screenshots.

### Supply chain and packaging

All Vaadin runtime modules are Apache-2.0, audit results are clean, npm integrity is pinned, Pro modules are absent, and Maven packaging works offline at browser runtime.
The score is lower than the baseline because production would add npm and esbuild to release engineering, 33 runtime package instances, telemetry-code governance, and CSP review.
Evidence: `candidates.md`, `packaging-assessment.md`, and `results/asset-verification.json`.

### Performance

The broad bundle is 127,143 bytes gzip.
A cold reference-controls route is 57,145 bytes gzip, Grid is 58,296 bytes, and their shared combined closure is 86,244 bytes.
These costs are credible for route-lazy high-value widgets but not for unconditional shell loading.
Evidence: `performance.md` and `results/asset-sizes.json`.

### Theming

Causeway tokens mapped coherently to Vaadin and Lumo variables with no global CSS reset.
Shadow DOM reduces collisions but limits private-state correction, and current strict CSP blocks component inline styles.
Evidence: `theme-assessment.md` and the real-viewer integration report.

## Interpretation

The broad Vaadin suite does not outperform the current architecture once payload, release tooling, and adapter cost are included.
The constrained reference-widget subset has the strongest net value because it directly replaces the WicketStuff and Select2 class of dependency with one maintained free-core component family.

The score margin is small and the current CSP gate fails.
The recommendation is therefore a CSP-first, opt-in reference-widget pilot with an automatic stop condition, not broad adoption.
Grid remains a credible second slice for read-only lazy windows, but interactive sort and filter require an explicit GraphQL capability change first.
