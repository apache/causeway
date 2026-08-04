## 1. Recording-aware safe-action facets

- [x] 1.1 Add a configuration-backed action command-publishing facet that reports enabled for safe semantics only when command-log recording support is `ENABLED`.
- [x] 1.2 Update action facet selection so unannotated and `AS_CONFIGURED` safe actions use recording-aware eligibility while explicit disablement, explicit enablement, DTO processors, and state-changing actions preserve their existing branches.
- [x] 1.3 Add focused metamodel tests for enabled and disabled recording support, global `NONE` and ignore-safe policies, explicit safe-action opt-out, explicit publication, and non-safe action compatibility.

## 2. Recording-aware property facets

- [x] 2.1 Add a property command-publishing facet selected when recording support is enabled and reporting publishing enabled through the normal facet contract.
- [x] 2.2 Update property facet selection so the recording-support facet is the single authoritative choice before annotation and global-policy branches, including for explicitly disabled and already-published properties.
- [x] 2.3 Add focused metamodel tests for unannotated, `AS_CONFIGURED`, explicitly disabled, explicitly enabled, globally configured, contributed, and mixin properties with recording support enabled and disabled.

## 3. Runtime compatibility and documentation

- [x] 3.1 Extend runtime or commandlog coverage to prove recording-aware eligible safe actions and property edits use normal command publication while C1 target and owner suppression remains authoritative.
- [x] 3.2 Add regression coverage proving explicitly published safe actions and property edits do not create duplicate command publication.
- [x] 3.3 Update recording-support documentation to describe safe-action inclusion, safe-action explicit opt-out, authoritative property-edit inclusion, disabled-mode compatibility, and suppression.

## 4. Verification

- [x] 4.1 Run focused metamodel, runtime-service, and commandlog tests for recording-aware command publishing with JDK 21.
- [x] 4.2 Run the affected aggregate Maven build with JDK 21 and confirm existing command-recording and result-metadata tests remain green.
- [x] 4.3 Run IDE compilation and inspections plus strict OpenSpec and repository checks, confirming the implementation introduces no C3/C4b synthetic actions or later replay, export, manager, reachability, or background-gate behavior.
