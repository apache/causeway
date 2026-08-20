## 1. Menu disclosure lifecycle

- [ ] 1.1 Close an expanded menu after enabled service-action selection while preserving the semantic action request.
- [ ] 1.2 Close expanded menus after outside pointer activation and remove document listeners when a bar disconnects.
- [ ] 1.3 Restore prompt and result focus to the visible menu disclosure after the selected action closes its panel.
- [ ] 1.4 Add Node coverage for selection, outside dismissal, Escape, sibling coordination, and focus restoration.

## 2. Property explanations and multiline editing

- [ ] 2.1 Preserve property descriptions as the default title and accessible description.
- [ ] 2.2 Replace visible disabled-reason paragraphs with a separate focusable tooltip indicator and hidden accessible reason.
- [ ] 2.3 Add bounded effective-grid `multiLine` parsing and propagate valid row counts to generated property components.
- [ ] 2.4 Add a multiline string textarea editor with the same parsing, validation, and focus behavior as the standard text editor.
- [ ] 2.5 Add Node coverage for description and disabled-reason semantics, valid and invalid multiline hints, textarea selection, and value parsing.

## 3. Associated action composition and Petclinic

- [ ] 3.1 Preserve nested effective-grid action nodes beneath their owning property or collection while retaining deterministic allocation.
- [ ] 3.2 Render top-level and associated action groups with responsive wrapping and consistent synchronized CSS gaps.
- [ ] 3.3 Mark Petclinic notes as multiline and nest update-name, pet, and visit actions beneath their associated members in the effective layout.
- [ ] 3.4 Extend Petclinic integration and headless Playwright acceptance for menu closure, tooltips, action placement, spacing, and multiline notes editing.

## 4. Validation and archive readiness

- [ ] 4.1 Run foundation and HTMX Node tests, ordinary web-components Maven verification, and headless Petclinic Playwright acceptance.
- [ ] 4.2 Run GraphQL regression tests, module syntax, AsciiDoc, formatting, source-approval, top-level reactor discovery, and strict OpenSpec validation.
