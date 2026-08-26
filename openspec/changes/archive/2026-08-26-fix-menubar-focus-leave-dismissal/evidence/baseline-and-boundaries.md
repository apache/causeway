# Baseline and authority boundaries

## Reproduced defect

The screenshot shows the “Visits” menu panel still expanded while focus has moved to the “Helen Leary” route-content link.
A focused foundation regression reproduced the missing lifecycle behavior before implementation: 5 tests ran, 4 passed, and 1 failed because `aria-expanded` remained `true` after `focusout` identified an external next target.
The existing outside-click and Escape assertions continued to pass.

## Preserved boundaries

- Focus movement within the same semantic menubar must not close its active panel.
- Focus departure closes only expanded menu panels in the owning bar and does not collapse the responsive bar container.
- Focus remains on the newly selected external target; focus restoration remains specific to Escape and enabled action selection.
- Menu action visibility, usability, disabled reasons, invocation, parameters, validation, results, and service identity remain GraphQL-authoritative.
- No action request is emitted by focus-leave dismissal.
- Menu XML, menu contexts, action dispatch, routes, CSP, toolkit policy, public elements, and semantic events remain unchanged.
- Protected values remain absent from markup, events, errors, diagnostics, and evidence.
