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
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectBinding;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ObjectViewFx extends VBox {
    
    public static ObjectViewFx fromObject(
            @NonNull final UiComponentFactoryFx uiComponentFactory,
            @NonNull final ManagedObject managedObject) {
        return new ObjectViewFx(uiComponentFactory, managedObject);
    }
    
    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewFx(
            final UiComponentFactoryFx uiComponentFactory,
            final ManagedObject managedObject) {


        val objectInteractor = ObjectBinding.bind(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);

        // force new row
        //formLayout.getElement().appendChild(ElementFactory.createBr());

//        val gridVisistor = new UiGridLayout.Visitor<Pane>(this) {
//
//            @Override
//            protected void onObjectTitle(Pane container, DomainObjectLayoutData domainObjectData) {
//                _fx.newLabel(container, objectInteractor.getTitle());
//            }
//
//            @Override
//            protected Pane newRow(Pane container, BS3Row bs3Row) {
//                val uiRow = _fx.newHBox(container);
//                //uiRow.setWidthFull();
//                //uiRow.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
//                return uiRow;
//            }
//
//            @Override
//            protected Parent newCol(Parent container, BS3Col bs3col) {
//
//                val uiCol = _fx.newVBox(container);
//                
//                val uiCol = new VerticalLayout();
//                container.add(uiCol);
//
//                final int span = bs3col.getSpan();
//                ((FlexLayout)container).setFlexGrow(span, uiCol);
//                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
//                uiCol.setWidth(null); // clear preset width style
//                uiCol.setMinWidth(widthEm);
//
//                return uiCol;
//            }
//
//            @Override
//            protected Pane newActionPanel(Pane container) {
//                val uiActionPanel = new FlexLayout();
//                container.add(uiActionPanel);
//
//                uiActionPanel.setWrapMode(FlexLayout.WrapMode.WRAP); // allow line breaking
//                uiActionPanel.setAlignItems(Alignment.BASELINE);
//                return uiActionPanel;
//            }
//
//            @Override
//            protected Pane newTabGroup(Pane container, BS3TabGroup tabGroupData) {
//                val uiTabGroup = new Tabs();
//                container.add(uiTabGroup);
//                uiTabGroup.setOrientation(Tabs.Orientation.HORIZONTAL);
//                return uiTabGroup;
//            }
//
//            @Override
//            protected Pane newTab(Pane container, BS3Tab tabData) {
//                val uiTab = new Tab(tabData.getName());
//                container.add(uiTab);
//                return uiTab;
//            }
//
//            @Override
//            protected Pane newFieldSet(Pane container, FieldSet fieldSetData) {
//
//                container.add(new H2(fieldSetData.getName()));
//
//                val uiFieldSet = new FormLayout();
//                container.add(uiFieldSet);
//
//                uiFieldSet.setResponsiveSteps(
//                        new ResponsiveStep("0", 1)); // single column only
//
//                return uiFieldSet;
//            }
//
//
//            @Override
//            protected void onClearfix(Pane container, BS3ClearFix clearFixData) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            protected void onAction(Pane container, ActionLayoutData actionData) {
//                
//                val owner = objectInteractor.getManagedObject();
//                ActionInteraction.start(owner, actionData.getId())
//                .checkVisibility(Where.OBJECT_FORMS)
//                .get()
//                .ifPresent(managedAction -> {
//                    val uiAction = ActionButton.forManagedAction(uiComponentFactory, managedAction);
//                    container.add(uiAction);
//                });
//            }
//
//            @Override
//            protected void onProperty(Pane container, PropertyLayoutData propertyData) {
//                
//                val owner = objectInteractor.getManagedObject();
//                
//                PropertyInteraction.start(owner, propertyData.getId())
//                .checkVisibility(Where.OBJECT_FORMS)
//                .get()
//                .ifPresent(managedProperty -> {
//                    val uiProperty = uiComponentFactory
//                            .componentFor(UiComponentFactory.Request.of(Where.OBJECT_FORMS, managedProperty));
//                    container.add(uiProperty);
//                });
//            }
//
//            @Override
//            protected void onCollection(Pane container, CollectionLayoutData collectionData) {
//                
//                val owner = objectInteractor.getManagedObject();
//                
//                CollectionInteraction.start(owner, collectionData.getId())
//                .checkVisibility(Where.OBJECT_FORMS)
//                .get()
//                .ifPresent(managedCollection -> {
//                    container.add(new H3(managedCollection.getName()));
//                    
//                    val uiCollection = TableViewVaa.forManagedCollection(managedCollection);
//                    container.add(uiCollection);
//                });
//                
//            }
//
//        };
//
//        uiGridLayout.visit(gridVisistor);
//        setWidthFull();

    }

}
