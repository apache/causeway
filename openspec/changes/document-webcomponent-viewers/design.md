## Context

The existing Wicket and GraphQL guides use viewer-local `adoc/antora.yml` descriptors, `modules/ROOT/pages`, and navigation partials.
The local `antora/playbooks/site.yml` explicitly lists component source paths, including `viewers/graphql/adoc` and `viewers/wicket/adoc`, but no web-component source path.
The web-component foundation, HTMX and Vue hosts, security integrations, toolkit adapters, and Petclinic samples already contain README material that can seed source-verified developer guides.
HTMX supplies a server-routed shell, while Vue supplies frontend host integration; both delegate semantic behaviour and rich GraphQL interaction to the shared components.

## Goals / Non-Goals

**Goals:**

- Provide discoverable, task-oriented documentation for application developers choosing, enabling, and customising either viewer.
- Explain shared component behaviour once and cross-link host-specific guides to it.
- Follow existing Antora and Apache licensing conventions, with one sentence per line.
- Validate the actual rendered site using the repository's preview tooling.

**Non-Goals:**

- Change viewer, GraphQL, security, or toolkit runtime behaviour.
- Upgrade frontend or documentation dependencies.
- Promise feature parity with Wicket or promote unverified behaviour as supported.
- Replace contributor-oriented build and regression instructions or exhaustively document every internal component API.

## Decisions

### Use one Antora component with three navigable guide areas

Create `viewers/webcomponents/adoc/antora.yml` with a proposed component name of `webcomponents`, a title of `Web Component Viewers`, and `ROOT:about.adoc` as the start page.
Use ROOT pages for the shared architecture, prerequisites, components, interactions, presentation, and troubleshooting, with `htmx/` and `vue/` page groups for host-specific guidance.
Follow the existing `nav.adoc` and component/module navigation partial conventions so shared site navigation can include the guides consistently.
Check the component identifier for collisions before adding it.

Separate Antora components per host were considered, but a single component keeps the shared foundation prominent and avoids three independently maintained descriptors for closely related guides.
A single long page was rejected because installation, customisation, and reference material need distinct entry points.

### Organise around adoption and customisation rather than README structure

The overview will compare host responsibilities and show the relationship between application shell, shared custom elements, rich GraphQL, and domain model.
Shared pages will cover GraphQL prerequisites, client and object contexts, menus, effective layouts, properties, actions, collections and previews, value/resource presentation including PDF, styling, accessibility, and native versus optional toolkit presentation.
Document supported boundaries rather than implying every value type or widget supports every interaction.

The HTMX area will cover Maven enablement, bootstrap imports, configuration defaults, canonical routes, application shells, generic and custom pages, resource handling, and a Petclinic walkthrough.
The Vue area will cover package and peer dependencies, custom-element compilation, router/plugin setup, application-owned shells and assets, custom route pages, public host integration points, lifecycle, and a Petclinic walkthrough.
Each area will explain its optional SecMan integration, distinguishing authentication/session/CSRF responsibilities from generic viewer behaviour and server-side authorization.
Sample credentials will be labelled as development-only.

Wholesale README inclusion was rejected because current READMEs mix adopter guidance with test procedures and implementation/release evidence.
Retain contributor instructions in place and add links to the canonical developer guides.

### Treat implementation and runnable samples as the source of truth

Inventory public configuration, exported host APIs, resource paths, and sample startup instructions before drafting examples.
Cross-check README claims against current implementation and tests, especially toolkit defaults, resource policies, deployment paths, and security setup.
Reuse source excerpts through repository-supported Antora include mechanisms where practical; otherwise keep examples small and record their source during verification.
Do not introduce application code changes solely to make documentation examples work.
If an implementation gap prevents a claimed workflow, document the limitation or raise it separately.

Link to existing GraphQL setup and rich-behaviour guides rather than duplicating their schema reference.
Update relevant overview wording where needed so existing GraphQL documentation recognises the concrete viewers rather than describing them only as hypothetical clients.

### Integrate only with applicable site versions and validate through preview.sh

Inspect `site.yml`, `site.NEXT.yml`, `site-deploy.yml`, and `site-verify.yml` and their version/source selection before deciding which require the new content source.
Do not register a component against historical sources that do not contain it.
Add site-level discovery links using the existing viewer navigation pattern.

Establish a preview warning baseline before edits, then use `./preview.sh -A` for Antora generation and `./preview.sh -S` to serve the generated site.
The generation-only flag avoids unrelated Maven, example, and generated-documentation phases; run additional phases only if required by the existing site's prerequisites.
Inspect the overview, both getting-started paths, shared pages, security pages, code blocks, and cross-links in the browser.
Require no new unresolved cross-references, missing includes, or missing assets from this change, while reporting unrelated baseline warnings separately.
Record tooling or network blockers explicitly rather than treating an unexecuted check as passed.

## Risks / Trade-offs

- [README details may be stale or overly implementation-oriented] → Verify against current sources and samples and rewrite around developer tasks.
- [Duplicated examples can drift] → Prefer source-backed excerpts where supported and keep host-specific content separate from shared explanations.
- [Toolkit and security descriptions can overstate support] → State supported boundaries, opt-in behaviour, and sample-only assumptions explicitly.
- [Site builds may depend on generated files, external UI assets, or diagram services] → Capture the baseline and distinguish prerequisites and environmental failures from new documentation defects.
- [One component gives viewers less top-level identity than separate components] → Provide clearly labelled HTMX and Vue entry points in both component and site navigation.

## Migration Plan

This is an additive documentation change with no application migration.
Publish the new component through the applicable existing site build, keeping existing README entry points and adding guide links.
Rollback consists of reverting the documentation additions and their navigation/playbook references together.

## Open Questions

No product decisions block implementation.
Exact page boundaries and applicable publication playbooks will be confirmed during the documentation inventory without changing the agreed coverage.
