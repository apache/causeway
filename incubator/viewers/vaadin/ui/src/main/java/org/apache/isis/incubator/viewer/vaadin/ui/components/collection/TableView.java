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
package org.apache.isis.incubator.viewer.vaadin.ui.components.collection;

import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class TableView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a (page-able) {@link Grid} from given {@code collection}  
     * @param collection of (wrapped) domain objects
     */
    public TableView(final ManagedObject collection) {
        val collectionFacet = collection.getSpecification()
                .getFacet(CollectionFacet.class);
        val objects = collectionFacet.stream(collection)
                .collect(Collectors.toList());
        
        val objectGrid = new Grid<ManagedObject>();
        objectGrid.setItems(objects);
        add(objectGrid);
    }
}
