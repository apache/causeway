# Analysis Maven packaging proof

This analysis-only module proves that selectively built Vaadin Web Component assets and direct-package licenses can be delivered as ordinary Maven resources without a browser runtime CDN.
It is not a production module and is not referenced by the repository reactor.

## Reproduce

From `evidence/harness`:

```shell
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run verify
npm run measure
```

Then run:

```shell
mvn -f ../packaging/pom.xml clean package
```

The JAR contains:

- The broad analysis bundle beneath `META-INF/resources/causeway-vaadin-analysis`.
- Route-split reference, Grid, field, and shared chunks beneath `META-INF/resources/causeway-vaadin-analysis/split`.
- One retained license copy for every direct Vaadin package beneath `META-INF/licenses/vaadin-analysis`.

A production implementation would choose either broad or route-split delivery, invoke the pinned frontend build from Maven, include the complete accepted dependency and NOTICE closure, and verify generated checksums during release.
