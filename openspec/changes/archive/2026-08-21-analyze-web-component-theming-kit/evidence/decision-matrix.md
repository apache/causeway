# Hard gates and weighted decision matrix

## Hard gates

| Strategy | License and provenance | Offline Maven path | No required commercial feature | Causeway semantic boundary | Accessibility | Browser support or fallback | Result |
|---|---|---|---|---|---|---|---|
| Current Causeway baseline | Pass | Pass | Pass | Pass | Pass with known contrast work | Pass under current Chromium contract | Pass |
| Bootstrap CSS-only or Sass-derived | Pass | Pass through explicit WebJar or Maven build | Pass | Pass only while Bootstrap JavaScript and public toolkit contracts remain excluded | Pass with contextual contrast fixes | Pass | Pass with scope constraint |
| Web Awesome selective adapters | Pass for MIT core; full transitive report still required | Pass through pinned npm and selective Maven bundle | Pass for evaluated core | Pass only through explicit internal adapters | Pass in fixture | Pass for current evergreen Chromium; wider policy still required | Pass with adapter and bundle constraints |
| Open Props plus native primitives | Pass | Pass through selected CSS packaging | Pass | Pass | Pass after curated dark and control mappings | Pass only with current disclosure fallback until browser floor is explicit | Pass with fallback |

Material Web remains comparison-only because of maintenance status.
Shoelace fails the active-maintenance gate.

## Scores

Scores range from 0 to 5 and use the weights defined before prototype work.

| Strategy | Accessibility 25% | Compatibility 20% | Maintenance 15% | Packaging 15% | Visual and theming 15% | Performance 10% | Weighted total |
|---|---:|---:|---:|---:|---:|---:|---:|
| Current Causeway baseline | 3.5 | 5.0 | 3.0 | 5.0 | 3.5 | 4.0 | **4.00** |
| Bootstrap constrained theme | 3.5 | 4.0 | 4.5 | 5.0 | 4.0 | 4.0 | **4.10** |
| Web Awesome selective adapters | 5.0 | 2.5 | 4.0 | 3.0 | 4.5 | 2.5 | **3.73** |
| Open Props and native primitives | 3.5 | 5.0 | 4.0 | 4.5 | 2.5 | 5.0 | **4.03** |

## Score evidence

### Accessibility and interaction correctness

- Web Awesome scored 100 in desktop and mobile Lighthouse audits and passed the common focus journeys.
- Baseline and Bootstrap scored 96 because of contextual contrast failures while retaining correct menu, prompt, tab, and overflow behavior.
- Open Props scored 100 desktop and 96 mobile, with 27 dark mobile contrast failures caused by incomplete token and button-pack integration.
- Evidence: `accessibility-and-visual-review.md`, `results/browser-evidence.json`, and `results/lighthouse-summary.json`.

### Semantic and architectural compatibility

- Baseline is the contract being preserved.
- Bootstrap CSS can retain Causeway behavior but creates global collision and internal class pressure.
- Web Awesome needs adapter code for every adopted control and introduces shadow DOM boundaries.
- Open Props can remain a private token source and native primitives can preserve Causeway events.
- Evidence: `adapter-assessment.md` and `results/real-viewer-integration.json`.

### Maintenance and governance

- Bootstrap has the most mature maintenance and broadest existing ecosystem.
- Web Awesome is active and MIT licensed but is a newer successor with a 35-instance resolved runtime dependency closure.
- Open Props has a small dependency surface but provides fewer maintained component decisions.
- Baseline avoids upstream risk but leaves all design-system maintenance with Causeway.
- Evidence: `candidates.md`, `packaging-assessment.md`, and `results/security-audit.json`.

### Packaging and build integration

- Baseline and Bootstrap fit the existing Maven and WebJar model directly.
- Open Props is small and packages easily, although the indexed WebJar trails the frozen release.
- Web Awesome requires pinned npm acquisition and bundling but the analysis built and packaged selective assets successfully.
- Evidence: `packaging/README.md`, `results/packaging-verification.json`, and `results/asset-verification.json`.

### Visual and theming capability

- Web Awesome provides the most complete coherent control-level design system.
- Bootstrap gives a familiar compact result quickly and aligns with the Wicket viewer's design lineage.
- Baseline is coherent and product-specific but visibly handcrafted in control hierarchy.
- Open Props supplies ingredients rather than a complete visual system and needs the most Causeway-authored theme work.
- Evidence: retained screenshots and `accessibility-and-visual-review.md`.

### Performance

- Open Props has a 12,028-byte-gzip candidate theme payload.
- Bootstrap CSS plus adapter is 31,084 bytes gzip, excluding optional JavaScript.
- The selective twelve-component Web Awesome bundle is 84,848 bytes gzip including toolkit CSS and the adapter.
- Web Awesome's unbundled browser distribution generated 132 requests and is rejected for production.
- Evidence: `performance.md` and `results/asset-sizes.json`.

## Decision interpretation

Bootstrap's 4.10 score is only 0.10 above the current baseline and 0.075 above Open Props.
That margin does not justify a wholesale rewrite or a public toolkit contract.
It does justify a constrained implementation pilot because Bootstrap provides the strongest combination of visual coverage, mature maintenance, Maven packaging, and continuity with the Wicket-inspired viewer.

Web Awesome is not selected for broad adoption because its adapter and payload costs outweigh its stronger component-level accessibility and polish for the current semantic architecture.
Open Props remains a useful token and native-platform reference, but the prototype shows that it does not remove enough Causeway-authored design work to serve as the primary theming kit.
