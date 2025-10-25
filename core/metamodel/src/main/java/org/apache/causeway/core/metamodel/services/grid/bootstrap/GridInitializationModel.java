/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GridInitializationModel {

    record Disjunction(
        Set<String> left,
        Set<String> right) {

        public static Disjunction of(final Set<String> left, final Set<String> right){
            var leftOnly = _Sets.minus(left, right, LinkedHashSet::new); // preserve order
            var rightOnly = _Sets.minus(right, left, LinkedHashSet::new); // preserve order
            return new Disjunction(leftOnly, rightOnly);
        }
    }

    /**
     * find all row and col ids<br>
     * - ensure that all Ids are different<br>
     * - ensure that there is exactly one col with the
     * unreferencedActions, unreferencedProperties and unreferencedCollections attribute set.
     * @param bsGrid
     * @return empty if not valid
     */
    public static Optional<GridInitializationModel> createFrom(final BSGrid bsGrid) {

        var gridModel = new GridInitializationModel();

        bsGrid.visit(new BSElement.Visitor() {
            @Override
            public void visit(final BSRow bsRow) {
                final String id = bsRow.getId();
                if(id == null) return;

                if(gridModel.contains(id)) {
                    bsRow.setMetadataError("There is another element in the grid with this id: " + id);
                    gridModel.gridErrorsDetected = true;
                    return;
                }
                gridModel.putRow(id, bsRow);
            }

            @Override
            public void visit(final BSCol bsCol) {
                final String id = bsCol.getId();
                if(id == null) return;

                if(gridModel.contains(id)) {
                    bsCol.setMetadataError("There is another element in the grid with this id: " + id);
                    gridModel.gridErrorsDetected = true;
                    return;
                }
                gridModel.putCol(id, bsCol);
            }

            @Override
            public void visit(final FieldSet fieldSet) {
                var groupIdAndName = GroupIdAndName.forFieldSet(fieldSet);
                if(!groupIdAndName.isPresent()) {
                    fieldSet.setMetadataError("a fieldset must at least have an id or a name");
                    gridModel.gridErrorsDetected = true;
                    return;
                }
                String id = groupIdAndName.get().id();
                if(gridModel.contains(id)) {
                    fieldSet.setMetadataError("There is another element in the grid with this id: " + id);
                    gridModel.gridErrorsDetected = true;
                    return;
                }
                gridModel.putFieldSet(id, fieldSet);
            }
        });

        if(gridModel.gridErrorsDetected) {
            return Optional.empty();
        }

        bsGrid.visit(new BSElement.Visitor(){

            @Override
            public void visit(final BSCol bsCol) {
                if(isSet(bsCol.isUnreferencedActions())) {
                    if(gridModel.colForUnreferencedActionsRef != null) {
                        bsCol.setMetadataError("More than one col with 'unreferencedActions' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else if(gridModel.fieldSetForUnreferencedActionsRef != null) {
                        bsCol.setMetadataError("Already found a fieldset with 'unreferencedActions' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else {
                        gridModel.colForUnreferencedActionsRef=bsCol;
                    }
                }
                if(isSet(bsCol.isUnreferencedCollections())) {
                    if(gridModel.colForUnreferencedCollectionsRef != null) {
                        bsCol.setMetadataError("More than one col with 'unreferencedCollections' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else if(gridModel.tabGroupForUnreferencedCollectionsRef != null) {
                        bsCol.setMetadataError("Already found a tabgroup with 'unreferencedCollections' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else {
                        gridModel.colForUnreferencedCollectionsRef = bsCol;
                    }
                }
            }

            @Override
            public void visit(final FieldSet fieldSet) {
                if(isSet(fieldSet.isUnreferencedActions())) {
                    if(gridModel.fieldSetForUnreferencedActionsRef != null) {
                        fieldSet.setMetadataError("More than one fieldset with 'unreferencedActions' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else if(gridModel.colForUnreferencedActionsRef != null) {
                        fieldSet.setMetadataError("Already found a column with 'unreferencedActions' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else {
                        gridModel.fieldSetForUnreferencedActionsRef = fieldSet;
                    }
                }
                if(isSet(fieldSet.isUnreferencedProperties())) {
                    if(gridModel.fieldSetForUnreferencedPropertiesRef != null) {
                        fieldSet.setMetadataError("More than one column with 'unreferencedProperties' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else {
                        gridModel.fieldSetForUnreferencedPropertiesRef = fieldSet;
                    }
                }
            }

            @Override
            public void visit(final BSTabGroup bsTabGroup) {
                if(isSet(bsTabGroup.isUnreferencedCollections())) {
                    if(gridModel.tabGroupForUnreferencedCollectionsRef != null) {
                        bsTabGroup.setMetadataError("More than one tabgroup with 'unreferencedCollections' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else if(gridModel.colForUnreferencedCollectionsRef != null) {
                        bsTabGroup.setMetadataError("Already found a column with 'unreferencedCollections' attribute set");
                        gridModel.gridErrorsDetected = true;
                    } else {
                        gridModel.tabGroupForUnreferencedCollectionsRef = bsTabGroup;
                    }
                }
            }
        });

        if(gridModel.colForUnreferencedActionsRef == null
            && gridModel.fieldSetForUnreferencedActionsRef == null) {
            bsGrid.getMetadataErrors().add("No column and also no fieldset found with the 'unreferencedActions' attribute set");
            gridModel.gridErrorsDetected = true;
        }
        if(gridModel.fieldSetForUnreferencedPropertiesRef == null) {
            bsGrid.getMetadataErrors().add("No fieldset found with the 'unreferencedProperties' attribute set");
            gridModel.gridErrorsDetected = true;
        }
        if(gridModel.colForUnreferencedCollectionsRef == null
            && gridModel.tabGroupForUnreferencedCollectionsRef == null) {
            bsGrid.getMetadataErrors().add("No column and also no tabgroup found with the 'unreferencedCollections' attribute set");
            gridModel.gridErrorsDetected = true;
        }

        bsGrid.streamPropertyLayoutData()
            .forEach(prop->gridModel.propertyLayoutDataById.putElement(prop.getId(), prop));
        bsGrid.streamCollectionLayoutData()
            .forEach(coll->gridModel.collectionLayoutDataById.putElement(coll.getId(), coll));
        bsGrid.streamActionLayoutData()
            .forEach(act->gridModel.actionLayoutDataById.putElement(act.getId(), act));

        return gridModel.gridErrorsDetected
            ? Optional.empty()
            : Optional.of(gridModel);
    }

    // --

    private final LinkedHashSet<String> allIds = _Sets.newLinkedHashSet();
    private final LinkedHashMap<String, BSRow> rows = _Maps.newLinkedHashMap();
    private final LinkedHashMap<String, BSCol> cols = _Maps.newLinkedHashMap();
    private final LinkedHashMap<String, FieldSet> fieldSets = _Maps.newLinkedHashMap();

    private BSCol colForUnreferencedActionsRef;
    private FieldSet fieldSetForUnreferencedActionsRef;

    private BSCol colForUnreferencedCollectionsRef;
    private BSTabGroup tabGroupForUnreferencedCollectionsRef;

    private FieldSet fieldSetForUnreferencedPropertiesRef;

    FieldSet nodeForUnreferencedProperties() {
        return Objects.requireNonNull(fieldSetForUnreferencedPropertiesRef);
    }
    Either<BSCol, FieldSet> nodeForUnreferencedActions() {
        return colForUnreferencedActionsRef!=null
            ? Either.left(colForUnreferencedActionsRef)
                : Either.right(fieldSetForUnreferencedActionsRef);
    }
    Either<BSCol, BSTabGroup> nodeForUnreferencedCollections() {
        return colForUnreferencedCollectionsRef!=null
            ? Either.left(colForUnreferencedCollectionsRef)
                : Either.right(tabGroupForUnreferencedCollectionsRef);
    }

    private boolean gridErrorsDetected = false;

    // grids are allowed to reference (by id) the same feature multiple times
    final _Multimaps.ListMultimap<String, PropertyLayoutData> propertyLayoutDataById = _Multimaps.newListMultimap();
    final _Multimaps.ListMultimap<String, CollectionLayoutData> collectionLayoutDataById = _Multimaps.newListMultimap();
    final _Multimaps.ListMultimap<String, ActionLayoutData> actionLayoutDataById = _Multimaps.newListMultimap();

    Disjunction propertyDisjunction(final Set<String> rightKeySet) {
        return Disjunction.of(propertyLayoutDataById.keySet(), rightKeySet);
    }
    Disjunction collectionDisjunction(final Set<String> rightKeySet) {
        return Disjunction.of(collectionLayoutDataById.keySet(), rightKeySet);
    }
    Disjunction actionDisjunction(final Set<String> rightKeySet) {
        return Disjunction.of(actionLayoutDataById.keySet(), rightKeySet);
    }

    public boolean contains(final String id) {
        return allIds.contains(id);
    }

    public Collection<FieldSet> fieldSets() {
        return fieldSets.values();
    }
    public boolean containsFieldSetId(final String id) {
        return fieldSets.containsKey(id);
    }
    public FieldSet getFieldSet(final String id) {
        return fieldSets.get(id);
    }

    private void putRow(final String id, final BSRow bsRow) {
        rows.put(id, bsRow);
        allIds.add(id);
    }
    private void putCol(final String id, final BSCol bsCol) {
        cols.put(id, bsCol);
        allIds.add(id);
    }
    private void putFieldSet(final String id, final FieldSet fieldSet) {
        fieldSets.put(id, fieldSet);
        allIds.add(id);
    }
    private static Boolean isSet(final Boolean flag) {
        return flag != null && flag;
    }

}