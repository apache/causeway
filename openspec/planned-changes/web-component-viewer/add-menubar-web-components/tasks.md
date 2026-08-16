## 1. Public Vocabulary and Coordination

- [ ] 1.1 Define contracts for `<causeway-menubars>` and primary, secondary, and tertiary bar elements.
- [ ] 1.2 Define shared application-entry state, generation, caching, invalidation, standalone-bar, and declarative-child behavior.
- [ ] 1.3 Register all elements and preserve pre-upgrade declarative bar children.

## 2. Menu Layout and Service Actions

- [ ] 2.1 Consume effective primary, secondary, tertiary, menu, section, and service-action data from rich GraphQL application entry points.
- [ ] 2.2 Preserve ordering, labels, descriptions, icons, supported hints, and partial-layout diagnostics.
- [ ] 2.3 Implement the service-action adapter over existing editor, prompt, choices, autocomplete, validation, invocation, cancellation, stale-response, and result primitives.
- [ ] 2.4 Omit hidden entries, present disabled entries accessibly, and collapse empty sections, menus, and bars.

## 3. Accessible Responsive Presentation

- [ ] 3.1 Implement labelled navigation landmarks and native disclosure and action controls.
- [ ] 3.2 Implement Tab, Shift+Tab, Enter, Space, Escape, Home, End, arrow navigation, sibling-menu closing, and focus restoration.
- [ ] 3.3 Implement responsive wide and narrow layouts without changing semantic order or events.
- [ ] 3.4 Add light-DOM role, bar, menu, section, action, state, CSS-variable, and styling hooks.

## 4. Events, Errors, and Host Policy

- [ ] 4.1 Publish standard semantic interaction states and scalar, object, collection, and void results.
- [ ] 4.2 Keep routing, navigation, home-page invocation, and result presentation under host policy.
- [ ] 4.3 Isolate malformed references, service-action errors, and stale responses without discarding unrelated menus.

## 5. Acceptance and Documentation

- [ ] 5.1 Add deterministic menu fixtures for all bars, sections, hidden and disabled actions, parameters, results, empty groups, localization, and malformed references.
- [ ] 5.2 Add vanilla-HTML examples for the composite and each standalone bar.
- [ ] 5.3 Add real-browser pointer, keyboard-only, focus, responsive, light/dark, action-interaction, and accessibility verification.
- [ ] 5.4 Document component names, nesting, independent use, GraphQL dependencies, styling, events, interaction behavior, and host-policy boundaries.
- [ ] 5.5 Run Node, Maven, browser, Lighthouse, formatting, and strict OpenSpec validation checks.
