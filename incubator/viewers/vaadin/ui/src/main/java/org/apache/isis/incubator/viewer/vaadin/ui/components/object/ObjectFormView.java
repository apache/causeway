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
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3ClearFix;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3TabGroup;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;

import elemental.json.Json;
import elemental.json.JsonValue;

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
        
        val uiGridLayout = UiGridLayout.bind(managedObject);
        
        // force new row
        //formLayout.getElement().appendChild(ElementFactory.createBr());
        
        val gridVisistor = new UiGridLayout.Visitor<HasComponents>(this) {

            @Override
            protected void onObjectTitle(HasComponents container, DomainObjectLayoutData domainObjectData) {
                val uiTitle = new H1(objectInteractor.getTitle());
//                uiTitle.addThemeVariants(
//                        ButtonVariant.LUMO_LARGE,
//                        ButtonVariant.LUMO_TERTIARY_INLINE);
                container.add(uiTitle);     
            }
            
            @Override
            protected HasComponents newRow(HasComponents container, BS3Row bs3Row) {
                val uiRow = new FlexLayout();
                container.add(uiRow);
                uiRow.setWidthFull();
                uiRow.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                
                // instead of a FlexLayout we need to convert to a layout where we can control 
                // the responsive steps
//                val steps = _Lists.of(
//                        new ResponsiveStep("0", 1),
//                        new ResponsiveStep("50em", 2)
//                        );
                
                return uiRow;
            }

            @Override
            protected HasComponents newCol(HasComponents container, BS3Col bs3col) {
                
                val uiCol = new VerticalLayout();
                container.add(uiCol);
                
                final int span = bs3col.getSpan();
                ((FlexLayout)container).setFlexGrow(span, uiCol);
                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
                uiCol.setWidth(null); // clear preset width style
                uiCol.setMinWidth(widthEm);
                
                return uiCol;
            }
            
            @Override
            protected HasComponents newActionPanel(HasComponents container) {
                val uiActionPanel = new FlexLayout();
                container.add(uiActionPanel);
                
                uiActionPanel.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                uiActionPanel.setAlignItems(Alignment.BASELINE);
                return uiActionPanel;
            }

            @Override
            protected HasComponents newTabGroup(HasComponents container, BS3TabGroup tabGroupData) {
                val uiTabGroup = new Tabs();
                container.add(uiTabGroup);
                uiTabGroup.setOrientation(Tabs.Orientation.HORIZONTAL);
                return uiTabGroup;
            }

            @Override
            protected HasComponents newTab(HasComponents container, BS3Tab tabData) {
                val uiTab = new Tab(tabData.getName());
                container.add(uiTab);
                return uiTab;
            }
            
            @Override
            protected HasComponents newFieldSet(HasComponents container, FieldSet fieldSetData) {
                
                container.add(new H2(fieldSetData.getName()));
                
                val uiFieldSet = new FormLayout();
                container.add(uiFieldSet);
                
                uiFieldSet.setResponsiveSteps(
                        new ResponsiveStep("0", 1)); // single column only
                
                return uiFieldSet;
            }

            
            @Override
            protected void onClearfix(HasComponents container, BS3ClearFix clearFixData) {
                // TODO Auto-generated method stub
            }

            @Override
            protected void onAction(HasComponents container, ActionLayoutData actionData) {
                objectInteractor.lookupAction(actionData.getId())
                .ifPresent(action->{
                    val uiAction = new Button(action.getName());
                    container.add(uiAction);
                    uiAction.getStyle().set("margin-left", "0.5em");
                    uiAction.addThemeVariants(
                            ButtonVariant.LUMO_SMALL);
                    
                });
            }

            @Override
            protected void onProperty(HasComponents container, PropertyLayoutData propertyData) {
                objectInteractor.lookupProperty(propertyData.getId())
                .ifPresent(property->{
                    val uiPropertyCreateRequest = objectInteractor.newUiComponentCreateRequest(property);
                    val uiProperty = uiComponentFactory.componentFor(uiPropertyCreateRequest);
                    container.add(uiProperty);                    
                });
            }

            @Override
            protected void onCollection(HasComponents container, CollectionLayoutData collectionData) {
                objectInteractor.lookupCollection(collectionData.getId())
                .ifPresent(collection->{
                    container.add(new H3(collection.getName()));
                    
                    val collectionValue = Optional.ofNullable(collection.get(managedObject))
                            .orElse(ManagedObject.of(collection.getSpecification(), null));
                    container.add(createCollectionComponent(collection, collectionValue));
                });
            }
            
        };
        
        uiGridLayout.visit(gridVisistor);
        setWidthFull();
        
        // -- populate actions
        
//        objectInteractor
//        .streamVisisbleActions()
//        .forEach(action -> {
//            
//            val button = new Button(action.getName());
//            actionsLayout.add(button);
//            
//            Dialog dialog = new Dialog();
//            dialog.add(new Label("Under Construction ... Close me with the esc-key or an outside click"));
//
//            dialog.setWidth("400px");
//            dialog.setHeight("150px");
//
//            button.addClickListener(e->{
//                dialog.open();
//            });
//
////similar code in menu item ...            
////            val actionModel = (ActionVaa)menuItemModel.getMenuActionUiModel();
////            
////            subMenu.addItem(
////                    (Component)menuActionModel.getUiComponent(), 
////                    e->subMenuEventHandler.accept(menuActionModel));
//            
//        });


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
