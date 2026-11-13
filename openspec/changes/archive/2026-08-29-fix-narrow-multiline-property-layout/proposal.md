## Why

Narrow native-toolkit rendering can misplace a multiline property's edit control because responsive CSS detects only the legacy `multiline` host attribute, while authored `multi-line` and metadata-derived multiline state do not activate the intended grid rows.
The resulting implicit grid placement creates a small but real vertical overlap and leaves native Playwright acceptance failing.

## What Changes

- Expose effective multiline presentation as a framework-owned attribute on the rendered property shell in every component state.
- Drive wide and narrow multiline layout selectors from that effective shell state rather than one host-attribute spelling.
- Preserve support for canonical `multi-line`, legacy `multiline`, and metadata-derived `multiLine` inputs.
- Strengthen component stylesheet and Petclinic browser coverage for narrow native alignment, no overlap, and no overflow.
- Leave Vaadin toolkit internals and application-specific styling unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require responsive multiline property layout to follow effective presentation metadata and place labels, descriptions, values, editors, and controls in explicit non-overlapping grid rows.

## Impact

Affected code is limited to the framework property shell, shared semantic theme selectors, foundation tests, and existing Petclinic acceptance geometry.
There are no GraphQL schema, domain model, route, persistence, dependency, or toolkit implementation changes.
