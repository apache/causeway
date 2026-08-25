# Adoption baseline

## Qualified closures

| Closure | Raw bytes | Gzip bytes | SHA-256 | Exact style hashes |
|---|---:|---:|---|---:|
| Reference | 191,342 | 48,684 | `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81` | 4 |
| Basic fields | 163,339 | 42,429 | `879c4162cc0957c59d3f76f7c8b15ad7d4c7aca4763242a0100b5f487e6b9398` | 6 |
| Numeric fields | 96,131 | 25,677 | `c225135710434681739ea8fd6987130af692d1f2bb1be97a45cfd64a36e723ef` | 4 |
| Local temporal fields | 235,777 | 61,051 | `05866b47c4fbba2fd56a71c94046c31cc13cb78d23c27b7b66cbce0de645325f` | 6 |

The independent field-family cold gzip sum is 129,157 bytes against the accepted 204,800-byte ceiling.
The closures use pinned same-origin Maven-packaged free-core inputs with accepted Apache-2.0, MIT, or BSD-3-Clause production licenses and zero npm production vulnerabilities.
They contain no Flow, Binder, Grid, Pro component, upload component, server-side Vaadin state, telemetry collector, or CDN input.

## Previous selection policy

Foundation reference configuration defaulted disabled and field-family configuration defaulted empty.
Petclinic and the Reference Application explicitly set `vaadin-reference-widgets=true` and `vaadin-field-families=basic,numeric,local-temporal`.
Explicit native browser runs set the reference boolean false and the field-family value empty.
The HTMX shell emitted adapter attributes and exact hashes only for those independently enabled pilot properties.

## Regression baseline

The foundation Node suite passed 161 tests before this policy change and the HTMX route-policy Node suite passed 5 tests.
Petclinic passed 4 browser tests in candidate and explicit-native modes.
The Reference Application passed 9 browser tests in candidate and explicit-native modes.
The reviewed Reference Application inventory contains 4,286 items and has SHA-256 `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a`.
Strict-CSP, axe, keyboard, focus, responsive, theme, reduced-motion, forced-colors, console, page-error, external-request, overflow, RAT, and deterministic packaging gates were clean.
