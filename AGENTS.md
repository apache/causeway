# AGENTS.md

## Repository snapshot
- This repo is Apache Causeway `2.x` and is being decommissioned (`README.adoc`); prefer minimal-risk, targeted fixes.
- Security warning is already documented for this branch (`README.adoc`, CVE note); avoid introducing new deps unless necessary.
- Build and dependency versions are centrally managed from `bom/pom.xml` (parented by Spring Boot `2.7.18`).

## Big-picture architecture (where to start)
- Top-level aggregator is `pom.xml`; default `essential` profile builds `bom`, `core`, `extensions`, `mavendeps`, `starters`, `testing`, `valuetypes`.
- `core/pom.xml` is the framework spine: metamodel, runtime, interaction, transaction, security, webapp, plus viewer and persistence integration modules.
- Spring module composition is explicit via `@Import`; example chain:
  - `core/runtime/.../CausewayModuleCoreRuntime.java`
  - imports `CausewayModuleCoreMetamodel`, interaction, transaction
  - `core/metamodel/.../CausewayModuleCoreMetamodel.java` wires value semantics + core services.
- Persistence and viewers are split by adapter boundary (`persistence/{jdo,jpa,querydsl}`, `viewers/{wicket,restfulobjects,graphql}`).
- Extensions are isolated under `extensions/` and often provide both applib + persistence-specific modules (see `extensions/pom.xml`).

## Build and test workflows that matter
- Recommended toolchain and full build flags are documented in `adoc/build-instructions.adoc` (Maven `3.9.12`, Java `25`, compile release often `11`).
- Typical full build from repo root:
  - `mvn clean install -Drevision=2.2.0-SNAPSHOT -Dmaven.compiler.release=11 -Dmaven.compiler.proc=full -Dmaven.source.skip=true -T 6 -e`
- Include regression tests only when needed with `-Dmodule-regressiontests` (profile declared in root `pom.xml`).
- Test selection is naming-driven (configured in root and `starters/pom.xml` surefire setup):
  - unit tests: `*Test*` (excluding `*IntegTest*`)
  - integration tests: `*IntegTest*`
  - BDD/spec tests: `*Spec*`
- CI helper scripts in `scripts/ci/` are opinionated wrappers (`build-artifacts.sh`, `calc-revision.sh`) and filter noisy Maven output.

## Codebase-specific patterns
- Keep module declarations alphabetized where noted (see comment in root `pom.xml` under `essential` profile).
- Domain behavior is heavily convention-based (eg `choices0Act`, `validateAct`, mixins); see regression examples in `regressiontests/base/.../model/good/`.
- Headless tests bootstrap with explicit Spring config imports, not full app startup; see `regressiontests/base/.../Configuration_headless.java`.
- Use `CausewayPresets` for test logging/noise control (`core/config/.../CausewayPresets.java`).
- Many modules produce `test-jar` artifacts; preserve this when adjusting plugin config (`extensions/pom.xml`).

## Integrations and boundaries
- Security implementations are pluggable (`security/{bypass,keycloak,shiro,simple,spring}`), wired via module imports.
- External integration surfaces include RESTful Objects and GraphQL viewers (`viewers/restfulobjects`, `viewers/graphql`).
- Data-store concerns are adapter-specific; do not mix JDO and JPA assumptions in shared `core/*` modules.

## Agent workflow rules for this repo
- Commit messages must be prefixed with current branch/Jira key (eg `CAUSEWAY-4010: fixes ...`) and use present tense.
- For `/opsx-apply`, commit any uncommitted proposal artifacts first.
- For `/opsx-archive`, auto-select the only active change, sync delta specs to main specs, then commit archive+sync.
- If `SESSION_HANDOFF.md` exists at repo root, read it at session start.
