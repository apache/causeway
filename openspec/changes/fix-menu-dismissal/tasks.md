## 1. Regression Coverage

- [ ] 1.1 Extend the foundation menubar component test to reproduce action-selection and Escape dismissal, including `aria-expanded`, panel `hidden`, action dispatch count, prevented Escape, and disclosure focus assertions.
- [ ] 1.2 Extend the Petclinic Playwright acceptance test to verify that `Pet Owners > Create` closes the menu before the prompt is used and that Escape closes an open menu without invoking an action.

## 2. Menu Dismissal Behavior

- [ ] 2.1 Update `CausewayMenubarElement` so enabled service-action selection closes the containing menu through the shared disclosure-state path while dispatching the semantic action request exactly once.
- [ ] 2.2 Ensure Escape gives an expanded menu priority, closes only that menu panel, restores focus to its disclosure, and leaves responsive bar state unchanged.
- [ ] 2.3 Confirm prompt, scalar result, collection result, and route-navigation focus hand-offs continue to override disclosure fallback focus where appropriate.

## 3. Verification

- [ ] 3.1 Run the foundation JavaScript test suite and confirm all menubar and interaction tests pass.
- [ ] 3.2 Run the HTMX Petclinic unit and opt-in Playwright acceptance tests and confirm menu dismissal introduces no browser, GraphQL, focus, or navigation failures.
