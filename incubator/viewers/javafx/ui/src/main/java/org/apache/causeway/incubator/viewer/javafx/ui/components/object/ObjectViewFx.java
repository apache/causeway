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
package org.apache.causeway.incubator.viewer.javafx.ui.components.object;

import java.util.function.Consumer;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSClearFix;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtil;
import org.apache.causeway.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.causeway.incubator.viewer.javafx.model.util._fx;
import org.apache.causeway.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.causeway.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.causeway.incubator.viewer.javafx.ui.components.form.FormPane;
import org.apache.causeway.incubator.viewer.javafx.ui.components.panel.TitledPanel;
import org.apache.causeway.viewer.commons.model.components.UiComponentFactory;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator.DisablingDecorationModel;
import org.apache.causeway.viewer.commons.model.layout.UiGridLayout;

import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectViewFx extends VBox {

    public static ObjectViewFx fromObject(
            final @NonNull UiContextFx uiContext,
            final @NonNull UiComponentFactoryFx uiComponentFactory,
            final @NonNull Consumer<ManagedAction> actionEventHandler,
            final @NonNull ManagedObject managedObject) {
        return new ObjectViewFx(uiContext, uiComponentFactory, actionEventHandler, managedObject);
    }

    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewFx(
            final UiContextFx uiContext,
            final UiComponentFactoryFx uiComponentFactory,
            final Consumer<ManagedAction> actionEventHandler,
            final ManagedObject managedObject) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(managedObject)) {
            log.warn("invalid managedObject, skipping");
            return;
        }

        log.info("binding object interaction to owner {}", managedObject.getSpecification().getFeatureIdentifier());
        _Assert.assertTrue(uiContext.getInteractionService().isInInteraction(), "requires an active interaction");

        val objectTitle = MmTitleUtil.titleOf(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);

        val gridVisitor = new UiGridLayout.Visitor<Pane, TabPane>(this) {

            @Override
            protected void onObjectTitle(final Pane container, final DomainObjectLayoutData domainObjectData) {
                val label = _fx.h2(_fx.newLabel(container, objectTitle));
                label.maxWidthProperty().bind(
                        container.widthProperty());
            }

            @Override
            protected Pane newRow(final Pane container, final BSRow bsRow) {
                val uiRow = _fx.newFlowPane(container);
                return uiRow;
            }

            @Override
            protected Pane newCol(final Pane container, final BSCol bscol) {

                val uiCol = _fx.newVBox(container);

                // note: also account for insets and padding, assuming that 98% seems reasonable
                double realtiveWidthWithRespectToContainer = bscol.getSpan()*0.98/12;

                uiCol.prefWidthProperty().bind(
                        container.widthProperty().multiply(realtiveWidthWithRespectToContainer));

                uiCol.maxWidthProperty().bind(
                        container.widthProperty().multiply(realtiveWidthWithRespectToContainer));

                uiCol.setFillWidth(true);

                return uiCol;
            }

            @Override
            protected Pane newActionPanel(final Pane container) {
                val uiActionPanel = _fx.newFlowPane(container);
                _fx.toolbarLayout(uiActionPanel);

                return uiActionPanel;
            }

            @Override
            protected TabPane newTabGroup(final Pane container, final BSTabGroup tabGroupData) {
                val uiTabGroup = _fx.newTabGroup(container);
                return uiTabGroup;
            }

            @Override
            protected Pane newTab(final TabPane container, final BSTab tabData) {
                val uiTab = _fx.newTab(container, tabData.getName());
                val uiTabContentPane = new VBox();
                uiTab.setContent(uiTabContentPane);
                return uiTabContentPane;
            }

            @Override
            protected Pane newFieldSet(final Pane container, final FieldSet fieldSetData) {

                val titledPanel = _fx.add(container, new TitledPanel(fieldSetData.getName()));

                // handle associated actions
                for(val actionData : fieldSetData.getActions()) {
                    onAction(titledPanel.getUiActionBar(), actionData);
                }

                val uiFieldSet = _fx.add(titledPanel, new FormPane());
                return uiFieldSet;
            }


            @Override
            protected void onClearfix(final Pane container, final BSClearFix clearFixData) {
                // TODO Auto-generated method stub
            }

            @Override
            protected void onAction(final Pane container, final ActionLayoutData actionData) {

                val owner = managedObject;
                val interaction = ActionInteraction.start(owner, actionData.getId(), Where.OBJECT_FORMS);
                interaction.checkVisibility()
                .getManagedAction()
                .ifPresent(managedAction -> {

                    interaction.checkUsability();

                    val uiButton = uiComponentFactory.buttonFor(
                                    UiComponentFactory.ButtonRequest.of(
                                        managedAction,
                                        DisablingDecorationModel.of(interaction),
                                        actionEventHandler));

                    if(container instanceof FormPane) {
                        ((FormPane)container).addActionLink(uiButton);
                    } else {
                        _fx.add(container, uiButton);
                    }


                });
            }

            @Override
            protected void onProperty(final Pane container, final PropertyLayoutData propertyData) {

                val owner = managedObject;

                val formPane = (FormPane) container;

                val interaction = PropertyInteraction.start(owner, propertyData.getId(), Where.OBJECT_FORMS);
                interaction.checkVisibility()
                .getManagedProperty()
                .ifPresent(managedProperty -> {

                    interaction.checkUsability();

                    val propNeg = managedProperty.startNegotiation();

                    val request = UiComponentFactory.ComponentRequest.of(
                            propNeg,
                            managedProperty,
                            DisablingDecorationModel.of(interaction));

                    val uiPropertyField = uiComponentFactory.componentFor(request);
                    val labelAndPostion = uiComponentFactory.labelFor(request);

                    formPane.addField(
                            labelAndPostion.getLabelPosition(),
                            labelAndPostion.getUiLabel(),
                            uiPropertyField);

                    // handle associated actions
                    for(val actionData : propertyData.getActions()) {
                        onAction(container, actionData);
                    }

                });
            }

            @Override
            protected void onCollection(final Pane container, final CollectionLayoutData collectionData) {

                val owner = managedObject;

                CollectionInteraction.start(owner, collectionData.getId(), Where.OBJECT_FORMS)
                .checkVisibility()
                .getManagedCollection()
                .ifPresent(managedCollection -> {

                    val titledPanel = _fx.add(container, new TitledPanel(managedCollection.getFriendlyName()));

                    // handle associated actions
                    for(val actionData : collectionData.getActions()) {
                        onAction(titledPanel.getUiActionBar(), actionData);
                    }

                    _fx.add(titledPanel,
                            TableViewFx.forManagedCollection(
                                    uiContext,
                                    managedCollection,
                                    Where.PARENTED_TABLES));

                });

            }

        };

        uiGridLayout.visit(gridVisitor);
        //setWidthFull();

    }

}
