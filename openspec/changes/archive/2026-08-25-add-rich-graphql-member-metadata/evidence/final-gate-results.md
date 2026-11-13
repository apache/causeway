# Final gate results

## Passed gates

- Core metamodel reactor and full metamodel tests: `BUILD SUCCESS`.
- Full GraphQL model reactor and tests: `BUILD SUCCESS`.
- Focused metadata unit tests: five tests passed with zero failures.
- Metadata integration tests: property, collection, action, parameter, introspection, absence, localization, authorization-negative, and resource compatibility coverage passed.
- Broad GraphQL integration suite excluding two documented unrelated baseline failures: `BUILD SUCCESS`.
- Collection window, resource policy, value/resource semantics, application entry, department, staff, and metadata compatibility subset: `BUILD SUCCESS`.
- Reference Application full reactor and integration suite: `BUILD SUCCESS`.
- Reference Application `REF-METADATA-01` and `REF-METADATA-02` executable probe: passed.
- Reference Application inventory SHA-256 remained `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a` byte-for-byte.
- Core metamodel, GraphQL model, GraphQL test, and Reference Application RAT checks: `BUILD SUCCESS` with no unapproved resources.
- Strict OpenSpec validation and Git whitespace checks: passed.

## Schema and startup gates

- Exactly one shared GraphQL metadata object type was added.
- Representative object type count changed from 782 to 783.
- Generated SDL changed from 284,200 to 307,984 bytes, an 8.37-percent increase beneath the ten-percent ceiling.
- A rejected direct-scalar prototype would have increased SDL by 40.4 percent and was not retained.
- Warm end-to-end schema-test median changed from 6,742 ms to 6,809 ms, a 0.99-percent increase beneath the five-percent ceiling.
- Accepted generated schema SHA-256 is `fae5f09107277d5cc1276f14f948d1a1eeb97be2f8c32445214df0be20e7f2ef`.

## Existing unrelated baseline observations

The unrestricted GraphQL integration command still reports two pre-existing failures unrelated to this capability.
One calculator approval expects a trailing `.000` that the current JDK offset formatter omits.
The editable-memento approval reports an existing input-conversion error.
Both failures reproduce without selecting member metadata, while every other integration test passes in the broad combined run.

## Stable boundaries

No existing operation argument, generated name, input type, resource field, route, persisted value, web-component contract, or dynamic interaction behavior changed.
Grid and menu resources retain structural authority.
Validation and GraphQL input nullability retain behavioral and requiredness authority.
