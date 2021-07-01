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

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.incubator.viewer.vaadin.model.context.UiContextVaa;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TableViewVaa extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String NULL_LITERAL = "<NULL>";

    public static TableViewVaa empty() {
        return new TableViewVaa();
    }

    /**
     * Constructs a (page-able) {@link Grid} from given {@code collection}
     * @param collection - of (wrapped) domain objects
     * @param where
     */
    public static TableViewVaa fromCollection(
            final @NonNull UiContextVaa uiContext,
            final @NonNull ManagedObject collection,
            final @NonNull Where where) {

        val collectionFacet = collection.getSpecification()
                .getFacet(CollectionFacet.class);

        val objects = collectionFacet.stream(collection)
                .collect(Can.toCan());

        return ManagedObjects.commonSpecification(objects)
                .map(elementSpec->new TableViewVaa(elementSpec, objects, where))
                .orElseGet(TableViewVaa::empty);
    }

    /**
     * Constructs a (page-able) {@link Grid} from given {@code managedCollection}
     * @param managedCollection
     * @param where
     */
    public static Component forManagedCollection(
            final @NonNull UiContextVaa uiContext,
            final @NonNull ManagedCollection managedCollection,
            final @NonNull Where where) {

        val elementSpec = managedCollection.getElementSpecification();
        val elements = managedCollection.streamElements()
                .collect(Can.toCan());
        return elements.isEmpty()
                ? empty()
                : new TableViewVaa(elementSpec, elements, where);
    }

    private Can<OneToOneAssociation> columnProperties(final ObjectSpecification elementSpec, final Where where) {

        //TODO honor column order (as per layout)
        return elementSpec.streamProperties(MixedIn.INCLUDED)
                .filter(ObjectAssociation.Predicates.staticallyVisible(where))
                .collect(Can.toCan());
    }

    /**
     *
     * @param elementSpec - as is common to all given {@code objects} aka elements
     * @param objects - (wrapped) domain objects to be rendered by this table
     */
    private TableViewVaa(
            @NonNull final ObjectSpecification elementSpec,
            @NonNull final Can<ManagedObject> objects,
            @NonNull final Where where) {

        //            final ComboBox<ManagedObject> listBox = new ComboBox<>();
        //            listBox.setLabel(label + " #" + objects.size());
        //            listBox.setItems(objects);
        //            if (!objects.isEmpty()) {
        //                listBox.setValue(objects.get(0));
        //            }
        //            listBox.setItemLabelGenerator(o -> o.titleString());

        val objectGrid = new Grid<ManagedObject>();
        add(objectGrid);

        if (objects.isEmpty()) {
            //TODO show placeholder: "No rows to display"
            return;
        }

        val columnProperties = columnProperties(elementSpec, where);

        // rather prepare all table cells into a multi-map eagerly,
        // than having to spawn new transactions/interactions for each table cell when rendered lazily
        val table = _Multimaps.<Oid, String, String>newMapMultimap();

        objects.stream()
        .forEach(object->{

            val id = object.getBookmark().orElse(null);
            if(id==null) {
                return;
            }

            columnProperties.forEach(property->{
                table.putElement(id, property.getId(), stringifyPropertyValue(property, object));
            });

        });

        // object link as first column
        objectGrid.addColumn(targetObject->{
            // TODO provide icon with link
            return "obj. ref ["+targetObject.getBookmark().orElse(null)+"]";
        });

        // property columns
        columnProperties.forEach(property->{
            objectGrid.addColumn(targetObject -> {
                log.debug("about to get property value for property {}", property.getId());
                return stringifyPropertyValue(property, targetObject);
            })
            .setHeader(property.getCanonicalFriendlyName());
            //TODO add column description as is provided via property.getColumnDescription()
        });

        // populate the model
        objectGrid.setItems(objects.toList());
        objectGrid.recalculateColumnWidths();
        objectGrid.setColumnReorderingAllowed(true);

    }

    private String stringifyPropertyValue(
            final ObjectAssociation property,
            final ManagedObject targetObject) {
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
