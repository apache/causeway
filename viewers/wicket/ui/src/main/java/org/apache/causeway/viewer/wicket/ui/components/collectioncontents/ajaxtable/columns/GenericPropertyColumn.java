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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

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
    private final String parentTypeName;
    private final String describedAs;

    public GenericPropertyColumn(
            final MetaModelContext commonContext,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final String sortProperty,
            final String propertyId,
            final String parentTypeName,
            final Optional<String> describedAs) {

        super(commonContext, columnNameModel, sortProperty);
        this.collectionVariant = collectionVariant;
        this.propertyId = propertyId;
        this.parentTypeName = parentTypeName;
        this.describedAs = describedAs.orElse(null);
    }

    @Override
    public Component getHeader(final String componentId) {
        final Label label = new Label(componentId, getDisplayModel());
        label.setEscapeModelStrings(true); // the default anyway
        if(describedAs!=null) {
            WktTooltips.addTooltip(label, describedAs);
        }
        return label;
    }

    @Override
    public String getCssClass() {
        final String cssClass = super.getCssClass();
        return (_Strings.isNotEmpty(cssClass)
                        ? (cssClass + " ")
                        : "")
                + Wkt.cssNormalize("causeway-" + parentTypeName + "-" + propertyId);
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
        val entityModel = UiObjectWkt.ofAdapter(super.getMetaModelContext(), domainObject);

        val scalarModel = entityModel
                .getPropertyModel(
                        property,
                        ScalarRepresentation.VIEWING,
                        collectionVariant.getColumnRenderingHint());

        return findComponentFactory(UiComponentType.SCALAR_NAME_AND_VALUE, scalarModel)
                .createComponent(id, scalarModel);
    }

}
