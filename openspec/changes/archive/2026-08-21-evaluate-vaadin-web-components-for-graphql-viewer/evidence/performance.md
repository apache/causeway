# Asset and performance evidence

## Static assets

All values use gzip level 9 and exact pinned generated files.

| Asset group | Files | Raw bytes | Gzip bytes |
|---|---:|---:|---:|
| Current foundation and HTMX browser assets | 42 | 390,614 | 106,713 |
| Broad fourteen-component Vaadin bundle | 1 | 527,772 | 127,143 |
| Complete route-split output | 7 | 533,432 | 132,936 |
| Direct installed Vaadin package sources | 443 | 3,449,469 | 683,018 |

The current-viewer row includes its semantic JavaScript and CSS, while Vaadin is an additional candidate widget layer.
A broad production adoption would therefore roughly double compressed viewer code before application and GraphQL assets.

## Route-split entry closures

| Entry and recursive shared chunks | Files | Raw bytes | Gzip bytes |
|---|---:|---:|---:|
| Reference Combo Box and Multi-Select | 4 | 220,112 | 57,145 |
| Grid | 4 | 214,538 | 58,296 |
| Fields, dates, dialog, tabs, and upload | 4 | 384,590 | 95,709 |
| Reference plus Grid combined closure | 6 | 331,115 | 86,244 |
| All split entries and shared chunks | 7 | 533,432 | 132,936 |

Shared chunks make the second route family cheaper after the first, but splitting adds approximately 5.8 KB gzip over the single broad bundle when every entry is loaded.
The reference family is a credible first slice near 57 KB gzip and provides substantially more behavior than a visual theme.
The complete field family is too large to justify eagerly loading on every route.

## Headless browser medians

Five successful headless Chromium runs produced approximate medians of:

- Navigation duration: 98 ms.
- Analysis-ready mark: 87 ms.
- Local resources: 3.
- Transfer size: 559 KB because the analysis server intentionally disables compression and caching.
- Decoded body size: 558 KB.
- Wall-clock `networkidle` journey: 561 ms.

These local measurements compare reproducible runs rather than production network conditions.
Production transfer should use the static gzip values and a throttled deployment-like profile.

## Request and privacy evidence

The standalone and real-viewer journeys made zero requests to origins other than the local application origin.
No usage-statistics submission occurred even though telemetry endpoint strings remain in the generated bundle.
The broad harness loads one candidate JavaScript resource, while the route-split strategy requires one entry and three shared chunks for either reference selection or Grid on a cold route.

## Proposed budgets for a follow-up

- First adopted widget family: at most 65 KB gzip JavaScript including cold shared chunks.
- Reference plus Grid after shared-chunk optimization: at most 90 KB gzip.
- Shell routes without rich widgets: no Vaadin JavaScript request.
- External runtime requests: zero.
- Route-ready regression under a production-like profile: less than 10% after the candidate route is cached.
- Broad eager fourteen-component delivery: rejected.

## Assessment

Vaadin's payload is material but not disqualifying for route-lazy reference selection or Grid.
It is considerably larger than Bootstrap or Open Props styling because it supplies real interaction and accessibility behavior.
Any implementation should begin with one high-value family and preserve native or current Causeway controls elsewhere until route evidence justifies expansion.
