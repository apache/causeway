# Packaging, licensing, and supply-chain assessment

## Direct packages

| Package | Version | License | Production runtime package closure |
|---|---:|---|---:|
| `bootstrap` | 5.3.8 | MIT | CSS package alone has no direct runtime dependency; optional interactive behavior adds Popper |
| `@awesome.me/webawesome` | 3.11.0 | MIT core package | 35 resolved transitive package instances in the analysis installation |
| `open-props` | 1.7.23 | MIT | No reported runtime dependency |

The analysis harness also pins Playwright 1.61.0 under Apache-2.0 and esbuild 0.28.2 under MIT as development-only evidence and selective-bundling tools.
The package lock records registry URLs and integrity hashes for every resolved package.
`npm ci --ignore-scripts` recreated the complete installation without running package lifecycle scripts.

## Transitive license observation

The resolved Web Awesome production closure reported 24 MIT packages, 9 BSD-3-Clause packages, 1 BSD-2-Clause package, and 1 ISC package among locally resolved metadata.
One `@types/node` entry was nested outside the simple top-level metadata scan and requires normal automated license-report handling in a production proposal.
No evaluated core feature depends on Web Awesome Pro or another commercial asset.
A production dependency review must generate a complete transitive license report and update ASF notice material rather than relying on this bounded scan.

## Security observation

`npm audit --omit=dev --json` and `npm audit --json` both reported zero known advisories across all severities on 2026-08-20.
This is a point-in-time registry advisory result and does not replace ASF dependency review, source provenance, release vote checks, or an ongoing update policy.

## Offline Maven proof

The analysis-only Maven project under `evidence/packaging` successfully built a standard JAR after pinned npm acquisition.
The final JAR contained 26 entries and was 226,029 bytes.
It packaged:

- 2 Bootstrap browser files.
- 2 selectively bundled Web Awesome browser files.
- 3 Open Props CSS files.
- 3 candidate license files.

The Web Awesome bundle includes the twelve components exercised by the fixture and proves that a Maven-packaged selective build is feasible.
A production build must reduce that set to the first adopted slice and enforce the agreed bundle budget.

## Candidate paths

### Bootstrap

Bootstrap 5.3.8 is already resolvable in the repository's wider Maven dependency graph as a WebJar.
The preferred production path is an explicit WebJar dependency or a selective Sass build packaged by Maven.
No npm step is required for the complete compiled CSS path.
The implementation must not rely on Bootstrap being present transitively through Wicket.

### Web Awesome

No Web Awesome artifact was found in the Maven Central WebJar search during the evaluation.
The production path is a repository-owned `package.json` and lockfile, pinned npm acquisition invoked by Maven, selective bundling, checksum verification, and copying generated immutable assets and licenses into the foundation JAR.
Downstream applications continue consuming Maven artifacts and do not run npm.
The optional Web Awesome Pro distribution is outside scope.

### Open Props

Maven Central contains an `org.webjars.npm:open-props` artifact, but the indexed release trails 1.7.23.
The production path can update the WebJar or use Maven-invoked pinned npm acquisition and copy only selected CSS prop packs or generated resolved tokens into the foundation JAR.
No browser JavaScript is required.

## Update policy

Any adopted package requires:

- An exact version and lockfile update through review.
- Integrity and selected-asset checksum verification.
- Automated advisory and transitive license reports.
- Regenerated Maven assets with a clean-tree comparison.
- Browser acceptance and visual regression execution.
- A documented response path for abandoned or compromised upstream projects.

## Hard-gate result

- Bootstrap passes licensing, provenance, offline packaging, and maintenance gates.
- Web Awesome core passes the bounded licensing and offline-packaging gates, but selective bundling and full transitive review are mandatory before adoption.
- Open Props passes licensing and packaging gates; native Popover browser policy remains conditional.
- Material Web fails the strategic-maintenance preference and remains comparison-only.
- Shoelace fails the active-maintenance gate and remains rejected.
