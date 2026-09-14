# ADR: Pilot a constrained Bootstrap-derived Causeway theme

## Status

Analysis recommendation for separate implementation review.

## Context

Causeway's semantic components already provide correct domain state, interaction events, HTMX lifecycle, keyboard behavior, and a substantial application-facing theme contract.
The main opportunity is to reduce bespoke visual-system work without exposing a toolkit as the framework's public component API.
The evaluated candidates were Bootstrap 5.3, Web Awesome 3.11, and Open Props 1.7 with native Dialog and Popover, with the current Causeway implementation as baseline.

## Decision

Recommend a constrained Bootstrap-derived theme pilot rather than a full component-toolkit migration.

The pilot will:

- Retain public `<causeway-*>` elements, semantic events, GraphQL contexts, HTMX routing, and `--causeway-*` variables.
- Use selected Bootstrap Sass variables, functions, and mixins or equivalently scoped generated rules to implement Causeway-owned selectors.
- Keep Bootstrap classes and implementation details undocumented and non-contractual if any are used internally.
- Exclude Bootstrap JavaScript, Popper, dropdown, modal, collapse, and tab state machines.
- Package generated CSS through Maven with pinned source and reproducible checksums.
- Begin as an opt-in Petclinic and vanilla-sample theme pilot rather than changing the default theme immediately.
- Fix every audited contrast failure and meet the existing responsive, dark, reduced-motion, forced-colors, focus, overflow, and Playwright acceptance gates.

Native Dialog remains appropriate for modal prompts.
Native Popover should be evaluated independently once Causeway adopts an explicit browser floor or retains the current disclosure fallback.

## Rationale

Bootstrap produced the highest weighted score at 4.10 and the strongest pragmatic balance of visual coverage, maintenance, packaging, and current architectural compatibility.
It achieved a compact familiar viewer with only a small adapter stylesheet, and the repository already has Bootstrap 5.3.8 in its wider Maven ecosystem.
The score margin is small, so the decision is a pilot with measurable exit criteria rather than irreversible adoption.

Web Awesome produced the strongest Lighthouse and component polish evidence but requires explicit adapters, shadow-DOM-aware tests, a frontend bundle, and approximately 84.8 KB gzip for the twelve-component selective fixture bundle.
Its value may become stronger if Causeway later chooses to replace internal control renderers, but it is not the lowest-risk theming step.

Open Props provides excellent performance and contract compatibility, but it is a token source rather than a finished application design system.
The prototype required substantial Causeway CSS and produced the weakest narrow typography and dark control mapping before customization.

## Consequences

### Positive

- Causeway can reuse a mature design system without replacing its semantic architecture.
- The Wicket and web-component viewers can converge visually without sharing runtime behavior.
- Maven and WebJar compatibility remain available.
- The pilot can be removed by dropping generated CSS and restoring the current theme.
- Existing application customization continues through Causeway variables.

### Negative

- A selective Sass or CSS-generation step introduces frontend build maintenance.
- Bootstrap assumptions can leak through light-DOM classes or global selectors unless the build is carefully scoped.
- Causeway still owns semantic behavior, domain composition, and contextual accessibility testing.
- Applications may observe internal markup even when it is documented as non-contractual.
- The existing specification language rejecting Bootstrap markup may need clarification for internal generated theme implementation.

## Rejected alternatives

- Full Bootstrap CSS and JavaScript adoption was rejected because it creates global collisions and duplicate interaction state machines.
- Broad Web Awesome replacement was rejected because of adapter, bundle, dependency, and shadow DOM costs.
- Open Props as the sole theme system was rejected because it does not supply enough component-level visual decisions.
- Keeping the current theme unchanged was rejected as the only next step because the evidence supports a low-risk Bootstrap-derived pilot with clear rollback.

## Review trigger

The pilot must be reconsidered if selective Bootstrap styling cannot remain below the agreed CSS budget, cannot avoid public contract leakage, or cannot outperform the baseline in accessibility and maintainability evidence.
Web Awesome may be reconsidered for a narrow control family if a selective bundle meets budget and removes more custom behavior than its adapter introduces.
