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
package org.apache.isis.incubator.viewer.vaadin.ui.components.object;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;

import lombok.val;

public class ObjectFormView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    
    private static final String NULL_LITERAL = "<NULL>";

    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    public ObjectFormView(final ManagedObject managedObject) {
        val specification = managedObject.getSpecification();
        val title = specification.getTitle(null, managedObject);
        add(new H1(title));

        val objectAssociations = specification
                .streamAssociations(Contributed.INCLUDED)
                .filter(ObjectMember::isPropertyOrCollection)
                .collect(Collectors.toList());
        val formLayout = new FormLayout();
        val tablesLayout = new VerticalLayout();
        objectAssociations.forEach(objectAssociation -> {
            val assocObject = objectAssociation.get(managedObject);
            if (assocObject == null) {
                formLayout.add(createErrorField(objectAssociation, "assoc. object is null: "));
                return;
            }
            val propSpec = assocObject.getSpecification();
            switch (propSpec.getBeanSort()) {
            case VALUE: {
                formLayout.add(createValueField(objectAssociation, assocObject));
                break;
            }
            case COLLECTION: {
                tablesLayout.add(new Label(objectAssociation.getName()));
                tablesLayout.add(createCollectionComponent(objectAssociation, assocObject));
                break;
            }
            case VIEW_MODEL:
            case ENTITY:
            case MANAGED_BEAN_CONTRIBUTING:
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case MIXIN:
            case UNKNOWN:
            default: 
                val stringValue = propSpec.toString();
                val textField = new TextField(stringValue);
                textField.setLabel(
                        "Unhandled kind assoc.: " + propSpec.getBeanSort() + " " + objectAssociation.getName());
                textField.setValue(propSpec.toString());
                textField.setInvalid(true);
                formLayout.add(textField);
                break;
            }
        });

        add(formLayout);
        add(new H3("Tables"));
        add(tablesLayout);
        setWidthFull();

    }
    
    // -- HELPER

    private Component createErrorField(final ObjectAssociation objectAssociation, final String error) {
        return createErrorField("Error:" + objectAssociation.getName(),
                error + objectAssociation.toString());
    }

    private Component createErrorField(final String label, final String message) {
        val textField = new TextField();
        textField.setLabel(label);
        textField.setValue(message);
        return textField;
    }

    private Component createCollectionComponent(
            final ObjectAssociation objectAssociation,
            final ManagedObject assocObject
    ) {

        val labelLiteral = "Collection: " + objectAssociation.getName();
        val pojo = assocObject.getPojo();
        if (pojo instanceof Collection) {
            return TableView.fromObjectAssociation(objectAssociation, assocObject);
        }

        if (pojo == null) {
            val textField = new TextField();
            textField.setLabel(labelLiteral);

            textField.setValue(NULL_LITERAL);
            return textField;
        }

        val textField = new TextField();
        textField.setLabel(labelLiteral);
        textField.setValue("Unknown collection type: " + pojo.getClass());
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
        val description = assocObject.getSpecification().streamFacets()
                .map(facet -> facet.getClass().getName())
                .collect(Collectors.joining("\n"));

        val textField = createTextField(association, assocObject);
        //        Tooltips.getCurrent().setTooltip(textField, description);
        return textField;
    }

    private Image convertToImage(final byte[] imageData) {
        val streamResource = new StreamResource("isr",
                (InputStreamFactory) () -> new ByteArrayInputStream(imageData));
        return new Image(streamResource, "photo");
    }

    private TextField createTextField(final ObjectAssociation association, final ManagedObject assocObject) {
        val textField = new TextField();
        textField.setLabel(association.getName());
        textField.setValue(assocObject.titleString(null));
        return textField;
    }
}
