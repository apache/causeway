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

import java.util.function.Consumer;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

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
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.incubator.viewer.vaadin.model.util._vaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableViewVaa;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectBinding;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;

public class ObjectViewVaa extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public static ObjectViewVaa from(
            @NonNull final UiComponentFactoryVaa uiComponentFactory,
            @NonNull final Consumer<ManagedAction> actionEventHandler,
            @NonNull final ManagedObject managedObject) {
        return new ObjectViewVaa(uiComponentFactory, actionEventHandler, managedObject);
    }
    
    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewVaa(
            final UiComponentFactoryVaa uiComponentFactory,
            final Consumer<ManagedAction> actionEventHandler,
            final ManagedObject managedObject) {


        val objectInteractor = ObjectBinding.bind(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);

        // force new row
        //formLayout.getElement().appendChild(ElementFactory.createBr());

        val gridVisistor = new UiGridLayout.Visitor<HasComponents, Tabs>(this) {

            @Override
            protected void onObjectTitle(HasComponents container, DomainObjectLayoutData domainObjectData) {
                val uiTitle = _vaa.add(container, new H1(objectInteractor.getTitle()));
                //                uiTitle.addThemeVariants(
                //                        ButtonVariant.LUMO_LARGE,
                //                        ButtonVariant.LUMO_TERTIARY_INLINE);
            }

            @Override
            protected HasComponents newRow(HasComponents container, BS3Row bs3Row) {
                val uiRow = _vaa.add(container, new FlexLayout());
                
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

                val uiCol = _vaa.add(container, new VerticalLayout());

                final int span = bs3col.getSpan();
                ((FlexLayout)container).setFlexGrow(span, uiCol);
                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
                uiCol.setWidth(null); // clear preset width style
                uiCol.setMinWidth(widthEm);

                return uiCol;
            }

            @Override
            protected HasComponents newActionPanel(HasComponents container) {
                val uiActionPanel = _vaa.add(container, new FlexLayout());

                uiActionPanel.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                uiActionPanel.setAlignItems(Alignment.BASELINE);
                return uiActionPanel;
            }

            @Override
            protected Tabs newTabGroup(HasComponents container, BS3TabGroup tabGroupData) {
                val uiTabGroup = _vaa.add(container, new Tabs());

                uiTabGroup.setOrientation(Tabs.Orientation.HORIZONTAL);
                return uiTabGroup;
            }

            @Override
            protected HasComponents newTab(Tabs container, BS3Tab tabData) {
                val uiTab = _vaa.add(container, new Tab(tabData.getName()));
                return uiTab;
            }

            @Override
            protected HasComponents newFieldSet(HasComponents container, FieldSet fieldSetData) {

                _vaa.add(container, new H2(fieldSetData.getName()));
                
                // handle associated actions
                val actionBar = newActionPanel(container);
                for(val actionData : fieldSetData.getActions()) {
                    onAction(actionBar, actionData);
                }

                val uiFieldSet = _vaa.add(container, new FormLayout());

                uiFieldSet.setResponsiveSteps(
                        new ResponsiveStep("0", 1)); // single column only

                return uiFieldSet;
            }


            @Override
            protected void onClearfix(HasComponents container, BS3ClearFix clearFixData) {
                // TODO Auto-generated method stub
            }

            @SuppressWarnings("unused")
            @Override
            protected void onAction(HasComponents container, ActionLayoutData actionData) {
                
                val owner = objectInteractor.getManagedObject();
                val interaction = ActionInteraction.start(owner, actionData.getId());
                interaction.checkVisibility(Where.OBJECT_FORMS)
                .getManagedAction()
                .ifPresent(managedAction -> {
                    
                    interaction.checkUsability(Where.OBJECT_FORMS);
                    
                    val uiButton = _vaa.add(container, 
                            uiComponentFactory.buttonFor(
                                    UiComponentFactory.ButtonRequest.of(
                                            managedAction, 
                                            DisablingUiModel.of(interaction), 
                                            actionEventHandler)));
                });
                
            }

            @SuppressWarnings("unused")
            @Override
            protected void onProperty(HasComponents container, PropertyLayoutData propertyData) {
                
                val owner = objectInteractor.getManagedObject();
                
                val interaction = PropertyInteraction.start(owner, propertyData.getId());
                interaction.checkVisibility(Where.OBJECT_FORMS)
                .getManagedProperty()
                .ifPresent(managedProperty -> {
                    
                    interaction.checkUsability(Where.OBJECT_FORMS);
                    
                    val uiProperty = _vaa.add(container, 
                            uiComponentFactory.componentFor(
                                    UiComponentFactory.ComponentRequest.of(
                                            managedProperty,
                                            DisablingUiModel.of(interaction),
                                            Where.OBJECT_FORMS)));
                    
                    // handle associated actions
                    val actionBar = newActionPanel(container);
                    for(val actionData : propertyData.getActions()) {
                        onAction(actionBar, actionData);
                    }
                    
                });
            }

            @Override
            protected void onCollection(HasComponents container, CollectionLayoutData collectionData) {
                
                val owner = objectInteractor.getManagedObject();
                
                CollectionInteraction.start(owner, collectionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .getManagedCollection()
                .ifPresent(managedCollection -> {
                    _vaa.add(container, new H3(managedCollection.getName()));
                    
                    // handle associated actions
                    val actionBar = newActionPanel(container);
                    for(val actionData : collectionData.getActions()) {
                        onAction(actionBar, actionData);
                    }
                    
                    val uiCollection = _vaa.add(container, 
                            TableViewVaa.forManagedCollection(managedCollection));
                    
                });
                
            }

        };

        uiGridLayout.visit(gridVisistor);
        setWidthFull();

    }



}
