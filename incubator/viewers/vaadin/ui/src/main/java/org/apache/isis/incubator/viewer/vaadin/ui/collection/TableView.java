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
package org.apache.isis.incubator.viewer.vaadin.ui.collection;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public class TableView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public TableView(final ManagedObject collection) {
        final ObjectSpecification assocObjectSpecification = collection.getSpecification();
        final CollectionFacet facet = assocObjectSpecification.getFacet(CollectionFacet.class);
        final List<ManagedObject> objects = facet.stream(collection).collect(Collectors.toList());

        final Grid<ManagedObject> objectGrid = new Grid<>();
        objectGrid.setItems(objects);
        add(objectGrid);
    }
}
