## Context

The ordinary Petclinic runtimes use bypass security, so authorized framework tertiary actions such as **Me** and **Configuration** are visible alongside application menus.
The secured runtimes use deterministic SecMan users whose sample role currently grants only the Petclinic namespace.
The menu projection correctly preserves authoritative authorization and therefore cannot display framework actions that SecMan omits, even though exact Logout is separately available and labeled **Sign out** by host policy.

The HTMX and Vue secured variants maintain parallel seed configurations and use the same shared menu layout and semantic components.

## Goals / Non-Goals

**Goals:**

- Give the deterministic secured user explicit role-backed authority for **Me** and **Configuration**.
- Preserve **Me**, **Configuration**, and **Sign out** in the effective tertiary menu for both HTMX and Vue.
- Verify exact action identities and tertiary placement in integration and headless browser tests.
- Keep HTMX and Vue seed behavior aligned.

**Non-Goals:**

- Do not bypass SecMan authorization in menu projection.
- Do not synthesize missing actions in either viewer.
- Do not grant broad SecMan administration capabilities.
- Do not change generic menu, logout, routing, CSRF, or GraphQL behavior.

## Decisions

### Assign established framework roles to the deterministic user

Both secured seed configurations will add the existing SecMan regular-user role and configuration-access role to `sven`, in addition to the Petclinic application role.
The regular-user role is the framework-owned authority for `MeService`, and the configuration role is the framework-owned authority for `ConfigurationMenu`.
This reuses established permission boundaries instead of duplicating feature strings or granting an entire framework namespace through the sample role.

Adding direct broad permissions to the Petclinic role was rejected because it would couple the sample's application role to framework internals and could accidentally expand authority as namespaces evolve.
Changing the viewer to retain unauthorized menu entries was rejected because authorization remains authoritative and hidden metadata must not be exposed.

### Keep menu placement metadata authoritative

The viewers will not insert or move **Me** and **Configuration**.
Once SecMan authorizes them, the existing application-menu resource and service layout metadata determine their tertiary placement, while host policy continues to relabel only exact Logout as **Sign out**.

### Assert semantic parity rather than markup identity

Acceptance tests will inspect projected action identities and roles for both native and Vaadin-backed menu behavior where applicable.
Tests will assert the presence of exact **Me**, **Configuration**, and Logout identities without depending on renderer-specific DOM structure or item ordering beyond authoritative menu projection.

## Risks / Trade-offs

- [Risk] Framework seed roles may not exist before the sample runner assigns them. → Resolve role names through the seeded repository and make integration tests fail clearly if startup ordering regresses.
- [Risk] Configuration access exposes runtime properties intended only for selected users. → Assign the dedicated configuration role only to the deterministic acceptance user, not globally.
- [Risk] HTMX and Vue seed implementations can drift. → Apply equivalent changes and equivalent assertions to both modules.

## Migration Plan

No data migration is required because both secured samples use deterministic in-memory data.
Existing deployments remain unaffected unless they copy the sample seed configuration, in which case they can opt into the same standard role assignments.
Rollback removes the two framework role assignments and the corresponding assertions.

## Open Questions

None.
