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

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.Button;
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
     * @param where 
     */
    public static TableViewFx fromCollection(
            final @NonNull UiContextFx uiContext, 
            final @NonNull ManagedObject collection, 
            final @NonNull Where where) {

        val collectionFacet = collection.getSpecification()
                .getFacet(CollectionFacet.class);

        val objects = collectionFacet.stream(collection)
                .collect(Can.toCan());

        return ManagedObjects.commonSpecification(objects)
                .map(elementSpec->new TableViewFx(uiContext, elementSpec, objects, where))
                .orElseGet(TableViewFx::empty);
    }

    /**
     * Constructs a (page-able) {@link Grid} from given {@code managedCollection}   
     * @param uiContext
     * @param managedCollection
     * @param where 
     */
    public static TableViewFx forManagedCollection(
            final @NonNull UiContextFx uiContext, 
            final @NonNull ManagedCollection managedCollection, 
            final @NonNull Where where) {

        
        val elementSpec = managedCollection.getElementSpecification(); 
        val elements = managedCollection.streamElements()
                .collect(Can.toCan());
        return elements.isEmpty()
                ? empty()
                : new TableViewFx(uiContext, elementSpec, elements, where);
    }

    private Can<OneToOneAssociation> columnProperties(ObjectSpecification elementSpec, Where where) {

        //TODO honor column order (as per layout)
        return elementSpec.streamProperties(MixedIn.INCLUDED)
                .filter(ObjectAssociation.Predicates.staticallyVisible(where))
                .collect(Can.toCan());
    }

    /**
     * 
     * @param elementSpec - as is common to all given {@code objects} aka elements 
     * @param objects - (wrapped) domain objects to be rendered by this table
     * @param where
     */
    private TableViewFx(
            @NonNull final UiContextFx uiContext,
            @NonNull final ObjectSpecification elementSpec, 
            @Nullable final Can<ManagedObject> objects,
            @NonNull final Where where) {

        val objectGrid = new TableView<ManagedObject>();
        super.getChildren().add(objectGrid);

        if (_NullSafe.isEmpty(objects)) {
            objectGrid.setPlaceholder(new Label("No rows to display"));
            return;
        }

        val columnProperties = columnProperties(elementSpec, where);

        // rather prepare all table cells into a multi-map eagerly, 
        // than having to spawn new transactions/interactions for each table cell when rendered lazily 
        val table = _Multimaps.<Oid, String, String>newMapMultimap();

        _NullSafe.stream(objects)
        .forEach(object->{

            val id = object.getRootOid().orElse(null);
            if(id==null) {
                return;
            }

            columnProperties.forEach(property->{
                table.putElement(id, property.getId(), stringifyPropertyValue(property, object));
            });

        });

        // object link as first column
        val firstColumn = _fx.newColumn(objectGrid, "", Button.class);
        
        firstColumn.setCellValueFactory(cellDataFeatures -> {
            val objectIcon = uiContext.getJavaFxViewerConfig().getObjectFallbackIcon();
            val uiObjectRefLink = _fx.bottonForImage(objectIcon, 24, 24);
            uiObjectRefLink.setOnAction(e->{
                uiContext.route(cellDataFeatures::getValue);
            });
            return _fx.newObjectReadonly(uiObjectRefLink);
        });
        
        // property columns
        columnProperties.forEach(property->{
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
        
        objects.forEach(objectGrid.getItems()::add);

        //objectGrid.recalculateColumnWidths();
        //objectGrid.setColumnReorderingAllowed(true);

    }

    private String stringifyPropertyValue(
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
