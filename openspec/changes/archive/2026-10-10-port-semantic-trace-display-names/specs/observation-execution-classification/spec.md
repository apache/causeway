## MODIFIED Requirements

### Requirement: Foreground HTTP entry classification

With the observation profile active and a supported tracing bridge, Causeway SHALL annotate the existing HTTP entry span with execution mode `foreground`. Classification SHALL NOT create spans, alter parentage, or tag a nested security/viewer span in place of the entry span. Semantic trace display naming MAY update the same entry independently; classification itself SHALL NOT select its name. Classification SHALL cover framework servlet routes including static resources and SHALL NOT mean that an authenticated human initiated the request.

#### Scenario: Framework HTTP request
- **WHEN** a Wicket, GraphQL/HTMX, REST or static-resource request enters the filter under Boot-managed or agent-managed tracing
- **THEN** its exported HTTP entry span carries `foreground` under `causeway.execution.mode` and parentage remains intact; entry names follow semantic trace display naming when a candidate exists

#### Scenario: Failure and redispatch
- **WHEN** request processing fails or undergoes async/error redispatch
- **THEN** classification preserves the failure and existing context lifecycle without creating spans or classifying an unrelated child
