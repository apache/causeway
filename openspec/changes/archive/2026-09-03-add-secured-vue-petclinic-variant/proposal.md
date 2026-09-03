## Why

The HTMX Petclinic reference application has a SecMan-backed secured variant, but the corresponding Vue application currently demonstrates only bypass authentication.
A secured Vue variant is needed to prove that the host-owned action policy, CSRF-protected GraphQL traffic, session lifecycle, deep-link restoration, and native logout contract work for an application-owned Vue shell.

## What Changes

- Add an optional viewer-neutral SecMan-to-Spring authentication bridge that can be reused by secured web-component hosts without importing HTMX or Vue presentation behavior.
- Refactor the existing HTMX SecMan integration to consume the shared bridge without changing its routes or security behavior.
- Add an optional Vue SecMan integration with scoped form login, safe Vue-route restoration, CSRF metadata, session-expiry handling, and POST logout.
- Add a deterministic secured Vue Petclinic application variant and headless authentication coverage corresponding to the existing secured HTMX journey.
- Add dedicated `run-secured.sh` scripts for the HTMX and Vue Petclinic samples while preserving their existing unsecured `run.sh` scripts.
- Update HTMX, Vue, shared-security, sample, and top-level web-component documentation with secured run commands, credentials, ownership boundaries, and verification guidance.

## Capabilities

### New Capabilities

- `web-component-secman-spring-authentication-bridge`: Provides an optional presentation-neutral SecMan-backed Spring username/password authentication bridge for web-component hosts.
- `vue-viewer-local-secman-authentication`: Provides optional local SecMan form authentication, CSRF-safe Vue GraphQL traffic, host-owned logout, and secured Vue Petclinic acceptance.

### Modified Capabilities

- `htmx-viewer-local-secman-authentication`: Reuses the shared authentication bridge and exposes the secured sample through a dedicated run script without changing authenticated behavior.
- `generic-vue-web-component-viewer`: Documents and verifies the host integration points used by a secured Vue shell while keeping generic Vue authentication-neutral.

## Impact

The change affects the web-components reactor, a new shared security integration module, the HTMX SecMan integration, a new Vue SecMan integration, both Petclinic sample modules, Vue frontend assets, Maven run and test profiles, headless Playwright coverage, shell scripts, and documentation.
It adds no authentication behavior to generic foundation, HTMX, or Vue modules unless an application explicitly imports the corresponding integration.
