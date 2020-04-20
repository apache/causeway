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

import java.util.Collection;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor;

import lombok.NonNull;
import lombok.val;

public class ObjectFormView extends VerticalLayout {

    private static final long serialVersionUID = 1L;
    
    private static final String NULL_LITERAL = "<NULL>";
    
    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    public ObjectFormView(
            @NonNull final UiComponentFactoryVaa uiComponentFactory,
            @NonNull final ManagedObject managedObject) {
        
        val objectInteractor = ObjectInteractor.bind(managedObject);
        
        add(new H1(objectInteractor.getTitle()));

        val actionsLayout = new HorizontalLayout();
        add(actionsLayout);
        
        val formLayout = new FormLayout();
        add(formLayout);
        
        val tablesLayout = new VerticalLayout();
        add(tablesLayout);
        
        setWidthFull();
        
        // -- populate actions
        
        objectInteractor
        .streamVisisbleActions()
        .forEach(action -> {
            actionsLayout.add(new Button(action.getName()));
        });
        
        // -- populate properties
        
        objectInteractor
        .streamVisisbleProperties()
        .forEach(property -> {
            val uiComponentCreateRequest = objectInteractor.newUiComponentCreateRequest(property);
            val uiComponent = uiComponentFactory.componentFor(uiComponentCreateRequest);
            formLayout.add(uiComponent);
        });
        
        // -- populate collections
        
        objectInteractor
        .streamVisisbleCollections()
        .forEach(collection->{
            
            val collectionValue = Optional.ofNullable(collection.get(managedObject))
                    .orElse(ManagedObject.of(collection.getSpecification(), null));
            
            tablesLayout.add(new H3(collection.getName()));
            tablesLayout.add(createCollectionComponent(collection, collectionValue));
            
        });

    }
    
    // -- HELPER

    private Component createCollectionComponent(
            final ObjectAssociation objectAssociation,
            final ManagedObject assocObject) {

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


}
