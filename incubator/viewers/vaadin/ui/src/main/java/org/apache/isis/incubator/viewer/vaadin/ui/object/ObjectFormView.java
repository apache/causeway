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
package org.apache.isis.incubator.viewer.vaadin.ui.object;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class ObjectFormView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    
    public static final String NULL = "<NULL>";

    public ObjectFormView(final ManagedObject managedObject) {
        final ObjectSpecification specification = managedObject.getSpecification();
        final String title = specification.getTitle(null, managedObject);
        add(new H1(title));

        final List<? extends ObjectAssociation> objectAssociations = specification
                .streamAssociations(Contributed.INCLUDED)
                .filter(ObjectMember::isPropertyOrCollection)
                .collect(Collectors.toList());
        final FormLayout formLayout = new FormLayout();
        final VerticalLayout tables = new VerticalLayout();
        objectAssociations.forEach(objectAssociation -> {
            final ManagedObject assocObject = objectAssociation.get(managedObject);
            if (assocObject == null) {
                formLayout.add(createErrorField(objectAssociation, "assoc. object is null: "));
                return;
            }
            final ObjectSpecification propSpec = assocObject.getSpecification();
            switch (propSpec.getBeanSort()) {
            case VALUE: {
                formLayout.add(createValueField(objectAssociation, assocObject));
                break;
            }
            case COLLECTION: {
                tables.add(new Label(objectAssociation.getName()));
                tables.add(createCollectionComponent(objectAssociation, assocObject));
                break;
            }
            case VIEW_MODEL:
            case ENTITY:
            case MANAGED_BEAN_CONTRIBUTING:
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case MIXIN:
            case UNKNOWN:
            default: {
                final String value = propSpec.toString();
                final TextField textField = new TextField(value);
                textField.setLabel(
                        "Unhandled kind assoc.: " + propSpec.getBeanSort() + " " + objectAssociation.getName());
                textField.setValue(propSpec.toString());
                textField.setInvalid(true);
                formLayout.add(textField);
                break;
            }
            }
        });

        add(formLayout);
        add(new H3("Tables"));
        add(tables);
        setWidthFull();

    }

    private Component createErrorField(final ObjectAssociation objectAssociation, final String error) {
        return createErrorField("Error:" + objectAssociation.getName(),
                error + objectAssociation.toString());
    }

    private Component createErrorField(final String s, final String s2) {
        final TextField textField = new TextField();
        textField.setLabel(s);
        textField.setValue(s2);
        return textField;
    }

    private Component createCollectionComponent(
            final ObjectAssociation objectAssociation,
            final ManagedObject assocObject
    ) {
        final ObjectSpecification assocObjectSpecification = assocObject.getSpecification();
        final CollectionFacet collectionFacet = assocObjectSpecification.getFacet(CollectionFacet.class);

        final String label = "Collection:" + objectAssociation.getName();
        final Object pojo = assocObject.getPojo();
        if (pojo instanceof Collection) {

            final List<ManagedObject> objects = collectionFacet.stream(assocObject).collect(Collectors.toList());

            //            final ComboBox<ManagedObject> listBox = new ComboBox<>();
            //            listBox.setLabel(label + " #" + objects.size());
            //            listBox.setItems(objects);
            //            if (!objects.isEmpty()) {
            //                listBox.setValue(objects.get(0));
            //            }
            //            listBox.setItemLabelGenerator(o -> o.titleString(null));

            final Grid<ManagedObject> objectGrid = new Grid<>();
            if (objects.isEmpty()) {
                return objectGrid;
            }
            final ManagedObject firstObject = objects.get(0);
            final List<ObjectAssociation> properties = firstObject.getSpecification()
                    .streamAssociations(Contributed.INCLUDED)
                    .filter(m -> m.getFeatureType().isProperty())
                    .collect(Collectors.toList());
            properties.forEach(p -> {
                final Grid.Column<ManagedObject> column = objectGrid.addColumn(managedObject -> {
                    final ManagedObject managedObject1 = p.get(managedObject);
                    if (managedObject1 == null) {
                        return NULL;
                    }
                    return managedObject1.titleString();
                });
                column.setHeader(p.getName());
            });
            objectGrid.setItems(objects);
            objectGrid.recalculateColumnWidths();
            objectGrid.setColumnReorderingAllowed(true);
            return objectGrid;
        }

        if (pojo == null) {
            final TextField textField = new TextField();
            textField.setLabel(label);

            textField.setValue("<NULL>");
            return textField;
        }

        final TextField textField = new TextField();
        textField.setLabel(label);

        textField.setValue("Unknown collection type:" + pojo.getClass());
        return textField;
    }

    private Component createValueField(final ObjectAssociation association, final ManagedObject assocObject) {
        // TODO how to handle object type / id

        // How to handle blobs?
        //        final BlobValueSemanticsProvider blobValueFacet = association.getFacet(BlobValueSemanticsProvider.class);
        //        if (blobValueFacet != null) {
        //            final java.awt.Image aByte = blobValueFacet.getParser(assocObject);
        //            new Image(aByte);
        //            return null;
        //        }
        final String description = assocObject.getSpecification().streamFacets()
                .map(facet -> facet.getClass().getName())
                .collect(Collectors.joining("\n"));

        final TextField textField = createTextField(association, assocObject);
        //        Tooltips.getCurrent().setTooltip(textField, description);
        return textField;
    }

    private Image convertToImage(final byte[] imageData) {
        final StreamResource streamResource = new StreamResource("isr",
                (InputStreamFactory) () -> new ByteArrayInputStream(imageData));
        return new Image(streamResource, "photo");
    }

    private TextField createTextField(final ObjectAssociation association, final ManagedObject assocObject) {
        final TextField textField = new TextField();
        textField.setLabel(association.getName());
        textField.setValue(assocObject.titleString(null));
        return textField;
    }
}
