## REMOVED Requirements

### Requirement: Duration filtering survives observation composition
**Reason**: The threshold wrapper and duration policy are removed rather than extended to independently exported descendants.
**Migration**: Use regular observations and normal sampling/export settings. Observation lifecycle and failure cleanup requirements remain in force.
