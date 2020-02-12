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
package org.apache.isis.core.metamodel.services.grid.bootstrap3;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * package private helper
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GridModel {
        private final LinkedHashSet<String> allIds = _Sets.newLinkedHashSet();
        private final LinkedHashMap<String, BS3Row> rows = _Maps.newLinkedHashMap();
        private final LinkedHashMap<String, BS3Col> cols = _Maps.newLinkedHashMap();
        private final LinkedHashMap<String, FieldSet> fieldSets = _Maps.newLinkedHashMap();
        
        @Getter private boolean duplicateIdDetected = false;
        
        public boolean contains(String id) {
            return allIds.contains(id);
        }
        
//        public Collection<BS3Row> rows() {
//            return rows.values();
//        }
//        public Collection<BS3Col> cols() {
//            return cols.values();
//        }
        public Collection<FieldSet> fieldSets() {
            return fieldSets.values();
        }
        public boolean containsFieldSetId(String id) {
            return fieldSets.containsKey(id);
        }
        public FieldSet getFieldSet(String id) {
            return fieldSets.get(id);
        }
        
        public static GridModel createFrom(BS3Grid bs3Grid) {
            
            val gridModel = new GridModel();

            bs3Grid.visit(new BS3Grid.VisitorAdapter(){
                @Override
                public void visit(final BS3Row bs3Row) {
                    final String id = bs3Row.getId();
                    if(id == null) {
                        return;
                    }
                    if(gridModel.contains(id)) {
                        bs3Row.setMetadataError("There is another element in the grid with this id");
                        gridModel.duplicateIdDetected = true;
                        return;
                    }
                    gridModel.putRow(id, bs3Row);
                }

                @Override
                public void visit(final BS3Col bs3Col) {
                    final String id = bs3Col.getId();
                    if(id == null) {
                        return;
                    }
                    if(gridModel.contains(id)) {
                        bs3Col.setMetadataError("There is another element in the grid with this id");
                        gridModel.duplicateIdDetected = true;
                        return;
                    }
                    gridModel.putCol(id, bs3Col);
                }

                @Override
                public void visit(final FieldSet fieldSet) {
                    String id = fieldSet.getId();
                    if(id == null) {
                        final String name = fieldSet.getName();
                        fieldSet.setId(id = GridSystemServiceBS3.asId(name));
                    }
                    if(gridModel.contains(id)) {
                        fieldSet.setMetadataError("There is another element in the grid with this id");
                        gridModel.duplicateIdDetected = true;
                        return;
                    }
                    gridModel.putFieldSet(id, fieldSet);
                }
            });

            return gridModel;
            
        }
        
        private void putRow(String id, BS3Row bs3Row) {
            rows.put(id, bs3Row);
            allIds.add(id);            
        }
        private void putCol(String id, BS3Col bs3Col) {
            cols.put(id, bs3Col);
            allIds.add(id);            
        }
        private void putFieldSet(String id, FieldSet fieldSet) {
            fieldSets.put(id, fieldSet);
            allIds.add(id);            
        }
        
    }