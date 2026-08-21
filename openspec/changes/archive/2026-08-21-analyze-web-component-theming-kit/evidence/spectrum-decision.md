# Spectrum fallback decision

## Decision

A separate Spectrum Web Components prototype is not required for this analysis.
The three primary candidates produced working, comparable prototypes and already cover the intended strategy space of global CSS, toolkit-owned Web Components, and low-lock-in design tokens with native platform behavior.
Web Awesome is sufficient to expose the relevant Web Component integration questions around shadow DOM, slots, CSS parts, event translation, focus, selective loading, and runtime dependency cost.

## Evidence

- `@spectrum-web-components/bundle@1.12.2` is Apache-2.0 licensed and currently published.
- The bundle declares 74 direct Spectrum package dependencies.
- Official Spectrum guidance recommends selective component packages and explicitly discourages the full bundle for production.
- A fair Spectrum prototype would therefore require a separate selective-package design rather than adding one bounded comparison page.
- Web Awesome already provides the full-component comparison required by the specification and revealed a 132-request, approximately 615 KB encoded local fixture load when using its browser-ready autoloader distribution.
- The analysis can reach a toolkit-category decision without a second Lit and shadow-DOM prototype.

## Reconsideration trigger

Spectrum should be evaluated in a follow-up only if Web Awesome fails a hard gate while the decision still favors a Web Component toolkit over Bootstrap styling or Open Props with native primitives.
That follow-up must use selective Spectrum packages, `sp-theme`, and a pinned Maven-integrated frontend build rather than the all-components bundle.
