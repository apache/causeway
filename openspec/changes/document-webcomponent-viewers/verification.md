## Pre-change Antora baseline

Executed `bash ./preview.sh -A` before documentation changes on 2026-09-13.
The command exited with status 1.
The full local log is `/tmp/webcomponents-antora-baseline.log`.

Antora failed before collecting content with the message `Local content source must be a git repository: /Users/danhaywood/repos/github/apache/causeway/htmx (url: .)`.
This checkout is a Git worktree: `git rev-parse --git-dir` resolves to `/Users/danhaywood/repos/github/apache/causeway/main/.git/worktrees/htmx`, and the common Git directory is `/Users/danhaywood/repos/github/apache/causeway/main/.git`.
Git itself recognises the checkout, but the installed Antora content aggregator rejects this local source.
The build wrapper subsequently attempted to touch the absent output site's `.nojekyll` file.

No page-level warning baseline or rendered-site verification is available because content aggregation did not start.
The user authorised switching the primary checkout to `htmx` after discarding its unrelated changes.
The secondary checkout now uses `htmx-docs-worktree-placeholder`; implementation and preview commands must use `/Users/danhaywood/repos/github/apache/causeway/main`.
The two uncommitted OpenSpec progress files were copied and compared before removing their copies from the secondary checkout.

## Primary-checkout baseline

Executed `bash ./preview.sh -A` in `/Users/danhaywood/repos/github/apache/causeway/main` on branch `htmx` before documentation edits.
The command exited with status 0 and generated the site in approximately 32 seconds.
The full local log is `/tmp/webcomponents-antora-main-baseline.log`.
The log contains 19 warning diagnostics and 73 error diagnostics, comprising 30 distinct messages.
These include existing unresolved reference-guide links for interaction and grid services and missing `causewayrel` attributes.
This is the baseline for comparing subsequent builds; a zero exit status alone does not establish that references are valid.
The Git worktree aggregation blocker is resolved.
This baseline was taken before Antora source edits.

## Source inventory and implementation

Added 12 developer-guide pages under `viewers/webcomponents/adoc/modules/ROOT/pages`, plus the component descriptor and navigation partials.
The `webcomponents` identifier has no other component-descriptor matches.
The existing Wicket and GraphQL page trees use navigation partials and contain no `example$` or tagged source includes to reuse for these new examples.
The new snippets are deliberately small literal examples checked against the following sources rather than requiring unrelated generated documentation phases.

- `viewers/webcomponents/htmx/src/main/java/org/apache/causeway/viewer/webcomponents/htmx/HtmxViewerProperties.java`: current settings, defaults, and legacy toolkit precedence.
- `viewers/webcomponents/htmx/src/main/java/org/apache/causeway/viewer/webcomponents/htmx/HtmxDeclarativeTemplate.java`: route-boundary and interaction-controller validation.
- `viewers/webcomponents/htmx/src/main/resources/META-INF/causeway/webcomponents/htmx/default-shell.html`: stable shell landmarks and bindings.
- `viewers/webcomponents/sample-htmx-petclinic/src/main/resources/META-INF/causeway/webcomponents/pages/petclinic.PetOwner.html`: route tokens, member IDs, and authored composition.
- The sample's sibling `previews/` and `collections/` resources: exact-type presentation defaults.
- `viewers/webcomponents/sample-vue-petclinic/frontend/src/pages/PetOwnerPage.vue`: typed route props, markers, and native custom-element composition.
- `viewers/webcomponents/sample-vue-petclinic/frontend/vite.config.ts` and `frontend/src/main.ts`: compiler and host bootstrap reference.
- Both samples' `application.properties`: rich schema and structural/value resource policies.
- Both samples' `run.sh` and `run-secured.sh`: Java selection, Maven profile selection, and argument forwarding.
- `HtmxSecmanSecurityProperties.java` and `VueSecmanSecurityProperties.java`: optional security configuration defaults.
- Foundation, host, security, and sample READMEs: public component, presentation, authentication, and deployment contracts.
- Vue documentation from Context7 `/vuejs/docs`: native custom-element compiler recognition and DOM-property binding.

