# Evaluation plan

## Principle

Every candidate and the current Causeway baseline will render the same state catalogue and will be assessed with the same procedures.
Hard gates apply before weighted scores.
Evidence is retained under this change so the analysis moves with its archive.

## Fixture catalogue

The analysis harness will expose these stable state identifiers:

| State | Required content |
|---|---|
| `shell` | Brand, primary and secondary navigation, main region, result region, and footer |
| `menu-open` | Expanded application menu, enabled actions, disabled action with reason, long label, and keyboard focus |
| `prompt` | Modal action prompt, text input, select input, multiline input, validation message, submit, and cancel |
| `object` | Object title, object actions, associated actions, tabs, property labels and values, editable controls, and status metadata |
| `collection` | Compact table, object links, numeric and date values, empty alternative, and constrained overflow |
| `status` | Loading, ready, partial-error, validation-error, empty, unsupported, and terminal-error presentations |
| `responsive-nav` | Collapsed and expanded narrow navigation with nested application menu |

Each state will include deliberately difficult content such as long labels, long unbroken values, disabled controls, validation failures, empty data, partial errors, and enough table columns to test overflow.

## Viewports and preferences

- Desktop viewport: `1440 × 900` CSS pixels.
- Narrow viewport: `390 × 844` CSS pixels.
- Required color modes: explicit light and explicit dark.
- Required motion modes: normal and `prefers-reduced-motion: reduce`.
- Required contrast mode: `forced-colors: active` where Chromium emulation or host support permits it.
- Required scale check: 200% browser zoom or an equivalent `2×` text and layout check for the leading strategies.

The full Cartesian product is not required for every state.
At minimum, shell, menu, prompt, object, collection, and status evidence is captured in desktop light, desktop dark, narrow light, and narrow dark, with separate reduced-motion and forced-colors journeys for interactive states.

## Browser matrix

- Required executable browser: the Chromium version supplied or selected by Playwright for Java 1.61.0.
- Required secondary compatibility review: current Firefox and WebKit support statements for every adopted API and package.
- Optional executable browsers: Firefox and WebKit when the local Playwright installation already provides them.
- Unresolved policy gate: Causeway must define a minimum browser floor before relying on Baseline 2025 APIs such as Popover without fallback.

A candidate cannot receive a full browser-compatibility score solely from Chromium execution.

## Keyboard and focus journeys

1. Tab to the primary menu disclosure, open it with Enter or Space, move through items, invoke one action, and verify dismissal and resulting focus.
2. Open a menu and press Escape, then verify disclosure state, panel visibility, and restored focus.
3. Open the responsive navigation, open a nested menu, dismiss it, and verify that no focused element becomes hidden.
4. Open the prompt, verify initial focus, traverse every control, trigger validation, verify invalid-field focus, cancel with button and Escape, and verify invoking-control focus.
5. Traverse tabs with keyboard controls and verify selected state, controlled panel visibility, and focus order.
6. Traverse editable properties, disabled controls with explanations, collection links, error actions, and result presentation.
7. Repeat the menu and prompt journeys in narrow layout and forced-colors mode.

## Automated checks

- Chrome Lighthouse accessibility audit for every candidate's desktop shell and narrow interactive state.
- DOM assertions for accessible names, `aria-expanded`, controlled-panel visibility, dialog modality, selected tabs, validation relationships, hidden focus, and page overflow.
- Console and network failure capture.
- Screenshot capture with fixed viewport, state, color mode, and candidate naming.
- Asset inventory and compressed-size calculation.
- Navigation and rendering timing capture after a warm-up load and at least five measured reloads.

## Performance metrics

- Raw and gzip-compressed CSS bytes.
- Raw and gzip-compressed JavaScript bytes.
- Number of candidate-owned CSS, JavaScript, font, icon, and image requests.
- Total candidate-owned transfer bytes for a cold local load.
- DOM content loaded, load, first contentful paint, and largest contentful paint where available.
- Custom-element definition and fixture-ready timing for Web Component candidates.
- Median of at least five measured runs after one warm-up run.
- Separate full-distribution and selective-import measurements when both are realistic production options.

## Weighted score

| Category | Weight | Evidence |
|---|---:|---|
| Accessibility and interaction correctness | 25% | Lighthouse, DOM assertions, manual keyboard and focus journeys |
| Semantic and architectural compatibility | 20% | Adapter map, contract leakage, HTMX lifecycle, styling boundary |
| Maintenance and governance | 15% | License, release status, dependencies, security and update path |
| Packaging and build integration | 15% | Offline Maven path, lockfile, generated assets, release implications |
| Visual and theming capability | 15% | Comparable screenshots, preferences, branding, override surface |
| Performance | 10% | Compressed size, requests, startup and rendering measurements |

Every category is scored from 0 to 5 and multiplied by its weight.
A score must link to evidence and cannot compensate for a failed hard gate.

## Hard gates

- ASF-compatible license, provenance, notices, and dependency governance.
- Pinned production assets with no runtime CDN requirement.
- No commercial-only feature required for the recommended scope.
- No disclosure of sensitive application or domain data.
- Preservation of Causeway domain semantics and a credible adapter boundary.
- Operable keyboard and focus behavior with no critical automated accessibility failure.
- Credible browser support or documented progressive fallback for required APIs.

## Visual review rubric

Reviewers record facts separately from preferences.
Facts include clipping, overflow, contrast failure, hidden focus, unstyled states, collision, missing responsive adaptation, and unsupported controls.
Preferences include density, visual warmth, brand character, radius, typography, and familiarity.
The final recommendation may cite preferences only after factual failures and architectural costs are accounted for.

## Reproduction output

A complete evidence run produces:

- Candidate metadata and integrity records.
- Baseline and candidate screenshots.
- Lighthouse reports.
- Keyboard and focus results.
- DOM assertion results.
- Console and network failure results.
- Asset and performance measurements.
- Adapter and packaging assessments.
- Weighted matrix and hard-gate decision.
- Architectural decision record and migration outline.
