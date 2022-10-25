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
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

public final class GenericTitleColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;
    private final Variant variant;
    private Bookmark contextBookmark;

    public GenericTitleColumn(
            final MetaModelContext commonContext,
            final Variant variant,
            final Bookmark contextBookmark,
            final int maxTitleLength) {

        super(commonContext, columnName(variant, maxTitleLength)); // i18n
        this.variant = variant;
        this.contextBookmark = contextBookmark;
    }

    @Override
    public void populateItem(
            final Item<ICellPopulator<DataRow>> cellItem,
            final String componentId,
            final IModel<DataRow> rowModel) {

        cellItem.add(createComponent(componentId, rowModel));
        Wkt.cssAppend(cellItem, "title-column");
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

    private Component createComponent(final String id, final IModel<DataRow> rowModel) {
        val dataRow = rowModel.getObject();

        val adapter = dataRow.getRowElement();

        if(ManagedObjects.isValue(adapter)) {
            val objectMember = dataRow.getParentTable().getMetaModel();
            val valueModel = ValueModel.of(super.getMetaModelContext(), objectMember, adapter);
            val componentFactory = findComponentFactory(UiComponentType.VALUE, valueModel);
            return componentFactory.createComponent(id, valueModel);
        }

        val uiObject = UiObjectWkt.ofAdapterForCollection(super.getMetaModelContext(), adapter, variant);
        uiObject.setContextBookmarkIfAny(contextBookmark);

        // will use EntityLinkSimplePanelFactory as model is an EntityModel
        val componentFactory = findComponentFactory(UiComponentType.ENTITY_LINK, uiObject);
        return componentFactory.createComponent(id, uiObject);
    }


}
