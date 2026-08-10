<!--
DRAFT DELTA — re-validate on promotion.
MODIFIES the existing requirement "Built-in SecMan identities are declared as reference data"
to add the permission-feature reference view-model ApplicationFeatureChoices.AppFeat to the
set of built-in SecMan reference-data identities (previously the four entity abstractions only).
-->

## MODIFIED Requirements

### Requirement: Built-in SecMan identities are declared as reference data

The SecMan applib `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` domain abstractions SHALL implement `RefData`, and the permission-feature reference view-model `ApplicationFeatureChoices.AppFeat` SHALL also implement `RefData`. No persistence field or schema change SHALL be required by these marker declarations. A bookmark whose logical type resolves to any of these designated SecMan types SHALL therefore be classified as replay reference data by the default classifier.

#### Scenario: SecMan domain type is classified by the default service

- **WHEN** a bookmark resolves to any of the four designated SecMan domain abstractions
- **THEN** the default classifier identifies it as replay reference data

#### Scenario: Permission-feature reference view-model is classified as reference data

- **WHEN** a bookmark resolves to the `ApplicationFeatureChoices.AppFeat` permission-feature reference view-model
- **THEN** the default classifier identifies it as replay reference data
- **AND** no domain object is loaded to make that classification

#### Scenario: Permission-feature command is a known export participant

- **GIVEN** a command whose target or reference parameter is an `AppFeat` bookmark
- **WHEN** export reachability is evaluated
- **THEN** the `AppFeat` participant is a known export participant with no prior result establishing that bookmark

#### Scenario: Marker declarations require no schema change

- **WHEN** the SecMan abstractions and the `AppFeat` view-model implement `RefData`
- **THEN** no persistence field or schema change is required