Registered the component in `site.yml` and `site-verify.yml`, both of which read HEAD.
Left `site.NEXT.yml` and `site-deploy.yml` unchanged because they select explicit historical/release branches rather than this feature checkout.
A future publication configuration must select a branch containing these docs before adding the source there.
Added developer-guide links to the shared, HTMX, and Vue READMEs without removing contributor verification material.

## Updated Antora build and browser checks

`bash ./preview.sh -A` exited 0 after the additions.
Comparing diagnostic multisets by level, source file, and message against the primary-checkout baseline found zero new occurrences.
The local updated log is `/tmp/webcomponents-antora-updated.log`.

`bash ./preview.sh -S` started the preview server.
macOS Control Center already occupied port 5000, so `serve` selected port 55701 without stopping the existing service.
The preview entry point is `http://localhost:55701/webcomponents/latest/about.html`.

Browser checks fetched all 12 guide pages successfully, confirmed their headings and code blocks, and found no unresolved-reference markers.
Opened the overview, both getting-started pages, both security pages, and Vue customisation in browser tabs.
Visually inspected the overview screenshot, including comparison table and expanded component navigation.
Checked the Vue customisation code block retains its typed `defineProps` example, its local article links return successfully, and it has no document-level horizontal overflow.

A generated-site scan checked 1,271 local link/asset references across the 12 pages and generated index page.
The missing targets were inherited site-header links to `docs/latest/reference/about.html`, `tooling/latest/about.html`, `more-thanks/latest/more-thanks.html`, and `regressiontests/latest/index.html`.
Confirmed the existing GraphQL overview contains the same header targets; these are not new guide-content references.
The overview's ASCII architecture diagram requires no remote diagram generation.
`git diff --check` passed.

## Sample execution limitations

Attempted both ordinary sample launchers with `JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.10-tem` and its `bin` directory first in PATH.
The default shell Java is 11, and macOS `java_home -v 21` does not discover this SDKMAN installation, so explicit selection was required.
Both launchers reached Maven but failed before application startup in `verify-vaadin-action-assets` because `viewers/webcomponents/foundation/vaadin-actions/package-lock.json` is missing.
Logs are `/tmp/webcomponents-htmx-sample-startup.log` and `/tmp/webcomponents-vue-sample-startup.log`.
No runtime assets were regenerated or security settings changed to bypass that failure.
The secured launchers select the corresponding secured profiles and use the same foundation build; their browser journeys were not executed.
Therefore sample startup success, live edits/actions, login/logout, deep-link restoration, and CSRF behaviour remain source-checked rather than independently exercised in this documentation change.
This limitation does not affect the successful Antora generation and documentation preview checks.

## Follow-up build repair

The user requested investigation and repair of the full `mvnd clean install -DskipTests` failure in the primary checkout.
Reproduced the same missing action lockfile failure with JDK 21.
Comparison with the original secondary checkout found four required ignored inputs: `package.json` and `package-lock.json` in each of `foundation/vaadin-actions` and `foundation/vaadin-grid`.
The root `.gitignore` ignored both filenames, so switching the primary checkout to the feature branch had not transferred these local-only files.

With user approval, copied those four existing files unchanged from the secondary checkout and added exact-path ignore exceptions so they can be committed.
No dependency versions were regenerated or upgraded.
The original lockfiles' direct package pins match the checked-in policies, and the policies are identical across checkouts.

Reran `mvnd clean install -DskipTests -B --no-transfer-progress` from the repository root with SDKMAN JDK `21.0.10-tem`.
The full build exited 0 with `BUILD SUCCESS` in approximately 30 seconds.
The local log is `/tmp/webcomponents-mvnd-fixed-build.log`.
This resolves the earlier build prerequisite blocker; it does not retroactively verify sample browser journeys or tests skipped by `-DskipTests`.
