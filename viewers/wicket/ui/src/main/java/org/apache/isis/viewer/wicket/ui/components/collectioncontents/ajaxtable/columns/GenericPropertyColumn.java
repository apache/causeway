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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanel;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * A {@link GenericColumnAbstract column} within a
 * {@link CollectionContentsAsAjaxTablePanel} representing a single property of the
 * provided {@link ManagedObject}.
 *
 * <p>
 * Looks up the {@link ComponentFactory} to render the property from the
 * {@link ComponentFactoryRegistry}.
 */
public final class GenericPropertyColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel.Variant collectionVariant;
    private final String propertyId;
    private final boolean escaped;
    private final String parentTypeName;
    private final String describedAs;

    public GenericPropertyColumn(
            final IsisAppCommonContext commonContext,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final String sortProperty,
            final String propertyId,
            final boolean escaped,
            final String parentTypeName,
            final Optional<String> describedAs) {

        super(commonContext, columnNameModel, sortProperty);
        this.collectionVariant = collectionVariant;
        this.propertyId = propertyId;
        this.escaped = escaped;
        this.parentTypeName = parentTypeName;
        this.describedAs = describedAs.orElse(null);

    }

    @Override
    public Component getHeader(final String componentId) {
        final Label label = new Label(componentId, getDisplayModel());
        label.setEscapeModelStrings(escaped);
        if(describedAs!=null) {
            Tooltips.addTooltip(label, describedAs);
        }
        return label;
    }

    @Override
    public String getCssClass() {
        final String cssClass = super.getCssClass();
        return (_Strings.isNotEmpty(cssClass)
                        ? (cssClass + " ")
                        : "")
                + Wkt.cssNormalize("isis-" + parentTypeName + "-" + propertyId);
    }

    @Override
    public void populateItem(
            final Item<ICellPopulator<DataRow>> cellItem,
            final String componentId,
            final IModel<DataRow> rowModel) {

        cellItem.add(createComponent(componentId, rowModel));
    }

    private Component createComponent(final String id, final IModel<DataRow> rowModel) {

        val domainObject = rowModel.getObject().getRowElement();
        val property = domainObject.getSpecification().getPropertyElseFail(propertyId);
        val entityModel = EntityModel.ofAdapter(super.getCommonContext(), domainObject);

        final ScalarModel scalarModel = entityModel
                .getPropertyModel(
                        property,
                        ScalarRepresentation.VIEWING,
                        collectionVariant.getColumnRenderingHint());

        final ComponentFactory componentFactory = findComponentFactory(ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
        return componentFactory.createComponent(id, scalarModel);
    }

}