## 1. Regression Coverage

- [ ] 1.1 Add a focused `core/mmtest` regression proving `ServiceRegistryDefault.select(...)` returns an empty `Can` without throwing when `CausewaySystemEnvironment` has no Spring context holder.
- [ ] 1.2 Retain or extend coverage proving active-context selection still delegates the requested type and qualifiers unchanged.

## 2. Lifecycle-Safe Selection

- [ ] 2.1 Update `ServiceRegistryDefault.select(...)` to read the current `SpringContextHolder` once, return `Can.empty()` when it is unavailable, and otherwise preserve existing delegation.

## 3. Verification

- [ ] 3.1 Run the focused `core/mmtest` service-registry tests with JDK 21.
- [ ] 3.2 Run a focused Restful Objects integration test with JDK 21 and verify context shutdown logs `Metamodel disposed.` without a `SpecificationLoaderDefault` destroy-method exception.
- [ ] 3.3 Run the full reconciliation reactor and strict OpenSpec validation with JDK 21.

## 4. Reconciliation Evidence

- [ ] 4.1 Update the maintenance-branch reconciliation ledger to mark CAUSEWAY-4002 resolved with the implementation and verification evidence.
