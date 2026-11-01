# Candidate freeze and supply-chain baseline

## Evaluation date

Candidate metadata was captured from official documentation and the npm registry on 2026-08-20.
The analysis uses exact versions rather than floating tags.

## Primary candidates

| Strategy | Frozen package | License | Distribution | Direct runtime dependencies |
|---|---|---|---|---|
| Bootstrap CSS and utilities | `bootstrap@5.3.8` | MIT | Compiled CSS, JavaScript, Sass, npm tarball, CDN, and Maven WebJar | CSS has none; interactive dropdown, popover, and tooltip behavior requires Bootstrap JavaScript and Popper |
| Web Awesome adapters | `@awesome.me/webawesome@3.11.0` | MIT for the evaluated core npm package | Selective ES modules, bundled browser-ready `dist-cdn`, CSS themes, utilities, npm tarball | Lit, Lit SSR packages, Floating UI DOM, TinyColor, localization and animation helpers, QR helper, Marked, Nano ID, and adapter helpers |
| Open Props plus native primitives | `open-props@1.7.23` | MIT | Individual CSS prop packs, combined CSS, JSON design tokens, npm tarball, CDN | None reported by npm metadata |

## Integrity records

- `bootstrap@5.3.8` tarball: `https://registry.npmjs.org/bootstrap/-/bootstrap-5.3.8.tgz`.
- `bootstrap@5.3.8` integrity: `sha512-HP1SZDqaLDPwsNiqRqi5NcP0SSXciX2s9E+RyqJIIqGo+vJeN5AJVM98CXmW/Wux0nQ5L7jeWUdplCEf0Ee+tg==`.
- `@awesome.me/webawesome@3.11.0` tarball: `https://registry.npmjs.org/@awesome.me/webawesome/-/webawesome-3.11.0.tgz`.
- `@awesome.me/webawesome@3.11.0` integrity: `sha512-WM8kyP+AdAT3/zwNpc/YzN3Nu5pRD6ksREFZAct/DIn6KMnQs8lPVfiPbJrOpKMk8OaSnQX19iiwFGjoswXVXw==`.
- `open-props@1.7.23` tarball: `https://registry.npmjs.org/open-props/-/open-props-1.7.23.tgz`.
- `open-props@1.7.23` integrity: `sha512-+xyrJmxV9QUBFbSwPROYhOg/FLXry+uuPr4R+B5EaE4556Oc18/8ZC/fL5A+/lbRSwNvA0Alh+C0KpyFWLmLZg==`.

## Primary candidate notes

### Bootstrap 5.3.8

Bootstrap is actively maintained and already appears in the repository's Wicket dependency management and local Maven dependency graph as WebJar `org.webjars:bootstrap:5.3.8`.
The analysis uses compiled CSS variables and selected utility or component classes while preserving Causeway-owned disclosure, dialog, and domain behavior.
Bootstrap documentation states that dropdowns, popovers, and tooltips require Bootstrap JavaScript and Popper, while CSS-only styling does not.
A production proposal could use an explicit Maven WebJar dependency and would not need npm, but it must account for global selectors, reset behavior, class coupling, and toolkit-specific markup.

### Web Awesome 3.11.0

Web Awesome is the active successor to Shoelace and provides themes, utilities, and Web Components.
The evaluated npm package is MIT licensed and supports selective ES-module imports and a browser-ready `dist-cdn` distribution.
The official service also advertises optional Pro installation paths, but this analysis evaluates only the MIT core npm package and must not depend on commercial assets.
Web Awesome's shadow DOM requires documented CSS custom properties and `::part()` selectors rather than ordinary descendant selectors.
Its runtime dependency surface is materially larger than the other primary candidates and requires a Maven-integrated npm or vendored browser-distribution strategy.

### Open Props 1.7.23 with native Dialog and Popover

Open Props supplies design tokens rather than a component lifecycle or accessibility layer.
The candidate uses pinned local CSS files and Causeway-owned semantic markup.
Native `<dialog>` supplies modal behavior, while the Popover API supplies top-layer non-modal presentation, light dismissal, Escape handling, and `showPopover()`, `hidePopover()`, or declarative target control.
MDN reports Popover as Baseline 2025, so the unresolved Causeway browser floor is a hard adoption question rather than an assumed pass.
This strategy has the smallest third-party runtime dependency surface but leaves Causeway responsible for composition, semantic mapping, and unsupported-browser fallback.

## Fallback benchmark

`@spectrum-web-components/bundle@1.12.2` is Apache-2.0 licensed and exposes a broad Lit-based component catalogue.
The complete bundle has a very large direct package graph, and official guidance recommends selective component imports rather than the all-components bundle.
Spectrum remains a fallback benchmark only if the primary candidates fail to establish a credible component-toolkit comparison.

## Comparison-only and rejected candidates

### Material Web 2.5.0

`@material/web@2.5.0` is Apache-2.0 licensed and depends on Lit, `@lit/context`, and `tslib`.
Official project documentation currently describes Material Web as being in maintenance mode pending new maintainers.
It remains a comparison baseline and is not a preferred strategic dependency.

### Shoelace 2.20.1

`@shoelace-style/shoelace@2.20.1` is MIT licensed, but the official site states that Shoelace is sunset with no active development and directs new work to Web Awesome.
It is excluded as an independent candidate.

## Governance and maintenance gates

A candidate fails before scoring if any of these conditions applies:

- Its production distribution is not compatible with ASF dependency and notice policy.
- Required assets cannot be pinned, verified, and built without runtime CDN access.
- The evaluated feature requires commercial-only assets or an undisclosed service dependency.
- Security updates or project continuity have no credible maintenance path.
- The toolkit requires public Causeway domain contracts to depend on toolkit-specific semantics without a separately reviewed specification change.

## Sources

- Bootstrap documentation: `https://getbootstrap.com/docs/5.3/`.
- Bootstrap npm metadata: `https://www.npmjs.com/package/bootstrap/v/5.3.8`.
- Web Awesome documentation: `https://webawesome.com/docs/`.
- Web Awesome npm metadata: `https://www.npmjs.com/package/@awesome.me/webawesome/v/3.11.0`.
- Open Props documentation: `https://open-props.style/`.
- Open Props npm metadata: `https://www.npmjs.com/package/open-props/v/1.7.23`.
- Spectrum Web Components documentation: `https://opensource.adobe.com/spectrum-web-components/`.
- Material Web repository: `https://github.com/material-components/material-web`.
- Shoelace installation and project status: `https://shoelace.style/getting-started/installation`.
- Popover API baseline and behavior: `https://developer.mozilla.org/en-US/docs/Web/API/Popover_API`.
