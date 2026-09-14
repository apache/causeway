## Why

The Apache Causeway reference application is the broadest executable catalogue of Causeway domain-model and presentation semantics, but the initial audit was intentionally source-level and found several possible GraphQL gaps with different urgency and ownership.
Implementing all possible gaps as one change would mix datatype protocol, member metadata, collections, resources, application entry points, and web-component concerns before the exact coverage boundary is proven.
A dedicated analysis change should first produce an evidence-backed coverage matrix and roadmap, without changing production GraphQL or web-component behavior.

## What Changes

- Pin and inspect a specific `apache/causeway-app-referenceapp` revision.
- Inventory representative services, entities, view models, mixins, properties, actions, parameters, collections, values, layouts, menu bars, and home-page behavior.
- Exercise the current rich GraphQL schema against representative reference-app cases and retain schema, query, mutation, and failure evidence.
- Classify every audited feature as already supported, intentionally viewer-specific, explicitly unsupported, a GraphQL gap, or a web-component gap.
- Distinguish merely lossy presentation from failures that prevent a valid query, mutation, action invocation, or bounded read.
- Produce a prioritized roadmap and refine the pending implementation proposals for value and resource semantics, member metadata, collection windowing, application entry points, composite object rendering, and menu bars.
- Make no production JavaScript, Java, schema, or component behavior changes.

## Capabilities

### New Capabilities

- `rich-graphql-referenceapp-analysis`: Defines the reproducible analysis and roadmap used to assess rich GraphQL and web-component coverage against the Causeway reference application.

### Modified Capabilities

None.

## Impact

- Adds durable analysis and roadmap documentation only.
- May revise pending OpenSpec proposals based on evidence, but does not implement them.
- Uses reduced probes and captured evidence rather than making the normal build depend on an external checkout.
- Establishes the gate for promoting any reference-app coverage implementation proposal.
