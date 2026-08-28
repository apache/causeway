## Why

Causeway domain Web Components currently use a custom `member` attribute even though each host element is identifying the domain member it represents.
Using the standard HTML `id` attribute makes authored markup shorter, conventional, and directly addressable through ordinary DOM APIs and selectors.

## What Changes

- **BREAKING** Replace the public `member` attribute with `id` on `<cw-property>`, `<cw-action>`, `<cw-collection>`, and `<cw-collection-column>`.
- **BREAKING** Remove the custom `member` element property in favor of the native reflected `id` property, without a compatibility alias.
- Update generated layouts, declarative pages, selectors, tests, samples, and documentation to use member identifiers through `id`.
- Preserve domain-model and semantic-event uses of the term `member`, including internal descriptors, requirement payloads, and `data-causeway-associated-member`.
- Add source auditing that rejects the former Web Component member attribute and property API while allowing unrelated domain-member terminology.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Define `id` as the public member-identifier contract and update declarative member-associated action composition.

## Impact

The breaking public markup and DOM API change affects foundation component implementations, generated object layouts, HTMX custom pages, application-authored fragments, browser selectors, samples, acceptance tests, and Web Components documentation.
Applications must replace element attributes such as `<cw-property member="firstName">` with `<cw-property id="firstName">` and replace selectors or scripts that read `member` with native `id` usage.
No third-party dependencies change.
