# Causeway and Vaadin theme assessment

## Stable customization boundary

The prototype keeps Causeway variables at the document boundary and maps them to Vaadin and Lumo properties:

| Causeway variable | Candidate mapping |
|---|---|
| `--causeway-color-primary` | `--lumo-primary-color` and `--lumo-primary-text-color` |
| `--causeway-color-error` | `--lumo-error-color` |
| `--causeway-color-surface` | `--lumo-base-color` and `--vaadin-input-field-background` |
| `--causeway-color-text` | `--lumo-body-text-color` |
| `--causeway-color-muted` | `--lumo-secondary-text-color` |
| `--causeway-radius` | `--lumo-border-radius-m` |
| `--causeway-focus` | `--vaadin-focus-ring-color` |

Applications therefore need not import or address Vaadin tokens for standard semantic Causeway pages.
A production adapter may require additional Causeway variables for control density, overlay elevation, Grid row height, chip size, and validation emphasis.

## Light, dark, motion, and forced colors

The analysis uses the browser color preference to set the Vaadin dark theme and maps dark Causeway surface, text, border, muted, error, and focus colors.
The retained dark desktop and narrow screenshots show coherent component surfaces without a global reset affecting the surrounding Causeway layout.

Reduced-motion and forced-colors rules remain Causeway-owned at page level.
Vaadin internal motion and forced-color behavior appeared operable in headless journeys, but production review must test component-specific animations, overlay borders, Grid focus, and selected chips with assistive technology.

## Shadow DOM and parts

Vaadin's shadow DOM prevents the global font, radius, and native-control collisions observed with complete Bootstrap and Open Props stylesheets.
It also means ordinary descendant selectors cannot repair every internal state.
The supported customization strategy must use documented custom properties, theme variants, and parts rather than private shadow selectors.

No evaluated fixture state required a private shadow selector.
Grid column prioritization, semantic grouping, shell layout, and custom page composition remain outside the widget theme and stay Causeway-owned.

## Global side effects

The candidate initializes the global `window.Vaadin` namespace and includes development-mode and usage-statistics detection code.
The minified headless fixture selected production behavior and made zero external requests, but the code and endpoint strings remained in the bundle.
A production build must prove that telemetry remains inert or exclude the module explicitly.

The candidate does not apply a document-wide CSS reset, which is a material advantage for custom HTML pages.
Raw allowlisted widgets nevertheless inherit version-coupled Vaadin theme behavior and cannot promise the same compatibility horizon as `--causeway-*`.

## Strict CSP

Vaadin control behavior attempted dynamic inline styles under the real viewer's strict `style-src 'self'` policy.
Externalizing the analysis panel's own CSS removed one class of error but four component-originated failures remained.
Theme adoption is therefore conditional on a security-reviewed style strategy and cannot be described as compatible with the current CSP today.

## Assessment

Vaadin is themeable enough for an internal Causeway adapter and visually coherent enough to replace several handcrafted widget families.
The recommended public contract remains Causeway variables and semantic elements.
Raw Vaadin widgets, if made available, must be documented as a version-coupled lower-level facility with their own theme and CSP responsibilities.
