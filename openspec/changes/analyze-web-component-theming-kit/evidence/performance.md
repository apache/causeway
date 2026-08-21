# Asset and performance evidence

## Static asset groups

The asset measurement script compresses each file independently with gzip level 9 and excludes shared harness files.

| Group | Files | Raw bytes | Gzip bytes |
|---|---:|---:|---:|
| Current foundation and HTMX production assets plus baseline adapter | 43 | 393,535 | 107,443 |
| Bootstrap CSS plus candidate adapter | 2 | 233,395 | 31,084 |
| Optional Bootstrap JavaScript bundle with Popper | 1 | 80,496 | 23,812 |
| Web Awesome entry CSS, loader, and adapter | 3 | 3,820 | 1,325 |
| Selective Web Awesome bundle for twelve fixture components plus adapter | 3 | 378,423 | 84,848 |
| Complete Web Awesome `dist-cdn` measurable browser asset set | 662 | 2,462,791 | 675,990 |
| Open Props packs plus candidate adapter | 4 | 50,947 | 12,028 |

The baseline group includes JavaScript modules used by the real semantic component library, while the CSS-oriented candidates do not replace that JavaScript.
Candidate totals therefore represent additional or alternative theme payloads rather than equivalent full viewers.

## Browser-loaded fixture medians

Measurements use one warm-up and five local unthrottled loads with `Cache-Control: no-store`.
Values are useful for relative request and payload comparison but are not production network benchmarks.

| Strategy | Candidate requests | Encoded bytes | DOM content loaded | Load | FCP | LCP |
|---|---:|---:|---:|---:|---:|---:|
| Baseline fixture styles | 4 | 36,055 | 6.5 ms | 8.2 ms | 16 ms | 16 ms |
| Bootstrap CSS | 2 | 233,395 | 5.3 ms | 8.2 ms | 20 ms | 20 ms |
| Web Awesome browser autoloader | 132 | 615,233 | 3.8 ms | 11.2 ms | 12 ms | 32 ms |
| Open Props packs | 4 | 50,947 | 5.8 ms | 8.2 ms | 12 ms | 16 ms |

Bootstrap's source CSS is large but compresses efficiently.
A production Bootstrap integration should compile or select only used Sass layers rather than treating the complete distribution as a budget target.
Web Awesome's `dist-cdn` autoloader is convenient for analysis but creates 132 local requests for the representative fixture and is rejected as a production loading strategy.
The reproducible selective bundle reduced delivery to one 70,009-byte-gzip JavaScript file and one 14,124-byte-gzip CSS file before the 715-byte-gzip adapter.
That 84,848-byte combined payload is credible but exceeds the initial 50 KB JavaScript budget and remains substantially larger than either CSS-oriented strategy.
Open Props has the smallest candidate payload, but Causeway must still ship its existing component behavior and a curated adapter theme.

## Request interpretation

- Baseline fixture requests include the foundation theme, structural component styles, HTMX shell style, and the analysis adapter.
- Bootstrap requests include complete compiled Bootstrap CSS and the analysis adapter; Bootstrap JavaScript is excluded because Causeway behavior remains authoritative.
- Web Awesome requests include theme imports, loader, selected components, and their shared Lit, style, localization, positioning, and utility chunks.
- Open Props requests include the core props, normalize pack, buttons pack, and adapter.

## Real-viewer integration

The bounded Petclinic injection check produced zero browser failures and zero page-level overflow for every strategy.
Bootstrap changed the first real viewer button from a 4-pixel to a 0-pixel border radius and replaced the inherited font stack, demonstrating global collision before purposeful class adoption.
Open Props changed the generic control font and radius through its normalize and button packs.
Web Awesome left ordinary Causeway controls largely unchanged and required one explicit internal adapter element to display a toolkit control.

## Proposed production budgets

A follow-on implementation should use these initial compressed budgets:

- Theme CSS added to the existing viewer: no more than 40 KB gzip.
- Toolkit JavaScript added to the existing viewer: no more than 50 KB gzip for the first adopted slice.
- Candidate-owned initial requests: no more than 8 before HTTP caching.
- No font or icon download unless separately justified, pinned, and packaged.
- No regression beyond 10% in route-ready or largest-contentful-paint medians under a throttled production-like profile.

These are decision budgets rather than established project policy and require review in the implementation proposal.
