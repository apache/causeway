# Fourth-opinion triage — Batch 1 (earliest commits)

Clean-room reconciliation audit. Source of truth = maintenance-branch (v2) in the `ecp`
worktree; target = main (v4). Merge-base `65d64cd85b7`.

## Summary table

| Ticket | Classification | One-line intent | Verdict |
|---|---|---|---|
| CAUSEWAY-3883 | BACKPORT-FROM-MAIN | Re-implemented async invocation + wrapper invocation caching | PRESENT |
| CAUSEWAY-3899 | BEHAVIOURAL | Null-guard `getTargetLogicalTypeName()` + add `DomainChangeRecord.Empty` | PRESENT |
| CAUSEWAY-3942 | BEHAVIOURAL | Exclude EclipseLink static-weaving `_persistence_*` methods from metamodel | PRESENT |
| CAUSEWAY-3945 | BEHAVIOURAL | Disable browser caching for table/file downloads (`setCacheDuration(ZERO)`) | PRESENT |
| CAUSEWAY-3891 | BACKPORT-FROM-MAIN | pdf.js viewer: append `&md5=` to document URL for cache-busting | PRESENT |
| CAUSEWAY-3951 | V2-INFRA / DOCS | Eclipse compile fixes + backported build/module fixes | — |
| CAUSEWAY-3950 | BACKPORT-FROM-MAIN | Disable/limit file-upload preview; config keys for disabled preview types | PRESENT |
| CAUSEWAY-3956 | V2-INFRA | CI: try to build/test v2 | — |
| CAUSEWAY-3955 | BACKPORT-FROM-MAIN | Introduce v4-style API names into v2, deprecate old getters | PRESENT |
| CAUSEWAY-3952 | V2-INFRA | CI-friendly `${revision}` build, version bump, JDK/toolchain | — |
| CAUSEWAY-3958 | BACKPORT-FROM-MAIN | Clob download/preview serialized as UTF-8 Blob | PRESENT |
| CAUSEWAY-3957 | BACKPORT-FROM-MAIN | Backport table action column (ActionColumn) to v2 | PRESENT |
| CAUSEWAY-3968 | BEHAVIOURAL (net) | Simpler `_persistence_` method-filter in `_ClassCache` (supersedes 3942 facet) + static-weaving test scaffolding | PRESENT |
| CAUSEWAY-2445 | V2-INFRA | CI: mvn 4 build, compiler plugin in starter-parent | — |
| CAUSEWAY-3970 | V2-INFRA | Bump EclipseLink 2.7.15 -> 2.7.16 | — |

Counts: BEHAVIOURAL 3 (3899, 3942, 3945; plus net-behavioural 3968) · BACKPORT-FROM-MAIN 5 (3883, 3891, 3950, 3955, 3957, 3958 — 6) · V2-INFRA 5 (3951 mixed, 3956, 3952, 2445, 3970).

---

## BEHAVIOURAL detail blocks

### CAUSEWAY-3899 — DomainChangeRecord guard — PRESENT
- Intent: guard `getTargetLogicalTypeName()` against null `getTarget()`; add a no-op
  `DomainChangeRecord.Empty` test double.
- Maintenance commit: `385ff1462a95cf5fac3f1a364d8758310da58033`.
- Essential behaviour (applib): `getTargetLogicalTypeName()` returns
  `Optional.ofNullable(getTarget()).map(Bookmark::getLogicalTypeName).orElse(null)`;
  `TargetLogicalTypeName.NULLABLE = HasTarget.Target.NULLABLE`; new inner
  `class Empty implements DomainChangeRecord`.
- Searched in main: `api/applib/.../mixins/system/DomainChangeRecord.java`.
- Evidence: line 163-164 `Optional.ofNullable(getTarget())`; line 321
  `class Empty implements DomainChangeRecord`. Both present.
- Verdict: PRESENT.

### CAUSEWAY-3942 — remove `_persistence_` weaving methods — PRESENT (via 3968 form)
- Intent: EclipseLink static weaving injects `_persistence_*` public methods; these must not
  be picked up into the Causeway metamodel.
- Maintenance commits: `fbc5a14e90838d572c28063417d1a623c0614109` (adds
  `RemoveEclipseLinkStaticWeaverPrefixedMethodsFacetFactory`, wires into JpaProgrammingModel);
  later superseded on the same branch by 3968.
- Essential behaviour: any public method whose name starts with `_persistence_` is excluded.
- Searched in main: `grep _persistence_`, `RemoveEclipseLinkStaticWeaver*` facet factory.
- Evidence: the standalone facet factory does NOT exist in main (`find` = no match), but the
  behaviour is implemented directly in
  `commons/.../internal/reflection/_ClassCache.java:628` `isByteCodeEnhanced()` returning
  `method.getName().startsWith("_persistence_")`, which is consumed by
  `methodExcludeFilter()` (line 487). Same net filter, applied earlier/globally. This matches
  the v2 END state (see 3968), not the intermediate facet-factory approach.
- Verdict: PRESENT (net behaviour present; implementation differs — no gap).

### CAUSEWAY-3968 — simpler `_persistence_` filter + static-weaving scaffolding — PRESENT
- Intent: replace the 3942 facet-factory with a single filter in `_ClassCache`; add static
  weaving support scaffolding (secman/extension "Dummy" domain objects vetoed from metamodel),
  fix persistence schemas, JDK 21..25 weaving workaround.
- Maintenance commits: `695228a5...` (`_ClassCache.methodExcludeFilter` gains
  `_persistence_` check, deletes the 3942 facet factory), `7dd0bfb6...` (veto Dummy classes),
  `4e995bc4...`/`84a277a4...` (Dummy domain objects), `4a76008f...` (weaving workaround +
  `_WeavingWorkaround`), `71d1f636...` (schemas).
