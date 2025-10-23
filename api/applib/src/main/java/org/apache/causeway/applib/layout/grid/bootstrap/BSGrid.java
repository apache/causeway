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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.mixins.dto.Dto;

import lombok.Getter;
import lombok.Setter;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.
 * It simply consists of a number of rows.
 *
 * @since 1.x {@index}
 */
public final class BSGrid implements Grid, BSElement, Dto, BSRowOwner {

    private static final long serialVersionUID = 1L;

    @Getter @Setter private Class<?> domainClass;
    @Getter @Setter private String tnsAndSchemaLocation;
    @Getter @Setter private boolean fallback;
    @Getter @Setter private boolean normalized;
    @Getter @Setter private String cssClass;

    @Getter private final List<BSRow> rows = new ArrayList<>();
    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @Getter private final List<String> metadataErrors = new ArrayList<>();

    @Override
    public void visit(final Grid.Visitor visitor) {
        new BSWalker(this).visit(visitor);
    }

    @Override
    public LinkedHashMap<String, PropertyLayoutData> getAllPropertiesById() {
        final LinkedHashMap<String, PropertyLayoutData> propertiesById = new LinkedHashMap<>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                propertiesById.put(propertyLayoutData.getId(), propertyLayoutData);
            }
        });
        return propertiesById;
    }

    @Override
    public LinkedHashMap<String, CollectionLayoutData> getAllCollectionsById() {
        final LinkedHashMap<String, CollectionLayoutData> collectionsById = new LinkedHashMap<>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                collectionsById.put(collectionLayoutData.getId(), collectionLayoutData);
            }
        });
        return collectionsById;
    }

    @Override
    public LinkedHashMap<String, ActionLayoutData> getAllActionsById() {
        final LinkedHashMap<String, ActionLayoutData> actionsById = new LinkedHashMap<>();
        visit(new BSElement.Visitor() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actionsById.put(actionLayoutData.getId(), actionLayoutData);
            }
        });
        return actionsById;
    }

// -- UNUSED

//  public LinkedHashMap<String, BSTab> getAllTabsByName() {
//      final LinkedHashMap<String, BSTab> tabsByName = new LinkedHashMap<>();
//
//      visit(new BSGrid.Visitor() {
//          @Override
//          public void visit(final BSTab bSTab) {
//              tabsByName.put(bSTab.getName(), bSTab);
//          }
//      });
//      return tabsByName;
//  }
//
//  public LinkedHashMap<String, HasElementId> getAllCssId() {
//      final LinkedHashMap<String, HasElementId> divsByCssId = new LinkedHashMap<>();
//
//      visit(new BSGrid.Visitor() {
//          @Override
//          public void visit(final BSRow bsRow) {
//              final String id = bsRow.getId();
//              divsByCssId.put(id, bsRow);
//          }
//      });
//      return divsByCssId;
//  }
//
//    public LinkedHashMap<String, FieldSet> getAllFieldSetsByName() {
//        final LinkedHashMap<String, FieldSet> fieldSetsByName = new LinkedHashMap<>();
//
//        visit(new BSGrid.Visitor() {
//            @Override
//            public void visit(final FieldSet fieldSet) {
//                fieldSetsByName.put(fieldSet.getName(), fieldSet);
//            }
//        });
//        return fieldSetsByName;
//    }

}
