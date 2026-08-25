# Baseline and ownership

## Generated schema baseline

The baseline was generated before production edits with Temurin 25 and `PrintSchemaIntegTest` using the full rich GraphQL test domain.

| Measure | Baseline |
|---|---:|
| Generated SDL bytes | 284,200 |
| GraphQL object types | 782 |
| GraphQL input types | 57 |
| GraphQL unions | 6 |
| End-to-end Maven invocation | 6,742 ms |
| SDL SHA-256 | `d3f7087fe1ce66939d1338c2149b1b16f5a1fe778d588a4b0e94ba10479abe60` |

Existing property wrappers expose hidden, disabled, get, validate, choices, autocomplete, autocomplete windows where applicable, set where allowed, and datatype.
Existing collection wrappers expose hidden, disabled, get, window, and datatype.
Existing action wrappers expose hidden, disabled, validate, invoke where allowed, and params.
Existing action-parameter wrappers expose hidden, disabled, choices, autocomplete, autocomplete windows where applicable, default, validate, datatype, and resource metadata where applicable.

## Reference-derived fixtures

`REF-METADATA-01` identifies independently named and described collection fixtures in `CollectionLayoutNamedPage` and `CollectionLayoutDescribedAsPage`.
Qualification maps those fixtures to independent property, collection, action, and parameter name and description assertions, including a description-absent case.

`REF-METADATA-02` identifies `PropertyFileAcceptPage` and a paged collection layout as evidence that local constraints and structural layout have different owners.
Qualification maps local text constraints to property and parameter fixtures while preserving existing resource `fileAccept` fields.
Paging, ordering, grouping, and placement remain owned by established collection operations and grid resources.

## Ownership classification

| Candidate | Classification | Owner |
|---|---|---|
| Canonical friendly name | Local semantic metadata | Known rich wrapper |
| Canonical description | Local semantic metadata | Known rich wrapper |
| Finite maximum length | Local semantic constraint | Property or parameter wrapper |
| Java regular-expression text and flags | Local semantic constraint | Property or parameter wrapper |
| Multiline line count | Local semantic editor hint | Property or parameter wrapper |
| Typical length | Local semantic editor hint | Property or parameter wrapper |
| Resource file acceptance | Existing resource metadata | Existing property-get or parameter field |
| Rows, columns, tabs, field sets, grouping, placement, and sequence | Grid structure | Effective grid resource |
| Bars, menus, sections, entries, labels, and ordering | Menu structure | Effective menu resource |
| Prompt style, redirects, repainting, and Wicket decorators | Viewer-specific behavior | Explicit exclusion |
| Icons and CSS classes | Resource-owned presentation | Grid or menu resource |
| Collection paging and sorting | GraphQL collection operations | Existing or separately proposed capability |
| Requiredness | GraphQL type structure | Generated input nullability |
| Validation result | Dynamic domain behavior | Existing validation operation |

No action position, prompt, redirect, icon, CSS, collection paging, sorting, sequence, resource deployment policy, or aggregate metamodel field enters the local metadata surface.
