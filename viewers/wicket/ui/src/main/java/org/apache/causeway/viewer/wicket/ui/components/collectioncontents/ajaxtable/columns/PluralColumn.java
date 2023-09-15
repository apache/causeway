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

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Builder;
import lombok.val;
import lombok.experimental.Accessors;

public final class PluralColumn
extends AssociationColumnAbstract {

    private static final long serialVersionUID = 1L;

    @lombok.Value @Builder @Accessors(fluent=true)
    public static class RenderOptions implements Serializable {
        private static final long serialVersionUID = 1L;
        @Builder.Default
        final int titleAbbreviationThreshold = 50;
        @Builder.Default
        final int maxElements = 5;
        @Builder.Default
        final boolean isRenderEmptyBadge = true;
    }

    private final RenderOptions opts;

    public PluralColumn(
            final MetaModelContext commonContext,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final String sortProperty,
            final String propertyId,
            final String parentTypeName,
            final Optional<String> describedAs,
            final RenderOptions opts) {
        super(commonContext, collectionVariant, columnNameModel, sortProperty, propertyId, parentTypeName, describedAs);
        this.opts = opts;
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRow dataRow, final IModel<Boolean> dataRowToggle) {

        val cellElements = dataRow.getCellElementsForColumn(memberId);

        // if empty, render the 'empty' badge or blank based on RenderOptions
        if(cellElements.isEmpty()) {
            return Wkt.markup(componentId,
                    opts.isRenderEmptyBadge()
                    ? getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION)
                    : "");
        }

        val container = new RepeatingView(componentId);
        cellElements.stream()
            .limit(opts.maxElements())
            .forEach(cellElement->container
                    .add(createCellElementComponent(container.newChildId(), cellElement)));

        // if cardinality exceeds threshold, truncate with '... has more' label at the end
        final int overflow = cellElements.size()-opts.maxElements();
        if(overflow>0) {
            //val hasMoreText = String.format("... " + translate("has %d more"), overflow);
            //Wkt.labelAdd(container, container.newChildId(), hasMoreText);
            Wkt.markupAdd(container, container.newChildId(),
                    getPlaceholderRenderService().asHtml(PlaceholderLiteral.HAS_MORE, Map.of("number", ""+overflow)));
        }

        return container;
    }

    /**
     * Inspired by {@link TitleColumn}
     */
    private Component createCellElementComponent(
            final String componentId, final ManagedObject cellElement) {

        if(ManagedObjects.isValue(cellElement)) {
//            val objectMember = dataRow.getParentTable().getMetaModel();
//            val valueModel = ValueModel.of(super.getMetaModelContext(), objectMember, cellElement);
//            val componentFactory = findComponentFactory(UiComponentType.VALUE, valueModel);
//            return componentFactory.createComponent(componentId, valueModel);

            //TODO[CAUSEWAY-3578] implement value rendering
            return Wkt.label(componentId, "TODO[CAUSEWAY-3578]");
        }

        val uiObject = UiObjectWkt.ofAdapterForCollection(super.getMetaModelContext(), cellElement, collectionVariant);

        val componentFactory = findComponentFactory(UiComponentType.ENTITY_LINK, uiObject);
        final Component entityLink =
                componentFactory.createComponent(componentId, uiObject);

        ColumnAbbreviationOptions.builder()
                .maxElementTitleLength(opts.titleAbbreviationThreshold())
                .build()
                .applyTo(entityLink);

        return entityLink;
    }

}
