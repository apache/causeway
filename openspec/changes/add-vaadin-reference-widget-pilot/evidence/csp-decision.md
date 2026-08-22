# Vaadin reference widget CSP decision gate

## Decision requested

Approve an exact SHA-256 `style-src-elem` allowlist for the four static style blocks created by the pinned Combo Box and Multi-Select Combo Box closure, while explicitly retaining `style-src-attr 'none'` and rejecting blanket inline-style permission.

This is the preferred technical strategy for the pilot because it requires no Vaadin source patch, authorizes only four byte-exact static CSS texts, passes the complete isolation matrix, and passes same-origin injection into the real Petclinic viewer.
The dependency and CSP hashes remain version-coupled and must be regenerated and reviewed together.

## Exact current policy result

The fixture used the current controller policy exactly:

```text
default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'
```

Across 24 fresh-document cases for single and multi-reference controls, the current policy produced 60 violations and 60 matching console errors.
Every violation reported `effectiveDirective: style-src-elem`, `blockedURI: inline`, and the generated candidate module as its source.
No violation reported `style-src-attr`.

The cases covered connection, opening, closing, filtering, selecting, clearing, validation, disabled state, narrow presentation, dark presentation, disconnection, and reconnection.
They produced no page error, external request, or page-level overflow.

## Source trace

The first style is the 14,282-character `vaadin-base` block created by `addGlobalStyles()` in `@vaadin/component-base/src/css-utils.js` and called from the base style module.
The remaining three blocks are outer-scope styles created by `insertStyles()` in `@vaadin/component-base/src/slot-styles-mixin.js` for the single and multi-reference controls.
The retained creation stacks point to those two generated bundle paths.

Lit static component styles used Constructable StyleSheets in the tested browser and did not violate CSP.
The fixture counted adopted shadow-root sheets separately.
Vaadin overlay positioning, scroller height, and chip sizing performed CSSOM style mutations during open, close, filter, and multi-selection operations, but those trusted-script property mutations produced no `style-src-attr` violation even when the diagnostic policy explicitly set `style-src-attr 'none'`.

## Pinned style sources

The selective Vaadin 25.2.8 closure produced these four static sources:

```text
sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=
sha256-LGebpGBP4rWWgHT+HLo2ODJGtFNV4EbTdFjEntFbBEQ=
sha256-ziQO1YDNfjUz1uv42IGxQ5sgC85OPgAo+omSWhbRRdE=
sha256-/SVoMIwewnXJnEBdXJkzrloVkCW9YHHQ40uLtX2rU0g=
```

The proposed policy retains the existing fallback and narrows element and attribute handling explicitly:

```text
style-src 'self' 'sha256-…' 'sha256-…' 'sha256-…' 'sha256-…';
style-src-elem 'self' 'sha256-…' 'sha256-…' 'sha256-…' 'sha256-…';
style-src-attr 'none'
```

The generated build must supply and verify the full values rather than the abbreviated documentation form.

## Remedy comparison

| Strategy | Isolation result | Security and maintenance assessment |
|---|---:|---|
| Current `style-src 'self'` | 60 violations in 24 cases | Rejects required static Vaadin styles. |
| Exact SHA-256 element sources plus attribute `none` | 0 violations in 24 cases | Preferred; byte-exact, no runtime patch, deterministic, and coupled to the pinned bundle. |
| `style-src-elem 'unsafe-inline'` | 0 violations in 2 diagnostic cases | Rejected because it authorizes arbitrary inline style elements. |
| `style-src-attr 'unsafe-inline'` with strict elements | 5 violations in 2 diagnostic cases | Does not solve the problem because the violations are style elements. |
| Standard Lit nonce configuration | 5 violations in 2 diagnostic cases | Does not reach Vaadin's two direct `document.createElement('style')` helpers. |
| Instrumented nonce propagation to every created style | 0 violations in 2 diagnostic cases | Technically viable but would require an upstream change or maintained source patch and per-response nonce plumbing. |
| Externalized styles | Not selected | The base block can be externalized, but slotted styles may target a document or containing shadow root and require changing Vaadin insertion behavior. |
| Constructable replacement | Not selected | Lit already uses adopted sheets where supported; replacing Vaadin's outer-scope helpers requires a maintained source patch and root-specific sheet management. |

## Real Petclinic result

A disposable headless Playwright route modified only the fetched document response and added the four exact hashes to Petclinic's current policy.
Production source and server files were unchanged.
The selective candidate asset remained same-origin.

The journey instantiated both controls, opened and filtered both overlays, selected single and multiple values, closed overlays, validated required state, removed the probe, and checked the surrounding viewer.
It reported:

- Zero CSP violations.
- Zero console errors.
- Zero page errors.
- Zero external requests.
- No Flow runtime.
- No horizontal overflow.
- Route and menu readiness preserved.
- Probe removal completed.

The detailed result is retained in `results/petclinic-csp-check.json`.

## Security properties

CSP hashes authorize only style elements whose complete text matches a reviewed source.
They do not authorize arbitrary inline style elements, arbitrary style attributes, scripts, network origins, or candidate-generated CSS with different bytes.
An attacker who can inject markup cannot alter a hashed block and retain authorization.

The policy does increase the permitted style set from zero inline element texts to four known Vaadin texts.
That residual capability is explicit and reviewable.
The browser still treats CSSOM mutations by already authorized scripts as trusted script behavior, so script integrity and same-origin asset control remain essential.

Every dependency or build update must fail until the regenerated CSS texts, hashes, package closure, licenses, vulnerability status, CSP matrix, and Petclinic journey are reviewed together.
Unexpected additional style blocks must fail rather than being added automatically to the header.

## Browser evidence boundary

The retained automated matrix uses headless Google Chrome 151.0.7922.172, which is the browser available to the repository's current headless workflow on this machine.
The same matrix covers desktop, 390-pixel narrow, light, and dark states.
Any project decision to support additional browser engines for this viewer must run the identical CSP matrix before the pilot is promoted beyond sample scope.

## Gate outcome

The technical gate has a least-privilege passing strategy and does not require blanket inline-style permission.
The exact-hash policy and its version-coupled maintenance obligation were accepted for this pilot on 2026-08-21.
The pilot may proceed while retaining automated hash-drift, CSP, external-request, and rollback gates.
A later failure of those gates stops promotion and leaves the existing reference editor supported.
