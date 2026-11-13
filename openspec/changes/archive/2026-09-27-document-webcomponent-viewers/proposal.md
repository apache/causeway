## Why

The HTMX and Vue viewers and their shared web components have substantial README documentation but no integrated Antora guides for application developers.
Documenting their current capabilities alongside the existing Wicket and GraphQL viewers will make adoption, configuration, and customisation discoverable without requiring readers to navigate implementation notes.

## What Changes

- Add Antora documentation for shared web-component concepts, rich GraphQL prerequisites, component composition, interactions, presentation, and troubleshooting.
- Add separate HTMX and Vue guides covering installation, configuration or host setup, routing, shells, custom pages, optional SecMan authentication, and Petclinic walkthroughs.
- Follow existing viewer documentation conventions and connect the new guides to site navigation, applicable playbooks, and existing GraphQL documentation.
- Verify examples against the current implementation and samples, retaining contributor-focused verification instructions in READMEs and linking readers to the guides.
- Build and inspect the rendered documentation using `preview.sh`, checking navigation, cross-references, examples, and new build warnings.

## Capabilities

### New Capabilities

- `webcomponent-viewer-documentation`: Application-developer Antora guides for the shared components and HTMX and Vue viewers, including discoverability, source-grounded examples, and preview verification.

### Modified Capabilities

None.
Existing viewer and GraphQL runtime requirements remain unchanged.

## Impact

Documentation will be added under `viewers/webcomponents/` and integrated with the existing Antora site configuration and navigation.
Related READMEs and GraphQL overview links may be updated to provide consistent entry points without duplicating reference material.
The change introduces no runtime APIs, dependency upgrades, or viewer behaviour changes.
