## 1. Restore test-source compilation

- [x] 1.1 Replace `RestEasyConfiguration` in `RestEndpointService` with the current `RestfulPathProvider` contract while preserving context-path and trailing-slash URI assembly.
- [x] 1.2 Migrate `EntityScan`, `LocalServerPort`, and any other stale test imports to current Spring Boot and Causeway APIs.
- [x] 1.3 Run forced test compilation with `-Dmaven.test.skip=false` and resolve every remaining compile error without excluding sources or weakening compiler checks.
