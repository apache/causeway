# Current Causeway viewer inventory

## Scope

This inventory records the baseline used for toolkit comparison on 2026-08-20.
It covers `viewers/webcomponents/foundation`, `viewers/webcomponents/htmx`, and the HTMX Petclinic sample.

## Asset and build model

- The foundation contains 37 JavaScript modules and two CSS files under `viewers/webcomponents/foundation/src`.
- Maven copies the complete foundation `src` directory to `META-INF/resources/causeway-webcomponents` without filtering or bundling.
- The foundation Maven `test` phase runs Node's built-in test runner with single-test concurrency.
- The HTMX module adds a server-rendered shell stylesheet and one ES module while depending on the foundation Maven artifact.
- The HTMX shell loads foundation component styles, the theme, the HTMX shell stylesheet, HTMX 2.0.6 from a WebJar, and the Causeway HTMX ES module.
- The web-component reactor currently has no `package.json`, npm lockfile, Vite, Rollup, Webpack, or other frontend bundler configuration.
- The sample application adds five application-level Causeway variable references and one `60rem` responsive threshold.

## Semantic component vocabulary

The public element vocabulary contains these registered custom elements:

- `<causeway-graphql-client>`
- `<causeway-object-context>`
- `<causeway-object>`
- `<causeway-object-header>`
- `<causeway-property>`
- `<causeway-value>`
- `<causeway-object-link>`
- `<causeway-action>`
- `<causeway-interaction-controller>`
- `<causeway-collection>`
- `<causeway-collection-column>`
- `<causeway-menubars>`
- `<causeway-menubar-primary>`
- `<causeway-menubar-secondary>`
- `<causeway-menubar-tertiary>`

The component contract also names semantic prompt, result, editor, menu, loading, disabled, empty, error, and unsupported parts and publishes navigation, action, property, collection, layout, and menubar events.
Toolkit prototypes must not replace this public vocabulary with toolkit tags or events.

## Theme surface

`theme.css` defines or consumes 63 `--causeway-*` variables covering typography, spacing, surfaces, text, links, borders, radii, shadows, status colors, action colors, navigation, shell dimensions, object layout, and menu presentation.
`component-styles.css` consumes 22 structural variables, primarily for object groups, tabs, menus, menu panels, and action spacing.
The HTMX shell stylesheet consumes 26 shared variables for shell, route, result, footer, status, and responsive presentation.

The principal customization groups are:

- Typography: family, base size, small and large sizes, and line height.
- Spacing: `--causeway-space-1` through `--causeway-space-8`.
- Surfaces: page, base, raised, subtle, navigation, and menu panel.
- Color: text, muted, link, link hover, success, warning, danger, info, disabled, and focus.
- Shape: small, medium, large, and pill radii plus weak and medium shadows.
- Layout: content width, shell width, label width, control height, object gaps, and navigation height.
- Menus: bar colors, borders, controls, focus, panel colors, panel shadow, and z-index.

## Responsive and preference states

- `48rem` is the shared narrow threshold for foundation object layouts, menu bars, menus, and the HTMX shell.
- The Petclinic application adds a `60rem` breakpoint for its page-specific home presentation.
- The default theme has explicit light and `prefers-color-scheme: dark` values.
- The default theme and shell include `prefers-reduced-motion: reduce` handling.
- The default theme includes `forced-colors: active` handling.
- Component layout uses both media queries and a `48rem` object container query.

## Browser and acceptance baseline

No explicit minimum browser-version policy was found in the web-component documentation or specifications.
The executable browser contract currently uses Playwright for Java 1.61.0 with Chromium as the required opt-in acceptance browser.
The browser tests use desktop `1440 × 900` and narrow `390 × 844` viewports.
The tests cover custom and generic routes, menus, browser history, responsive overflow, prompts, validation, cancellation, scalar and collection results, property editing, object actions, route focus, and unexpected console, HTTP, GraphQL, and resource failures.
The vanilla sample integration test asserts that light, dark, reduced-motion, and forced-colors theme rules are packaged.

## Representative Petclinic states

The shared comparison fixture and real-viewer check must represent:

- Stable brand header, primary and secondary menus, route loading, result region, content region, and footer.
- Custom home and generic object pages.
- Normal, hover, active, focus, disabled, loading, ready, partial-error, validation-error, empty, and terminal-error states.
- Service action menu, action prompt, mandatory input error, select input, multiline input, cancellation, scalar result, collection result, object navigation, and void completion.
- Object title, actions, associated actions, tabs, property labels and values, editable properties, collections, compact tables, long labels, and constrained-width overflow.
- Desktop, narrow, light, dark, reduced-motion, and forced-colors presentation.

## Baseline constraints

- Public semantic elements and events are framework-neutral.
- Domain state continues to flow through Causeway contexts and GraphQL contracts.
- HTMX owns routing and fragment lifecycle rather than component data rendering.
- Production assets are Maven-packaged and usable without a runtime CDN.
- Application theming is expressed through documented Causeway variables and selectors.
- Existing specifications avoid Bootstrap or Wicket markup in the public semantic layer.
