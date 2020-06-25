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
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectBinding;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;

import javafx.geometry.Insets;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ObjectViewFx extends VBox {
    
    public static ObjectViewFx fromObject(
            @NonNull final UiComponentFactoryFx uiComponentFactory,
            @NonNull final Consumer<ManagedAction> actionEventHandler,
            @NonNull final ManagedObject managedObject) {
        return new ObjectViewFx(uiComponentFactory, actionEventHandler, managedObject);
    }
    
    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewFx(
            final UiComponentFactoryFx uiComponentFactory,
            final Consumer<ManagedAction> actionEventHandler,
            final ManagedObject managedObject) {


        val objectInteractor = ObjectBinding.bind(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);

        val gridVisistor = new UiGridLayout.Visitor<Pane, TabPane>(this) {

            @Override
            protected void onObjectTitle(Pane container, DomainObjectLayoutData domainObjectData) {
                _fx.newLabel(container, objectInteractor.getTitle());
            }

            @Override
            protected Pane newRow(Pane container, BS3Row bs3Row) {
                val uiRow = _fx.newHBox(container);
                //uiRow.setWidthFull();
                //uiRow.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
                return uiRow;
            }

            @Override
            protected Pane newCol(Pane container, BS3Col bs3col) {

                val uiCol = _fx.newVBox(container);
                
                uiCol.setOpaqueInsets(new Insets(10, 10, 10, 10));
                

//                final int span = bs3col.getSpan();
//                ((FlexLayout)container).setFlexGrow(span, uiCol);
//                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
//                uiCol.setWidth(null); // clear preset width style
//                uiCol.setMinWidth(widthEm);

                return uiCol;
            }

            @Override
            protected Pane newActionPanel(Pane container) {
                val uiActionPanel = _fx.newHBox(container);
                
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

                _fx.newLabel(container, fieldSetData.getName())
                .setStyle("isis-field-set-label"); // corresponds to H2 (html)
                
                val uiFieldSet = _fx.newGrid(container);
                return uiFieldSet;
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
                .get()
                .ifPresent(managedAction -> {
                    _fx.newButton(
                            container, 
                            actionData.getNamed(), 
                            event->actionEventHandler.accept(managedAction));
                });
            }

            @Override
            protected void onProperty(Pane container, PropertyLayoutData propertyData) {
                
                val owner = objectInteractor.getManagedObject();
                
                PropertyInteraction.start(owner, propertyData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .get()
                .ifPresent(managedProperty -> {
                    val uiProperty = uiComponentFactory
                            .componentFor(UiComponentFactory.Request.of(Where.OBJECT_FORMS, managedProperty));
                    container.getChildren().add(uiProperty);
                });
            }

            @Override
            protected void onCollection(Pane container, CollectionLayoutData collectionData) {
                
                val owner = objectInteractor.getManagedObject();
                
                CollectionInteraction.start(owner, collectionData.getId())
                .checkVisibility(Where.OBJECT_FORMS)
                .get()
                .ifPresent(managedCollection -> {
                    
                    _fx.newLabel(container, managedCollection.getName())
                    .setStyle("isis-collection-label"); // corresponds to H3 (html)
                    
                    val uiCollection = TableViewFx.forManagedCollection(managedCollection);
                    container.getChildren().add(uiCollection);
                });
                
            }

        };

        uiGridLayout.visit(gridVisistor);
        //setWidthFull();

    }

}
