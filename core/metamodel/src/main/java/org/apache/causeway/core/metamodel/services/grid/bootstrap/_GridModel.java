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
import java.util.Optional;

import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * package private helper
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class _GridModel {
        private final LinkedHashSet<String> allIds = _Sets.newLinkedHashSet();
        private final LinkedHashMap<String, BSRow> rows = _Maps.newLinkedHashMap();
        private final LinkedHashMap<String, BSCol> cols = _Maps.newLinkedHashMap();
        private final LinkedHashMap<String, FieldSet> fieldSets = _Maps.newLinkedHashMap();

        @Getter private BSCol colForUnreferencedActionsRef;
        @Getter private BSCol colForUnreferencedCollectionsRef;
        @Getter private FieldSet fieldSetForUnreferencedActionsRef;
        @Getter private FieldSet fieldSetForUnreferencedPropertiesRef;
        @Getter private BSTabGroup tabGroupForUnreferencedCollectionsRef;

        private boolean gridErrorsDetected = false;

        public boolean contains(String id) {
            return allIds.contains(id);
        }

        public Collection<FieldSet> fieldSets() {
            return fieldSets.values();
        }
        public boolean containsFieldSetId(String id) {
            return fieldSets.containsKey(id);
        }
        public FieldSet getFieldSet(String id) {
            return fieldSets.get(id);
        }

        /**
         * find all row and col ids<br>
         * - ensure that all Ids are different<br>
         * - ensure that there is exactly one col with the
         * unreferencedActions, unreferencedProperties and unreferencedCollections attribute set.
         * @param bsGrid
         * @return empty if not valid
         */
        public static Optional<_GridModel> createFrom(BSGrid bsGrid) {

            val gridModel = new _GridModel();

            bsGrid.visit(new BSGrid.VisitorAdapter(){
                @Override
                public void visit(final BSRow bsRow) {
                    final String id = bsRow.getId();
                    if(id == null) {
                        return;
                    }
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
                    if(id == null) {
                        return;
                    }
                    if(gridModel.contains(id)) {
                        bsCol.setMetadataError("There is another element in the grid with this id: " + id);
                        gridModel.gridErrorsDetected = true;
                        return;
                    }
                    gridModel.putCol(id, bsCol);
                }

                @Override
                public void visit(final FieldSet fieldSet) {
                    val groupIdAndName = GroupIdAndName.forFieldSet(fieldSet);
                    if(!groupIdAndName.isPresent()) {
                        fieldSet.setMetadataError("a fieldset must at least have an id or a name");
                        gridModel.gridErrorsDetected = true;
                        return;
                    }
                    String id = groupIdAndName.get().getId();
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

            bsGrid.visit(new BSGrid.VisitorAdapter(){

                @Override
                public void visit(final BSCol bsCol) {
                    if(isSet(bsCol.isUnreferencedActions())) {
                        if(gridModel.colForUnreferencedActionsRef != null) {
                            bsCol.setMetadataError("More than one col with 'unreferencedActions' attribute set");
                        } else if(gridModel.fieldSetForUnreferencedActionsRef != null) {
                            bsCol.setMetadataError("Already found a fieldset with 'unreferencedActions' attribute set");
                        } else {
                            gridModel.colForUnreferencedActionsRef=bsCol;
                        }
                    }
                    if(isSet(bsCol.isUnreferencedCollections())) {
                        if(gridModel.colForUnreferencedCollectionsRef != null) {
                            bsCol.setMetadataError("More than one col with 'unreferencedCollections' attribute set");
                        } else if(gridModel.tabGroupForUnreferencedCollectionsRef != null) {
                            bsCol.setMetadataError("Already found a tabgroup with 'unreferencedCollections' attribute set");
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
                        } else if(gridModel.colForUnreferencedActionsRef != null) {
                            fieldSet.setMetadataError("Already found a column with 'unreferencedActions' attribute set");
                        } else {
                            gridModel.fieldSetForUnreferencedActionsRef = fieldSet;
                        }
                    }
                    if(isSet(fieldSet.isUnreferencedProperties())) {
                        if(gridModel.fieldSetForUnreferencedPropertiesRef != null) {
                            fieldSet.setMetadataError("More than one column with 'unreferencedProperties' attribute set");
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
                        } else if(gridModel.colForUnreferencedCollectionsRef != null) {
                            bsTabGroup.setMetadataError("Already found a column with 'unreferencedCollections' attribute set");
                        } else {
                            gridModel.tabGroupForUnreferencedCollectionsRef = bsTabGroup;
                        }
                    }
                }
            });

            if(gridModel.colForUnreferencedActionsRef == null && gridModel.fieldSetForUnreferencedActionsRef == null) {
                bsGrid.getMetadataErrors().add("No column and also no fieldset found with the 'unreferencedActions' attribute set");
            }
            if(gridModel.fieldSetForUnreferencedPropertiesRef == null) {
                bsGrid.getMetadataErrors().add("No fieldset found with the 'unreferencedProperties' attribute set");
            }
            if(gridModel.colForUnreferencedCollectionsRef == null && gridModel.tabGroupForUnreferencedCollectionsRef == null) {
                bsGrid.getMetadataErrors().add("No column and also no tabgroup found with the 'unreferencedCollections' attribute set");
            }

            final boolean hasErrors =
                    gridModel.colForUnreferencedActionsRef == null
                    && gridModel.fieldSetForUnreferencedActionsRef == null
                    || gridModel.fieldSetForUnreferencedPropertiesRef == null
                    || gridModel.colForUnreferencedCollectionsRef == null
                    && gridModel.tabGroupForUnreferencedCollectionsRef == null;

            return hasErrors ? Optional.empty() : Optional.of(gridModel);

        }

        private void putRow(String id, BSRow bsRow) {
            rows.put(id, bsRow);
            allIds.add(id);
        }
        private void putCol(String id, BSCol bsCol) {
            cols.put(id, bsCol);
            allIds.add(id);
        }
        private void putFieldSet(String id, FieldSet fieldSet) {
            fieldSets.put(id, fieldSet);
            allIds.add(id);
        }
        private static Boolean isSet(final Boolean flag) {
            return flag != null && flag;
        }

    }