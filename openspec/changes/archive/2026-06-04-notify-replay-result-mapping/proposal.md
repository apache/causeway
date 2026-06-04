## Why

Command replay can legitimately create a different domain object than the object recorded during export/import.
Applications need an explicit extension point so they can learn that the recorded result bookmark, such as `demoInvoice:1`, now corresponds to the actual replay result bookmark, such as `demoInvoice:2`.

## What Changes

- Add an SPI that is invoked after a successful command replay when both recorded and actual result bookmarks are available.
- Provide the SPI with the recorded returned object bookmark from the imported `CommandLogEntry` and the actual bookmark returned by replay execution.
- Invoke the SPI only after replay succeeds, and avoid invoking it for failed replay attempts.
- Do not require either bookmark to resolve to a live domain object before notifying the SPI.

## Capabilities

### New Capabilities

- `command-replay-result-mapping`: Allows applications to observe mappings from recorded replay result bookmarks to actual replay result bookmarks.

### Modified Capabilities

- None.

## Impact

- Affects command replay success handling in `ReplayableCommand`.
- Adds a command log replay SPI type for application-provided mapping handlers.
- Adds module registration for the default/no-op SPI implementation if required.
- Adds tests for SPI invocation, non-invocation, and bookmark values.
