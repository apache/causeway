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

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGridDto;

record GridConverterFromDto(BSGridDto dto) {

    public BSGrid createGrid() {
        var grid = new BSGrid();
        dto.getRows().forEach(row->{
            grid.getRows().add(row);
        });
        dto.getRows().clear();
        return grid;
    }


//    private void getAllMemberById(final BSGridDto dto) {
//        final LinkedHashMap<String, PropertyLayoutData> propertiesById = new LinkedHashMap<>();
//        final LinkedHashMap<String, CollectionLayoutData> collectionsById = new LinkedHashMap<>();
//        final LinkedHashMap<String, ActionLayoutData> actionsById = new LinkedHashMap<>();
//
//        new BSWalker(dto).visit(new Grid.Visitor() {
//            @Override
//            public void visit(final PropertyLayoutData propertyLayoutData) {
//                propertiesById.put(propertyLayoutData.getId(), propertyLayoutData);
//            }
//            @Override
//            public void visit(final CollectionLayoutData collectionLayoutData) {
//                collectionsById.put(collectionLayoutData.getId(), collectionLayoutData);
//            }
//            @Override
//            public void visit(final ActionLayoutData actionLayoutData) {
//                actionsById.put(actionLayoutData.getId(), actionLayoutData);
//            }
//        });
//    }
//
//    record DtoVisitor(
//            LinkedHashMap<String, PropertyLayoutData> propertiesById,
//            LinkedHashMap<String, CollectionLayoutData> collectionsById,
//            LinkedHashMap<String, ActionLayoutData> actionsById) implements Grid.Visitor {
//        DtoVisitor() {
//            this(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
//        }
//        @Override
//        public void visit(final PropertyLayoutData propertyLayoutData) {
//            propertiesById.put(propertyLayoutData.getId(), propertyLayoutData);
//        }
//        @Override
//        public void visit(final CollectionLayoutData collectionLayoutData) {
//            collectionsById.put(collectionLayoutData.getId(), collectionLayoutData);
//        }
//        @Override
//        public void visit(final ActionLayoutData actionLayoutData) {
//            actionsById.put(actionLayoutData.getId(), actionLayoutData);
//        }
//    }

}
