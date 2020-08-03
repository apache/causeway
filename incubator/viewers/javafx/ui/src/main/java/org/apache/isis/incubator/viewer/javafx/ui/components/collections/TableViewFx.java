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
package org.apache.isis.incubator.viewer.javafx.ui.components.collections;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Multimaps;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TableViewFx extends VBox {
    
    private static final String NULL_LITERAL = "<NULL>";
    
    public static TableViewFx empty() {
        return new TableViewFx();
    }
            
    /**
     * Constructs a (page-able) {@link Grid} from given {@code collection}  
     * @param collection - of (wrapped) domain objects
     */
    public static TableViewFx fromCollection(UiContext uiContext, ManagedObject collection) {
        val collectionFacet = collection.getSpecification()
                .getFacet(CollectionFacet.class);
        
        val objects = collectionFacet.stream(collection)
                .collect(Collectors.toList());
        
        return inferElementSpecification(objects)
                .map(elementSpec->new TableViewFx(uiContext, elementSpec, objects))
                .orElseGet(TableViewFx::empty);
    }
    
    /**
     * Constructs a (page-able) {@link Grid} from given {@code managedCollection}   
     * @param managedCollection
     */
    public static TableViewFx forManagedCollection(UiContext uiContext, ManagedCollection managedCollection) {
        
        val elementSpec = managedCollection.getElementSpecification(); 
        val elements = managedCollection.streamElements()
                .collect(Collectors.toList());
        return elements.isEmpty()
                ? empty()
                : new TableViewFx(uiContext, elementSpec, elements);
    }
    
    
    /**
     * 
     * @param elementSpec - as is common to all given {@code objects} aka elements 
     * @param objects - (wrapped) domain objects to be rendered by this table
     */
    private TableViewFx(
            @NonNull final UiContext uiContext,
            @NonNull final ObjectSpecification elementSpec, 
            @Nullable final Collection<ManagedObject> objects) {

        val objectGrid = new TableView<ManagedObject>();
        super.getChildren().add(objectGrid);
        
        if (_NullSafe.isEmpty(objects)) {
            objectGrid.setPlaceholder(new Label("No rows to display"));
            return;
        }
        
        val columnMetaModels = elementSpec
                .streamAssociations(Contributed.INCLUDED)
                .filter(assoc -> assoc.getFeatureType().isProperty())
                .collect(Can.toCan());
 
        // rather prepare all table cells into a multi-map eagerly, 
        // than having to spawn new transactions/interactions for each table cell when rendered lazily 
        val table = _Multimaps.<RootOid, String, String>newMapMultimap();
        
        _NullSafe.stream(objects)
        .forEach(object->{
           
            val id = object.getRootOid().orElse(null);
            if(id==null) {
                return;
            }
            
            columnMetaModels.forEach(property->{
                table.putElement(id, property.getId(), stringifyPropertyValue(uiContext, property, object));
            });
            
        });
        
        columnMetaModels.forEach(property->{
            val column = _fx.newColumn(objectGrid, property.getName(), String.class);
            column.setCellValueFactory(cellDataFeatures -> {
                log.debug("about to get property value for property {}", property.getId());                
                val targetObject = cellDataFeatures.getValue();
                
                val cellValue = targetObject.getRootOid()
                .map(id->table.getElement(id, property.getId()))
                .orElseGet(()->String.format("table cell not found for object '%s' (property-id: '%s')",
                        ""+targetObject, 
                        property.getId())); 

                return _fx.newStringReadonly(cellValue);
            });
        });
        
        // populate the model        
        objectGrid.getItems().addAll(objects);
        
        //objectGrid.recalculateColumnWidths();
        //objectGrid.setColumnReorderingAllowed(true);
        
    }
    
    private static Optional<ObjectSpecification> inferElementSpecification(List<ManagedObject> objects) {
        if (_NullSafe.isEmpty(objects)) {
            return Optional.empty();
        }
        val elementSpec = objects.iterator().next().getSpecification();
        return Optional.of(elementSpec);
    }
    
    private String stringifyPropertyValue(
            UiContext uiContext,
            ObjectAssociation property, 
            ManagedObject targetObject) {
        
        //return uiContext.getIsisInteractionFactory().callAnonymous(()->{
            
            try {
                val propertyValue = property.get(targetObject);
                return propertyValue == null 
                        ? NULL_LITERAL
                        : propertyValue.titleString();
            } catch (Exception e) {
                return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getName());
            }
            
        //});
        
        
    }
    
}
