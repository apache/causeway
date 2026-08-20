## 1. Regression Coverage

- [x] 1.1 Extend the foundation menubar component test to reproduce action-selection and Escape dismissal, including `aria-expanded`, panel `hidden`, action dispatch count, prevented Escape, and disclosure focus assertions.
- [x] 1.2 Extend the Petclinic Playwright acceptance test to verify that `Pet Owners > Create` closes the menu before the prompt is used and that Escape closes an open menu without invoking an action.

## 2. Menu Dismissal Behavior

- [x] 2.1 Update `CausewayMenubarElement` so enabled service-action selection closes the containing menu through the shared disclosure-state path while dispatching the semantic action request exactly once.
- [x] 2.2 Ensure Escape gives an expanded menu priority, closes only that menu panel, restores focus to its disclosure, and leaves responsive bar state unchanged.
- [x] 2.3 Confirm prompt, scalar result, collection result, and route-navigation focus hand-offs continue to override disclosure fallback focus where appropriate.

## 3. Verification

- [x] 3.1 Run the foundation JavaScript test suite and confirm all menubar and interaction tests pass.
- [x] 3.2 Run the HTMX Petclinic unit and opt-in Playwright acceptance tests and confirm menu dismissal introduces no browser, GraphQL, focus, or navigation failures.
