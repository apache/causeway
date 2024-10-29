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
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.services.linking.DeepLinkService;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Builder;
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
            final ObjectSpecification elementType,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final String propertyId,
            final String parentTypeName,
            final Optional<String> describedAs,
            final RenderOptions opts) {
        super(elementType, collectionVariant, columnNameModel,
                Optional.empty(), // empty sortProperty (hence never sortable)
                propertyId, parentTypeName, describedAs);
        this.opts = opts;
    }

    @Override
    protected Component createCellComponent(final String componentId, final DataRowWkt dataRowWkt) {
        var dataRow = dataRowWkt.getObject();
        var dataColumn = dataRow.lookupColumnById(memberId).orElseThrow();
        var cellElements = dataRow.getCellElementsForColumn(dataColumn);

        // if empty, render the 'empty' badge or blank based on RenderOptions
        if(cellElements.isEmpty()) {
            return Wkt.markup(componentId,
                    opts.isRenderEmptyBadge()
                    ? getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION)
                    : "");
        }

        var container = new RepeatingView(componentId);
        cellElements.stream()
            .limit(opts.maxElements())
            .forEach(cellElement->container
                    .add(createCellElementComponent(container.newChildId(), dataColumn, cellElement)));

        // if cardinality exceeds threshold, truncate with '... has more' label at the end
        final int overflow = cellElements.size()-opts.maxElements();
        if(overflow>0) {

            var href = getMetaModelContext().getServiceRegistry().lookupService(DeepLinkService.class)
                    .map(deepLinkService->deepLinkService.deepLinkFor(dataRow.getRowElement()))
                    .map(URI::toString)
                    .orElse("#");

            Wkt.markupAdd(container, container.newChildId(),
                    getPlaceholderRenderService().asHtml(PlaceholderLiteral.HAS_MORE,
                            Map.of("number", ""+overflow,
                                   "href", href)));
        }

        return container;
    }

    /**
     * Inspired by {@link TitleColumn}
     */
    private Component createCellElementComponent(
            final String componentId, final DataColumn dataColumn, final ManagedObject cellElement) {

        if(ManagedObjects.isValue(cellElement)) {
            var valueModel = ValueModel.of(dataColumn.getAssociationMetaModel(), cellElement);
            var componentFactory = findComponentFactory(UiComponentType.VALUE, valueModel);
            return componentFactory.createComponent(componentId, valueModel);
        }

        var uiObject = UiObjectWkt.ofAdapterForCollection(cellElement, collectionVariant);

        var componentFactory = findComponentFactory(UiComponentType.ENTITY_LINK, uiObject);
        final Component entityLink =
                componentFactory.createComponent(componentId, uiObject);

        ColumnAbbreviationOptions.builder()
                .maxElementTitleLength(opts.titleAbbreviationThreshold())
                .build()
                .applyTo(entityLink);

        return entityLink;
    }

}
