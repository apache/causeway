# Packaging and policy evidence

## Deterministic closures

| Family | Raw bytes | Gzip bytes | Ceiling | SHA-256 | Exact style hashes |
|---|---:|---:|---:|---|---:|
| basic | 163,339 | 42,429 | 81,920 | `879c4162cc0957c59d3f76f7c8b15ad7d4c7aca4763242a0100b5f487e6b9398` | 6 |
| numeric | 96,131 | 25,677 | 66,560 | `c225135710434681739ea8fd6987130af692d1f2bb1be97a45cfd64a36e723ef` | 4 |
| local-temporal | 235,777 | 61,051 | 102,400 | `05866b47c4fbba2fd56a71c94046c31cc13cb78d23c27b7b66cbce0de645325f` | 6 |

The sum of independent cold gzip assets is 129,157 bytes against the accepted 204,800-byte aggregate ceiling.
The separately packaged reference closure remains byte-identical at SHA-256 `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81` and 48,684 gzip bytes.
The generated minified assets have a path-scoped Git whitespace-check exemption because significant template-literal character classes retain trailing horizontal whitespace before embedded newlines.

## Inputs and legal metadata

All ten direct Vaadin packages are pinned to 25.2.8 and esbuild is pinned to 0.27.4.
The production closure contains 30 packages under Apache-2.0, MIT, or BSD-3-Clause licenses.
Every production package has integrity, license, repository, and packaged legal-file metadata.
Vaadin usage statistics resolve to its opt-out module.
No Flow, Binder, Grid, Pro component, upload component, CDN module, or telemetry collector is present.

## CSP

The browser collector connects, focuses, changes, opens, closes, disables, and reconnects representative controls before hashing candidate-originated style elements.
The enforcing audit uses only the recorded per-family hashes, same-origin assets, and `style-src-attr 'none'`.
It reports zero CSP, axe, console, page, external-request, or narrow-overflow failures for all three families.
HTMX policy builds a deterministic deduplicated union for enabled reference and field families and emits no Vaadin hash when all policies are disabled.

## Security check

`npm audit --omit=dev` reports zero vulnerabilities.
Build and verify fail on checksum, entry-point, integrity, legal metadata, gzip, aggregate budget, CSP, or prohibited-runtime drift.
