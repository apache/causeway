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
package org.apache.isis.incubator.viewer.javafx.ui.components.object;

import java.util.function.Consumer;

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
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.panel.TitledPanel;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectBinding;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@Log4j2
public class ObjectViewFx extends VBox {
    
    public static ObjectViewFx fromObject(
            @NonNull final UiContext uiContext,
            @NonNull final UiComponentFactoryFx uiComponentFactory,
            @NonNull final Consumer<ManagedAction> actionEventHandler,
            @NonNull final ManagedObject managedObject) {
        return new ObjectViewFx(uiContext, uiComponentFactory, actionEventHandler, managedObject);
    }
    
    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewFx(
            final UiContext uiContext, 
            final UiComponentFactoryFx uiComponentFactory,
            final Consumer<ManagedAction> actionEventHandler,
            final ManagedObject managedObject) {

        log.info("binding object interaction to owner {}", managedObject.getSpecification().getIdentifier());

        val objectInteractor = ObjectBinding.bind(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);
        
        //this.setFillWidth(true);
        
        val gridVisistor = new UiGridLayout.Visitor<Pane, TabPane>(this) {

            @Override
            protected void onObjectTitle(Pane container, DomainObjectLayoutData domainObjectData) {
                val label = _fx.h2(_fx.newLabel(container, objectInteractor.getTitle()));
                label.maxWidthProperty().bind(
                        container.widthProperty());
            }

            @Override
            protected Pane newRow(Pane container, BS3Row bs3Row) {
                val uiRow = _fx.newFlowPane(container);
                //uiRow.setSpacing(4);
                
                //uiRow.setWidthFull();
                //uiRow.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                return uiRow;
            }

            @Override
            protected Pane newCol(Pane container, BS3Col bs3col) {

                val uiCol = _fx.newVBox(container);
                
                // note: also account for insets and padding, assuming that 98% seems reasonable
                double realtiveWidthWithRespectToContainer = bs3col.getSpan()*0.98/12; 
                
                uiCol.prefWidthProperty().bind(
                        container.widthProperty().multiply(realtiveWidthWithRespectToContainer));
                
                uiCol.maxWidthProperty().bind(
                        container.widthProperty().multiply(realtiveWidthWithRespectToContainer));
                
//                container.minWidthProperty().bind(
//                        container.widthProperty().multiply(realtiveWidthWithRespectToContainer));
                
                uiCol.setFillWidth(true);
                
                uiCol.setSpacing(4);
                
                //uiCol.setOpaqueInsets(new Insets(10, 10, 10, 10));
                

//                final int span = bs3col.getSpan();
//                ((FlexLayout)container).setFlexGrow(span, uiCol);
//                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
//                uiCol.setWidth(null); // clear preset width style
//                uiCol.setMinWidth(widthEm);

                return uiCol;
            }

            @Override
            protected Pane newActionPanel(Pane container) {
                val uiActionPanel = _fx.newFlowPane(container);
                _fx.toolbarLayout(uiActionPanel);
                
                uiActionPanel.prefWidthProperty().bind(
                        container.widthProperty().multiply(0.95));
                
                uiActionPanel.maxWidthProperty().bind(
                        container.widthProperty().multiply(0.95));
                
                _fx.backround(uiActionPanel, Color.FLORALWHITE);
                
                //uiActionPanel.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                //uiActionPanel.setAlignItems(Alignment.BASELINE);
                return uiActionPanel;
            }

            @Override
            protected TabPane newTabGroup(Pane container, BS3TabGroup tabGroupData) {
                val uiTabGroup = _fx.newTabGroup(container);
                return uiTabGroup;
            }

            @Override
            protected Pane newTab(TabPane container, BS3Tab tabData) {
                val uiTab = _fx.newTab(container, tabData.getName());
                val uiTabContentPane = new VBox();
                uiTab.setContent(uiTabContentPane);
                return uiTabContentPane; 
            }

            @Override
            protected Pane newFieldSet(Pane container, FieldSet fieldSetData) {

                val titledPanel = _fx.add(container, new TitledPanel(fieldSetData.getName()));
                
                // handle associated actions
                for(val actionData : fieldSetData.getActions()) {
                    onAction(titledPanel.getUiActionBar(), actionData);
                }
                
                val uiFieldSet = _fx.newGrid(titledPanel);
                return _fx.formLayout(uiFieldSet);
            }


            @Override
            protected void onClearfix(Pane container, BS3ClearFix clearFixData) {
                // TODO Auto-generated method stub
            }

            @Override
            protected void onAction(Pane container, ActionLayoutData actionData) {
                
                val owner = objectInteractor.getManagedObject();
                ActionInteraction.start(owner, actionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .getManagedAction()
                .ifPresent(managedAction -> {
                    val uiButton = _fx.newButton(
                            container, 
                            managedAction.getName(), 
                            event->actionEventHandler.accept(managedAction));
                });
            }

            @Override
            protected void onProperty(Pane container, PropertyLayoutData propertyData) {
                
                val owner = objectInteractor.getManagedObject();
                
                PropertyInteraction.start(owner, propertyData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .getManagedProperty()
                .ifPresent(managedProperty -> {
                    
                    val uiProperty = _fx.add(container, 
                            uiComponentFactory.componentFor(
                                    UiComponentFactory.Request.of(
                                            Where.OBJECT_FORMS, 
                                            managedProperty)));
                    
                    // handle associated actions
                    for(val actionData : propertyData.getActions()) {
                        onAction(container, actionData);
                    }
                    
                });
            }

            @Override
            protected void onCollection(Pane container, CollectionLayoutData collectionData) {
                
                val owner = objectInteractor.getManagedObject();
                
                CollectionInteraction.start(owner, collectionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .getManagedCollection()
                .ifPresent(managedCollection -> {
                    
                    val titledPanel = _fx.add(container, new TitledPanel(managedCollection.getName()));
                    
                    // handle associated actions
                    for(val actionData : collectionData.getActions()) {
                        onAction(titledPanel.getUiActionBar(), actionData);
                    }
                    
                    _fx.add(titledPanel, 
                            TableViewFx.forManagedCollection(uiContext, managedCollection));

                });
                
            }

        };

        uiGridLayout.visit(gridVisistor);
        //setWidthFull();

    }

}
