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

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.tabular.interactive.DataRow;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;

import lombok.val;

public final class TitleColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;
    private final Variant variant;
    private final ColumnAbbreviationOptions opts;
    private final Bookmark contextBookmark;

    public TitleColumn(
            final MetaModelContext commonContext,
            final Variant variant,
            final Bookmark contextBookmark,
            final int maxColumnTitleLength,
            final ColumnAbbreviationOptions opts) {

        super(commonContext, columnName(variant, maxColumnTitleLength)); // i18n
        this.variant = variant;
        this.contextBookmark = contextBookmark;
        this.opts = opts;
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRow dataRow, final IModel<Boolean> dataRowToggle) {
        val rowElement = dataRow.getRowElement();

        if(ManagedObjects.isValue(rowElement)) {
            val objectMember = dataRow.getParentTable().getMetaModel();
            val valueModel = ValueModel.of(super.getMetaModelContext(), objectMember, rowElement);
            val componentFactory = findComponentFactory(UiComponentType.VALUE, valueModel);
            return componentFactory.createComponent(componentId, valueModel);
        }

        val uiObject = UiObjectWkt.ofAdapterForCollection(super.getMetaModelContext(), rowElement, variant);
        uiObject.setContextBookmarkIfAny(contextBookmark);

        // will use EntityLinkSimplePanelFactory as model is an EntityModel
        val componentFactory = findComponentFactory(UiComponentType.ENTITY_LINK, uiObject);
        final Component entityLink = opts.applyTo(
                componentFactory.createComponent(componentId, uiObject));
        return entityLink;
    }

    // -- HELPER

    private static String columnName(
            final @Nullable Variant variant,
            final int maxTitleLength) {
        if(maxTitleLength == 0) {
            return "";
        }
        return (variant.isParented() ? "Related ":"") + "Object";
    }

}
