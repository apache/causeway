# Vaadin reference control CSP matrix

Generated: 2026-08-21T19:59:22.625Z

Browser: 151.0.7922.172

## Discovered static style hashes

- `vaadin-base` in `head`: `sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=` (14282 characters).
- `(no id)` in `head`: `sha256-LGebpGBP4rWWgHT+HLo2ODJGtFNV4EbTdFjEntFbBEQ=` (952 characters).
- `(no id)` in `head`: `sha256-ziQO1YDNfjUz1uv42IGxQ5sgC85OPgAo+omSWhbRRdE=` (991 characters).
- `(no id)` in `head`: `sha256-/SVoMIwewnXJnEBdXJkzrloVkCW9YHHQ40uLtX2rU0g=` (165 characters).

## Policy summary

| Policy | Cases | Violations | Console errors | Page errors | External requests | Overflow cases |
|---|---:|---:|---:|---:|---:|---:|
| exact | 24 | 60 | 60 | 0 | 0 | 0 |
| hashes | 24 | 0 | 0 | 0 | 0 | 0 |
| attribute-inline | 2 | 5 | 5 | 0 | 0 | 0 |
| element-inline | 2 | 0 | 0 | 0 | 0 | 0 |
| nonce | 2 | 5 | 5 | 0 | 0 | 0 |
| nonce-patched | 2 | 0 | 0 | 0 | 0 | 0 |

The exact policy is the current viewer policy.
The hash policy adds only the recorded SHA-256 sources for Vaadin-created global style elements and explicitly denies style attributes.
The element-inline, attribute-inline, and unmodified nonce variants are diagnostic comparisons and are not adoption recommendations.
The nonce-patched variant demonstrates that nonce propagation would work only after both Vaadin style-creation paths are changed to apply the application nonce.

