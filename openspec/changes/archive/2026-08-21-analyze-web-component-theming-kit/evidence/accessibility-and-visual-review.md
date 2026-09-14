# Accessibility and visual review

## Evidence coverage

The retained screenshot set contains four deterministic views for each baseline or candidate:

- Desktop light with the application menu open.
- Desktop dark with the action prompt open.
- Narrow light with responsive navigation open.
- Narrow dark with the full object, collection, and status fixture.

The browser harness also exercised reduced motion and forced colors, checked structural labels and duplicate identifiers, captured console and network failures, and ran menu, prompt, tab, and focus journeys.
Eight Lighthouse 13.1.0 audits covered desktop and mobile modes for all four strategies using Chrome 151.

## Lighthouse results

| Strategy | Desktop accessibility | Mobile accessibility | Contrast failures |
|---|---:|---:|---:|
| Current Causeway baseline fixture | 96 | 96 | 9 desktop and 14 mobile nodes |
| Bootstrap CSS prototype | 96 | 96 | 6 desktop and 10 mobile nodes |
| Web Awesome prototype | 100 | 100 | 0 |
| Open Props and native prototype | 100 | 96 | 0 desktop and 27 mobile nodes |

All strategies scored 100 for Lighthouse best practices.
Every audit reported the fixture-only missing meta description as an SEO failure, which is common evidence-harness noise rather than a toolkit distinction.
The retained `lighthouse-summary.json` contains every failed selector, snippet, and explanation.

## Keyboard and focus results

All four strategies passed the bounded application-menu journey:

- The menu opened through its disclosure.
- Escape closed the menu.
- Focus returned to the disclosure.
- Selecting Create closed the menu and opened the prompt.
- Initial prompt focus reached the representative required input.
- Escape closed the prompt and returned focus to the application-menu disclosure.
- ArrowRight selected and focused the next tab.

The Web Awesome adapter used `wa-input`, `wa-dialog`, `wa-dropdown`, and `wa-tab-group` focus behavior behind Causeway hosts and produced the same observable journey.
The Open Props prototype used the native Popover top layer and native Dialog behavior without custom document-level dismissal code when Popover was supported.
No strategy produced hidden focus, duplicate IDs, unlabeled ordinary inputs, unlabeled buttons, unnamed dialogs, console errors, failed resources, or page-level horizontal overflow in the retained browser run.

## Forced colors and reduced motion

Chromium confirmed `prefers-reduced-motion: reduce`, `forced-colors: active`, and dark color preference in the narrow preference journey for every strategy.
All strategies retained visible content and responsive navigation with zero page-level overflow.
The result proves media-query and browser-mode reachability, but manual Windows High Contrast and assistive-technology testing remains necessary before production adoption.

## Visual facts

### Current Causeway baseline

- The baseline preserves the current compact Wicket-inspired information hierarchy and the strongest continuity with existing viewer screenshots.
- Desktop cards, properties, collections, and status states align consistently.
- The open menu and many ordinary buttons use a filled green treatment that gives primary and secondary controls insufficient differentiation.
- The fixture token mapping produced dark text on the green action background, which accounts for the recorded contrast failures.
- Narrow dark presentation is compact and readable, with internal table scrolling and no page overflow.

### Bootstrap

- Bootstrap provides immediately familiar buttons, cards, tables, spacing, and responsive density with very little candidate CSS.
- The desktop layout is the most compact candidate and preserves the existing light-DOM structure.
- Global Bootstrap typography and reset rules visibly alter code text, button fonts, borders, and radius in the real viewer even without adding Bootstrap classes everywhere.
- `btn-outline-secondary` is too low contrast on the dark header and in dark surfaces without explicit contextual overrides.
- Narrow dark layout is coherent, but disabled and secondary controls retain the recorded contrast limitations.

### Web Awesome

- Web Awesome provides the most consistent menu, tab, button, prompt, focus, disabled, and dark-mode component presentation in the fixture.
- Lighthouse found no accessibility failure in either audited mode.
- Spacing is more generous than the current viewer and increases narrow page length.
- Toolkit controls do not style existing Causeway controls automatically because they render in toolkit-owned shadow DOM.
- The full visual benefit therefore appears only where a Causeway renderer explicitly adopts a Web Awesome component.
- The browser-ready autoloader generated many small module requests, which is unacceptable as a production delivery strategy without selective bundling.

### Open Props and native primitives

- Open Props offers the smallest and least coupled token source and gives native menu and dialog lifecycle a clear implementation path.
- The light desktop prototype is coherent after a modest adapter stylesheet.
- Open Props normalize and button packs are not a complete application theme and produce strong global typography and control changes.
- Fluid heading defaults made the narrow page substantially taller and visually unbalanced.
- The provisional dark token mapping combined poorly with generic button-pack colors and produced the largest mobile contrast failure set.
- The candidate is viable only as a curated token source rather than as an unmodified normalize-and-buttons theme.

## Preference observations

The following are preferences rather than pass or fail findings:

- Bootstrap looks familiar and dense but more generic.
- Web Awesome looks polished and modern but less like the current Causeway viewer.
- The Causeway baseline has stronger product continuity but more handcrafted control styling.
- Open Props provides freedom rather than a finished visual identity.

## Accessibility conclusion

Web Awesome provides the strongest out-of-box component accessibility evidence but requires the largest adapter and delivery investment.
Bootstrap and Open Props can preserve Causeway behavior, but neither removes the need for Causeway-owned contextual contrast and focus testing.
The current baseline is already structurally sound, so wholesale component replacement is not justified solely by Lighthouse scores.
