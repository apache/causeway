# Schema and startup evidence

## Acceptance criteria

The implementation may add one shared metadata object type but no per-wrapper descriptor type.
Representative generated SDL growth must remain below ten percent.
A warm end-to-end schema test invocation must remain within five percent of the recorded baseline, acknowledging that Maven and Spring startup dominate this coarse measure.

## Direct-scalar prototype rejected

The initial prototype placed seven scalar definitions directly on applicable generated wrappers.
It preserved the 782 object-type count but increased generated SDL from 284,200 to 399,154 bytes.
The 114,954-byte or 40.4-percent increase was rejected before qualification.

## Accepted shared-object implementation

| Measure | Baseline | Accepted | Delta |
|---|---:|---:|---:|
| Generated SDL bytes | 284,200 | 307,984 | +23,784 (+8.37%) |
| GraphQL object types | 782 | 783 | +1 |
| GraphQL input types | 57 | 57 | 0 |
| GraphQL unions | 6 | 6 | 0 |
| Metadata type definitions | 0 | 1 | +1 |
| Existing wrapper metadata fields | 0 | 341 | +341 |
| Warm end-to-end schema test median | 6,742 ms baseline | 6,809 ms | +67 ms (+0.99%) |

The accepted SDL SHA-256 is `fae5f09107277d5cc1276f14f948d1a1eeb97be2f8c32445214df0be20e7f2ef` for the documented test-domain fixture set.
The three accepted warm invocations measured 7,003 ms, 6,629 ms, and 6,809 ms.

`RichMemberMetadata` is the only added object type.
Every known wrapper references that shared type through `metadata: RichMemberMetadata!`.
The shared scalar fields use the default map property fetcher, while one request-time wrapper resolver evaluates canonical metadata without caching a locale globally.
