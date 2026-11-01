# Real Petclinic Vaadin integration check

Generated: 2026-08-21T08:55:56.690Z

The check injects selective Vaadin assets only into a disposable headless browser context through a same-origin Playwright route.
Production source files and server resources are unchanged.

## Assertions

- PASS: routeRemainedReady
- PASS: menuRemainedReady
- PASS: menuDismissal
- PASS: oneInjectedAdapter
- PASS: vaadinDefined
- PASS: noFlowRuntime
- PASS: noNewOverflow
- PASS: noUnexpectedBrowserFailures
- PASS: cspFailureClassified
- PASS: noExternalRequests

## Observations

- Route state before/after: ready/ready.
- Menu state before/after: ready/ready.
- Menu disclosure before Escape/after Escape: true/false.
- Horizontal overflow before/after: 0/0 pixels.
- Browser failures: 4, of which 4 are classified strict-style-CSP incompatibilities.
- External requests: 0.
- Flow runtime detected: false.
- Strict style CSP compatible: false.
- Screenshot: integration-vaadin.jpg.

The probe demonstrates same-origin module delivery and coexistence, but Vaadin component style insertion conflicts with the viewer current style-src self policy.
A production proposal must resolve that policy deliberately rather than silently enabling unsafe inline style.

