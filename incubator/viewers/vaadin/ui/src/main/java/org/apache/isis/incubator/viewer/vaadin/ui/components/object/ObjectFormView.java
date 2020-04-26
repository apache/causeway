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

import org.apache.isis.applib.annotation.Where;
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
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.interaction.ActionInteraction;
import org.apache.isis.core.metamodel.spec.interaction.CollectionInteraction;
import org.apache.isis.core.metamodel.spec.interaction.ManagedCollection;
import org.apache.isis.core.metamodel.spec.interaction.PropertyInteraction;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectBinding;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

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


        val objectInteractor = ObjectBinding.bind(managedObject);

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
                
                val owner = objectInteractor.getManagedObject();
                ActionInteraction.start(owner, actionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .get()
                .ifPresent(managedAction -> {
                    // TODO yet not doing anything
                    val uiAction = new Button(managedAction.getName());
                    container.add(uiAction);
                    uiAction.getStyle().set("margin-left", "0.5em");
                    uiAction.addThemeVariants(
                            ButtonVariant.LUMO_SMALL);
                });
            }

            @Override
            protected void onProperty(HasComponents container, PropertyLayoutData propertyData) {
                
                val owner = objectInteractor.getManagedObject();
                
                PropertyInteraction.start(owner, propertyData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .get()
                .ifPresent(managedProperty -> {
                    val uiProperty = uiComponentFactory
                            .componentFor(UiComponentFactory.Request.of(managedProperty));
                    container.add(uiProperty);
                });
            }

            @Override
            protected void onCollection(HasComponents container, CollectionLayoutData collectionData) {
                
                val owner = objectInteractor.getManagedObject();
                
                CollectionInteraction.start(owner, collectionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .get()
                .ifPresent(managedCollection -> {
                    container.add(new H3(managedCollection.getName()));
                    
                    val uiCollection = createCollectionComponent(managedCollection);
                    container.add(uiCollection);
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
            final ManagedCollection managedCollection) {

        val labelLiteral = "Collection: " + managedCollection.getName();
        val pojo = managedCollection.getCollectionValue().getPojo();
        if (pojo instanceof Collection) {
            return TableView.fromManagedCollection(managedCollection);
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
