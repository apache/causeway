## Why

SecMan delegated OAuth users are currently associated through one mutable provider claim such as `preferred_username`, email, or UPN.
Those values can be renamed, reassigned, or collide across providers, so future multi-provider authentication should identify an external account by the stable OIDC `(issuer, subject)` pair while retaining human-readable claims as mutable profile data.

## What Changes

- Extend the SecMan delegated-user model with an explicit external identity associated with provider issuer and subject.
- Enforce uniqueness of the normalized `(issuer, subject)` pair independently of mutable username and email values.
- Resolve an OAuth/OIDC login by stable external identity before consulting configurable legacy claim-based fallback behavior.
- Continue to use one selected claim such as `preferred_username` during an explicit compatibility and migration period.
- Define how an existing delegated `ApplicationUser` is linked to a first stable identity without silently merging ambiguous accounts.
- Define account-linking policy when one application user authenticates through multiple providers or multiple identities from one provider.
- Update mutable display, username, and email claims without replacing the stable external identity.
- Provide persistence migration, conflict reporting, audit evidence, administrative repair, import/export, and rollback guidance.
- Add provider-neutral tests plus representative Azure/Entra ID and Google OIDC claim fixtures.

## Capabilities

### New Capabilities

- `secman-delegated-user-identity-mapping`: Defines stable provider identity mapping, migration, linking, and conflict behavior for delegated SecMan users.

### Modified Capabilities

None identified until the current SecMan persistence and delegated-user specifications are reviewed during promotion.

## Impact

This future change likely affects the SecMan domain and persistence schema, delegated Spring OAuth integration, user auto-creation and lookup, administration, migrations, security DTOs, tests, and documentation.
It is intentionally deferred while the initial HTMX authentication work uses one configured claim value.
The draft requires current SecMan model analysis, migration design, and complete OpenSpec artifacts before promotion.
