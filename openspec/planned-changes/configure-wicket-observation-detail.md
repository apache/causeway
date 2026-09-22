# Configure Wicket Observation Detail

**Roadmap position:** Proposal 4, before broad production enablement if trace volume proves material.

## Why

Member-level Wicket observations are intentionally bounded but can still add tens or hundreds of spans to a complex page.
Operators may need a framework-level way to select useful detail and cap pathological requests independently of agent sampling.

## Proposed Changes

- Add Wicket observation detail levels for disabled, page-only, region-level, and member-level tracing.
- Add a configurable maximum number of Causeway Wicket observations per request.
- Preserve deterministic parentage when detail is reduced.
- Mark the page or current enclosing region when the request budget truncates lower-level observations.
- Expose configuration through the established Causeway Wicket configuration model.
- Document how framework detail controls interact with Java-agent head sampling and collector policies.

Illustrative configuration is:

```yaml
causeway:
  viewer:
    wicket:
      observation:
        detail: members
        max-spans-per-request: 200
```

Potential detail levels are:

| Level | Semantic observations |
|---|---|
| `none` | No Causeway Wicket observations |
| `page` | Entity-page rendering only |
| `regions` | Page, fieldsets, and collections |
| `members` | Properties and relevant action buttons in addition to regions |

## Non-Goals

- Do not implement duration-based sampling inside Causeway.
- Do not replace OpenTelemetry agent sampling or collector tail sampling.
- Do not provide exporter, endpoint, or SDK configuration.
- Do not silently change global tracing configuration outside the Wicket viewer.

## Prerequisites and Evidence Gate

The initial region spans should first establish realistic span counts for representative full-page and Ajax requests.
Proceed before broad production rollout if those measurements show unacceptable volume or if applications require different diagnostic detail levels.

## Design Questions

- Should the default remain full member detail when the observation profile is active, or default to region detail?
- Does the request budget count the page span or only descendants?
- Which enclosing observation receives the truncation attribute during an Ajax partial render with no page span?
- Should configuration changes require application restart, consistent with the current observation profile?

## Expected Exit Gate

Operators can choose page, region, or member detail and can prove that an oversized page never exceeds the configured Causeway Wicket observation budget while automatic agent spans remain unaffected.
