## Why

The secured HTMX and Vue Petclinic runtimes currently show **Sign out** in the tertiary Account menu but omit the authorized **Me** and **Configuration** actions that appear in their ordinary runtimes.
This makes the secured menu incomplete and hides useful framework capabilities rather than preserving the authoritative tertiary action set.

## What Changes

- Make the secured Petclinic users receive the framework roles and permissions needed for the **Me** and **Configuration** actions.
- Declare the complete intended Account action set in the shared menu layout and enable the same generic **Me** identity in secured profiles.
- Preserve **Me**, **Configuration**, and **Sign out** together in the authoritative tertiary menu for both HTMX and Vue.
- Keep authorization authoritative: viewers must not synthesize actions or expose actions that the current user is not permitted to invoke.
- Add integration and headless browser coverage comparing ordinary and secured tertiary menu semantics.
- Document the secured tertiary menu and its role-backed authorization.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `htmx-viewer-local-secman-authentication`: Require the secured HTMX Petclinic runtime to preserve authorized **Me** and **Configuration** actions alongside **Sign out** in the tertiary menu.
- `vue-viewer-local-secman-authentication`: Require the secured Vue Petclinic runtime to preserve authorized **Me** and **Configuration** actions alongside **Sign out** in the tertiary menu.

## Impact

The change affects the shared Petclinic menu layout, secured profiles and deterministic SecMan seed configuration, secured HTMX and Vue integration and browser tests, and sample documentation.
It does not change generic menu projection, action identity, GraphQL invocation, authentication endpoints, CSRF handling, or ordinary runtime authorization.
