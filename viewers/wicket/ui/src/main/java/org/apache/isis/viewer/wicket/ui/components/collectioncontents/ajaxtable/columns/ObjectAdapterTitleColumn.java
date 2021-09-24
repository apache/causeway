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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public class ObjectAdapterTitleColumn
extends ColumnAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;
    private final Variant variant;
    private Bookmark contextBookmark;

    public ObjectAdapterTitleColumn(
            final IsisAppCommonContext commonContext,
            final Variant variant,
            final Bookmark contextBookmark,
            final int maxTitleLength) {

        super(commonContext, columnName(variant, maxTitleLength)); // i18n
        this.variant = variant;
        this.contextBookmark = contextBookmark;
    }

    @Override
    public void populateItem(
            final Item<ICellPopulator<ManagedObject>> cellItem,
            final String componentId,
            final IModel<ManagedObject> rowModel) {

        final Component component = createComponent(componentId, rowModel);
        cellItem.add(component);
        cellItem.add(new CssClassAppender("title-column"));
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

    private Component createComponent(final String id, final IModel<ManagedObject> rowModel) {
        val adapter = rowModel.getObject();

        if(ManagedObjects.isValue(adapter)) {
            val valueModel = ValueModel.of(super.getCommonContext(), adapter);

            val componentFactory = findComponentFactory(ComponentType.VALUE, valueModel);
            return componentFactory.createComponent(id, valueModel);
        }

        final var entityModel = EntityModel.ofAdapter(super.getCommonContext(), adapter);
        entityModel.setRenderingHint(variant.isParented()
                ? RenderingHint.PARENTED_TITLE_COLUMN
                : RenderingHint.STANDALONE_TITLE_COLUMN);
        entityModel.setContextBookmarkIfAny(contextBookmark);

        // will use EntityLinkSimplePanelFactory as model is an EntityModel
        val componentFactory = findComponentFactory(ComponentType.ENTITY_LINK, entityModel);
        return componentFactory.createComponent(id, entityModel);
    }



}
