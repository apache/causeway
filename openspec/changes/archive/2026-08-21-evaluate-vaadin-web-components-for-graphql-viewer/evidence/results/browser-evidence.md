# Headless browser evidence

Generated: 2026-08-21T17:55:32.036Z

## Journey assertions

- PASS: standaloneNoFlow
- PASS: oneRouteContext
- PASS: componentsDefined
- PASS: keyboardSingleReference
- PASS: keyboardMultiReference
- PASS: keyboardGridFocus
- PASS: singleReference
- PASS: multiReference
- PASS: gridWindow
- PASS: sortFilterGapRecorded
- PASS: staleSearchSuppressed
- PASS: representativeStates
- PASS: validationAuthoritative
- PASS: actionPrompt
- PASS: semanticCustomPage
- PASS: rawTier
- PASS: lifecycleBounded
- PASS: noExternalRequests
- PASS: noBrowserErrors

## Scenarios

| Scenario | Axe violations | Axe incomplete | Overflow px | External requests | Browser errors |
|---|---:|---:|---:|---:|---:|
| generic-desktop-light | 0 | 3 | 0 | 0 | 0 |
| semantic-desktop-dark | 0 | 3 | 0 | 0 | 0 |
| raw-desktop-light | 0 | 3 | 0 | 0 | 0 |
| generic-narrow-light | 0 | 3 | 0 | 0 | 0 |
| semantic-narrow-dark | 0 | 3 | 0 | 0 | 0 |
| generic-forced-colors | 0 | 3 | 0 | 0 | 0 |

## Median timings

```json
{
  "navigation": 124.80000007152557,
  "readyMark": 111.80000007152557,
  "resources": 3,
  "transferSize": 559141,
  "decodedBodySize": 558241,
  "wallClock": 588.0749580000011
}
```

## Known contract evidence

- Combo Box paging is local because GraphQL autocomplete accepts search only.
- Grid sorting and filtering are recorded as unsupported because the collection window accepts only offset and size.
- Every route mode retains exactly one route object context.
- No Vaadin Flow client or external network request was present during the journey.