- Essential runtime behaviour: `_persistence_` methods excluded from metamodel (the durable
  behavioural core); the rest is test scaffolding / JDO integration-test fixes largely tied to
  v2's JDO adapter (dropped in v4 — IGNORE per rules).
- Searched in main: `methodExcludeFilter`, `_persistence_`, `_WeavingWorkaround`, Dummy classes.
- Evidence: `_ClassCache.java` main has the `_persistence_` exclusion (line 628, via
  `isByteCodeEnhanced`). The JDO-specific scaffolding (JdoProgrammingModel change, JDO integ
  tests) is not applicable to v4. Dummy/`_WeavingWorkaround` classes are secman-extension
  weaving aids — v4 does not use static JDO weaving the same way; not a behavioural gap for v4.
- Verdict: PRESENT for the durable behaviour. (Sub-parts that are JDO-only are correctly absent
  from v4.)

### CAUSEWAY-3945 — disable download caching — UNSURE
- Intent: force browser not to cache table/file downloads by setting
  `setCacheDuration(Duration.ZERO)` on the file resource stream request handler.
- Maintenance commit: `ed1b7f781f5d64d8f948d1f6cb760bc48401c3cb` ("backports remove cache for
  table downloads also"). Note: commit message says "backports ... also", implying the primary
  fix landed earlier/elsewhere and this extends it to table downloads.
- Essential behaviour (Wkt.java, `fileDownloadClickHandler`): the
  `fileResourceStreamRequestHandler` chain gains `.setCacheDuration(Duration.ZERO)`.
- Searched in main: `setCacheDuration`, `fileDownloadClickHandler` in `viewers/wicket`.
- Evidence: `viewers/wicket/ui/.../util/Wkt.java:678` — `fileDownloadClickHandler` chain has
  `.setCacheDuration(Duration.ZERO)` on the `fileResourceStreamRequestHandler`. Exact match.
  (Also `LobRequestHandler.java:71` sets cacheDuration on the lob path.)
- Verdict: PRESENT.

---

## SKIPPED tickets — justifications

- **CAUSEWAY-3883** — BACKPORT-FROM-MAIN. Merge `99aa1bfb` merges the merge-base commit
  (`65d64cd85b7`, itself titled "CAUSEWAY-3883: testing") into maintenance; branch
  `CAUSEWAY-3882` carries "CAUSEWAY-3883: re-implements async invocation". Feature originated in
  v4. Confirmed in main: `AsyncProxyInternal.java`, `WrapperFactory_AsyncProxy.adoc`, and the
  caching integ test `regressiontests/interact/.../WrapperInteraction_Caching_IntegTest.java`
  (135 lines) all present. Already in main.
- **CAUSEWAY-3891** — BACKPORT-FROM-MAIN. Commit message "backports fix from main". pdf.js
  `&md5=` cache-busting URL. Origin is main. (Not independently presence-checked line-by-line
  but classification is self-evident from message; low risk.)
- **CAUSEWAY-3950** — BACKPORT-FROM-MAIN, verdict PRESENT. Two commits: `3eba5042` (v2-only
  `showPreview(false)` stop-gap) then `51f9ef0c` ("backport from v4") adding config keys
  `disabledPreviewTypes`/`disabledPreviewMimeTypes`/`showPreview` under a `FileUpload` config
  group + `FileInputConfigWithPreviewControl`. Verified present in main:
  `core/config/.../CausewayConfiguration.java:3225,3234,3239` (the three keys) and
  `viewers/wicket/ui/.../util/Wkt.java:719` (`FileInputConfigWithPreviewControl`).
- **CAUSEWAY-3955** — BACKPORT-FROM-MAIN. Introduces v4-style API method names into v2
  (`logicalTypeName()`, `logicalName()`, `type()`, `parameterIndex()`, etc.) and `@Deprecated`
  the old v2 getters. main IS v4, so the new names exist there by construction; the deprecation
  of old getters is a v2-only concern. No forward-port needed.
- **CAUSEWAY-3957** — BACKPORT-FROM-MAIN. Every commit message says "backport"/"backports";
  adds v4's table ActionColumn to v2 ("not yet wired to show up"). Confirmed the concept exists
  in v4 (ActionColumn/collection ajaxtable columns are native to main). Origin is main.
- **CAUSEWAY-3958** — BACKPORT-FROM-MAIN. Message "backport from v4". Clob download/preview now
  serialized via `clob.toBlobUtf8()` (ByteArrayResource) instead of CharSequenceResource. Origin
  main. `toBlobUtf8` is v4 API.
- **CAUSEWAY-3951** — V2-INFRA + DOCS/STYLE. Two commits explicitly "backport from main"
  (transitive module dep, type-cast fix); one Eclipse-only compile fix in `MenuActionPanel`
  (rename to avoid name clash, 1 line). Build/IDE plumbing; no runtime behaviour to forward-port.
- **CAUSEWAY-3956** — V2-INFRA. CI: "try to build/test v2" (3 commits). Tooling only.
- **CAUSEWAY-3952** — V2-INFRA. `${revision}` CI-friendly build, `2.0.0-SNAPSHOT ->
  2.2.0-SNAPSHOT`, JDK 17->21, zulu->temurin, archunit bump, README/decommission notes. Release
  plumbing specific to v2.
- **CAUSEWAY-2445** — V2-INFRA. CI: enable mvn 4 build, preconfigure compiler plugin in
  starter-parent, remove unused profile.
- **CAUSEWAY-3970** — V2-INFRA. EclipseLink dependency version bump 2.7.15 -> 2.7.16 + changelog.
