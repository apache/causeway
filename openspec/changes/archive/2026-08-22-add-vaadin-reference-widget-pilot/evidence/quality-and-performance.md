# Quality and performance evidence

## Bundle and packaging

| Measure | Result |
|---|---:|
| Selective JavaScript raw | 191,342 bytes |
| Selective JavaScript gzip level 9 | 48,684 bytes |
| Approved cold budget | 66,560 bytes |
| Runtime packages | 19 |
| CSP style hashes | 4 |
| Foundation JAR | 227,486 bytes |
| JAR candidate resource entries including directory | 3 |
| JAR candidate license entries including directory | 21 |

The bundle is 17,876 bytes below the 65 KiB budget.
The bundle SHA-256 is `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81`.

## Browser scenarios

The retained headless Chrome suite runs:

- Desktop light.
- Desktop dark with reduced motion.
- Narrow light at 390 pixels.
- Narrow dark with reduced motion at 390 pixels.
- Forced colors.

Every scenario creates single-reference, required-reference, disabled-reference, multi-reference, and autocomplete semantic editors across generic and ordinary custom HTML sections.
The desktop journey uses keyboard selection for fixed and searched references, adds and removes a multi-reference token, clears an optional value, presents a required validation error, dismisses overlays, removes and reconnects an editor, and records semantic events.

The result is:

- Zero axe violations.
- Zero CSP violations.
- Zero console errors.
- Zero page errors.
- Zero external requests.
- Zero page-overflow scenarios.
- One candidate request per document after the first eligible editor.
- Zero candidate requests before an eligible editor.
- No Flow runtime.

Axe has no incomplete result in ordinary light, dark, reduced-motion, or narrow scenarios.
Forced-colors contrast remains manually incomplete because browser system colors do not expose a stable computed contrast value to axe; the retained screenshot preserves control borders, labels, required state, and focus structure for review.

## Initialization

Five local no-cache runs reported first candidate editor readiness in approximately 19 to 28 milliseconds, with a median near 24 milliseconds.
The local server intentionally sends the uncompressed 191,342-byte body with `no-store`, so production network cost is represented by the static 48,684-byte gzip value rather than local transfer size.
The resource itself completed in approximately one to two milliseconds on loopback.

Routes without an eligible enabled editor make no candidate request and do not wait for candidate readiness.
Repeated editors share the one browser module evaluation.

## Real Petclinic

The packaged opt-in Petclinic journey verified:

- Original route and menu readiness.
- No candidate request on the home or owner route before the reference prompt.
- One candidate request when `removePet` opened.
- Stable `petclinic.Pet:s_pet-basil` selection through actual keyboard input.
- Exact style hashes in the real response policy with no blanket inline permission.
- Zero CSP violations, console errors, page errors, HTTP failures, external requests, or overflow.
- No Flow runtime.
- Overlay cleanup and focus restoration to `removePet` after Escape.

The full existing Petclinic integration and Playwright suites also pass with the pilot enabled.

## Vanilla HTML sample

The sample loaded no candidate asset before `selectRelated` opened.
The prompt loaded one candidate asset, selected `causeway.webcomponents.sample.SampleRelatedObject:s_related-1`, dismissed with Escape, restored focus, and produced zero console error, page error, HTTP failure, external request, Flow runtime, or overflow.

## Supply-chain audit

`npm audit --omit=dev --json` reports zero info, low, moderate, high, or critical vulnerability for the production closure.
Every generated asset, package record, integrity, license, repository, bundle checksum, CSP hash, prohibited-package marker, collector marker, external import, and compressed budget is checked by `verify.mjs` during Maven validation.
