## 1. Re-verify on current HEAD

- [ ] 1.1 Confirm `AppFeat` implements only `Comparable<AppFeat>, ViewModel` and that the four existing SecMan
      abstractions still implement `RefData`.

## 2. Mark AppFeat as reference data

- [ ] 2.1 Add `RefData` to `ApplicationFeatureChoices.AppFeat`'s `implements` clause; add the import.

## 3. Tests

- [ ] 3.1 Classifier test: an `AppFeat` bookmark is classified as reference data by the default service without
      loading the object.
- [ ] 3.2 Reachability test: a permission-feature command whose target or reference parameter is an `AppFeat`
      bookmark is a known export participant with no prior finder.
- [ ] 3.3 Confirm the four existing SecMan opt-ins remain classified.

## 4. Verification

- [ ] 4.1 Run focused SecMan applib and commandlog applib reference-data tests plus the affected reactor under
      JDK 21, and strict OpenSpec validation.
- [ ] 4.2 Confirm no persistence field, schema, or logical-type change.
