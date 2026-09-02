## 1. Public Outlet Contract

- [x] 1.1 Add bounded case-insensitive `presentation-style` attribute normalization and reflected `presentationStyle` property for `INLINE`, `DIALOG`, and `SIDEBAR`.
- [x] 1.2 Add a host-supplied presentation context API for connected originating-focus targets without changing result ownership.
- [x] 1.3 Extend component contracts, exports, usage documentation, and CSS variables for styled result surfaces.

## 2. Surface Lifecycle

- [x] 2.1 Preserve current direct-child region, compact controls, sticky-header-aware reveal, replacement, and clear behavior for `INLINE`.
- [x] 2.2 Implement a bounded labelled modal dialog surface with backdrop, initial focus, Tab containment, Escape dismissal, and focus restoration.
- [x] 2.3 Implement a labelled non-modal inline-end sidebar with ordinary Tab navigation, focused Escape dismissal, bounded scrolling, and focus restoration.
- [x] 2.4 Make replacement, clear, disconnection, style changes, and stale generations retire surface structure, focus handling, backdrop, and overflow effects deterministically.
- [x] 2.5 Add narrow-viewport, forced-colors, and reduced-motion styling without horizontal document overflow.

## 3. HTMX Host Integration

- [x] 3.1 Supply the resolved outlet with the originating object-action or service-action focus target before mounting successful non-navigating results.
- [x] 3.2 Route explicit and Escape dismissal through the existing host-owned result lifecycle for all presentation styles.
- [x] 3.3 Restrict automatic viewport reveal to inline results while preserving outlet snapshotting, shell fallback, application claims, object navigation, void refresh, and announcements.

## 4. Foundation Verification

- [x] 4.1 Add unit tests for attribute normalization, property reflection, inline compatibility, node identity, replacement, clear, style changes, and disconnection.
- [x] 4.2 Add DOM tests for dialog semantics, focus entry and containment, Escape, explicit dismissal, and connected-origin focus restoration.
- [x] 4.3 Add DOM tests for non-modal sidebar semantics, ordinary Tab behavior, focused Escape, responsive bounds, reduced motion, and cleanup.

## 5. Petclinic Qualification

- [x] 5.1 Add deterministic Petclinic outlet declarations and journeys demonstrating inline, dialog, and sidebar collection results without changing invocation projection.
- [x] 5.2 Verify object-action and service-action origins, replacement, dismissal, focus restoration, stale/disconnected fallback, object navigation, and void refresh under Vaadin and native toolkit policies.
- [x] 5.3 Run Foundation Node and Maven suites, HTMX and Petclinic integration tests, browser accessibility/console/network/focus/overflow audits, strict OpenSpec validation, and `git diff --check` with Java 21 where required.
