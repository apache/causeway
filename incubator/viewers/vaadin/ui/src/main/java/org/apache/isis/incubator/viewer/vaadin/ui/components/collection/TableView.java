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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TableView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    
    private static final String NULL_LITERAL = "<NULL>";
    
    public static TableView empty() {
        return new TableView();
    }
            
    /**
     * Constructs a (page-able) {@link Grid} from given {@code collection}  
     * @param collection - of (wrapped) domain objects
     */
    public static TableView fromCollection(final ManagedObject collection) {
        val collectionFacet = collection.getSpecification()
                .getFacet(CollectionFacet.class);
        
        val objects = collectionFacet.stream(collection)
                .collect(Collectors.toList());
        
        return inferElementSpecification(objects)
                .map(elementSpec->new TableView(elementSpec, objects))
                .orElseGet(TableView::empty);
    }
    
    /**
     * Constructs a (page-able) {@link Grid} from given {@code objectAssociation} and {@code assocObject}   
     * @param objectAssociation
     * @param assocObject
     */
    public static TableView fromObjectAssociation(
            @NonNull final ObjectAssociation objectAssociation,
            @Nullable final ManagedObject assocObject) {
        
        val assocObjectSpecification = assocObject.getSpecification();
        val collectionFacet = assocObjectSpecification.getFacet(CollectionFacet.class);

        val pojo = assocObject.getPojo();
        if (pojo instanceof Collection) {
            val objects = collectionFacet.stream(assocObject)
                    .collect(Collectors.toList());
            
            return inferElementSpecification(objects)
            .map(elementSpec->new TableView(elementSpec, objects))
            .orElseGet(TableView::empty);
        }
        
        return empty();
    }
    
    
    /**
     * 
     * @param elementSpec - as is common to all given {@code objects} aka elements 
     * @param objects - (wrapped) domain objects to be rendered by this table
     */
    private TableView(
            @NonNull final ObjectSpecification elementSpec, 
            @Nullable final Collection<ManagedObject> objects) {
        
        //            final ComboBox<ManagedObject> listBox = new ComboBox<>();
        //            listBox.setLabel(label + " #" + objects.size());
        //            listBox.setItems(objects);
        //            if (!objects.isEmpty()) {
        //                listBox.setValue(objects.get(0));
        //            }
        //            listBox.setItemLabelGenerator(o -> o.titleString(null));

        val objectGrid = new Grid<ManagedObject>();
        add(objectGrid);
        
        if (_NullSafe.isEmpty(objects)) {
            return;
        }
        
        objectGrid.setItems(objects);
        
        elementSpec
        .streamAssociations(Contributed.INCLUDED)
        .filter(assoc -> assoc.getFeatureType().isProperty())
        .forEach(property -> {
            
            objectGrid.addColumn(targetObject -> {
                log.info("about to get property value for property {}", 
                        property.getId());
                return stringify(property, targetObject);
            })
            .setHeader(property.getName());
        });
        objectGrid.setItems(objects);
        objectGrid.recalculateColumnWidths();
        objectGrid.setColumnReorderingAllowed(true);
        
    }
    
    private static Optional<ObjectSpecification> inferElementSpecification(List<ManagedObject> objects) {
        if (_NullSafe.isEmpty(objects)) {
            return Optional.empty();
        }
        val elementSpec = objects.iterator().next().getSpecification();
        return Optional.of(elementSpec);
    }
    
    private String stringify(
            ObjectAssociation property, 
            ManagedObject targetObject) {
        try {
            val propertyValue = property.get(targetObject);
            return propertyValue == null 
                    ? NULL_LITERAL
                    : propertyValue.titleString();
        } catch (Exception e) {
            return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getName());
        }
    }
}
