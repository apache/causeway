## 1. Pin and Inventory the Reference Application

- [ ] 1.1 Record the audited reference-app repository, revision, build instructions, module configuration, and GraphQL variant.
- [ ] 1.2 Catalogue representative services, entities, view models, mixins, properties, actions, parameters, collections, datatypes, grid layouts, menu bars, and home-page examples.
- [ ] 1.3 Group duplicate demonstrations into semantic equivalence classes while retaining source traceability.

## 2. Capture Current GraphQL Evidence

- [ ] 2.1 Start or otherwise generate the current rich GraphQL schema for representative reference-app coverage and record startup or schema-construction failures.
- [ ] 2.2 Capture targeted introspection for services, objects, members, value shapes, collections, metadata, and application entry points.
- [ ] 2.3 Execute representative reads, mutations, action invocations, validation, choices, autocomplete, bulk parameters, resource values, and failure cases.
- [ ] 2.4 Verify suspected lossy scalar, unbounded collection, metadata, resource, menu, and home-page behavior with concrete evidence.

## 3. Build the Coverage Matrix

- [ ] 3.1 Record each representative feature, source location, expected semantic contract, current GraphQL evidence, and classification.
- [ ] 3.2 Separate GraphQL protocol gaps, web-component gaps, viewer policy, Wicket-specific behavior, and explicit non-goals.
- [ ] 3.3 Rank confirmed gaps by correctness, security, scalability, composition value, compatibility risk, and implementation dependency.
- [ ] 3.4 Record sensitive-value and authorization checks separately so absence of disclosure is treated as required coverage.

## 4. Produce the Roadmap

- [ ] 4.1 Define the recommended sequence and scope for value and resource semantics, member metadata, collection windowing, and application entry-point changes.
- [ ] 4.2 Define the prerequisites and boundaries for `<causeway-object>` and menu-bar web components.
- [ ] 4.3 Revise, merge, split, or reject the pending implementation drafts according to the evidence.
- [ ] 4.4 Identify the minimum prerequisite set before the generic HTMX viewer can be promoted.

## 5. Review and Publish

- [ ] 5.1 Publish the machine-readable coverage matrix and one-sentence-per-line narrative analysis in the GraphQL viewer documentation.
- [ ] 5.2 Review the conclusions for GraphQL versus component ownership and for accidental Wicket coupling.
- [ ] 5.3 Verify every confirmed gap has evidence and every follow-on proposal cites its matrix entries.
- [ ] 5.4 Run documentation, link, formatting, and strict OpenSpec validation checks.
