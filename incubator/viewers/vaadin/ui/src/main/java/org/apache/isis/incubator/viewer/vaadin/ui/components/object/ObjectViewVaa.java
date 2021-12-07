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
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
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
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.incubator.viewer.vaadin.model.context.UiContextVaa;
import org.apache.isis.incubator.viewer.vaadin.model.util._vaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableViewVaa;
import org.apache.isis.viewer.common.model.components.UiComponentFactory;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.gridlayout.UiGridLayout;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectViewVaa extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public static ObjectViewVaa fromObject(
            final @NonNull UiContextVaa uiContext,
            final @NonNull UiComponentFactoryVaa uiComponentFactory,
            final @NonNull Consumer<ManagedAction> actionEventHandler,
            final @NonNull ManagedObject managedObject) {
        return new ObjectViewVaa(uiContext, uiComponentFactory, actionEventHandler, managedObject);
    }

    /**
     * Constructs given domain object's view, with all its visible members and actions.
     * @param managedObject - domain object
     */
    protected ObjectViewVaa(
            final UiContextVaa uiContext,
            final UiComponentFactoryVaa uiComponentFactory,
            final Consumer<ManagedAction> actionEventHandler,
            final ManagedObject managedObject) {

        log.info("binding object interaction to owner {}", managedObject.getSpecification().getFeatureIdentifier());
        _Assert.assertTrue(uiContext.getInteractionService().isInInteraction(), "requires an active interaction");

        val objectTitle = ManagedObjects.titleOf(managedObject);

        val uiGridLayout = UiGridLayout.bind(managedObject);

        // force new row
        //formLayout.getElement().appendChild(ElementFactory.createBr());

        val gridVisitor = new UiGridLayout.Visitor<HasComponents, Tabs>(this) {

            @Override
            protected void onObjectTitle(final HasComponents container, final DomainObjectLayoutData domainObjectData) {
                val uiTitle = _vaa.add(container, new H1(objectTitle));
                //                uiTitle.addThemeVariants(
                //                        ButtonVariant.LUMO_LARGE,
                //                        ButtonVariant.LUMO_TERTIARY_INLINE);
            }

            @Override
            protected HasComponents newRow(final HasComponents container, final BS3Row bs3Row) {
                val uiRow = _vaa.add(container, new FlexLayout());

                uiRow.setWidthFull();
                uiRow.setFlexWrap(FlexWrap.WRAP); // allow line breaking

                // instead of a FlexLayout we need to convert to a layout where we can control
                // the responsive steps
                //                val steps = _Lists.of(
                //                        new ResponsiveStep("0", 1),
                //                        new ResponsiveStep("50em", 2)
                //                        );

                return uiRow;
            }

            @Override
            protected HasComponents newCol(final HasComponents container, final BS3Col bs3col) {

                val uiCol = _vaa.add(container, new VerticalLayout());

                final int span = bs3col.getSpan();
                ((FlexLayout)container).setFlexGrow(span, uiCol);
                val widthEm = String.format("%dem", span * 3); // 1em ~ 16px
                uiCol.setWidth(null); // clear preset width style
                uiCol.setMinWidth(widthEm);

                return uiCol;
            }

            @Override
            protected HasComponents newActionPanel(final HasComponents container) {
                val uiActionPanel = _vaa.add(container, new FlexLayout());

                uiActionPanel.setFlexWrap(FlexWrap.WRAP); // allow line breaking
                uiActionPanel.setAlignItems(Alignment.BASELINE);
                return uiActionPanel;
            }

            @Override
            protected Tabs newTabGroup(final HasComponents container, final BS3TabGroup tabGroupData) {
                val uiTabGroup = _vaa.add(container, new Tabs());

                uiTabGroup.setOrientation(Tabs.Orientation.HORIZONTAL);
                return uiTabGroup;
            }

            @Override
            protected HasComponents newTab(final Tabs container, final BS3Tab tabData) {
                val uiTab = _vaa.add(container, new Tab(tabData.getName()));
                return uiTab;
            }

            @Override
            protected HasComponents newFieldSet(final HasComponents container, final FieldSet fieldSetData) {

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
            protected void onClearfix(final HasComponents container, final BS3ClearFix clearFixData) {
                // TODO Auto-generated method stub
            }

            @SuppressWarnings("unused")
            @Override
            protected void onAction(final HasComponents container, final ActionLayoutData actionData) {

                val owner = managedObject;
                val interaction = ActionInteraction.start(owner, actionData.getId(), Where.OBJECT_FORMS);
                interaction.checkVisibility()
                .getManagedAction()
                .ifPresent(managedAction -> {

                    interaction.checkUsability();

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
            protected void onProperty(final HasComponents container, final PropertyLayoutData propertyData) {

                val owner = managedObject;

                val interaction = PropertyInteraction.start(owner, propertyData.getId(), Where.OBJECT_FORMS);
                interaction.checkVisibility()
                .getManagedProperty()
                .ifPresent(managedProperty -> {

                    interaction.checkUsability();

                    val propNeg = managedProperty.startNegotiation();

                    val uiProperty = _vaa.add(container,
                            uiComponentFactory.componentFor(
                                    UiComponentFactory.ComponentRequest.of(
                                            propNeg,
                                            managedProperty,
                                            DisablingUiModel.of(interaction))));

                    // handle associated actions
                    val actionBar = newActionPanel(container);
                    for(val actionData : propertyData.getActions()) {
                        onAction(actionBar, actionData);
                    }

                });
            }

            @Override
            protected void onCollection(final HasComponents container, final CollectionLayoutData collectionData) {

                val owner = managedObject;

                CollectionInteraction.start(owner, collectionData.getId(), Where.OBJECT_FORMS)
                .checkVisibility()
                .getManagedCollection()
                .ifPresent(managedCollection -> {
                    _vaa.add(container, new H3(managedCollection.getFriendlyName()));

                    // handle associated actions
                    val actionBar = newActionPanel(container);
                    for(val actionData : collectionData.getActions()) {
                        onAction(actionBar, actionData);
                    }

                    val uiCollection = _vaa.add(container,
                            TableViewVaa.forManagedCollection(
                                    uiContext,
                                    managedCollection,
                                    Where.PARENTED_TABLES));

                });

            }

        };

        uiGridLayout.visit(gridVisitor);
        setWidthFull();

    }



}
