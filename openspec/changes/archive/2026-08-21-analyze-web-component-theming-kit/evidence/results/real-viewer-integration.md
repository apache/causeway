# Real Petclinic integration check

Generated: 2026-08-20T23:03:36.077Z

The check injects candidate assets only into a disposable browser context and does not modify production files.

## baseline

- Mode: unmodified real viewer.
- Browser failures: 0.
- Menu disclosure: true before Escape, false after Escape.
- Horizontal overflow before/after: 0/0 pixels.
- Body background before/after: rgb(243, 245, 248)/rgb(243, 245, 248).
- First button before/after: {"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"Inter, ui-sans-serif, system-ui, -apple-system, \"system-ui\", \"Segoe UI\", sans-serif"}/{"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"Inter, ui-sans-serif, system-ui, -apple-system, \"system-ui\", \"Segoe UI\", sans-serif"}.
- Internal analysis adapters: 0.
- Screenshot: screenshots/integration-baseline.jpg.

## bootstrap

- Mode: candidate CSS injected into the real viewer without source changes.
- Browser failures: 0.
- Menu disclosure: true before Escape, false after Escape.
- Horizontal overflow before/after: 0/0 pixels.
- Body background before/after: rgb(243, 245, 248)/rgb(243, 245, 248).
- First button before/after: {"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"Inter, ui-sans-serif, system-ui, -apple-system, \"system-ui\", \"Segoe UI\", sans-serif"}/{"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"0px","font":"system-ui, -apple-system, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", \"Liberation Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\""}.
- Internal analysis adapters: 0.
- Screenshot: screenshots/integration-bootstrap.jpg.

## webawesome

- Mode: single internal adapter plus toolkit theme loaded beside the real viewer.
- Browser failures: 0.
- Menu disclosure: true before Escape, false after Escape.
- Horizontal overflow before/after: 0/0 pixels.
- Body background before/after: rgb(243, 245, 248)/rgb(243, 245, 248).
- First button before/after: {"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"Inter, ui-sans-serif, system-ui, -apple-system, \"system-ui\", \"Segoe UI\", sans-serif"}/{"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"system-ui, sans-serif"}.
- Internal analysis adapters: 1.
- Screenshot: screenshots/integration-webawesome.jpg.

## openprops

- Mode: candidate CSS injected into the real viewer without source changes.
- Browser failures: 0.
- Menu disclosure: true before Escape, false after Escape.
- Horizontal overflow before/after: 0/0 pixels.
- Body background before/after: rgb(243, 245, 248)/rgb(243, 245, 248).
- First button before/after: {"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"4px","font":"Inter, ui-sans-serif, system-ui, -apple-system, \"system-ui\", \"Segoe UI\", sans-serif"}/{"color":"rgb(31, 41, 55)","background":"rgb(255, 255, 255)","borderRadius":"5px","font":"system-ui, -apple-system, \"Segoe UI\", Roboto, Ubuntu, Cantarell, \"Noto Sans\", sans-serif"}.
- Internal analysis adapters: 0.
- Screenshot: screenshots/integration-openprops.jpg.

## Interpretation

- Bootstrap and Open Props can affect the existing light DOM immediately through global CSS, which makes incremental styling easy but creates collision and regression risk.
- Web Awesome does not style existing Causeway controls automatically because its visual implementation lives behind toolkit-owned custom elements and shadow DOM.
- Web Awesome therefore requires explicit internal adapters or component renderer changes, while Bootstrap and Open Props can begin as token or stylesheet changes.
- All injected strategies preserved route readiness, menu readiness, Escape dismissal, and page-level overflow in this bounded check.

