# Implementation report

## Shared GraphQL shape

Every generated rich property, collection, action, and action-parameter wrapper now advertises `metadata: RichMemberMetadata!`.
`RichMemberMetadata` is built and registered once and contains `friendlyName`, `description`, `maxLength`, `pattern`, `patternFlags`, `multiLine`, and `typicalLength`.
Property and action-parameter wrappers populate the constraint fields.
Collection and action wrappers return null for constraints that do not apply.

A rejected direct-scalar prototype preserved type count but increased representative SDL by 40.4 percent.
The accepted shared shape increases SDL by 8.37 percent and adds exactly one object type.

## Request-time semantics

One wrapper resolver returns a request-local ordered map.
`friendlyName` calls `getCanonicalFriendlyName()` and is non-null.
`description` calls `getCanonicalDescription()` and remains null when absent.
Neither field invokes imperative object-dependent naming or description methods.
Tests change the active locale between equivalent resolver calls and prove no result is cached globally.

New public `Facets` utility methods expose finite explicit maximum length, explicit multiline count, Java regular-expression text and flags, and positive effective feature typical length without exporting internal facet packages.
Unlimited, fallback-only, blank, non-positive, or absent values normalize to null.
GraphQL input nullability remains the only structural requiredness contract, and validation operations remain authoritative.

## Resource and structural compatibility

The former package-local resource scalar helper is generalized and retains every existing resource metadata field and location.
Blob and Clob property reads continue to expose `fileAccept` beneath `get`.
Resource action parameters continue to expose `fileAccept` directly.
No duplicate resource field is introduced.

Grid and menu resources remain authoritative for structure and ordering.
No aggregate member list, metamodel object, icon, CSS, position, prompt, redirect, paging, sorting, sequence, disabled-reason, authorization-policy, or value field is added.

## Qualification

The GraphQL test domain provides distinct property, collection, action, and parameter names and descriptions plus every accepted constraint and absence case.
Targeted one-type introspection verifies both the shared shape and a known wrapper.
Known-wrapper reads verify values, nulls, hidden-member redaction, and established resource metadata.

The Reference Application directly exercises `REF-METADATA-01` through independently named and described collections.
It exercises `REF-METADATA-02` through existing property `fileAccept` metadata while proving collection structural hints remain outside local metadata.
The reviewed capability inventory remains byte-identical.
